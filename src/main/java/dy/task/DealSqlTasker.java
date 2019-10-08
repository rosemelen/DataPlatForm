package dy.task;

import dy.cache.Appcache;
import dy.dbserivce.impl.DeviceConfigService;
import dy.entity.DeviceConfig;
import dy.log.AppLogger;
import dy.mongo.MongoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dealsqltasker")
@EnableScheduling
public class DealSqlTasker {

    @Autowired
    Appcache appcache;

    @Autowired
    AppLogger appLogger;

    @Autowired
    DeviceConfigService deviceConfigService;


    @Scheduled(fixedDelayString = "${taskSqlDb}000",initialDelay = 5000)
    public void dealSql() throws Exception {

        List<DeviceConfig> deviceConfigList = appcache.getDeviceConfigDbOperationList().getClone();
        if(deviceConfigList.size() == 0){ return;}

        for (DeviceConfig deviceConfig : deviceConfigList){
            DeviceConfig updateDeviceConfig  = deviceConfigService.getConfigByConfigid(deviceConfig.getSn(),deviceConfig.getConfigid());
            if(updateDeviceConfig != null){
                updateDeviceConfig.setStatus(deviceConfig.getStatus());
            }else {
                appLogger.getLogger().info(String.format("device config change status false sn:[%s],configid:[%s],status:[%s]",
                        deviceConfig.getSn(),deviceConfig.getConfigid(),deviceConfig.getStatus()));
            }
            deviceConfigService.updateConfig(updateDeviceConfig);
        }
    }

}
