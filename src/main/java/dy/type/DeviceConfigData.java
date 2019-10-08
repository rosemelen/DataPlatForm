package dy.type;

import dy.ktv.KTV;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceConfigData {
    private String timeservice;
    private String configdata;
    private byte[] confighexdata;
    List<KTV> privatedata;
}

