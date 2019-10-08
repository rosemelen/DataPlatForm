package dy.type;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLog {
    private String devicesn;
    @JSONField(name="loglevel")
    private int logLevel;
    @JSONField(name="log")
    private String logContent;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createdate;
}
