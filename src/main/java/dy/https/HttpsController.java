package dy.https;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dy.block.CommonUtil;
import dy.cache.Appcache;
import dy.log.AppLogger;
import dy.type.DeviceLog;
import dy.type.DeviceMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "${httpsrevurl}")
public class HttpsController {
    @Autowired
    AppLogger appLogger;

    @Autowired
    Appcache appcache;

    @RequestMapping(value = "/deviceDataChanged",method = {RequestMethod.POST})
    public ResponseEntity<HttpStatus> getDataInfo(@RequestBody CoapPostMetadata coapPostMetadata){

        appLogger.getLogger().info(String.format("data platform received coap data %s",coapPostMetadata.toString()));

        if(coapPostMetadata.getService().getData().getMetadata() != null){
            DeviceMetaData deviceMetaData = new DeviceMetaData();
            deviceMetaData.setDeviceid(coapPostMetadata.getDeviceId());
            deviceMetaData.setBase64Data(coapPostMetadata.getService().getData().getMetadata());
            deviceMetaData.setMetaBytes(CommonUtil.decodeBase64(coapPostMetadata.getService().getData().getMetadata()));
            deviceMetaData.setCreatedate(new Date());
            deviceMetaData.setHexData(CommonUtil.byte2HexStr(deviceMetaData.getMetaBytes()));
            appcache.getDeviceCoapMetaDataList().addObj(deviceMetaData);
            DeviceLog deviceLog = new DeviceLog(deviceMetaData.getDevicesn(),0,String.format("device send coap data : %s",CommonUtil.byte2HexStr(deviceMetaData.getMetaBytes())),new Date());
            appcache.getDeviceLogList().addObj(deviceLog);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/commandRspData",method = {RequestMethod.POST})
    public ResponseEntity<HttpStatus> getDataInfo(@RequestBody Object body){
        appLogger.getLogger().info( String.format("commandRspData:  [%s]",JSONObject.toJSONString(body)));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/messageConfirm",method = {RequestMethod.POST})
    public ResponseEntity<HttpStatus> getmessageConfirm(@RequestBody Object body){
        appLogger.getLogger().info( String.format("messageConfirm :  [%s]",JSONObject.toJSONString(body)));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
