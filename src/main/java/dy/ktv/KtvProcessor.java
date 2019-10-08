package dy.ktv;

import dy.block.CommonUtil;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("ktvprocessor")
public class KtvProcessor {

    public List<byte[]> getKtvBytes(List<KTV> listKtv){
        List<byte[]> listKtvBytes = new ArrayList<>();
        for(KTV ktv:listKtv){
            if(ktv.getT().toLowerCase().equals("byte")){
                byte[] ktvByte = new byte[3];
                ktvByte[0] = (byte)ktv.getK();
                ktvByte[1] = 0x00;
                ktvByte[2] = (byte) Integer.parseInt(ktv.getRealV());
                listKtvBytes.add(ktvByte);
            }
            if(ktv.getT().toLowerCase().equals("int")){
                byte[] ktvInt = new byte[6];
                ktvInt[0] = (byte)ktv.getK();
                ktvInt[1] = 0x01;
                System.arraycopy(CommonUtil.intToByteArray(Integer.parseInt(ktv.getRealV())),0,ktvInt,2,4);
                listKtvBytes.add(ktvInt);
            }
            if(ktv.getT().toLowerCase().equals("float")){
                byte[] ktvFloat = new byte[6];
                ktvFloat[0] = (byte)ktv.getK();
                ktvFloat[1] = 0x02;
                System.arraycopy(CommonUtil.intToByteArray(Float.floatToIntBits(Float.parseFloat(ktv.getRealV()))),0,ktvFloat,2,4);
                listKtvBytes.add(ktvFloat);
            }
            if(ktv.getT().toLowerCase().equals("string")){
                byte[] bytes = ktv.getRealV().getBytes();
                byte[] ktvString = new byte[3+bytes.length];
                ktvString[0] = (byte)ktv.getK();
                ktvString[1] = 0x03;
                System.arraycopy(bytes,0,ktvString,2,bytes.length);
                ktvString[ktvString.length-1] = 0x00;
                listKtvBytes.add(ktvString);
            }
        }
        return listKtvBytes;
    }

    public List<byte[]> dealKtvFrame(byte[] frame) {
        List<byte[]> list = new ArrayList<>();
        int point = 0;
        if (frame[0] == -86 && frame[frame.length - 1] == -85) {
            do {
                if (frame[point] == -86 ) {
                    int frameLength = new BigInteger(CommonUtil.byte2HexStr(Arrays.copyOfRange(frame, point+1, point+3)), 16).intValue()+17;
                    list.add(Arrays.copyOfRange(frame, point+15, point+frameLength-2));
                    point += frameLength;
                }else { point++; }
            } while (point < frame.length - 1);
        }
        return list;
    }

    public byte[] getKtvFrame(List<byte[]> listKtv){
        short dataLen = 0;
        for(byte[] bytes:listKtv){
            dataLen += bytes.length;
        }
        byte[] tmpBytes = new byte[dataLen];
        int pos = 0;
        for(byte[] bytes:listKtv){
            System.arraycopy(bytes,0,tmpBytes,pos,bytes.length);
            pos += bytes.length;
        }
        return tmpBytes;
    }
}
