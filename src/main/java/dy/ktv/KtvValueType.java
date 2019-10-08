package dy.ktv;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class KtvValueType {
    @JSONField(serialize = false)
    private int isArr;
    @JSONField(serialize = false)
    private int num;
    @JSONField(serialize = false)
    private int alarm;
    @JSONField(serialize = false)
    private int type;
    @JSONField(name="datastatus")
    private String alarms;
    @JSONField(name="datatype")
    private String types;

    public KtvValueType(int a) {
        int b0 = a % 4;
        int b7 = a / 128;
        a = a - b0;
        if (b7 == 0) {
            int b4 = a / 16;
            int b2 = a % 16;
            isArr = 0;
            num = b4;
            alarm = judgeAlarm(b2/4);
            this.type = judgeType(b0);
        } else {
            a = a - 128;
            int b4 = a / 16;
            int b2 = a % 16;
            if (b4 != 0) {
                isArr = 1;
                num = b4;
                alarm = judgeAlarm(b2/4);
                this.type = judgeType(b0);
            }

        }

    }

    private int judgeType(int b0) {
        return b0;
    }

    private int judgeAlarm(int b2) {
        switch (b2) {
            case 0:
                //正常
                return 1;
            case 1:
                //上限
                return 3;
            case 2:
                //下限
                return 2;
        }
        //设备异常
        return 4;
    }
}
