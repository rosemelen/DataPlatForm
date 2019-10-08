package dy.dbserivce.impl;

import dy.dbdao.IAppDao;
import dy.dbserivce.IAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;

@Service
@org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
public class AppService implements IAppService{
    @Autowired
    private IAppDao dao;

    @Override
    public String getApiKey(String apikey) {
        return dao.getApiKey(apikey);
    }
}
