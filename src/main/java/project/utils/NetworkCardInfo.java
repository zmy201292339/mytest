package project.utils;

public class NetworkCardInfo {
    /**
     * 网卡名
     */
    private String cardName;

    /**
     * ip地址
     */
    private String address;

    /**
     * mac地址
     */
    private String mac;

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
