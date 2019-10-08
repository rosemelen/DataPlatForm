package dy.ktv;

import dy.block.CommonUtil;
import dy.cache.Appcache;
import dy.dbserivce.IDeviceConfigService;
import dy.entity.DeviceConfig;
import dy.entity.ResponseBytes;
import dy.log.AppLogger;
import dy.type.DeviceIpMap;
import dy.type.DeviceLog;
import dy.type.DeviceMetaData;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Component("communicationktvprocessor")
public class CommunicationKtvProcessor {
    @Autowired
    private AppLogger appLogger;

    @Autowired
    private Appcache appcache;
    @Autowired
    KtvParser ktvParser;
    @Autowired
    KtvProcessor ktvProcessor;
    @Autowired
    IDeviceConfigService deviceConfigService;

    private static int RTU_TIME_REQUEST = 6;
    private static int KTV_RESPONSE_SUCCESS = 1;
    private static int KTV_RESPONSE_FAIL = 0;

    public CommunicationKtv getCommunicationKtvData(byte[] data){
        CommunicationKtv communicationKtv = new CommunicationKtv();
        communicationKtv.setHead(data[0]);
        communicationKtv.setTail(data[data.length-1]);
        byte[] snBytes = new byte[12];
        System.arraycopy(data,3,snBytes,0,12);
        communicationKtv.setSn(new String(snBytes).trim());
        communicationKtv.setDataLen((short)((data[1]<<8 & 0xff) | (data[2] & 0xff)));
//        communicationKtv.setMetaData(new byte[communicationKtv.getDataLen()]);
//        System.arraycopy(data, 15, communicationKtv.getMetaData(), 0, communicationKtv.getDataLen());
        communicationKtv.setMetaData(new byte[data.length]);
        System.arraycopy(data, 0, communicationKtv.getMetaData(), 0, data.length);
        communicationKtv.setCheckSum(data[data.length-2]);
        return communicationKtv;
    }

    public byte[] dealKtvkData(byte[] data, DeviceIpMap deviceIpMap){
        CommunicationKtv communicationKtv = getCommunicationKtvData(data);
        //将设备地址端口信息写入全局缓存
        appcache.getDeviceIpMap().put(communicationKtv.getSn(),deviceIpMap);
        DeviceLog deviceLog = new DeviceLog(communicationKtv.getSn(),0,String.format("device report rtu data : %s",CommonUtil.byte2HexStr(communicationKtv.getMetaData())),new Date());
        appcache.getDeviceLogList().addObj(deviceLog);
        DeviceMetaData deviceMetaData = new DeviceMetaData();
        deviceMetaData.setDevicesn(communicationKtv.getSn());
        deviceMetaData.setCreatedate(new Date());
        deviceMetaData.setMetaBytes(communicationKtv.getMetaData());
        deviceMetaData.setHexData(CommonUtil.byte2HexStr(communicationKtv.getMetaData()));
        deviceMetaData.setProcessorType(deviceIpMap.getProcessorType());
        deviceIpMap.setSn(communicationKtv.getSn());


        byte[] responseBytes = null;

        ResponseBytes ktvResponseBytes = dealResponseBytes(data,communicationKtv,deviceMetaData);
        if(ktvResponseBytes != null){
            responseBytes = ktvResponseBytes.getResponseBytes();
            if(ktvResponseBytes.isReturnData()){
                return responseBytes;
            }
        }

        if(responseBytes != null){
            deviceLog = new DeviceLog(communicationKtv.getSn(),0,String.format("dataplatform response device content : %s", CommonUtil.byte2HexStr(responseBytes)),new Date());
            appcache.getDeviceLogList().addObj(deviceLog);
        }
        appcache.getDeviceMetaDataList().addObj(deviceMetaData);
        DeviceLog devicetmpLog = new DeviceLog(communicationKtv.getSn(),0,String.format("rtu received device data and response[%s]",CommonUtil.byte2HexStr(responseBytes)),new Date());
        return responseBytes;
    }

