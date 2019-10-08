package dy.ktv;

import lombok.Data;

@Data
public class CommunicationKtv {
    private byte head;
    private short dataLen;
    private String sn;
    private byte[] metaData;
    private byte checkSum;
    private byte tail;
}
