package dy.block;

import dy.block.type.ENUM_BLOCKCONTROL;
import dy.block.type.ENUM_BLOCKSIGN;
import dy.block.type.ENUM_DEVICESTATUS;
import dy.block.type.ENUM_SERVERESPONSE;
import dy.cache.Appcache;
import dy.entity.DeviceConfig;
import dy.entity.ResponseBytes;
import dy.log.AppLogger;
import dy.type.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("blockprotocolprocessor")
public class BlockProtocolProcessor {
    @Autowired
    private AppLogger appLogger;

    @Autowired
    private Appcache appcache;

    @Autowired
    BlockNumberGenerator blockNumberGenerator;

    private ConcurrentHashMap<String,ConcurrentHashMap<Integer,byte[]>> deviceBlockDataMap;

    public BlockProtocolProcessor() {
        deviceBlockDataMap = new ConcurrentHashMap<>();
    }

    /**
     @函数描述:获取块传输协议帧长
     **/
    public int getBlockFrameLen(byte b1,byte b2){
        return CommonUtil.bytesToShort(b1,b2);
    }

    /**
     @函数描述:获取块传输协议帧号
     **/
    public int getBlockFrameSeqNum(byte b1, byte b2){
        return CommonUtil.bytesToShort(b1,b2);
    }

    public BlockData getBlockData(byte[] data){
        BlockData blockData = new BlockData();
        byte[] nbArr = new byte[12];
        System.arraycopy(data,6,nbArr,0,12);
        String nbsn = new String(nbArr).trim();
        //sn需要在此解决
        //nbsn = nbsn.substring(2,nbsn.length());
        //获取块号
        int blockNum = (data[0]);
        //获取帧号
        int blockFrameNum = getBlockFrameSeqNum(data[1],data[2]);
        //获取帧长
        int blockFrameLen = getBlockFrameLen(data[3],data[4]);
        //获取块标志
        int blockSig = (int)(data[5] >> 5 & 0x03);
        //获取可靠传输标志
        int reliableTrans = (int)(data[5] >> 4 & 0x01);
        //获取
        int blockCtrl = (int)(data[5] & 0x0f);
        blockData.setDataBytes(new byte[blockFrameLen]);
        System.arraycopy(data,18,blockData.getDataBytes(),0,blockFrameLen);
        blockData.setSn(nbsn);
        blockData.setBlockNum(blockNum);
        blockData.setFrameNum(blockFrameNum);
        blockData.setFrameLen(blockFrameLen);
        blockData.setBlocksign(ENUM_BLOCKSIGN.setEnumBlockSign(blockSig));
        blockData.setBlockcontrol(ENUM_BLOCKCONTROL.setEnumBlockControl(blockCtrl));
        return blockData;
    }

    private byte[] getResponseFrame(int blockNum, int frameNum, byte control, String sn, byte[] data){
        byte[] bytes = new byte[18 + data.length];
        bytes[0] = (byte) (blockNum & 0xff);
        System.arraycopy(CommonUtil.shortToByte((short)frameNum),0,bytes,1,2);
        System.arraycopy(CommonUtil.shortToByte((short)data.length),0,bytes,3,2);
        bytes[5] = control;
        System.arraycopy(sn.getBytes(),0,bytes,6,sn.getBytes().length);
        System.arraycopy(data,0,bytes,18,data.length);
        return bytes;
    }

