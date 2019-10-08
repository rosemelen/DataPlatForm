package dy.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConfigBlockData {
    private String sn;
    private int blockNum;
    private byte[] data;
}
