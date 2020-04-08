package project.utils;

import org.apache.commons.lang.StringUtils;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommonUtils {

    private static List<NetworkCardInfo> cardInfoListCache = null;

    public static Optional<String> hostname() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return Optional.ofNullable(addr.getHostName());
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    public static String ipv4ListString() {
        return String.join(",", ipv4List());
    }

    public static Collection<String> ipv4List() {
        return cardInfoList()
                .stream()
                .map(NetworkCardInfo::getAddress)
                .sorted()
                .collect(Collectors.toList());
    }

    protected static synchronized List<NetworkCardInfo> cardInfoList() {
        if(cardInfoListCache != null) {
            return cardInfoListCache;
        }

        List<NetworkCardInfo> cardInfoList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = netInterfaces.nextElement();
                // 过滤回环地址
                if(networkInterface.isLoopback()) {
                    continue;
                }
                // 过滤非启用的
                if(!networkInterface.isUp()) {
                    continue;
                }
                String interfaceName = networkInterface.getName();
                try {
                    NetworkCardInfo networkCardInfo = new NetworkCardInfo();
                    networkCardInfo.setCardName(interfaceName);
                    networkCardInfo.setMac(macAddrFromBytes(networkInterface.getHardwareAddress()));
                    String address = null;
                    Enumeration<InetAddress> netAddresses = networkInterface.getInetAddresses();
                    while (netAddresses.hasMoreElements()) {
                        InetAddress inetAddress = netAddresses.nextElement();
                        // 过滤ip6地址
                        if(inetAddress instanceof Inet6Address) {
                            continue;
                        }
                        // 是否可达
                        if(!inetAddress.isReachable(1000)) {
                            continue;
                        }
                        address = inetAddress.getHostAddress();
                    }
                    if(StringUtils.isBlank(address)) {
                        continue;
                    }
                    networkCardInfo.setAddress(address);
                    cardInfoList.add(networkCardInfo);
                } catch (Exception e) {

                }
            }
        } catch (SocketException e) {

        }
        cardInfoListCache = cardInfoList;
        return cardInfoList;
    }

    private static String macAddrFromBytes(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                stringBuilder.append(":");
            }
            int tmp = bytes[i] & 0xff; // 字节转换为整数
            String str = Integer.toHexString(tmp);
            if (str.length() == 1) {
                stringBuilder.append("0").append(str);
            } else {
                stringBuilder.append(str);
            }
        }
        return stringBuilder.toString();
    }
}
