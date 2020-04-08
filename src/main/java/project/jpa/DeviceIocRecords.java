package project.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity(name = "api_device_ioc_records")
public class DeviceIocRecords {

    private Date createDate;

    private Date updateDate;

    private String deviceId;

    private String iocList;

    protected static final long serialVersionUID = 1L;

    @Id
    protected Long id;

    //上次拉取时间
    private Date queryDate;

    //拉取的页数id
    private String scrollId;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIocList() {
        return iocList;
    }

    public void setIocList(String iocList) {
        this.iocList = iocList;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getQueryDate() {
        return queryDate;
    }

    public void setQueryDate(Date queryDate) {
        this.queryDate = queryDate;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }
}
