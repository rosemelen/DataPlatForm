package dy.ktv;


import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Component("ktvparser")
public class KtvParser {

    public List<KTV> dataAnalysis(byte[] bs) {
        List<KTV> keys = new ArrayList<KTV>();
        int index = 0;
        while (bs.length != index) {
            KTV ktv = new KTV();
            ktv.setK(bs[index] & 0xFF);
            index++;
            ktv.setType(new KtvValueType(bs[index] & 0xFF));
            if (ktv.getType().getIsArr() == 0) {
                int num=ktv.getType().getNum();
                for (int i = 0; i <= num; i++) {
                    KTV ktv1 = new KTV();
                    ktv1.setK(ktv.getK());
                    ktv1.setType(new KtvValueType(bs[index] & 0xFF));
                    index++;
                    int length = getVlength(ktv1);
                    if (length == 0) {
                        length = charlength(bs, index);
                    }
                    byte[] temp = new byte[length];
                    for (int j = 0; j < length; j++) {
                        temp[j] = bs[index + j];
                    }
                    ktv1.setV(temp);
                    index = index  + length;
                    keys.add(ktv1);
                }
            } else {
                ktv.setType(new KtvValueType(bs[index] & 0xFF));
                index++;
                List<byte[]> listV=new ArrayList<byte[]>();
                int length = getVlength(ktv);
                byte[] temp = new byte[length* (ktv.getType().getNum()+1)];
                if (length == 0) {
                    length = charlength(bs, index);
                }
                for (int j = 0; j < length* (ktv.getType().getNum()+1); j++) {
                    temp[j] = bs[index + j];
                }
                index = index + length* (ktv.getType().getNum()+1);
                ktv.setV(temp);
                keys.add(ktv);
            }
        }
        return keys;
    }

    private int getVlength(KTV ktv) {
        if (ktv.getType().getType() == 0) {
            return 1;
        } else if (ktv.getType().getType() == 1) {
            return 4;
        } else if (ktv.getType().getType() == 2) {
            return 4;
        } else {
            return 0;
        }
    }

    private int charlength(byte[] bytes, int index) {
        int length = 0;
        for (int i = index ; i < bytes.length; i++) {
            if (0 == (bytes[i] & 0xFF)) {
                return length + 1;
            }
            length++;
        }
        return length;
    }

    private Date getDOTime(int deviceTime){
        String sb = Integer.toString(deviceTime) ;
        StringBuffer time=new StringBuffer();
        String a1=sb.substring(0,2);
        String a2=sb.substring(2,4);
        String a3=sb.substring(4,6);
        String a4=sb.substring(6,8);
        String a5=sb.substring(8,10);
        time.append("20").append(a1).append("-").append(a2).append("-").append(a3).append(" ");
        time.append(a4).append(":").append(a5).append(":30");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date=new Date();
        try {
            date=sdf.parse(time.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;

    }

}
