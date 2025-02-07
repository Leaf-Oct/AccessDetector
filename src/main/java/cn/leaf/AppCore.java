package cn.leaf;

import cn.leaf.api.DescribeInstanceAPI;
import cn.leaf.api.RegionAPI;
import cn.leaf.api.SecurityGroupAPI;
import com.alibaba.fastjson2.JSONObject;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import static cn.leaf.PlatformEnum.*;

public class AppCore {
    public static final String IN = "ingress", OUT = "egress", TCP = "TCP", UDP = "UDP", ALL = "ALL",
            灾备_内网生产区VPC = "10.163.0.0/17",
            灾备_SOC关键基础区 = "10.163.128.0/22",
            X86_内网生产区 = "10.83.0.0/16",
            X86_内网测试区 = "10.79.192.0/19",
            X86_生产中立区 = "172.24.0.0/16",
            ARM_内网生产区 = "10.84.0.0/16";

    public static TextArea log_area;

    public static void printLog(String line){
        Platform.runLater(()->{
            log_area.appendText(line);
            log_area.appendText("\n");
        });
    }

    public static void main(String[] args) {
//        init();
//        var in = new Scanner(System.in);
//        String from, to;
//        int port;
//        System.out.println("输入访问关系四元组，用空格隔开：协议 源IP 目的IP 端口\n输入quit或exit退出\n输入help提示");
//        while (true) {
//            var line = in.nextLine();
//            if (line.equals("quit") || line.equals("exit")) {
//                break;
//            }
//            if (line.equals("help")) {
//                System.out.println("暂时只支持单个IP间访问关系检测");
//                System.out.println("可输入选项与示例：");
//                System.out.println("协议：TCP/UDP");
//                System.out.println("源IP、目的IP：IPv4地址，例如10.83.1.1, 10.163.233.0");
//                System.out.println("端口：1到65535之间的数");
//            }
//            var rules=line.split(" ");
//            if(checkRule(rules)){
//                from=rules[1];
//                to=rules[2];
//                port=Integer.parseInt(rules[3]);
//                judgeRule(rules[0], from, to, port);
//            }
//        }
//        in.close();
    }

