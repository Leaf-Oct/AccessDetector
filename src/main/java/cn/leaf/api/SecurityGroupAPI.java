package cn.leaf.api;

import cn.leaf.Config;
import cn.leaf.PlatformEnum;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.asapi.ASClient;

import java.util.HashMap;
import java.util.Map;

public class SecurityGroupAPI {
    public PlatformEnum p;
    public String key_id, key_secret, endpoint, regionID;
    public String name;

    public SecurityGroupAPI(PlatformEnum p) {
        this.p = p;
        switch (p){
            case DR:
                key_id= Config.DR_ACCESSKEYID;
                key_secret=Config.DR_ACCESSKEYSECRET;
                endpoint=Config.DR_ENDPOINT;
                regionID=Config.dr_region_id;
                break;
            case X86:
                key_id= Config.X86_ACCESSKEYID;
                key_secret=Config.X86_ACCESSKEYSECRET;
                endpoint=Config.X86_ENDPOINT;
                regionID=Config.x86_region_id;
                break;
            case ARM:
                key_id= Config.ARM_ACCESSKEYID;
                key_secret=Config.ARM_ACCESSKEYSECRET;
                endpoint=Config.ARM_ENDPOINT;
                regionID=Config.arm_region_id;
                break;
        }
    }


    public JSONArray getSG(String sg_id, String direction) {
        // 创建ASClient连接
        ASClient client = new ASClient();
        // 设置身份标识,标识调用来源,无实际作用,可随意设置,必填项
        client.setSdkSource("asapi-7521@asapi-inc.com");
        client.setIsFormat(false);
        // 准备请求参数
        Map<String, Object> parameters = new HashMap<String, Object>();
        // 授权相关参数
        parameters.put(ASClient.ACCESSKEY_ID, key_id);
        parameters.put(ASClient.ACCESSKEY_SECRET, key_secret);
        parameters.put(ASClient.REGIONID, regionID);

        // 产品接口信息参数
        parameters.put(ASClient.PRODUCT, "Ecs");
        parameters.put(ASClient.ACTION, "DescribeSecurityGroupAttribute");
        parameters.put(ASClient.VERSION, "2014-05-26");
        // 接口业务参数设置
        parameters.put("Direction", direction);
        parameters.put("SecurityGroupId", sg_id);
        // 设置Headers
        // 调用专有云API时，一般需要提供多个公共Header参数，包括：x-acs-regionid、x-acs-organizationid、x-acs-resourcegroupid、x-acs-instanceid。详情可参照开发指南中的“获取公共Header参数"
        Map<String, String> headers = new HashMap<String, String>();
        try {
            // 发起请求，并获取返回
            String result = client.doPost(endpoint, headers, parameters);
            var obj = JSONObject.parse(result);
            if(obj.getJSONObject("Permissions")==null){
                System.err.println("安全组不存在");
                return null;
            }
            name=obj.getString("SecurityGroupName");
            return obj.getJSONObject("Permissions").getJSONArray("Permission");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
