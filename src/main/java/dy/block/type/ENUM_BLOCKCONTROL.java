package dy.block.type;

import lombok.Getter;

public enum ENUM_BLOCKCONTROL {
    FILE_START(0),
    FILE_END(1),
    FILE_TRANS(2),
    FILE_RESPONSE(3),
    HEARTBEAT(7),
    HEARTBEAT_RESPONSE(8),
    POSTDATA(9),
    BLOCK_START_RESPONSE(10),
    ASK_DATA(11),
    BLOCK_END_RESPONSE(74),
    BLOCK_STATUS_RESPONSE(75);

    @Getter
    private int code;

    private ENUM_BLOCKCONTROL(int code){
        this.code = code;
    }

    public static ENUM_BLOCKCONTROL setEnumBlockControl(int value){
        ENUM_BLOCKCONTROL enum_blockcontrol = null;
        switch (value){
            case 0 : enum_blockcontrol =  FILE_START;break;
            case 1 : enum_blockcontrol = FILE_END;break;
            case 2 : enum_blockcontrol = FILE_TRANS;break;
            case 3 : enum_blockcontrol = FILE_RESPONSE;break;
            case 7 : enum_blockcontrol = HEARTBEAT;break;
            case 8 : enum_blockcontrol = HEARTBEAT_RESPONSE;break;
            case 9 : enum_blockcontrol = POSTDATA;break;
            case 10 : enum_blockcontrol = BLOCK_START_RESPONSE;break;
            case 11 : enum_blockcontrol = ASK_DATA;break;
            case 74 : enum_blockcontrol = BLOCK_END_RESPONSE;break;
            case 75 : enum_blockcontrol = BLOCK_STATUS_RESPONSE;break;
        }
        return enum_blockcontrol;
    }
}
