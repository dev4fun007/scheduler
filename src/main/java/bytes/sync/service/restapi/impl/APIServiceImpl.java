package bytes.sync.service.restapi.impl;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.errors.SchedulerObjectNotFound;
import bytes.sync.repository.SchedulerWrapperRepository;
import bytes.sync.service.restapi.contract.APIService;
import bytes.sync.service.scheduler.impl.QuartzSchedulerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class APIServiceImpl implements APIService {

    @Autowired
    private SchedulerWrapperRepository repository;

    @Autowired
    private QuartzSchedulerServiceImpl quartzSchedulerService;


    @Override
    public List<SchedulerWrapper> getAllSchedulerWrappers(boolean isActive) {
        List<SchedulerWrapper> list;
        if(!isActive) {
            list = repository.findAll();
        } else {
            list = repository.findByActiveTrue();
        }
        return list;
    }

    @Override
    public SchedulerWrapper getSchedulerWrapperById(String id) throws SchedulerObjectNotFound {
        Optional<SchedulerWrapper> wrapper = repository.findById(id);
        if(!wrapper.isPresent()) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
        return wrapper.get();
    }

    @Override
    public SchedulerWrapper createNewSchedulerWrapper(SchedulerWrapper wrapper) throws Exception {
        try {
            wrapper.setAddedOn(Date.valueOf(LocalDate.now()));
            SchedulerWrapper response = repository.save(wrapper);
            quartzSchedulerService.scheduleNewJob(wrapper);
            System.out.println("Job with id: "+response.getId()+" is created");
            return response;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public SchedulerWrapper updateExistingSchedulerWrapper(String id, SchedulerWrapper wrapper) throws Exception {
        try {
            SchedulerWrapper schedulerWrapper = getSchedulerWrapperById(id);
            if(schedulerWrapper != null) {
                wrapper.setId(id);
                schedulerWrapper = repository.save(wrapper);
                quartzSchedulerService.updateScheduleJob(schedulerWrapper);
                System.out.println("Job with id: "+wrapper.getId()+" is updated");
                return schedulerWrapper;
            } else {
                throw new SchedulerObjectNotFound("id: " + id);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void deleteSchedulerWrapperById(String id) throws Exception {
        try {
            SchedulerWrapper schedulerWrapper = getSchedulerWrapperById(id);
            if(schedulerWrapper != null) {
                boolean quartzDeleted = quartzSchedulerService.deleteScheduledJob(schedulerWrapper);
                repository.deleteById(id);
                System.out.println("Job with id: "+id+" isDeleted: "+quartzDeleted);
            } else {
                throw new SchedulerObjectNotFound("id: " + id);
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
