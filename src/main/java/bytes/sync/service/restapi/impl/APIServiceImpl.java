package bytes.sync.service.restapi.impl;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.errors.SchedulerObjectNotFound;
import bytes.sync.repository.SchedulerWrapperRepository;
import bytes.sync.service.restapi.contract.APIService;
import bytes.sync.service.scheduler.impl.QuartzSchedulerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class APIServiceImpl implements APIService {

    private Logger logger = LoggerFactory.getLogger(APIServiceImpl.class);

    @Autowired
    private SchedulerWrapperRepository repository;

    @Autowired
    private QuartzSchedulerServiceImpl quartzSchedulerService;


    @Override
    public List<SchedulerWrapper> getAllSchedulerWrappers(boolean isActive) {
        List<SchedulerWrapper> list;
        if(!isActive) {
            logger.debug("isActive query param is set to false - fetching all the objects from db");
            list = repository.findAll();
        } else {
            logger.debug("isActive query param is set to true - fetching the objects with active status as true from db");
            list = repository.findByActiveTrue();
        }
        return list;
    }

    @Override
    public SchedulerWrapper getSchedulerWrapperById(String id) throws SchedulerObjectNotFound {
        Optional<SchedulerWrapper> wrapper = repository.findById(id);
        if(!wrapper.isPresent()) {
            logger.error("no schedulerWrapper object found for the given id: {}", id);
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
            logger.debug("Job with id: {} is created", wrapper.getId());

            return response;
        } catch (Exception e) {
            logger.error("error creating a new entity", e);
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
                logger.debug("Job with id: {} is created", wrapper.getId());

                return schedulerWrapper;
            } else {
                logger.error("no schedulerWrapper object found for the given id: {}", id);
                throw new SchedulerObjectNotFound("id: " + id);
            }
        } catch (Exception e) {
            logger.error("error updating a new entity", e);
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
                logger.debug("Job with id: {} isDeleted: {}", id, quartzDeleted);
            } else {
                logger.error("no schedulerWrapper object found for the given id: {}", id);
                throw new SchedulerObjectNotFound("id: " + id);
            }
        } catch (Exception e) {
            logger.error("error deleting a new entity", e);
            throw e;
        }
    }
}