    public static boolean checkRule(String[] rules){
        try {
            rules[0]=rules[0].toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (!rules[0].equals(TCP)&&!rules[0].equals(UDP)){
            printLog("协议不符，须为TCP或者UDP");
            return false;
        }
        try {
            Inet4Address.getByName(rules[1]);
            Inet4Address.getByName(rules[2]);
        } catch (Exception e){
            e.printStackTrace();
            printLog("IP地址格式不对");
            return false;
        }
        try {
            var port=Integer.valueOf(rules[3]);
            if (port<1||port>65535){
                printLog("端口范围不对");
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            printLog("端口格式不对");
            return false;
        }
        return true;
    }

    //    初始化，获取一些基本信息，包括区域ID，VPCid
    public static void init() {
        var region_api = new RegionAPI();
        Config.dr_region_id = region_api.getRegion(Config.DR_ACCESSKEYID, Config.DR_ACCESSKEYSECRET, Config.DR_ENDPOINT);
        Config.arm_region_id = region_api.getRegion(Config.ARM_ACCESSKEYID, Config.ARM_ACCESSKEYSECRET, Config.ARM_ENDPOINT);
        Config.x86_region_id = region_api.getRegion(Config.X86_ACCESSKEYID, Config.X86_ACCESSKEYSECRET, Config.X86_ENDPOINT);
    }

    /*
    一条网络访问规则，单次只支持一个源IP一个目的IP一个端口，目前仅支持IPV4。例如
    from 10.83.1.1
    to  10.84.1.1
    port 443
    目前仅支持灾备云
     */
    public static boolean judgeRule(String protocal, String from, String to, int port) {
        PlatformEnum fromIP_platform = isCloudPlatform(from), toIP_platform = isCloudPlatform(to);
        if (fromIP_platform != null) {
            System.out.println("出方向是云平台IP，所属平台为 "+getPlatformName(fromIP_platform));
            var result = outCheck(fromIP_platform, protocal, from, to, port);
            if (result) {
                System.out.println("出方向通");
            } else {
                System.out.println("出方向不通");
            }
        }
        if (toIP_platform != null) {
            System.out.println("入方向是云平台IP，所属平台为 "+getPlatformName(toIP_platform));
            var result = inCheck(toIP_platform, protocal, from, to, port);
            if (result) {
                System.out.println("入方向通");
            } else {
                System.out.println("入方向不通");
            }
        }
        return true;
    }

    //    判断一个IP是否是系统组该管的云平台，如果不是则返回false
    public static PlatformEnum isCloudPlatform(String ip) {
        if (!isIPNOinSubnet(ip, 灾备_内网生产区VPC) || !isIPNOinSubnet(ip, 灾备_SOC关键基础区)) {
            return DR;
        }
        if (!isIPNOinSubnet(ip, X86_内网生产区) || !isIPNOinSubnet(ip, X86_内网测试区) || !isIPNOinSubnet(ip, X86_生产中立区)) {
            return X86;
        }
        if (!isIPNOinSubnet(ip, ARM_内网生产区)) {
            return ARM;
        }
        return null;
    }

    public static boolean isIPNOinSubnet(String ip, String net) {
        var net_and_subnet_length = net.split("/");
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            InetAddress subnetAddress = InetAddress.getByName(net_and_subnet_length[0]);
            int prefixLength = Integer.parseInt(net_and_subnet_length[1]);

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
                        return true;
                    }

                    prefixLength -= maskBits;
                } else {
                    break;
                }
            }
            return false;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address or subnet", e);
        }
    }

    public static boolean outCheck(PlatformEnum platform, String protocal, String from, String to, int port) {
//            根据IP查询实例id，得到String, id
        var instance_api = new DescribeInstanceAPI(platform);
        var instance_id = instance_api.getInstanceID(from);
        if (instance_id == null) {
            System.err.println("查不到IP " + from + " 对应的机器，可能是负载均衡或者数据库？");
            return false;
        }
//            根据实例ID查询安全组，得到安全组出方向，可能有多个，所以是List
//            一个一个核查toIP和端口，如果有符合的，返回这一条规则所有内容
        var sgs = instance_api.getSecurityGroupID();
        for (var sg_id : sgs) {
            boolean ok = false;
            System.out.print("检查安全组" + sg_id);
            var sg = new SecurityGroupAPI(platform);
            var rules = sg.getSG(sg_id, OUT);
            System.out.println(" (" + sg.name + ")");
            for (var i : rules) {
                var rule_obj = (JSONObject) i;
//                      先看允许还是拒绝
                var policy = rule_obj.getString("Policy");
                if (policy.equals("Drop")) {
                    continue;
                }
//                    其次看协议是否匹配
                var p = rule_obj.getString("IpProtocol");
                if (!p.equals(protocal) && !p.equals(ALL)) {
                    continue;
                }
//                    再看网段是否符合
                var ip = rule_obj.getString("DestCidrIp");
//                        如果没有/，即是纯IP，不是网段，直接判断
                if (ip.contains("/")) {
//                  判断
                    if (isIPNOinSubnet(to, ip)) {
                        continue;
                    }
                } else {
//                  纯IP直接判断，不相等就直接判不同
                    if (!ip.equals(to)) {
                        continue;
                    }
                }
//             看端口是否适配
                var port_range = rule_obj.getString("PortRange");
                if (port_range.equals("-1/-1")) {
                    System.out.println("发现一条通行规则：");
                    System.out.println(policy + "\t" + p + "\t" + ip + "\t" + port_range);
                    ok = true;
                }
                var port_ranges = port_range.split("/");
                var from_port = Integer.parseInt(port_ranges[0]);
                var to_port = Integer.parseInt(port_ranges[1]);
                if (port <= to_port && port >= from_port) {
                    System.out.println("发现一条通行规则：");
                    System.out.println(policy + "\t" + p + "\t" + ip + "\t" + port_range);
                    ok = true;
                }
            }
            if (ok) {
                return true;
            }
        }
        return false;
    }

    public static boolean inCheck(PlatformEnum platform, String protocal, String from, String to, int port) {
//            根据IP查询实例id，得到String, id
        var instance_api = new DescribeInstanceAPI(platform);
        var instance_id = instance_api.getInstanceID(to);
        if (instance_id == null) {
            System.err.println("查不到IP " + to + " 对应的机器，可能是负载均衡或者数据库？");
            return false;
        }
//            根据实例ID查询安全组，得到安全组出方向，可能有多个，所以是List
//            一个一个核查toIP和端口，如果有符合的，返回这一条规则所有内容
        var sgs = instance_api.getSecurityGroupID();
        for (var sg_id : sgs) {
            System.out.print("检查安全组" + sg_id);
            boolean ok = false;
            var sg = new SecurityGroupAPI(platform);
            var rules = sg.getSG(sg_id, IN);
            System.out.println(" (" + sg.name + ")");
            for (var i : rules) {
                var rule_obj = (JSONObject) i;
//                      先看允许还是拒绝
                var policy = rule_obj.getString("Policy");
//                System.out.println(policy);
                if (policy.equals("Drop")) {
                    continue;
                }
//                    其次看协议是否匹配
                var p = rule_obj.getString("IpProtocol");
//                System.out.println(p);
                if (!p.equals(protocal) && !p.equals(ALL)) {
                    continue;
                }
//                    再看网段是否符合
                var ip = rule_obj.getString("SourceCidrIp");
//                System.out.println(ip);
//                        如果没有/，即是纯IP，不是网段，直接判断
                if (ip.contains("/")) {
//                  判断
                    if (isIPNOinSubnet(from, ip)) {
                        continue;
                    }
                } else {
//                  纯IP直接判断，不相等就直接判不同
                    if (!ip.equals(to)) {
                        continue;
                    }
                }
//             看端口是否适配
                var port_range = rule_obj.getString("PortRange");
//                System.out.println(port_range);
                if (port_range.equals("-1/-1")) {
                    System.out.println("发现一条通行规则：");
                    System.out.println(policy + "\t" + p + "\t" + ip + "\t" + port_range);
                    ok = true;
                    continue;
                }
                var port_ranges = port_range.split("/");
                var from_port = Integer.parseInt(port_ranges[0]);
                var to_port = Integer.parseInt(port_ranges[1]);
                if (port <= to_port && port >= from_port) {
                    System.out.println("发现一条通行规则：");
                    System.out.println(policy + "\t" + p + "\t" + ip + "\t" + port_range);
                    ok = true;
                }
            }
            if (ok) {
                return true;
            }
        }
        return false;
    }
    public static String getPlatformName(PlatformEnum pe){
        switch (pe){
            case ARM -> {
                return "ARM";
            }
            case X86 -> {
                return "X86";
            }
            case DR -> {
                return "灾备";
            }
            default -> {
                return null;
            }
        }
    }

}
