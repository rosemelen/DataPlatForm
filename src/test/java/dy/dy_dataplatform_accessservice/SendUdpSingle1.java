package dy.dy_dataplatform_accessservice;

import dy.block.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class SendUdpSingle1 {
    public static void main(String[] args) {

        //旧块
        //DY2017061408
        String data = "00 00 00 1B 00 59 44 59 32 30 31 37 30 36 31 34 30 38 00 78 39 E9 58 32 00 00 01 01 3E 25 00 00 00 00 00 6B 8A D6 F5 00 00 00 00 AD 6D ";
        List<String> list = new ArrayList<>();
        data = data.replace(" ","");
        list.add(data);
        //旧块+KTV
        //DY2017061308
        data = "0000001600015A52313830353130303031370D033436303131313137363337353836360014016B9AB538270240651D3126023F800831";
        data = data.replace(" ","");
        list.add(data);


        List<String> listnew = new ArrayList<>();
        //新块
        //DY2017061208
        String datanew = "00 00 00 1B 00 59 44 59 32 30 31 37 30 36 31 32 30 38 00 78 39 E9 58 32 00 00 01 01 3E 25 00 00 00 00 00 6B 8A D6 F5 00 00 00 00 AD 6D ";
        datanew = datanew.replace(" ","");
        listnew.add(datanew);
        //新块+KTV
        //DY2017061508
        datanew = "00 00 00 15 00 59 44 59 32 30 31 37 30 36 31 35 30 38 07 00 0B 02 01 0B 02 01 01 15 02 3E 1E 9E 9F 09 03 41 42 43 00 ";
        datanew = datanew.replace(" ","");
        listnew.add(datanew);

        while (true){
            try {
//                for(String str : list){
//                    UdpClient.sendUdpData("127.0.0.1",6000,10001,CommonUtil.hexString2Bytes(str));
//                    Thread.sleep(5000);
//                }
                for (String newstr : listnew) {
                    UdpClient.sendUdpData("127.0.0.1", 6001, 10000, CommonUtil.hexString2Bytes(newstr));
                    Thread.sleep(8000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}

/**
 * 00           块号
 * 00 00        帧号
 * 1B 00        帧长
 * 59           控制字
 * 44 59 32 30 31 37 30 36 31 34 30 38      SN
 * 00 78
 * 39 E9 58 32 00 00 01 01
 * 3E 25 00 00 00 00 00 6B 8AD6 F5 00 00 00 00 AD 6D     数据
 *
 */