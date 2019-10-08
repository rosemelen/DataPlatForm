package dy.ktv;

import com.alibaba.fastjson.annotation.JSONField;
import dy.block.CommonUtil;
import fr.devnied.bitlib.BytesUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2017/7/25 0025.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KTV {
    private int k;
    @JSONField(name="datainfo")
    private KtvValueType type;
    private String t;
    @JSONField(serialize = false,name = "byteV")
    private byte[] v;
    @JSONField(name = "v")
    private String realV;

    public void setTypeValue() throws UnsupportedEncodingException {
        if(this.getType() == null || v == null){
            return;
        }

        if(type.getType() == 0){
            type.setTypes("byte");
            this.realV = String.valueOf(BytesUtils.byteArrayToInt(v));
        }
        if(type.getType() == 1){
            type.setTypes("int");
            this.realV = String.valueOf(CommonUtil.byteArrayToInt(v));
        }
        if(type.getType() == 2){
            type.setTypes("float");
            this.realV = String.valueOf(Float.intBitsToFloat(Integer.valueOf(CommonUtil.byte2HexStr(this.v).trim(), 16)));
        }
        if(type.getType() == 3){
            type.setTypes("string");
            this.realV = new String(v,0,v.length-1,"ASCII");
        }

        if(type.getAlarm() == 1){
            type.setAlarms("data normal");
        }
        if(type.getAlarm() == 2){
            type.setAlarms("data lower limit alarm");
        }
        if(type.getAlarm() == 3){
            type.setAlarms("data upper limit alarm");
        }
        if(type.getAlarm() == 4){
            type.setAlarms("sensor anomaly");
        }
    }
}
