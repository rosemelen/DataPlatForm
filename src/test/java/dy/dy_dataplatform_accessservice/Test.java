package dy.dy_dataplatform_accessservice;

import com.alibaba.fastjson.JSON;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {

        byte[] bytes = new byte[3];
        bytes[0] = (byte)0x61;
        bytes[1] = (byte)0xab;
        bytes[2] = (byte)0xac;


            String str1 = new String(bytes);
            System.out.println(str1);


//
//        byte b1 = (byte)0xab;
//        String stra = BytesUtils.bytesToStringNoSpace(b1);
//
//        List<MType> list = new ArrayList<>();
//        for(int i=0; i<100; i++){
//            MType hh = new MType();
//            hh.setPort(1);
//            DateTime time = DateTime.now();
//            time = time.plusMonths(new Random().nextInt(10));
//            hh.setDate(time.toDate());
//            list.add(hh);
//        }
//
//
//        int bb = 0;

//
//        List<Date> list1 = new ArrayList<>(Collections2.transform(list, new Function<httpd, Date>() {
//            @Override
//            public Date apply(httpd hh) {
//                return hh.getDate();
//            }
//        }));

//        Map<String,List<MType>> map = list.stream().collect(Collectors.groupingBy(ex->new DateTime(ex.getDate()).toString("yyyyMM")));
//        Map<Date,List<httpd>> map = list.stream().collect(Collectors.groupingBy();
//
//        System.out.println(Joiner.on(",").skipNulls().join(list1));

//        ENUM_BLOCKSIGN sdf = ENUM_BLOCKSIGN.setEnumBlockSign(0);
//        if(sdf == ENUM_BLOCKSIGN.BLOCK_START){
//            System.out.println("true");
//        }
//
//        DeviceMetaData deviceMetaData = new DeviceMetaData();
//        deviceMetaData.setReportTime(new Date());
//        deviceMetaData.setDeviceSn("test");
//        deviceMetaData.setBase64Data("aa");
//        deviceMetaData.setProcessorType(new ProcessorType(1,2,"sd"));
//        //deviceMetaData.setKtvData(new ArrayList<>());
//        System.out.println(JSONObject.toJSONString(deviceMetaData));

//        Cache<String,String> cache = CacheBuilder.newBuilder()
//                .maximumSize(1000).expireAfterWrite(5, TimeUnit. SECONDS).concurrencyLevel(10).
//                        build(new CacheLoader<String, String>() {
//            @Override
//            public String load(String s) throws Exception {
//                return null;
//            }
//        });
//
//        cache.put("a","1");
//        cache.put("b","1");

//        try {
//            ((LoadingCache<String, String>) cache).get("a");
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

//        try {
//            Thread.sleep(6*1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        System.out.println(cache.size());
//        try {
//           String test =  ((LoadingCache<String, String>) cache).get("a");
//           System.out.println(test);
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        try {
//            System.in.read();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            URL str = Test.class.getResource("/processor.json");
//
//            URL url = Test.class.getClassLoader().getResource("/processor.json");
//
//            String json = FileUtils.readFileToString(new File(str.getFile()),"utf-8");
//
//            JClass listProcessor = (JClass) JSON.parseObject(json, (Type) JClass.class);
//            int a = 1;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
