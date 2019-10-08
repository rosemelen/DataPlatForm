package dy.http;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.*;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.SignalDelivery;
import com.huawei.iotplatform.utils.JsonUtil;
import dy.log.AppLogger;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("coapUtil")
@Data
public class CoapUtil {

    @Value("${coap.appId}")
    private String appId;
    @Value("${coap.secret}")
    private String secret;
    @Value("${coap.platformIp}")
    private String platformIp;
    @Value("${coap.platformPort}")
    private String platformPort;
    @Value("${coap.serviceId}")
    private String serviceId;
    @Value("${coap.method}")
    private String method;

    @Autowired
    AppLogger appLogger;

    public String sendconfig(String deviceId, String configStr, boolean timeFlag) {
        try {
            SignalDelivery sd = new SignalDelivery();
            String accessToken = connCoap(sd);

            PostDeviceCommandInDTO pdcid = new PostDeviceCommandInDTO();
            if (!timeFlag) {
                pdcid.setExpireTime(0);
            }
            pdcid.setDeviceId(deviceId);
            AsynCommandDTO acdo = new AsynCommandDTO();
            ObjectNode paras = JsonUtil.convertObject2ObjectNode(configStr);
            acdo.setServiceId(serviceId);
            acdo.setMethod(method);
            acdo.setParas(paras);
            pdcid.setCommand(acdo);
            PostDeviceCommandOutDTO pdcod = sd.postDeviceCommand(pdcid, appId, accessToken);
            return pdcod.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            appLogger.getLogger().error(((NorthApiException) ex).getError_code() + "  " + ((NorthApiException) ex).getError_desc());
            return null;
        }
    }

    private String connCoap(SignalDelivery sd) throws Exception {
        NorthApiClient nac = new NorthApiClient();
        ClientInfo ci = new ClientInfo();
        ci.setAppId(appId);
        ci.setPlatformIp(platformIp);
        ci.setPlatformPort(platformPort);
        ci.setSecret(secret);
        nac.setClientInfo(ci);
        nac.initSSLConfig();
        Authentication authentication = new Authentication(nac);
        AuthOutDTO aod = authentication.getAuthToken();
        String accessToken = aod.getAccessToken();
        sd.setNorthApiClient(nac);
        return accessToken;
    }

}
