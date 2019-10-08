package dy.block.type;

import lombok.Getter;

public enum  ENUM_BLOCKSIGN {
    BLOCK_START(0),
    BLOCK_TRANS(1),
    BLOCK_END(2),
    BLOCK_ERROR(3);

    @Getter
    private int code;

    private ENUM_BLOCKSIGN(int code){
        this.code = code;
    }

    public static ENUM_BLOCKSIGN setEnumBlockSign(int value){
        ENUM_BLOCKSIGN enum_blocksign = null;
        switch (value){
            case 0 : enum_blocksign =  BLOCK_START;break;
            case 1 : enum_blocksign = BLOCK_TRANS;break;
            case 2 : enum_blocksign = BLOCK_END;break;
            case 3 : enum_blocksign = BLOCK_ERROR;break;
        }
        return enum_blocksign;
    }

}
