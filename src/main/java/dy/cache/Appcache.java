package dy.cache;

import dy.entity.DeviceConfig;
import dy.type.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component("appcache")
@Data
public class Appcache {
    //设备上报数据缓存
    private UserList<DeviceMetaData> deviceMetaDataList;
    //Coap数据缓存
    private UserList<DeviceMetaData> deviceCoapMetaDataList;
    //新旧块数据缓存
    private ConcurrentHashMap<String,ConcurrentHashMap<Integer,byte[]>> deviceBlockDataMap;
    //设备日志缓存
    private UserList<DeviceLog> deviceLogList;
    //设备配置缓存(第一个string表示sn，第二个string表示配置序列号)
    private ConcurrentHashMap<String, ConcurrentHashMap<String, PostConfigData>> deviceConfigMap;
    //处理端口映射
    private ConcurrentHashMap<String,DeviceIpMap> deviceIpMap;
    //服务下发组合配
    private ConcurrentHashMap<String,DeviceConfigBlockData>  serverSendedConfigDataMap;
    //设备配置状态缓存
    private UserList<DeviceConfig> deviceConfigList;
    //
    private UserList<DeviceConfig> deviceConfigDbOperationList;
    //设备配置状态缓存(第一个string表示sn，第二个string表示配置序列号,第三个string表示配置状态)
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> deviceConfigStatusMap;

    public Appcache(){
        deviceCoapMetaDataList = new UserList<>();
        deviceMetaDataList = new UserList<>();
        deviceBlockDataMap = new ConcurrentHashMap<>();
        deviceLogList = new UserList<>();
        deviceConfigMap = new ConcurrentHashMap<>();
        deviceIpMap = new ConcurrentHashMap<>();
        serverSendedConfigDataMap = new ConcurrentHashMap<>();
        deviceConfigList = new UserList<>();
        deviceConfigDbOperationList = new UserList<>();
        deviceConfigStatusMap = new ConcurrentHashMap<>();
    }
}
