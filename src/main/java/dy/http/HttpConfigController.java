package dy.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mchange.v2.lang.StringUtils;
import dy.block.CommonUtil;
import dy.cache.Appcache;
import dy.dbserivce.IBusinessService;
import dy.dbserivce.IDealTaskerService;
import dy.dbserivce.IDeviceConfigService;
import dy.entity.DeviceConfig;
import dy.entity.ViewAccessServiceQuery;
import dy.ktv.CommunicationKtvProcessor;
import dy.ktv.KtvProcessor;
import dy.log.AppLogger;
import dy.mongo.MongoUtil;
import dy.type.DeviceIpMap;
import dy.type.DeviceLog;
import dy.type.PostConfigData;
import org.apache.mina.core.buffer.IoBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class HttpConfigController {

    @Autowired
    Appcache appcache;

    @Autowired
    AppLogger appLogger;

    @Autowired
    KtvProcessor ktvProcessor;

    @Autowired
    IBusinessService businessService;

    @Autowired
    IDeviceConfigService deviceConfigService;

    @Autowired
    MongoUtil mongoUtil;

    @Autowired
    IDealTaskerService dealTaskerService;

    @Autowired
    CoapUtil coapUtil;


    @Value("${coap.config}")
    String coapConfig;

    @RequestMapping(value = "")
    public String deviceConfig(@RequestBody JSONObject json, HttpServletRequest request) throws Exception {
        appLogger.getLogger().info(String.format("data platform received config data : %s  ", JSON.toJSON(json)));
        PostConfigData postConfigData = JSONObject.parseObject(json.toJSONString(), PostConfigData.class);



        boolean insertFlag = false;
        DeviceConfig config =  deviceConfigService.getConfigByConfigid(postConfigData.getSn(),postConfigData.getConfigId());
        if(config == null){
            config = new DeviceConfig();
            insertFlag = true;
        }


        config.setSn(StringUtils.nonEmptyOrNull(postConfigData.getSn()));
        config.setConfigid(StringUtils.nonEmptyOrNull(postConfigData.getConfigId()));
        config.setTimeservice(StringUtils.nonEmptyOrNull(postConfigData.getDeviceConfigData().getTimeservice()));
        config.setConfigtype(StringUtils.nonEmptyOrNull(postConfigData.getConfigtype()));
        config.setCreatedate(new Date());
        postConfigData.setDate(new Date());

//        if (businessService.getBusinesskey(postConfigData.getBussinesskey()) <= 0) {
//            appLogger.getLogger().info(String.format("http can not find bussiness key : %s ", postConfigData.toString()));
//            return "data platform received config businesskey false";
//        }



        if(dealTaskerService.getByDeviceSnAndBusinessKey(postConfigData.getSn(),postConfigData.getBussinesskey()) == null){
            appLogger.getLogger().info(String.format("http can not find device false  : %s ", postConfigData.toString()));
            return "data platform received config data false";
        }


        ViewAccessServiceQuery device = dealTaskerService.getByDeviceSn(postConfigData.getSn());
        if(device != null){
            postConfigData.setSn(device.getSn());
        }else {
            return "data platform received config sn no exist";
        }


        //块协议
        if (postConfigData.getConfigtype().equals("block")) {
            //区分是否是ktv协议
            if (postConfigData.getDeviceConfigData().getPrivatedata() != null) {
                List<byte[]> listKtv = ktvProcessor.getKtvBytes(postConfigData.getDeviceConfigData().getPrivatedata());
                postConfigData.getDeviceConfigData().setConfighexdata(ktvProcessor.getKtvFrame(listKtv));
                config.setConfigdata(JSONArray.toJSONString(postConfigData.getDeviceConfigData().getPrivatedata()));
            } else if (postConfigData.getDeviceConfigData().getConfigdata() != null) {
                postConfigData.getDeviceConfigData().setConfighexdata(CommonUtil.hexString2Bytes(postConfigData.getDeviceConfigData().getConfigdata()));
                config.setConfigdata(postConfigData.getDeviceConfigData().getConfigdata());
            } else {
                return "data platform received http config parameters error";
            }
            //保存并发送配置
            if (insertFlag) {
                deviceConfigService.saveConfig(config);
            }else {
                deviceConfigService.updateConfig(config);
            }
            sendBlockAndKtvConfig(config, postConfigData);
        }
        //纯ktv协议
        else if (postConfigData.getConfigtype().equals("rtu")) {
            //生成ktv的byte[]
            List<byte[]> listKtv = ktvProcessor.getKtvBytes(postConfigData.getDeviceConfigData().getPrivatedata());
            postConfigData.getDeviceConfigData().setConfighexdata(CommunicationKtvProcessor.getKtvConfigFrame(postConfigData.getSn(), listKtv));
            config.setConfigdata(JSONArray.toJSONString(postConfigData.getDeviceConfigData().getPrivatedata()));
            //保存并发送配置
            if (insertFlag) {
                deviceConfigService.saveConfig(config);
            }else {
                deviceConfigService.updateConfig(config);
            }
            sendBlockAndKtvConfig(config, postConfigData);
        }
        //coap协议
        else if (postConfigData.getConfigtype().equals("coap")) {
            if (postConfigData.getDeviceConfigData().getPrivatedata() != null) {
                List<byte[]> listKtv = ktvProcessor.getKtvBytes(postConfigData.getDeviceConfigData().getPrivatedata());
                postConfigData.getDeviceConfigData().setConfighexdata(CommunicationKtvProcessor.getKtvConfigFrame(postConfigData.getSn(), listKtv));
                config.setConfigdata(JSONArray.toJSONString(postConfigData.getDeviceConfigData().getPrivatedata()));
            } else if (postConfigData.getDeviceConfigData().getConfigdata() != null) {
                postConfigData.getDeviceConfigData().setConfighexdata(CommonUtil.hexString2Bytes(postConfigData.getDeviceConfigData().getConfigdata()));
                config.setConfigdata(postConfigData.getDeviceConfigData().getConfigdata());
            } else {
                return "data platform received http config parameters error";
            }
            if (insertFlag) {
                deviceConfigService.saveConfig(config);
            }else {
                deviceConfigService.updateConfig(config);
            }
            sendCoapConfig(config, postConfigData);
        } else {
            return "data platform received http config parameters error";
        }
        return "data platform received nonconfig";
    }

    private void sendCoapConfig(DeviceConfig config, PostConfigData postConfigData) {
        //设备日志
        boolean timeFlag = false;
        if (postConfigData.getDeviceConfigData().getTimeservice().equals("false")) {
            timeFlag = true;
        }
        DeviceLog deviceLog = new DeviceLog(config.getSn(), 0, String.format("service send config to coap platform : %s ", config.getConfigdata()), new Date());
        appcache.getDeviceLogList().addObj(deviceLog);
        Map<String, Object> coapCongfig = new HashMap<>();
        String configCoapStr = CommonUtil.encode64(postConfigData.getDeviceConfigData().getConfighexdata());
        coapCongfig.put(coapConfig, configCoapStr);
//        coapCongfig.put("msgid", configCoapStr.length());
        ViewAccessServiceQuery viewAccessServiceQuery = dealTaskerService.getBySn(config.getSn());
        if(viewAccessServiceQuery != null){
            String coapResult = coapUtil.sendconfig(viewAccessServiceQuery.getCoapdeviceid(), JSON.toJSONString(coapCongfig), timeFlag);
            //更改配置状态
            if(coapResult != null){
                config.setStatus("成功发送设备配置到coap平台");
            }else{
                config.setStatus("发送设备coap配置失败");
            }
        }else{
            config.setStatus("设备不存在");
        }
        deviceConfigService.updateConfig(config);
    }


    private void sendBlockAndKtvConfig(DeviceConfig config, PostConfigData postConfigData) {
        //设备日志
        if (postConfigData.getDeviceConfigData().getTimeservice().equals("false")) {
            DeviceLog deviceLog = new DeviceLog(config.getSn(), 0, String.format("service put device config into cache : %s", config.getConfigdata()), new Date());
            appcache.getDeviceLogList().addObj(deviceLog);

            //通用性配置
            ConcurrentHashMap<String, PostConfigData> postConfigDataConcurrentHashMap = appcache.getDeviceConfigMap().get(postConfigData.getSn());
            if (postConfigDataConcurrentHashMap == null) {
                postConfigDataConcurrentHashMap = new ConcurrentHashMap<>();
                postConfigDataConcurrentHashMap.put(postConfigData.getConfigId(), postConfigData);
                appcache.getDeviceConfigMap().put(postConfigData.getSn(), postConfigDataConcurrentHashMap);
            } else {
                postConfigDataConcurrentHashMap.put(postConfigData.getConfigId(), postConfigData);
                appcache.getDeviceConfigMap().put(postConfigData.getSn(), postConfigDataConcurrentHashMap);
            }
        } else {
            DeviceLog deviceLog = new DeviceLog(config.getSn(), 0, String.format("service send device time config %s", config.getConfigdata()), new Date());
            appcache.getDeviceLogList().addObj(deviceLog);
            //实时配置
            DeviceIpMap deviceIpMap = appcache.getDeviceIpMap().get(postConfigData.getSn());
            SocketAddress remote = new InetSocketAddress(deviceIpMap.getIp(), deviceIpMap.getPort());
            IoBuffer configData = IoBuffer.wrap(postConfigData.getDeviceConfigData().getConfighexdata());
            deviceIpMap.getAcceptor().newSession(remote, deviceIpMap.getAcceptor().getLocalAddress()).write(configData);
            appLogger.getLogger().info(String.format("service send time config to device %s %s", JSON.toJSON(postConfigData), CommonUtil.byte2HexStr(postConfigData.getDeviceConfigData().getConfighexdata())));

            //更改配置状态
            config.setStatus("设备实时配置已发送");
            ConcurrentHashMap<String,String> deviceConfigStatusConcurrentHashMap = new ConcurrentHashMap<>();
            deviceConfigStatusConcurrentHashMap.put(config.getConfigid(),"设备配置已发送");
            if( appcache.getDeviceConfigStatusMap().containsKey(config.getSn())){
                appcache.getDeviceConfigStatusMap().get(config.getSn()).putAll(deviceConfigStatusConcurrentHashMap);
            }else {
                appcache.getDeviceConfigStatusMap().put(config.getSn(),deviceConfigStatusConcurrentHashMap);
            }
            deviceConfigService.updateConfig(config);
        }
    }
}
