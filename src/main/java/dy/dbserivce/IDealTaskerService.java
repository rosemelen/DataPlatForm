package dy.dbserivce;

import dy.entity.ViewAccessServiceQuery;

import java.io.Serializable;
import java.util.List;

public interface IDealTaskerService<T> {
    List<T> getQueryByCondition(String condition,Serializable s);
    ViewAccessServiceQuery getBySn(String sn);
    ViewAccessServiceQuery getByDeviceSn(String devicesn);
    ViewAccessServiceQuery getByDeviceSnAndBusinessKey(String devicesn,String busikey);
}