    public byte[] dealBlockData(byte[] data, DeviceIpMap deviceIpMap){
        byte[] responseBytes = null;
        Date date = new Date();
        BlockData blockData = getBlockData(data);
        appLogger.getLogger().info("devicesn:" + blockData.getSn() + " device data:" + CommonUtil.byte2HexStr(data));
        deviceIpMap.setSn(blockData.getSn());
        //将设备地址端口信息写入全局缓存
        appcache.getDeviceIpMap().put(blockData.getSn(),deviceIpMap);
        DeviceLog deviceLog = new DeviceLog(blockData.getSn(),0,String.format("device report data : %s",CommonUtil.byte2HexStr(blockData.getDataBytes())),new Date());
        appcache.getDeviceLogList().addObj(deviceLog);
        //设备主动请求处理
        if(blockData.getBlockcontrol() == ENUM_BLOCKCONTROL.ASK_DATA){
            //获取设备状态码
            ENUM_DEVICESTATUS enum_devicestatus = ENUM_DEVICESTATUS.setEnumDeviceStatus((int)blockData.getDataBytes()[0]);
            //设备主动请求数据
            if(enum_devicestatus == ENUM_DEVICESTATUS.DEVICE_REQUEST){
                deviceLog = new DeviceLog(blockData.getSn(),0,String.format("device query config data : %s",CommonUtil.byte2HexStr(blockData.getDataBytes())),new Date());
                appcache.getDeviceLogList().addObj(deviceLog);
                //去配置表查找有无该设备数据
                if(appcache.getDeviceConfigMap().containsKey(blockData.getSn())){
                    //去发送缓存找
                    if(appcache.getServerSendedConfigDataMap().containsKey(blockData.getSn())){
                        responseBytes = appcache.getServerSendedConfigDataMap().get(blockData.getSn()).getData();
                    }else{
                        //获取设备承载能力
                        short requestLen = CommonUtil.bytesToShort(blockData.getDataBytes()[2],blockData.getDataBytes()[1]);
                        ConcurrentHashMap<String, PostConfigData> deviceConfigDataMap = appcache.getDeviceConfigMap().get(blockData.getSn());
                        int tmpConfigLen = 0;
                        List<PostConfigData> listSendConfigData = new ArrayList<>();
                        List<Map.Entry<String, PostConfigData>> sort = deviceConfigDataMap.entrySet().stream().sorted(Map.Entry.comparingByValue(new Comparator<PostConfigData>() {
                            @Override
                            public int compare(PostConfigData o1, PostConfigData o2) {
                                return o1.getDate().compareTo(o2.getDate());
                            }
                        })).collect(Collectors.toList());
                        for(Map.Entry<String,PostConfigData> entry :sort){
                            if(tmpConfigLen + entry.getValue().getDeviceConfigData().getConfighexdata().length < requestLen){
                                listSendConfigData.add(entry.getValue());
                                tmpConfigLen += entry.getValue().getDeviceConfigData().getConfighexdata().length;
                            }
                            else{
                                break;}
                        }
                        if(tmpConfigLen > 0){
                            byte[] byteSendConfig = new byte[tmpConfigLen + 1];
                            byteSendConfig[0] = (byte)ENUM_DEVICESTATUS.SERVER_HAVEDATA.getCode();
                            int tmpLen = 1;
                            ConcurrentHashMap<String,String> deviceConfigStatusConcurrentHashMap = new ConcurrentHashMap<>();
                            for(PostConfigData deviceConfigData : listSendConfigData){
                                System.arraycopy(deviceConfigData.getDeviceConfigData().getConfighexdata(),0,byteSendConfig,tmpLen,deviceConfigData.getDeviceConfigData().getConfighexdata().length);
                                tmpLen += deviceConfigData.getDeviceConfigData().getConfighexdata().length;
                                deviceConfigDataMap.remove(deviceConfigData.getConfigId());
                                deviceConfigStatusConcurrentHashMap.put(deviceConfigData.getConfigId(),"设备配置已发送");
                                appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(deviceConfigData.getSn(), deviceConfigData.getConfigId(),"设备配置已发送"));
                            }

                            if( appcache.getDeviceConfigStatusMap().containsKey(blockData.getSn())){
                                appcache.getDeviceConfigStatusMap().get(blockData.getSn()).putAll(deviceConfigStatusConcurrentHashMap);
                            }else {
                                appcache.getDeviceConfigStatusMap().put(blockData.getSn(),deviceConfigStatusConcurrentHashMap);
                            }

                            int sendBlockNum = blockNumberGenerator.getBlockNum();
                            responseBytes = getResponseFrame(sendBlockNum, 0,(byte)ENUM_BLOCKCONTROL.BLOCK_STATUS_RESPONSE.getCode(),blockData.getSn(),byteSendConfig);
                            appcache.getServerSendedConfigDataMap().put(blockData.getSn(),new DeviceConfigBlockData(blockData.getSn(),sendBlockNum,responseBytes));
                        }
                        listSendConfigData.clear();
                    }
                }else{
                    //如果没有查找到该设备配置
                    byte[] byteSendConfig = new byte[1];
                    byteSendConfig[0] = (byte)ENUM_DEVICESTATUS.SERVER_NODATA.getCode();
                    int sendBlockNum = blockNumberGenerator.getBlockNum();
                    responseBytes = getResponseFrame(sendBlockNum, 0,(byte)ENUM_BLOCKCONTROL.BLOCK_STATUS_RESPONSE.getCode(),blockData.getSn(),byteSendConfig);
                    DeviceLog devicetmpLog = new DeviceLog(blockData.getSn(),0,String.format("dataplat not find device config"),new Date());
                    appcache.getDeviceLogList().addObj(devicetmpLog);
                }
            }
            //设备请求数据应答
            if(enum_devicestatus == ENUM_DEVICESTATUS.DEVICE_RESPONSE){
                deviceLog = new DeviceLog(blockData.getSn(),0,String.format("device response config : %s",CommonUtil.byte2HexStr(blockData.getDataBytes())),new Date());
                appcache.getDeviceLogList().addObj(deviceLog);
                int reponseBlockNum = blockData.getDataBytes()[0];

                if(appcache.getDeviceConfigStatusMap().containsKey(blockData.getSn())){
                    ConcurrentHashMap<String,String> deviceConfigStatusConcurrentHashMap = appcache.getDeviceConfigStatusMap().get(blockData.getSn());
                    Set<String> keySet = deviceConfigStatusConcurrentHashMap.keySet();
                    Iterator<String> keyIterator = keySet.iterator();
                    while (keyIterator.hasNext()){
                        appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(blockData.getSn(),keyIterator.next(),"设备已接收配置成功应答"));
                        keyIterator.remove();
                    }
                }

                if(appcache.getServerSendedConfigDataMap().containsKey(blockData.getSn())){
                    appcache.getServerSendedConfigDataMap().remove(blockData.getSn());
                    if(appcache.getDeviceConfigMap().get(blockData.getSn()).size() == 0){
                        appLogger.getLogger().info("remove config cache of device : " + blockData.getSn());
                        appcache.getDeviceConfigMap().remove(blockData.getSn());
                    }
                }

                appLogger.getLogger().info("BINGO!!! receive ACK data from device:" + blockData.getSn());
                return null;
            }
        }

        //设备推送数据
        ResponseBytes postDataBytes = dealPostData(blockData,deviceIpMap);
        if(postDataBytes != null){
            if(postDataBytes.isReturnData()){
                return postDataBytes.getResponseBytes();
            }else {
                responseBytes = postDataBytes.getResponseBytes();
            }
        }

        if(blockData.getBlockcontrol() == ENUM_BLOCKCONTROL.HEARTBEAT){
            //心跳应答，数据内容填入时间
            byte[] byteSendConfig = new byte[1];
            byteSendConfig[0] = (byte)ENUM_DEVICESTATUS.SERVER_NODATA.getCode();
            int sendBlockNum = blockData.getBlockNum();
            if(deviceIpMap.getProcessorType().getType() == 1){
                responseBytes = getResponseFrame(sendBlockNum, 0,(byte)ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(),blockData.getSn(),byteSendConfig);
            }else{
                //旧块应答
                responseBytes = getResponseFrame(sendBlockNum, 0,(byte)ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(),blockData.getSn(),byteSendConfig);
            }
        }
        if(responseBytes == null){
            //块单帧传输
            if(blockData.getFrameNum() == 0 && blockData.getBlocksign().getCode() == 2){
                DeviceMetaData deviceMetaData = new DeviceMetaData();
                deviceMetaData.setDevicesn(blockData.getSn());
                deviceMetaData.setCreatedate(date);
                deviceMetaData.setMetaBytes(blockData.getDataBytes());
                deviceMetaData.setProcessorType(deviceIpMap.getProcessorType());
                appcache.getDeviceMetaDataList().addObj(deviceMetaData);
                //发送应答帧
                byte[] byteServerResponse = new byte[1];
                byteServerResponse[0] = (byte) ENUM_SERVERESPONSE.SUCESSRECEIVE.getCode();
                int sendBlockNum = blockData.getBlockNum();
                if(deviceIpMap.getProcessorType().getType() == 1){
                    responseBytes = getResponseFrame(sendBlockNum, 0,(byte)ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(),blockData.getSn(),byteServerResponse);
                }else{
                    //旧块应答
                    responseBytes = getResponseFrame(blockData.getBlockNum(), 0,(byte)ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(),blockData.getSn(),byteServerResponse);
                }
            }
        }
        if(responseBytes != null){
            deviceLog = new DeviceLog(blockData.getSn(),0,String.format("dataplatform response device content : %s", CommonUtil.byte2HexStr(responseBytes)),new Date());
            appcache.getDeviceLogList().addObj(deviceLog);
        }
        return responseBytes;
    }

    public ResponseBytes dealPostData(BlockData blockData, DeviceIpMap deviceIpMap) {
        Date date = new Date();
        DeviceLog deviceLog = new DeviceLog();
        ResponseBytes postResponseBytes;
        byte[] responseBytes = null;
        //设备推送数据
        if (blockData.getBlockcontrol() == ENUM_BLOCKCONTROL.POSTDATA) {
            //单帧传输
            if (blockData.getBlocksign() == ENUM_BLOCKSIGN.BLOCK_END && blockData.getFrameNum() == 0) {
                deviceLog = new DeviceLog(blockData.getSn(), 0, String.format("device report single frame data : %s", CommonUtil.byte2HexStr(blockData.getDataBytes())), new Date());
                appcache.getDeviceLogList().addObj(deviceLog);
                DeviceMetaData deviceMetaData = new DeviceMetaData();
                deviceMetaData.setDevicesn(blockData.getSn());
                deviceMetaData.setCreatedate(date);
                deviceMetaData.setMetaBytes(blockData.getDataBytes());
                deviceMetaData.setProcessorType(deviceIpMap.getProcessorType());
                deviceMetaData.setHexData(CommonUtil.byte2HexStr(blockData.getDataBytes()));
                appcache.getDeviceMetaDataList().addObj(deviceMetaData);
                //发送应答帧
                byte[] byteServerResponse = new byte[1];
                byteServerResponse[0] = (byte) ENUM_SERVERESPONSE.SUCESSRECEIVE.getCode();
                int sendBlockNum = blockData.getBlockNum();
                if (deviceIpMap.getProcessorType().getType() == 1) {
                    responseBytes = getResponseFrame(sendBlockNum, 0, (byte) ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(), blockData.getSn(), byteServerResponse);
                } else {
                    //旧块应答
                    responseBytes = getResponseFrame(blockData.getBlockNum(), 0, (byte) ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(), blockData.getSn(), byteServerResponse);
                }
            }
            //首帧传输
            if (blockData.getBlocksign() == ENUM_BLOCKSIGN.BLOCK_START && blockData.getFrameNum() == 0) {
                deviceLog = new DeviceLog(blockData.getSn(), 0, String.format("device report multi first frame num : %s", blockData.getFrameNum()), new Date());
                appcache.getDeviceLogList().addObj(deviceLog);
                String blockSign = blockData.getSn() + "-" + blockData.getBlockNum();
                ConcurrentHashMap<Integer, byte[]> blockFrame = appcache.getDeviceBlockDataMap().get(blockSign);
                if (blockFrame == null) {
                    blockFrame = new ConcurrentHashMap<Integer, byte[]>();
                    blockFrame.put(blockData.getFrameNum(), blockData.getDataBytes());
                    appcache.getDeviceBlockDataMap().put(blockSign, blockFrame);
                } else {
                    blockFrame.clear();
                    blockFrame.put(blockData.getFrameNum(), blockData.getDataBytes());
                }
                //设备应答帧
                byte[] byteServerResponse = new byte[1];
                byteServerResponse[0] = (byte) ENUM_SERVERESPONSE.SUCESSRECEIVE.getCode();
                int sendBlockNum = blockData.getBlockNum();
                if (deviceIpMap.getProcessorType().getType() == 1) {
                    responseBytes = getResponseFrame(sendBlockNum, 0, (byte) ENUM_BLOCKCONTROL.BLOCK_START_RESPONSE.getCode(), blockData.getSn(), byteServerResponse);
                } else {
                    //旧块应答
                    responseBytes = getResponseFrame(blockData.getBlockNum(), 0, (byte) ENUM_BLOCKCONTROL.BLOCK_START_RESPONSE.getCode(), blockData.getSn(), byteServerResponse);
                }
            }
            //中间帧传输
            if (blockData.getBlocksign() == ENUM_BLOCKSIGN.BLOCK_TRANS && blockData.getFrameNum() > 0) {
                deviceLog = new DeviceLog(blockData.getSn(), 0, String.format("device report multi frame middle frame num : %s", blockData.getFrameNum()), new Date());
                appcache.getDeviceLogList().addObj(deviceLog);
                String blockSign = blockData.getSn() + "-" + blockData.getBlockNum();
                ConcurrentHashMap<Integer, byte[]> blockFrame = appcache.getDeviceBlockDataMap().get(blockSign);
                blockFrame.put(blockData.getFrameNum(), blockData.getDataBytes());

            }
            //尾帧传输
            if (blockData.getBlocksign() == ENUM_BLOCKSIGN.BLOCK_END && blockData.getFrameNum() > 0) {
                deviceLog = new DeviceLog(blockData.getSn(), 0, String.format("device report multi frame end frame num : %s", blockData.getFrameNum()), new Date());
                appcache.getDeviceLogList().addObj(deviceLog);
                String blockSign = blockData.getSn() + "-" + blockData.getBlockNum();
                ConcurrentHashMap<Integer, byte[]> blockFrame = appcache.getDeviceBlockDataMap().get(blockSign);
                if (blockFrame == null) {
                    //没有找到，单独传输尾帧，有可能是错误
                    deviceLog = new DeviceLog(blockData.getSn(), 0, String.format("dataplatform received multi end frame and can not find previous frame"), new Date());
                    appcache.getDeviceLogList().addObj(deviceLog);
                    byte[] byteServerResponse = new byte[1];
                    byteServerResponse[0] = (byte) ENUM_SERVERESPONSE.FAILDATA.getCode();
                    int sendBlockNum = blockData.getBlockNum();
                    if (deviceIpMap.getProcessorType().getType() == 1) {
                        responseBytes = getResponseFrame(sendBlockNum, 0, (byte) ENUM_BLOCKCONTROL.BLOCK_STATUS_RESPONSE.getCode(), blockData.getSn(), byteServerResponse);
                    } else {
                        //旧块应答
                        responseBytes = getResponseFrame(blockData.getBlockNum(), 0, (byte) ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(), blockData.getSn(), byteServerResponse);
                    }
                    postResponseBytes = new ResponseBytes(true,responseBytes);
                    return postResponseBytes;
                }
                blockFrame.put(blockData.getFrameNum(), blockData.getDataBytes());
                //进行缺帧校验
                List<Integer> listLackFrame = new ArrayList<Integer>();
                for (int i = 0; i <= blockData.getFrameNum(); i++) {
                    if (!blockFrame.containsKey(i)) {
                        listLackFrame.add(i);
                    }
                }

                if (listLackFrame.size() > 0) {
                    //有缺帧出现
                    byte[] responseContent = new byte[listLackFrame.size() * 2 + 1];
                    responseContent[0] = (byte) ENUM_SERVERESPONSE.LESSFRAME.getCode();
                    for (int i = 0; i < listLackFrame.size(); i++) {
                        int num = listLackFrame.get(i);
                        short realNum = (short) num;
                        byte[] shortbytes = CommonUtil.reverseShortToByte(realNum);
                        System.arraycopy(shortbytes, 0, responseContent, 1 + i * 2, shortbytes.length);
                    }
                    int sendBlockNum = blockData.getBlockNum();
                    if (deviceIpMap.getProcessorType().getType() == 1) {
                        responseBytes = getResponseFrame(sendBlockNum, 0, (byte) ENUM_BLOCKCONTROL.BLOCK_START_RESPONSE.getCode(), blockData.getSn(), responseContent);
                    } else {
                        //旧块应答
                        responseBytes = getResponseFrame(blockData.getBlockNum(), 0, (byte) ENUM_BLOCKCONTROL.BLOCK_START_RESPONSE.getCode(), blockData.getSn(), responseContent);
                    }
                    deviceLog = new DeviceLog(blockData.getSn(), 0, String.format("dataplatform find device less frame : %s", StringUtils.join(listLackFrame.toArray(), ",")), new Date());
                    appcache.getDeviceLogList().addObj(deviceLog);
                    postResponseBytes = new ResponseBytes(true,responseBytes);
                    return postResponseBytes;
                } else {
                    //给设备发送接收完整应答帧
                    //设备应答帧
                    byte[] byteServerResponse = new byte[1];
                    byteServerResponse[0] = (byte) ENUM_SERVERESPONSE.SUCESSRECEIVE.getCode();
                    int sendBlockNum = blockData.getBlockNum();
                    if (deviceIpMap.getProcessorType().getType() == 1) {
                        responseBytes = getResponseFrame(sendBlockNum, 0, (byte) ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(), blockData.getSn(), byteServerResponse);
                    } else {
                        //旧块应答
                        responseBytes = getResponseFrame(blockData.getBlockNum(), 0, (byte) ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(), blockData.getSn(), byteServerResponse);
                    }
                    //把完整帧放入缓存
                    byte[] tmpBytes = blockFrame.get(0);
                    for (int i = 1; i <= blockData.getFrameNum(); i++) {
                        byte[] tmpData = blockFrame.get(i);
                        tmpBytes = CommonUtil.mergeByteArray(tmpBytes, tmpData);
                    }
                    DeviceMetaData deviceMetaData = new DeviceMetaData();
                    deviceMetaData.setDevicesn(blockData.getSn());
                    deviceMetaData.setCreatedate(date);
                    deviceMetaData.setMetaBytes(tmpBytes);
                    deviceMetaData.setProcessorType(deviceIpMap.getProcessorType());
                    deviceMetaData.setHexData(CommonUtil.byte2HexStr(blockData.getDataBytes()));
                    appcache.getDeviceMetaDataList().addObj(deviceMetaData);
                    appcache.getDeviceBlockDataMap().remove(blockSign);
                    DeviceLog devicetmpLog = new DeviceLog(blockData.getSn(), 0, String.format("dataplatorm received whole frame : %s", CommonUtil.byte2HexStr(deviceMetaData.getMetaBytes())), new Date());
                    appcache.getDeviceLogList().addObj(devicetmpLog);
                }
            }
            postResponseBytes = new ResponseBytes(false,responseBytes);
            return postResponseBytes;
        }
        return null;
    }

    //以下两个函数用于兼容旧版块协议的设备配置
    public List<PostConfigData> getOldConfigList(String sn) {
        List<PostConfigData> listSendConfigData = null;
        //配置设备
        ConcurrentHashMap<String, PostConfigData> deviceConfigDataMap = appcache.getDeviceConfigMap().get(sn);
        if(deviceConfigDataMap != null){
            if(deviceConfigDataMap.size()>0){
                listSendConfigData = new ArrayList<>();
                //如果有该设备的配置，则首先发送配置给设备
                for(Map.Entry<String,PostConfigData> entry : deviceConfigDataMap.entrySet()){
                    listSendConfigData.add(entry.getValue());
                }
                appcache.getDeviceConfigMap().remove(sn);
                deviceConfigDataMap.clear();
            }
        }
        return listSendConfigData;
    }

    //兼容旧版块协议的设备配置
    public byte[] getOldConfigData(PostConfigData deviceConfigData) {
        byte[] deviceConfigBytes =  null;
        if(deviceConfigData != null){
            byte[] byteSendConfig = new byte[deviceConfigData.getDeviceConfigData().getConfighexdata().length];
            System.arraycopy(deviceConfigData.getDeviceConfigData().getConfighexdata(),0,byteSendConfig,0,deviceConfigData.getDeviceConfigData().getConfighexdata().length);
            int sendBlockNum = blockNumberGenerator.getBlockNum();
            deviceConfigBytes = getResponseFrame(sendBlockNum, 0,(byte)ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(),deviceConfigData.getSn(),byteSendConfig);
        }
        return deviceConfigBytes;
    }

    public static byte[] getConfigFrame(String sn,byte[] configData){
        if(sn.length() ==10){
            sn = "ZR" + sn;
        }
        byte[] nbarr = sn.getBytes();
        short frameLen = (short)(6 + nbarr.length + configData.length);
        byte[] frame = new byte[frameLen];
        frame[0] = 0;
        frame[1] = 0;
        frame[2] = 0;
        byte[] len = CommonUtil.shortToByte((short)(configData.length));
        System.arraycopy(len,0,frame,3,2);
        frame[5] = 0x59;
        System.arraycopy(nbarr,0,frame,6,nbarr.length);
        System.arraycopy(configData,0,frame,6+nbarr.length,configData.length);
        return frame;
    }

    public byte[] dealOldBlockData(byte[] data, DeviceIpMap deviceIpMap) {
        byte[] responseBytes = null;
        Date date = new Date();
        BlockData blockData = getBlockData(data);
        appLogger.getLogger().info("devicesn:" + blockData.getSn() + " device data:" + CommonUtil.byte2HexStr(data));
        deviceIpMap.setSn(blockData.getSn());
        //将设备地址端口信息写
        // 入全局缓存
        appcache.getDeviceIpMap().put(blockData.getSn(),deviceIpMap);
        //配置设备
        ConcurrentHashMap<String, PostConfigData> deviceConfigDataMap = appcache.getDeviceConfigMap().get(blockData.getSn());
        if(deviceConfigDataMap.size()>0){
            int tmpConfigLen = 0;
            List<PostConfigData> listSendConfigData = new ArrayList<>();
            //如果有该设备的配置，则首先发送配置给设备
            for(Map.Entry<String,PostConfigData> entry : deviceConfigDataMap.entrySet()){
                listSendConfigData.add(entry.getValue());
                tmpConfigLen += entry.getValue().getDeviceConfigData().getConfighexdata().length;
            }
            if(tmpConfigLen > 0){
                int tmpLen = 0;
                byte[] byteSendConfig = new byte[tmpConfigLen];
                for(PostConfigData deviceConfigData : listSendConfigData){
                    System.arraycopy(deviceConfigData.getDeviceConfigData().getConfighexdata(),0,byteSendConfig,tmpLen,deviceConfigData.getDeviceConfigData().getConfighexdata().length);
                    tmpLen += deviceConfigData.getDeviceConfigData().getConfighexdata().length;
                    deviceConfigDataMap.remove(deviceConfigData.getConfigId());
                }
                int sendBlockNum = blockNumberGenerator.getBlockNum();
                responseBytes = getResponseFrame(sendBlockNum, 0,(byte)ENUM_BLOCKCONTROL.BLOCK_END_RESPONSE.getCode(),blockData.getSn(),byteSendConfig);
            }
            appcache.getDeviceConfigMap().remove(blockData.getSn());
            listSendConfigData.clear();
        }
        return responseBytes;
    }
}
