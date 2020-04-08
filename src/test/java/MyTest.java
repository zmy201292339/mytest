import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import project.TestApplication;
import project.jpa.*;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import project.utils.IdGenerator;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@SpringBootTest(classes = TestApplication.class)
@RunWith(SpringRunner.class)
public class MyTest {

    @Resource
    private DeviceIocRecordsService deviceIocRecordsService;

    @Resource
    private AccessDeviceService accessDeviceService;

    @Resource
    private IdGenerator idGenerator;

    @Test
    public void scrollTest() throws IOException {
//        Optional<DeviceIocRecords> result = deviceIocRecordsService.findByDeviceId("DEVICE-API-GATEWAY-CLOUD");
//        if(result.isPresent()) {
//            System.out.println(result.get().getQueryTime());
//        }

        Optional<DeviceIocRecords> recordsOpt = this.deviceIocRecordsService.findByDeviceId("DEVICE-API-GATEWAY-CLOUD");
        RestHighLevelClient highLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("10.50.25.68", 9200, "http")));

        Integer limit = 3;

        DeviceIocRecords deviceIocRecords = recordsOpt.get();
        Date lastQueryDate = deviceIocRecords.getQueryDate();
        String scrollId = deviceIocRecords.getScrollId();

        List<String> iocList = JSONArray.parseArray(deviceIocRecords.getIocList(), String.class);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("key", iocList))
                .must(QueryBuilders.rangeQuery("lastUpdateTime").gt(lastQueryDate.getTime())))
                .sort("lastUpdateTime", SortOrder.ASC)
                .size(limit);

        Scroll scroll = new Scroll(TimeValue.timeValueSeconds(60));
        SearchHit[] hits;
        if (null == scrollId) {
            SearchRequest request = new SearchRequest("threat.ioc");
            request.source(searchSourceBuilder);
            request.scroll(scroll);

            SearchResponse response = highLevelClient.search(request, RequestOptions.DEFAULT);
            deviceIocRecords.setScrollId(response.getScrollId());
            deviceIocRecords.setQueryDate(new Date());
            deviceIocRecordsService.update(deviceIocRecords);
            hits = response.getHits().getHits();
            System.out.println("首次查询成功，共" + hits.length + "条数据");
        } else {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            SearchResponse response = highLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            hits = response.getHits().getHits();
            System.out.println("滚动查询成功，共" + hits.length + "条数据");
        }
        if (hits.length < limit) {
            deviceIocRecords.setScrollId(null);
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = highLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            System.out.println("清除滚动");
            System.out.println(clearScrollResponse.isSucceeded());

            deviceIocRecordsService.update(deviceIocRecords);
        }

        List<JSONObject> res = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            res.add(JSON.parseObject(hit.getSourceAsString(), JSONObject.class));
        }

        for (JSONObject jsonObject : res) {
            System.out.println(jsonObject.get("key"));
        }
    }

    @Test
    public void test() {
        System.out.println(idGenerator.nextId());

    }
}
