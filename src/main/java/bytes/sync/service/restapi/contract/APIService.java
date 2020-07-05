package bytes.sync.service.restapi.contract;

import bytes.sync.entity.SchedulerWrapper;

import java.util.List;

public interface APIService {

    List<SchedulerWrapper> getAllSchedulerWrappers(boolean isActive);
    SchedulerWrapper getSchedulerWrapperById(String id) throws Exception;
    SchedulerWrapper createNewSchedulerWrapper(SchedulerWrapper wrapper)  throws Exception;
    SchedulerWrapper updateExistingSchedulerWrapper(String id, SchedulerWrapper wrapper)  throws Exception;
    void deleteSchedulerWrapperById(String id)  throws Exception;

}
