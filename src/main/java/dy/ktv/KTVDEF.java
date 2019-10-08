package dy.ktv;

public class KTVDEF {
    public enum  KTVDATATYPE {
        KBYTE(0),
        KINT(1),
        KFLOAT(2),
        KSTRING(3);
        private int code;

        private KTVDATATYPE(int code){
            this.code = code;
        }

        public int value(){
            return this.code;
        }
    }
    public enum KTVBUSINESSTYPE{

        DEVICESAMPLE(4),
        DEVICEREPORT(5),
        SETDEVICETIME(6),
        APPCONFIG(8),
        DEVICEUPPER(9),
        DEVICELOWER(10),
        DEVICECONFIGRESPONSE(12),
        DEVICEIMSI(13),
        APPCONFIGSERIALNUMBER(14),
        APPRESPONSE(15),
        IPCONFRESPONSE(16),
        TIME(20),
        WATERLINE(38),
        VOLTAGE(39),
        PRESSURE(60);

        private int code;

        private KTVBUSINESSTYPE(int code){
            this.code = code;
        }

        public int value(){
            return this.code;
        }
    }
}
