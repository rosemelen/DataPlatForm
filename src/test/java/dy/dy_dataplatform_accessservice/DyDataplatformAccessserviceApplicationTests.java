package dy.dy_dataplatform_accessservice;

import dy.cache.Appcache;
import dy.type.DeviceConfigData;
import dy.type.PostConfigData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.ConcurrentHashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("dy")
@EnableScheduling
@EnableTransactionManagement
@EnableAsync
public class DyDataplatformAccessserviceApplicationTests {
  private String deviceSN = "DY2017061208";

  @Autowired Appcache appcache;

	@Test
	public void contextLoads() {
		ConcurrentHashMap<String, PostConfigData> hashMap = new ConcurrentHashMap<>();
		DeviceConfigData deviceConfigData = new DeviceConfigData();
		deviceConfigData.setConfigdata("123");
		deviceConfigData.setConfighexdata(new byte[]{0x01,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02});
		deviceConfigData.setTimeservice("abc");
    	for (int i = 0; i < 20; i++) {
			PostConfigData postConfigData = new PostConfigData();
			postConfigData.setSn(deviceSN);
			postConfigData.setDeviceConfigData(deviceConfigData);
			postConfigData.setConfigId(Integer.toString(i));
      		hashMap.put(Integer.toString(i), postConfigData);
    	}
		appcache.getDeviceConfigMap().put(deviceSN,hashMap);
	}

}
