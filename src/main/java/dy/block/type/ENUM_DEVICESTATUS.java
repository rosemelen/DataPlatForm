package dy.block.type;

import lombok.Getter;

public enum  ENUM_DEVICESTATUS {
    DEVICE_REQUEST(0),
    SERVER_HAVEDATA(1),
    SERVER_NODATA(2),
    DEVICE_RESPONSE(3);

    @Getter
    private int code;

    private ENUM_DEVICESTATUS(int code){
        this.code = code;
    }

    public static ENUM_DEVICESTATUS setEnumDeviceStatus(int value){
        ENUM_DEVICESTATUS enum_devicestatus = null;
        switch (value){
            case 0 : enum_devicestatus =  DEVICE_REQUEST;break;
            case 1 : enum_devicestatus = SERVER_HAVEDATA;break;
            case 2 : enum_devicestatus = SERVER_NODATA;break;
            case 3 : enum_devicestatus = DEVICE_RESPONSE;break;
        }
        return enum_devicestatus;
    }
}
