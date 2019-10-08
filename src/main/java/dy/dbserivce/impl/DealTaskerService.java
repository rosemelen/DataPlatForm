package dy.dbserivce.impl;

import dy.dbdao.IDealTaskerDao;
import dy.dbserivce.IDealTaskerService;
import dy.entity.ViewAccessServiceQuery;
import dy.entity.ViewDeviceapplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

@Service
@org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
public class DealTaskerService implements IDealTaskerService<ViewAccessServiceQuery>{

    @Autowired
    private IDealTaskerDao dao;


    @Override
    public List<ViewAccessServiceQuery> getQueryByCondition(String condition, Serializable s) {
        return dao.getQueryByCondition(condition,s);
    }

    @Override
    public ViewAccessServiceQuery getBySn(String sn) {
        return dao.getBySn(sn);
    }

    @Override
    public ViewAccessServiceQuery getByDeviceSn(String devicesn) {
        return dao.getByDeviceSn(devicesn);
    }

    @Override
    public ViewAccessServiceQuery getByDeviceSnAndBusinessKey(String devicesn, String busikey) {
        return dao.getByDeviceSnAndBusinessKey(devicesn,busikey);
    }
}
