package dy.dbdao.impl;

import dy.dbdao.IBusinessDao;
import dy.entity.Business;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BusinessDao implements IBusinessDao{
    @Autowired
    private SessionFactory sessionFactory;
    @Override
    public int getBusinesskey(String key) {
        Session session=sessionFactory.getCurrentSession();
        Long result = (Long) session.createCriteria(Business.class)
                .setProjection(Projections.count("id"))
                .add(Restrictions.eq("key", key)).uniqueResult();
        return result.intValue();
    }
}
