package dy.dbdao.impl;

import dy.dbdao.IDeviceConfigDao;
import dy.entity.DeviceConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DeviceConfigDao implements IDeviceConfigDao{

    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public DeviceConfig getConfigByConfigid(String sn,String configid) {
        Session session=sessionFactory.getCurrentSession();
        DeviceConfig deviceConfig = (DeviceConfig)session.createCriteria(DeviceConfig.class)
                .add(Restrictions.eq("sn", sn))
                .add(Restrictions.eq("configid", configid))
                .uniqueResult();
        return deviceConfig;
    }

    @Override
    public void saveConfig(DeviceConfig config) {
        Session session = sessionFactory.getCurrentSession();
        session.save(config);
        session.flush();
    }

    @Override
    public void updateConfig(DeviceConfig config) {
        Session session = sessionFactory.getCurrentSession();
        session.update(config);
        session.flush();
    }
}
