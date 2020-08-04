package bytes.sync.service.restapi.impl;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.errors.DuplicateJob;
import bytes.sync.errors.SchedulerObjectNotFound;
import bytes.sync.repository.SchedulerWrapperRepository;
import bytes.sync.service.restapi.contract.APIService;
import bytes.sync.service.scheduler.impl.QuartzSchedulerServiceImpl;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
public class APIServiceImpl implements APIService {

    private Logger logger = LoggerFactory.getLogger(APIServiceImpl.class);

    @Autowired
    private SchedulerWrapperRepository schedulerWrapperRepository;

    @Autowired
    private QuartzSchedulerServiceImpl quartzSchedulerService;


    @Override
    public List<SchedulerWrapper> getAllSchedulerWrappers(boolean isActive) {
        List<SchedulerWrapper> list;
        if(!isActive) {
            logger.debug("isActive query param is set to false - fetching all the objects from db");
            list = schedulerWrapperRepository.findAll();
        } else {
            logger.debug("isActive query param is set to true - fetching the objects with active status as true from db");
            list = schedulerWrapperRepository.findByActiveTrue();
        }
        return list;
    }

    @Override
    public SchedulerWrapper getSchedulerWrapperById(String id) throws SchedulerObjectNotFound {
        Optional<SchedulerWrapper> wrapper = schedulerWrapperRepository.findById(id);
        if(!wrapper.isPresent()) {
            logger.error("no schedulerWrapper object found for the given id: {}", id);
            throw new SchedulerObjectNotFound("id: " + id);
        }
        return wrapper.get();
    }

    @Override
    public SchedulerWrapper createNewSchedulerWrapper(SchedulerWrapper wrapper) throws Exception {
        try {
            //Creating custom uuid - to be used in jobDataMap for further processing
            wrapper.setId(UUID.randomUUID().toString());
            //Add current date/time to the AddedOn field
            wrapper.setAddedOn(Date.valueOf(LocalDate.now()));

            //First try to schedule the job
            quartzSchedulerService.scheduleNewJob(wrapper);

            //If there was no exception - save this object
            SchedulerWrapper response = schedulerWrapperRepository.saveAndFlush(wrapper);
            logger.debug("Job with id: {} is created", response.getId());
            return response;
        } catch (SchedulerException e) {
            logger.error("scheduler exception occurred with error message: {}", e.getMessage(), e);
            throw new DuplicateJob(e.getMessage());
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
                wrapper.setAddedOn(schedulerWrapper.getAddedOn());
                wrapper.setActivationExpression(schedulerWrapper.getActivationExpression());
                quartzSchedulerService.updateScheduleJob(wrapper);
                schedulerWrapper = schedulerWrapperRepository.save(wrapper);
                logger.debug("Job with id: {} is created", wrapper.getId());
                return schedulerWrapper;
            } else {
                logger.error("no schedulerWrapper object found for the given id: {}", id);
                throw new SchedulerObjectNotFound("id: " + id);
            }
        } catch (SchedulerException e) {
            logger.error("scheduler exception occurred with error message: {}", e.getMessage(), e);
            throw new DuplicateJob(e.getMessage());
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
                schedulerWrapperRepository.deleteById(id);
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
