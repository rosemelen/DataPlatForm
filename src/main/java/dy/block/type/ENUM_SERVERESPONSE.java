package dy.block.type;

import lombok.Getter;

public enum ENUM_SERVERESPONSE {
    SUCESSRECEIVE(0),
    FAILDATA(3),
    LESSFRAME(1);

    @Getter
    private int code;

    private ENUM_SERVERESPONSE(int code){
        this.code = code;
    }
}
