package dy.dy_dataplatform_accessservice;

import dy.block.CommonUtil;

public class TestKtv {
    public static void main(String[] args) {
        String data = "AA 00 24 5A 52 31 38 30 36 30 38 30 30 30 31 0D 03 34 36 30 31 31 31 31 37 36 33 37 35 38 36 36 00 14 01 6B 9A B5 38 27 02 40 65 1D 31 26 02 3F 80 08 31 DD AB";
        //String data = "0100008400194459323138303230303031380081F33652350204020D256B000000001206190E1D0000000000000000000000000000000000000000001C280081F33652350004020D256B000000001206190E1D0000000000000000000000000000000000000000009DD30081F33652350204020D256B000000001206190E3300000000000000000000000000000000000000000051BE";
        data = data.replace(" ","");

        while (true){

            try {
                UdpClient.sendUdpData("127.0.0.1",9998,10002,CommonUtil.hexString2Bytes(data));
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
