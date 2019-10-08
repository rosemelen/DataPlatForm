package dy.dbdao;

import dy.entity.DeviceConfig;

public interface IDeviceConfigDao {
    DeviceConfig getConfigByConfigid(String sn,String configid);
    void saveConfig(DeviceConfig config);
    void updateConfig(DeviceConfig config);
}
