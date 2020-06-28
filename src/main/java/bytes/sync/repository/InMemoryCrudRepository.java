package bytes.sync.repository;

import bytes.sync.model.SchedulerWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryCrudRepository implements CRUDRepository {

    private Map<String, SchedulerWrapper> inMemoryDB;

    public InMemoryCrudRepository() {
        inMemoryDB = new HashMap<>();
    }


    @Override
    public List<SchedulerWrapper> findAllSchedulerObjects() {
        return new ArrayList<SchedulerWrapper>(this.inMemoryDB.values());
    }

    @Override
    public SchedulerWrapper findSchedulerObjectById(String id) {
        return this.inMemoryDB.get(id);
    }

    @Override
    public boolean saveNewSchedulerObject(SchedulerWrapper schedulerWrapper) {
        this.inMemoryDB.put(schedulerWrapper.getId(), schedulerWrapper);
        return true;
    }

    @Override
    public boolean updateSchedulerObjectById(String id, SchedulerWrapper schedulerWrapper) {
        SchedulerWrapper wrapper = this.inMemoryDB.get(id);
        if(wrapper == null) {
            return false;
        }
        this.inMemoryDB.put(id, schedulerWrapper);
        return true;
    }

    @Override
    public boolean deleteSchedulerObjectById(String id) {
        SchedulerWrapper wrapper = this.inMemoryDB.get(id);
        if(wrapper == null) {
            return false;
        }
        this.inMemoryDB.remove(id);
        return true;
    }
}
