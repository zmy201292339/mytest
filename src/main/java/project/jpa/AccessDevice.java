package project.jpa;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity(name = "api_access_device")
public class AccessDevice {
    private String deviceId;

    private String customKey;

    private String customExtInfo;

    private Date createDate;

    private Date updateDate;

    @Id
    protected Long id;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }

    public String getCustomExtInfo() {
        return customExtInfo;
    }

    public void setCustomExtInfo(String customExtInfo) {
        this.customExtInfo = customExtInfo;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
