package cn.leaf;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPInSubnet {

    /**
     * 判断给定的IP地址是否在一个特定的网段内。
     *
     * @param ipAddress 给定的IP地址，例如 "192.168.1.1"
     * @param subnet 网段，以CIDR格式给出，例如 "192.168.0.0/16"
     * @return 如果IP地址属于网段，则返回true；否则返回false。
     */
    public static boolean isIpInSubnet(String ipAddress, String subnet) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            String[] parts = subnet.split("/");
            InetAddress subnetAddress = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipBytes = inetAddress.getAddress();
            byte[] subnetBytes = subnetAddress.getAddress();

            // 计算掩码并应用到ip和subnet上
            for (int i = 0; i < ipBytes.length; i++) {
                if (prefixLength > 0) {
                    // 对于每个字节，计算需要保留多少位（1的个数）
                    int maskBits = Math.min(8, prefixLength);
                    byte mask = (byte) ((0xFF << (8 - maskBits)) & 0xFF);

                    // 比较被掩码后的IP地址和子网地址的对应字节
                    if ((ipBytes[i] & mask) != (subnetBytes[i] & mask)) {
                        return false;
                    }

                    prefixLength -= maskBits;
                } else {
                    break;
                }
            }
            return true;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address or subnet", e);
        }
    }

    public static void main(String[] args) {
        // 测试代码
        System.out.println(isIpInSubnet("192.168.1.1", "192.168.0.0/16")); // 应该打印 true
        System.out.println(isIpInSubnet("192.168.1.1", "192.168.2.0/24")); // 应该打印 false
    }
}