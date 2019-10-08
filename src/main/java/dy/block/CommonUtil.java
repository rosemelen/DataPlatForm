package dy.block;

import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;

import java.security.MessageDigest;

public class CommonUtil {
    /**
     @函数描述:合并数组
     **/
    public static byte[] mergeByteArray(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    /**
     @函数描述:md5哈希计算
     **/
    public static String getMD5(String message) {
        String md5str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] input = message.getBytes();
            byte[] buff = md.digest(input);
            md5str = bytesToHex(buff);
        } catch (Exception e) {
            //AppLogger.logError("无法计算MD5值" + message, e);
            e.printStackTrace();
        }
        return md5str;
    }

    /**
     @函数描述:byte数组转string
     **/
    public static String bytesToHex(byte[] bytes) {
        return BytesUtils.bytesToString(bytes).toUpperCase();
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     @函数描述:16进制转byte数组
     **/
    public static byte[] hexString2Bytes(String hex) {

        if ((hex == null) || (hex.equals(""))) {
            return null;
        } else if (hex.length() % 2 != 0) {
            return null;
        } else {
            hex = hex.toUpperCase();
            int len = hex.length() / 2;
            byte[] b = new byte[len];
            char[] hc = hex.toCharArray();
            for (int i = 0; i < len; i++) {
                int p = 2 * i;
                b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p + 1]));
            }
            return b;
        }

    }

    /**
     @函数描述:byte数组转string
     **/
    public static String byte2HexStr(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
            // if (n<b.length-1) hs=hs+":";
        }
        return hs.toUpperCase();
    }

    /**
     @函数描述:int转byte[]
     **/
    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }


    /**
     @函数描述:byte[]转int
     **/
    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    /**
     @函数描述:short转byte数组
     **/
    public static byte[] shortToByte(short value){
        byte[] arr = new byte[2];
        arr[0] = (byte) (value & 0xff);
        arr[1] = (byte) (value>>8 & 0xff);
        return arr;
    }

    /**
     @函数描述:short反转
     **/
    public static byte[] reverseShortToByte(short value){
        byte[] arr = new byte[2];
        arr[1] = (byte) (value & 0xff);
        arr[0] = (byte) (value>>8 & 0xff);
        return arr;
    }

    /**
     @函数描述:byte转short
     **/
    public static short bytesToShort(byte b1,byte b2) {
        return (short) ((b1 & 0xFF)| (b2 << 8));
    }


    /**
     @函数描述:base64编码
     **/
    public static String encode64(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }

    /**
     @函数描述:base64解码
     **/
    public static byte[] decodeBase64(String str){
        byte[] b = null;
        if (str != null) {
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                b = decoder.decodeBuffer(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    /**
    @函数描述:字符串转asc2
    **/
    public static String StringToA(String content){
        String result = "";
        int max = content.length();
        for (int i=0; i<max; i++){
            char c = content.charAt(i);
            int b = (int)c;
            result = result + b;
        }
        return result;
    }

}
