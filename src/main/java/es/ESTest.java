package es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public class ESTest {

    public static void main(String[] args) throws IOException {
        RestHighLevelClient highLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("10.50.25.68", 9200, "http")));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("key", "787fbec694fb9f3630fca2f2075c50771ebb628eeb48b5f60d94e54d20bfc726")));
        SearchRequest rq = new SearchRequest();
        rq.indices("threat.ioc");
        rq.source(searchSourceBuilder);
        SearchResponse rp = highLevelClient.search(rq, RequestOptions.DEFAULT);
        SearchHit[] hits = rp.getHits().getHits();
        System.out.println(hits[0].getSourceAsString());
    }
}