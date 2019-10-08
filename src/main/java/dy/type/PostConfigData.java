package dy.type;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostConfigData {
    private String msgid;
    private String sn;
    private String configId;
    private String configtype;
    private String bussinesskey;
    @JSONField(name = "data")
    private DeviceConfigData deviceConfigData;
    @JSONField(serialize = false)
    private Date date;
}
