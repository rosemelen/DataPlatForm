package dy.https;

import lombok.Data;

@Data
public class CoapPostMetadata {
    private String notifyType;
    private String deviceId;
    private String gatewayId;
    private CoapServiceData service;
}
