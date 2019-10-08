package dy.https;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class CoapServiceData {
    private String serviceId;
    private String serviceType;
    private PostMetadata data;
    private String eventTime;
}
