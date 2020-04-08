package project.jpa;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DeviceIocRecordsService {

    @Resource
    private DeviceIocRecordsRepository deviceIocRecordsRepository;

    public Optional<DeviceIocRecords> findByDeviceId(String deviceId) {
        if(StringUtils.isBlank(deviceId)) {
            return Optional.empty();
        }
        return this.deviceIocRecordsRepository.findByDeviceId(deviceId);
    }

    public void update(DeviceIocRecords deviceIocRecords) {
        this.deviceIocRecordsRepository.save(deviceIocRecords);
    }

    public List<Map<String, Object>> customerQueryTest() {
        return deviceIocRecordsRepository.customerQueryTest();
    }
}
