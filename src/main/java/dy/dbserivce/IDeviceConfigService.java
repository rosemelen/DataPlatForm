package dy.dbserivce;

import dy.entity.DeviceConfig;

public interface IDeviceConfigService {
    DeviceConfig getConfigByConfigid(String sn,String configid);
    void saveConfig(DeviceConfig config);
    void updateConfig(DeviceConfig config);
}
