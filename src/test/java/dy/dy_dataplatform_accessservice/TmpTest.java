package dy.dy_dataplatform_accessservice;

import dy.block.CommonUtil;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;

public class TmpTest {
    public static void main(String[] args) {
        byte[] bytes = new byte[]{0x01,0x02};
        ArrayUtils.reverse(bytes);
        System.out.println(CommonUtil.byte2HexStr(bytes));
    }
}
