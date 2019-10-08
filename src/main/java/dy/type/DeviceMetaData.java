package dy.type;

import com.alibaba.fastjson.annotation.JSONField;
import dy.ktv.KTV;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceMetaData {
    private String devicesn;
    private String deviceid;
    @JSONField(serialize = false)
    private ProcessorType processorType;
    @JSONField(serialize = false)
    private byte[] metaBytes;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createdate;
    @JSONField(name = "metadata")
    private String hexData;
    @JSONField(name = "devicedata")
    private String base64Data;
    @JSONField(name="privatedata")
    private List<List<KTV>> ktvData;
}
