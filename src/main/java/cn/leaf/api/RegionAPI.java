package cn.leaf.api;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.asapi.ASClient;

import java.util.HashMap;
import java.util.Map;

public class RegionAPI {
    public String getRegion(String access_key_id, String access_key_secret, String endpoint) {
        // 创建ASClient连接
        ASClient client = new ASClient();
        // 设置身份标识,标识调用来源,无实际作用,可随意设置,必填项
        client.setSdkSource("asapi-1943@asapi-inc.com");
        client.setIsFormat(false);
        // ASAPI的Endpoint地址
        // 准备请求参数
        Map<String, Object> parameters = new HashMap<>();
        // 授权相关参数
        parameters.put(ASClient.ACCESSKEY_ID, access_key_id);
        parameters.put(ASClient.ACCESSKEY_SECRET, access_key_secret);
        parameters.put(ASClient.REGIONID, "a");

        // 产品接口信息参数
        parameters.put(ASClient.PRODUCT, "Ecs");
        parameters.put(ASClient.ACTION, "DescribeRegions");
        parameters.put(ASClient.VERSION, "2014-05-26");
        // 接口业务参数设置

        // 设置Headers
        // 调用专有云API时，一般需要提供多个公共Header参数，包括：x-acs-regionid、x-acs-organizationid、x-acs-resourcegroupid、x-acs-instanceid。详情可参照开发指南中的“获取公共Header参数"
        Map<String, String> headers = new HashMap<String, String>();
        try {
            // 发起请求，并获取返回
            String result = client.doPost(endpoint, headers, parameters);
            var obj = JSONObject.parse(result);
            return obj.getJSONObject("Regions").getJSONArray("Region").getJSONObject(0).getString("RegionId");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
