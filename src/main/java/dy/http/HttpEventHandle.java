package dy.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dy.block.BlockProtocolProcessor;
import dy.block.CommonUtil;
import dy.dbserivce.IAppService;
import dy.dbserivce.IBusinessService;
import dy.dbserivce.IDeviceConfigService;
import dy.dbserivce.impl.DeviceConfigService;
import dy.entity.DeviceConfig;
import dy.ktv.CommunicationKtvProcessor;
import dy.mongo.MongoUtil;
import dy.type.DeviceIpMap;
import dy.cache.Appcache;
import dy.ktv.KtvProcessor;
import dy.log.AppLogger;
import dy.type.DeviceLog;
import dy.type.DeviceMetaData;
import dy.type.PostConfigData;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/6/22.
 */
@Component("httphandler")
@Scope("prototype")
@Data
public class HttpEventHandle implements HttpHandler {
    @Autowired
    Appcache appcache;

    @Autowired
    AppLogger appLogger;

    @Autowired
    KtvProcessor ktvProcessor;

    @Autowired
    IDeviceConfigService deviceConfigService;

    @Autowired
    IAppService appService;

    @Autowired
    IBusinessService businessService;

    @Autowired
    MongoUtil mongoUtil;

    private void responseOk(HttpExchange httpExchange) throws IOException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg","received data.");
        httpExchange.getResponseHeaders().add("Content-Type","application/json");
        httpExchange.getResponseHeaders().add("charset","UTF-8");
        httpExchange.sendResponseHeaders(200, jsonObject.toString().length());
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(jsonObject.toString().getBytes());
        outputStream.flush();
        outputStream.close();

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException{
        String requestMethod = httpExchange.getRequestMethod();
        if(requestMethod.equalsIgnoreCase("POST")){

            String content = IOUtils.toString(httpExchange.getRequestBody());
            appLogger.getLogger().info(String.format("http received config data [%s]",content));

            responseOk(httpExchange);
            PostConfigData postConfigData = null;

            try {
                postConfigData = JSONObject.parseObject(content,PostConfigData.class);
            }catch (Exception ex){
                appLogger.getLogger().error("platform parse config data to json object error",ex);
                return;
            }

            DeviceConfig config=new DeviceConfig();
            config.setSn(postConfigData.getSn());
            config.setConfigid(postConfigData.getConfigId());
            config.setTimeservice(postConfigData.getDeviceConfigData().getTimeservice());
            config.setConfigtype(postConfigData.getConfigtype());
            config.setCreatedate(new Date());

            if(businessService.getBusinesskey(postConfigData.getBussinesskey())<0){
                appLogger.getLogger().info(String.format("http can not find bussiness key[%s]",postConfigData.toString()));
                return;
            }
            //校验字段
            //通过bussinessid判断合法性配置
            //校验成功给用户返回
            if(postConfigData.getConfigtype().equals("block")){
                //生成块的byte[]
                postConfigData.getDeviceConfigData().setConfighexdata(CommonUtil.hexString2Bytes(postConfigData.getDeviceConfigData().getConfigdata()));
                config.setConfigdata(postConfigData.getDeviceConfigData().getConfigdata());
                //将配置存入数据库中
                deviceConfigService.saveConfig(config);

            }else if(postConfigData.getConfigtype().equals("privateblock")){
                //生成ktv的byte[]
                List<byte[]> listKtv = ktvProcessor.getKtvBytes(postConfigData.getDeviceConfigData().getPrivatedata());
                postConfigData.getDeviceConfigData().setConfighexdata(CommonUtil.hexString2Bytes(postConfigData.getDeviceConfigData().getConfigdata()));
                config.setConfigdata(JSONArray.toJSONString(postConfigData.getDeviceConfigData().getPrivatedata()));
                //将配置存入数据库中
                deviceConfigService.saveConfig(config);
            }else if(postConfigData.getConfigtype().equals("private")){
                //生成ktv的byte[]
                List<byte[]> listKtv = ktvProcessor.getKtvBytes(postConfigData.getDeviceConfigData().getPrivatedata());
                postConfigData.getDeviceConfigData().setConfighexdata(CommunicationKtvProcessor.getKtvConfigFrame(postConfigData.getSn(),listKtv));
                config.setConfigdata(JSONArray.toJSONString(postConfigData.getDeviceConfigData().getPrivatedata()));
                //将配置存入数据库中
                deviceConfigService.saveConfig(config);
            }else{
                return;
            }

            //设备日志
            List<DeviceLog> ListDeviceLog = new ArrayList<>();
            if(postConfigData.getDeviceConfigData().getTimeservice().equals("false")){

                DeviceLog deviceLog= new DeviceLog(config.getSn(),0, String.format("device normal config into cache[%s]",config.getConfigdata()),new Date());
                ListDeviceLog.add(deviceLog);

                //通用性配置
                ConcurrentHashMap<String,PostConfigData> postConfigDataConcurrentHashMap = appcache.getDeviceConfigMap().get(postConfigData.getSn());
                if(postConfigDataConcurrentHashMap == null){
                    postConfigDataConcurrentHashMap = new ConcurrentHashMap<>();
                    postConfigDataConcurrentHashMap.put(postConfigData.getConfigId(),postConfigData);
                    appcache.getDeviceConfigMap().put(postConfigData.getSn(),postConfigDataConcurrentHashMap);
                }else{
                    postConfigDataConcurrentHashMap.put(postConfigData.getConfigId(),postConfigData);
                    appcache.getDeviceConfigMap().put(postConfigData.getSn(),postConfigDataConcurrentHashMap);
                }
            }else{

                DeviceLog deviceLog= new DeviceLog(config.getSn(),0, String.format("device time config [%s]",config.getConfigdata()),new Date());
                ListDeviceLog.add(deviceLog);

                //实时配置
                DeviceIpMap deviceIpMap = appcache.getDeviceIpMap().get(postConfigData.getSn());
                SocketAddress remote = new InetSocketAddress(deviceIpMap.getIp(),deviceIpMap.getPort());
                IoBuffer configData= IoBuffer.wrap(postConfigData.getDeviceConfigData().getConfighexdata());
                deviceIpMap.getAcceptor().newSession(remote,deviceIpMap.getAcceptor().getLocalAddress()).write(configData);
                appLogger.getLogger().info(String.format("send time config to device [%s] [%s]",content, CommonUtil.byte2HexStr(postConfigData.getDeviceConfigData().getConfighexdata())));
            }

            //将配置日志存入MongoDB
            mongoUtil.insertMongo(new ArrayList<DeviceMetaData>(),ListDeviceLog);

        }
    }
}
