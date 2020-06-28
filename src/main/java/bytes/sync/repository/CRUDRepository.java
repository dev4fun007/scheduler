package bytes.sync.repository;

import bytes.sync.model.SchedulerWrapper;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CRUDRepository {

    public List<SchedulerWrapper> findAllSchedulerObjects();
    public SchedulerWrapper findSchedulerObjectById(String id);
    public boolean saveNewSchedulerObject(SchedulerWrapper schedulerWrapper);
    public boolean updateSchedulerObjectById(String id, SchedulerWrapper schedulerWrapper);
    public boolean deleteSchedulerObjectById(String id);

}