    public ResponseBytes dealResponseBytes(byte[] data, CommunicationKtv communicationKtv, DeviceMetaData deviceMetaData){
        byte[] responseBytes = null;
        ResponseBytes ktvResponseBytes ;
        //请求授时
        if(data[15] == (byte) KTVDEF.KTVBUSINESSTYPE.SETDEVICETIME.value()){
            responseBytes = getSetTimeFrame(deviceMetaData.getDevicesn());
            ktvResponseBytes = new ResponseBytes(true,responseBytes);
            return ktvResponseBytes;
            //配置应答
        }else if(data[15] == (byte)KTVDEF.KTVBUSINESSTYPE.APPCONFIGSERIALNUMBER.value()){
            List<byte[]> ktvFrameList =ktvProcessor.dealKtvFrame(deviceMetaData.getMetaBytes());
            List<KTV> ktvList = ktvParser.dataAnalysis(ktvFrameList.get(0));
            String resConfigid = new BigInteger(CommonUtil.byte2HexStr(ktvList.get(0).getV()), 16).toString(10);

            //更改配置状态
            if(ktvList.get(1).getK() == (byte)KTVDEF.KTVBUSINESSTYPE.DEVICECONFIGRESPONSE.value()){
                byte[] bytes = ktvList.get(1).getV();
                if(bytes[bytes.length-1] == KTV_RESPONSE_SUCCESS){
//                    appcache.getDeviceConfigList().addObj(new DeviceConfig(deviceMetaData.getDevicesn(),resConfigid,"设备配置已成功"));
                    appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(deviceMetaData.getDevicesn(),resConfigid,"设备配置已成功"));

                }else if(bytes[bytes.length-1] == KTV_RESPONSE_FAIL){
//                    appcache.getDeviceConfigList().addObj(new DeviceConfig(deviceMetaData.getDevicesn(),resConfigid,"设备配置失败"));
                    appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(deviceMetaData.getDevicesn(),resConfigid,"设备配置失败"));
                }else {
                    appLogger.getLogger().info(String.format("device response config result parameter false [%s] :",CommonUtil.byte2HexStr(bytes)));
                }
                responseBytes = getKtvResponseFrame(communicationKtv);
                ktvResponseBytes = new ResponseBytes(false,responseBytes);
                return ktvResponseBytes;
            }else {
                responseBytes = getKtvResponseFrame(communicationKtv);
            }
        }else{
            responseBytes = getKtvResponseFrame(communicationKtv);
        }
        ktvResponseBytes = new ResponseBytes(false,responseBytes);
        return ktvResponseBytes;
    }

    public byte[] getSetTimeFrame(String nbsn){
        short frameLen = 23;
        byte[] frame = new byte[frameLen];
        //头尾帧
        frame[0] = (byte)0xaa;
        frame[frameLen-1] = (byte)0xab;
        //填充长度
        short dateLen = 6;
        System.arraycopy(ktvShortToByte(dateLen), 0, frame,1,2);
        //构造id
        System.arraycopy(nbsn.getBytes(),0,frame,3,12);
        //构造ktv
        frame[15] = (byte) KTVDEF.KTVBUSINESSTYPE.SETDEVICETIME.value();
        frame[16] = (byte) KTVDEF.KTVDATATYPE.KINT.value();
        System.arraycopy(getTimeBytes(), 0, frame,17,4);
        //构造校验字
        frame[frameLen-2] = getVerifyByte(frame);
        return frame;
    }

    public static byte[] getTimeBytes(){
        DateTime time = DateTime.now();
        String timeStr = time.toString("yyMMddHHmm");
        int timeInt = Integer.parseInt(timeStr);
        return BytesUtils.toByteArray(timeInt);
    }

    public byte[] getKtvResponseFrame(CommunicationKtv ktvData){
        short frameLen = 20;
        byte[] frame = new byte[frameLen];
        frame[0] = (byte)0xaa;
        frame[frameLen-1] = (byte)0xab;
        short dateLen = 3;
        System.arraycopy(ktvShortToByte(dateLen), 0, frame,1,2);
        System.arraycopy(ktvData.getSn().getBytes(),0,frame,3,12);
        frame[15] = (byte) KTVDEF.KTVBUSINESSTYPE.APPRESPONSE.value();
        frame[16] = (byte) KTVDEF.KTVDATATYPE.KBYTE.value();
        frame[17] = 0;
        frame[frameLen-2] = getVerifyByte(frame);
        return frame;
    }

    public static byte getVerifyByte(byte[] bytes){
        byte check = bytes[1];
        for(int i=2; i<bytes.length-2; i++){
            check ^= bytes[i];
        }
        return check;
    }

    public static byte[] ktvShortToByte(short value){
        byte[] arr = new byte[2];
        arr[1] = (byte) (value & 0xff);
        arr[0] = (byte) (value>>8 & 0xff);
        return arr;
    }

    public static byte[] getKtvConfigFrame(String sn, List<byte[]> ktvArray){
        short dataLen = 0;
        for(byte[] bytes:ktvArray){
            dataLen += bytes.length;
        }
        short frameLen = (short) (17+dataLen);
        byte[] frame = new byte[frameLen];
        frame[0] = (byte)0xaa;
        frame[frameLen-1] = (byte)0xab;
        byte[] bytesFrameLen = CommonUtil.shortToByte(dataLen);
        ArrayUtils.reverse(bytesFrameLen);
        System.arraycopy(bytesFrameLen, 0, frame,1,2);
        byte[] snBytes = new byte[12];
        System.arraycopy(sn.getBytes(),0,snBytes,0,sn.getBytes().length);
        System.arraycopy(snBytes,0,frame,3,12);

        int pos = 15;
        for(byte[] bytes:ktvArray){
            System.arraycopy(bytes,0,frame,pos,bytes.length);
            pos += bytes.length;
        }
        frame[frameLen-2] = getVerifyByte(frame);
        return frame;
    }


}
