package dy.dbdao.impl;

import dy.dbdao.IDealTaskerDao;
import dy.entity.ViewAccessServiceQuery;
import dy.entity.ViewDeviceapplication;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public class DealTaskerDao implements IDealTaskerDao<ViewAccessServiceQuery> {

    @Autowired
    private SessionFactory sessionFactory;

    @Value("${procedure.name}")
    private String name;

    @Override
    public List<ViewAccessServiceQuery> getQueryByCondition(String condition, Serializable s) {
        String sql = "{CALL "+name+"(:"+condition+",:parameter)}";
        Session session = sessionFactory.getCurrentSession();
        Query<ViewAccessServiceQuery> query = session.createNativeQuery(sql).addEntity(ViewAccessServiceQuery.class);
        query.setParameter(condition,condition);
        query.setParameter("parameter",s);
        return query.list();
    }

    @Override
    public ViewAccessServiceQuery getBySn(String sn) {
        return sessionFactory.getCurrentSession().get(ViewAccessServiceQuery.class,sn);
    }

    @Override
    public ViewAccessServiceQuery getByDeviceSn(String devicesn) {
        Session session=sessionFactory.getCurrentSession();
        ViewAccessServiceQuery device = (ViewAccessServiceQuery)session.createCriteria(ViewAccessServiceQuery.class)
                .add(Restrictions.eq("sn", devicesn))
                .uniqueResult();
        return device;
    }

    @Override
    public ViewAccessServiceQuery getByDeviceSnAndBusinessKey(String devicesn,String busikey) {
        Session session=sessionFactory.getCurrentSession();
        ViewAccessServiceQuery device = (ViewAccessServiceQuery)session.createCriteria(ViewAccessServiceQuery.class)
                .add(Restrictions.eq("sn", devicesn))
                .add(Restrictions.eq("busikey", busikey))
                .uniqueResult();
        return device;
    }

}
