package dy.cache;

import com.rits.cloning.Cloner;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class UserList<T> implements Serializable,Iterable<T> {

    private List<T> list;

    public UserList(){
        list = new ArrayList<T>();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    public void addObj(T obj){
        synchronized (list){
            list.add(obj);
        }
    }

    public void removeObj(T obj){
        synchronized (list){
            list.remove(obj);
        }
    }

    public int getListSize(){
        int listSize = 0;
        synchronized (list){
            listSize = list.size();
        }
        return listSize;
    }

    public List<T> getClone() {
        Cloner cloner = new Cloner();
        List<T> tmpList = null;
        synchronized (list){
            tmpList = cloner.deepClone(list);
            list.clear();
        }
        return tmpList;
    }

    public List<T> getCloneNoClear() {
        Cloner cloner = new Cloner();
        List<T> tmpList = null;
        synchronized (list){
            tmpList = cloner.deepClone(list);
        }
        return tmpList;
    }

}
