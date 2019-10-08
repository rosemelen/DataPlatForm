package dy.dbdao.impl;

import dy.dbdao.IAppDao;
import dy.entity.App;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AppDao implements IAppDao{
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public String getApiKey(String apikey) {
        Session session=sessionFactory.getCurrentSession();
//        session.createCriteria(App.class).add(Example.create(apikey)).list();
        App result=session.get(App.class,apikey);
        return result.getApikey();
    }
}
