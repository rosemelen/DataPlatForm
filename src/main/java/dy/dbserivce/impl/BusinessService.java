package dy.dbserivce.impl;

import dy.dbdao.IBusinessDao;
import dy.dbserivce.IBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;

@Service
@org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
public class BusinessService implements IBusinessService{
   @Autowired
   private IBusinessDao dao;
    @Override
    public int getBusinesskey(String key) {
        return dao.getBusinesskey(key);
    }
}
