package dy.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.mongodb.client.MongoCollection;
import dy.block.CommonUtil;
import dy.cache.Appcache;
import dy.dbserivce.IDealTaskerService;
import dy.entity.ViewAccessServiceQuery;
import dy.entity.ViewDeviceapplication;
import dy.http.HttpPoster;
import dy.ktv.KTV;
import dy.ktv.KtvParser;
import dy.ktv.KtvProcessor;
import dy.log.AppLogger;
import dy.mongo.MongoUtil;
import dy.type.DeviceLog;
import dy.type.DeviceMetaData;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("dealtasker")
@EnableScheduling
public class DealTasker {
    @Autowired
    private AppLogger appLogger;

    @Autowired
    private Appcache appcache;

    @Autowired
    private IDealTaskerService service;

    @Autowired
    KtvParser ktvParser;

    @Autowired
    HttpPoster httpPoster;

    @Autowired
    KtvProcessor ktvProcessor;

    @Autowired
    MongoUtil mongoUtil;

    @Scheduled(fixedDelayString = "${taskDealData}000",initialDelay = 5000)
    public void dealData() throws Exception {

        List<DeviceMetaData> deviceMetaDataList = appcache.getDeviceMetaDataList().getClone();
        if(deviceMetaDataList.size() == 0){
            return;
        }

//        //找出coap设备,devicesn为空deviceid不为空
//        List<DeviceMetaData> listCoapDeviceData = deviceMetaDataList.stream().filter(x->x.getDevicesn() == null).collect(Collectors.toList());
//
//        //查数据库，找出deviceid对应的devicesn
//        if(listCoapDeviceData.size()>0){
//            List<String> groupCoapList = new ArrayList<>(Collections2.transform(listCoapDeviceData, new Function<DeviceMetaData, String>() {
//                @Override
//                public String apply(DeviceMetaData deviceMetaData) {
//                    return deviceMetaData.getDeviceid();
//                }
//            }));
//
//            //去重
//            List<String> groupDistCoapDeviceId = groupCoapList.stream().distinct().collect(Collectors.toList());
//            String idStr=Joiner.on("','").skipNulls().join(groupDistCoapDeviceId);
//            List<ViewAccessServiceQuery> viewlist = service.getQueryByCondition("coapdeviceid","'"+idStr+"'");
//
//            //查询失败
//            if(viewlist.size() == 0){
//                appLogger.getLogger().error("dataplatform query coapdeviceid error :" + idStr);
//                System.out.println("dataplatform query coapdeviceid error :" + idStr);
//            }else{
//                for(ViewAccessServiceQuery viewAccessServiceQuery : viewlist ){
//                    for (DeviceMetaData deviceMetaData :deviceMetaDataList){
//                        if(viewAccessServiceQuery.getCoapdeviceid().equals(deviceMetaData.getDeviceid()) ){
//                            deviceMetaData.setDevicesn(viewAccessServiceQuery.getSn());
//                        }
//                    }
//                }
//            }
//
//        }

        for(DeviceMetaData deviceMetaData : deviceMetaDataList){
            if(deviceMetaData.getHexData() == null){
                deviceMetaData.setHexData(CommonUtil.byte2HexStr(deviceMetaData.getMetaBytes()));
            }
        }

        appLogger.getLogger().info("task count device group");
        //获取设备分组
        List<String> groupingList = new ArrayList<>(Collections2.transform(deviceMetaDataList, new Function<DeviceMetaData, String>() {
            @Override
            public String apply(DeviceMetaData deviceMetaData) {
                return deviceMetaData.getDevicesn();
            }
        }));


        //去重
        List<String> groupDistSn = groupingList.stream().distinct().collect(Collectors.toList());


        //查询设备处理信息
        String snStr=Joiner.on("','").skipNulls().join(groupDistSn);
        List<ViewAccessServiceQuery> viewlist= service.getQueryByCondition("sn","'"+snStr+"'");

        //查询失败
        if(viewlist.size() == 0){
            appLogger.getLogger().error("dataplatform query devicesn error :" + snStr);
            System.out.println("dataplatform query devicesn error :" + snStr);
            return;
        }

        //过滤掉未注册设备的数据
        for(String sn:groupDistSn){
            if(!viewlist.stream().anyMatch(list->list.getSn().contains(sn))){
                deviceMetaDataList.removeIf(s->s.getDevicesn().equals(sn));
            }
        }

        //获取设备查询分组的ktv
        List<ViewAccessServiceQuery> listResultKtv = viewlist.stream().filter(list->list.getKtvgroupid() != null).collect(Collectors.toList());

        if (listResultKtv.size() > 0) {
            appLogger.getLogger().info("dataplatform query device group success and begin to analysis ktv data");
            for (DeviceMetaData deviceMetaData : deviceMetaDataList) {
                if (listResultKtv.stream().anyMatch(data -> data.getSn().equals(deviceMetaData.getDevicesn()))) {
                    List<byte[]> ktvFrameList = ktvProcessor.dealKtvFrame(deviceMetaData.getMetaBytes());
                    if(ktvFrameList.size() == 0) {
                        continue;
                    }
                    if(deviceMetaData.getKtvData() == null){
                        List<List<KTV>> tmpList = new ArrayList<>();
                        deviceMetaData.setKtvData(tmpList);
                    }
                    for(byte[] bytes:ktvFrameList){
                        List<KTV> listTmpKtvList = ktvParser.dataAnalysis(bytes);
                        for(KTV ktv:listTmpKtvList){
                            ktv.setTypeValue();
                        }
                        deviceMetaData.getKtvData().add(listTmpKtvList);
                    }
                }
            }
        }


        appLogger.getLogger().info("task begin to push device data by url");
        //按照推送地址分组推送
        List<ViewAccessServiceQuery> listPushUrl = viewlist.stream().filter(data->data.getPushaddress()!=null).collect(Collectors.toList());
        Map<String,List<ViewAccessServiceQuery>> resultPushurl = listPushUrl.stream().collect(Collectors.groupingBy(ViewAccessServiceQuery::getPushaddress));

        for(String url:resultPushurl.keySet()){
            List<DeviceMetaData> devicepostList=new ArrayList<>();
            for(ViewAccessServiceQuery viewDeviceapplication:resultPushurl.get(url)){
                for(DeviceMetaData deviceMetaData:deviceMetaDataList){
                    if(deviceMetaData.getDevicesn().equals(viewDeviceapplication.getSn())){
//                        deviceMetaData.setDevicesn(viewDeviceapplication.getDevicesn());
                        devicepostList.add(deviceMetaData);
                    }
                }
            }

            JSONObject postdata=new JSONObject();
            postdata.put("msgid", DateTime.now().toString("yyyyMMddHHmmss")+RandomStringUtils.randomNumeric(10));
            postdata.put("data",devicepostList);
            httpPoster.postJson(url,postdata.toString());
            appLogger.getLogger().info(String.format("dataplatform task push device data url : %s  data : %s",url,postdata.toString()));
        }

        appLogger.getLogger().info("data platform task insert device data into mongodb");
        //将数据插入mongodb中
        mongoUtil.insertMongo(deviceMetaDataList,appcache.getDeviceLogList().getClone());

    }
}
