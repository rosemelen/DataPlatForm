package dy.task;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import dy.block.BlockData;
import dy.block.BlockProtocolProcessor;
import dy.block.CommonUtil;
import dy.cache.Appcache;
import dy.dbserivce.IDealTaskerService;
import dy.entity.DeviceConfig;
import dy.entity.ResponseBytes;
import dy.entity.ViewAccessServiceQuery;
import dy.http.CoapUtil;
import dy.ktv.*;
import dy.log.AppLogger;
import dy.type.DeviceIpMap;
import dy.type.DeviceMetaData;

import dy.type.ProcessorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("dealcoaptasker")
@EnableScheduling
public class DealCoapTasker {
    @Autowired
    AppLogger appLogger;
    @Autowired
    Appcache appcache;
    @Autowired
    IDealTaskerService service;
    @Autowired
    CommunicationKtvProcessor communicationKtvProcessor;
    @Autowired
    BlockProtocolProcessor blockProtocolProcessor;
    @Autowired
    CoapUtil coapUtil;
    @Value("${coap.config}")
    String coapConfig;

    @Scheduled(fixedDelayString = "${taskDealData}000", initialDelay = 5000)
    public void dealCoap() throws Exception {
        List<DeviceMetaData> deviceCoapMetaDataList = appcache.getDeviceCoapMetaDataList().getClone();
        if (deviceCoapMetaDataList.size() == 0) { return; }


        //查数据库，找出deviceid对应的devicesn
        List<String> groupCoapList = new ArrayList<>(Collections2.transform(deviceCoapMetaDataList, new Function<DeviceMetaData, String>() {
            @Override
            public String apply(DeviceMetaData deviceMetaData) {
                return deviceMetaData.getDeviceid();
            }
        }));

        //去重
        List<String> groupDistCoapDeviceId = groupCoapList.stream().distinct().collect(Collectors.toList());
        String idStr = Joiner.on("','").skipNulls().join(groupDistCoapDeviceId);
        List<ViewAccessServiceQuery> viewlist = service.getQueryByCondition("coapdeviceid", "'" + idStr + "'");
        List<DeviceMetaData> listResultKtv = new ArrayList<>();
        List<DeviceMetaData> listResultBlock = new ArrayList<>();
        //查询失败
        if (viewlist.size() == 0) {
            appLogger.getLogger().error("dataplatform query coapdeviceid error :" + idStr);
            return;
        }else {
            for (ViewAccessServiceQuery viewAccessServiceQuery : viewlist) {
                for (DeviceMetaData deviceMetaData : deviceCoapMetaDataList) {
                    if (viewAccessServiceQuery.getCoapdeviceid().equals(deviceMetaData.getDeviceid())) {
                        deviceMetaData.setDevicesn(viewAccessServiceQuery.getSn());
                        if(viewAccessServiceQuery.getKtvgroupid() != null){
                            listResultKtv.add(deviceMetaData);
                        }else {
                            listResultBlock.add(deviceMetaData);
                        }
                    }
                }
            }
        }

        //处理ktv数据
        for(DeviceMetaData deviceMetaData : listResultKtv){
            ResponseBytes responseBytes = communicationKtvProcessor.dealResponseBytes(deviceMetaData.getMetaBytes(),
                                                communicationKtvProcessor.getCommunicationKtvData(deviceMetaData.getMetaBytes()),deviceMetaData);
            if(responseBytes != null){
                if(responseBytes.isReturnData()){
                    Map<String, Object> coapCongfig = new HashMap<>();
                    coapCongfig.put(coapConfig, CommonUtil.encode64(responseBytes.getResponseBytes()));
                    String coapResult = coapUtil.sendconfig(deviceMetaData.getDeviceid(), JSON.toJSONString(coapCongfig), false);
                    if(coapResult != null){
                        appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(deviceMetaData.getDevicesn(), null,"成功发送授时到coap平台"));
                    }else{
                        appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(deviceMetaData.getDevicesn(), null,"发送授时到coap平台失败"));
                    }
                }else {
                    appcache.getDeviceMetaDataList().addObj(deviceMetaData);
                }
            }
        }
        //处理块数据
        for(DeviceMetaData deviceMetaData : listResultBlock){
            BlockData blockData = blockProtocolProcessor.getBlockData(deviceMetaData.getMetaBytes());
            DeviceIpMap deviceIpMap = new DeviceIpMap();
            ProcessorType processorType = new ProcessorType(0,1,null);
            deviceIpMap.setProcessorType(processorType);
            ResponseBytes responseBytes = blockProtocolProcessor.dealPostData(blockData,deviceIpMap);
            if(responseBytes != null) {
                if (responseBytes.isReturnData()) {
                    Map<String, Object> coapCongfig = new HashMap<>();
                    coapCongfig.put(coapConfig, CommonUtil.encode64(responseBytes.getResponseBytes()));
                    String coapResult = coapUtil.sendconfig(deviceMetaData.getDeviceid(), JSON.toJSONString(coapCongfig), false);
                    if(coapResult != null){
                        appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(deviceMetaData.getDevicesn(), null,"成功发送上报数据回复到coap平台"));
                    }else{
                        appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(deviceMetaData.getDevicesn(), null,"发送上报数据回复到coap平台失败"));
                    }
                }
            }
        }
    }
}

