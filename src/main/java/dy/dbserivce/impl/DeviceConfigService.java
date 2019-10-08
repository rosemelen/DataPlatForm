package dy.dbserivce.impl;

import dy.dbdao.impl.DeviceConfigDao;
import dy.dbserivce.IDeviceConfigService;
import dy.entity.DeviceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;

@Service
@org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
public class DeviceConfigService implements IDeviceConfigService{
    @Autowired
    private DeviceConfigDao dao;

    @Override
    public DeviceConfig getConfigByConfigid(String sn,String configid) {
        return dao.getConfigByConfigid(sn,configid);
    }

    @Override
    public void saveConfig(DeviceConfig config) {
        dao.saveConfig(config);
    }

    @Override
    public void updateConfig(DeviceConfig config) {
        dao.updateConfig(config);
    }

}
