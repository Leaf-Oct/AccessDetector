package cn.leaf.api;

import cn.leaf.Config;
import cn.leaf.PlatformEnum;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.asapi.ASClient;

import java.util.HashMap;
import java.util.Map;

public class DescribeInstanceAPI {

    public JSONObject instance;
    public String key_id, key_secret, endpoint, regionID;
    public PlatformEnum p;

    public DescribeInstanceAPI(PlatformEnum p) {
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


    public String getInstanceID(String ip){
        ASClient client = new ASClient();
        // 设置身份标识,标识调用来源,无实际作用,可随意设置,必填项
        client.setSdkSource("asapi-6880@asapi-inc.com");
        client.setIsFormat(false);
        // 准备请求参数
        Map<String, Object> parameters = new HashMap<String, Object>();
        // 授权相关参数
        parameters.put(ASClient.ACCESSKEY_ID, key_id);
        parameters.put(ASClient.ACCESSKEY_SECRET, key_secret);
        parameters.put(ASClient.REGIONID, regionID);

        // 产品接口信息参数
        parameters.put(ASClient.PRODUCT, "Ecs");
        parameters.put(ASClient.ACTION, "DescribeInstances");
        parameters.put(ASClient.VERSION, "2015-06-26");
        // 接口业务参数设置
        parameters.put("PrivateIpAddresses","[\""+ip+"\"]");
        // 设置Headers
        // 调用专有云API时，一般需要提供多个公共Header参数，包括：x-acs-regionid、x-acs-organizationid、x-acs-resourcegroupid、x-acs-instanceid。详情可参照开发指南中的“获取公共Header参数"
        Map<String, String> headers = new HashMap<String, String>();
        try {
            // 发起请求，并获取返回
            String result = client.doPost(endpoint, headers, parameters);
            var obj = JSONObject.parse(result);
            if(!obj.getBoolean("success")){
                System.err.println("通过IP "+ip+" 查询实例ID失败");
                return null;
            }
            var instance_list=obj.getJSONObject("Instances").getJSONArray("Instance");
            if (instance_list.size()==0){
                System.err.println("找不到IP " + ip + " 对应实例");
                return null;
            }
            if (instance_list.size()>1){
                System.out.println("找到多台机器，共"+instance_list.size()+"台\n逐台筛选中");
                for(var i:instance_list){
                    var instance_obj=(JSONObject)i;
                    var interfaces=instance_obj.getJSONObject("NetworkInterfaces").getJSONArray("NetworkInterface");
                    for(var j:interfaces){
                        var interface_obj=(JSONObject)j;
                        if (interface_obj.getString("PrimaryIpAddress").equals(ip)){
                            instance=instance_obj;
                            return instance_obj.getString("InstanceId");
                        }
                    }
                }
            }
            instance=instance_list.getJSONObject(0);
            return  instance.getString("InstanceId");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String[] getSecurityGroupID(){
        var list=instance.getJSONObject("SecurityGroupIds").getJSONArray("SecurityGroupId");
        String[] sg_ids=new String[list.size()];
        for(int i=0;i< list.size();i++){
            sg_ids[i]=list.getString(i);
        }
        return sg_ids;
    }
}
