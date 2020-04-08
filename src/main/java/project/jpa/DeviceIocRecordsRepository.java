package project.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DeviceIocRecordsRepository extends JpaRepository<DeviceIocRecords, Long> {
    /**
     * 根据设备id查找
     * @param deviceId 设备编号
     */
    Optional<DeviceIocRecords> findByDeviceId(String deviceId);

    @Query(value = "select id,device_id,ioc_list,create_date,update_date from api_device_ioc_records where ioc_list like '%8.8.8.8%'", nativeQuery = true)
    List<Map<String, Object>> customerQueryTest();
}
