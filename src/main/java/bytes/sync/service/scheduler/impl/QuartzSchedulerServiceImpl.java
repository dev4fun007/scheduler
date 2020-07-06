package bytes.sync.service.scheduler.impl;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.errors.InvalidCronExpression;
import bytes.sync.quartzcomponents.CustomTriggerListener;
import bytes.sync.quartzcomponents.PersistCronTriggerFactoryBean;
import bytes.sync.repository.SchedulerWrapperRepository;
import bytes.sync.service.scheduler.contract.GenericSchedulerService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.*;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class QuartzSchedulerServiceImpl implements GenericSchedulerService {

    private Logger logger = LoggerFactory.getLogger(QuartzSchedulerServiceImpl.class);


    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SchedulerWrapperRepository schedulerWrapperRepository;

    @Autowired
    private CustomTriggerListener customTriggerListener;


    @Override
    public void scheduleNewJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression, SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            //Create the job detail
            JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);

            //Create the trigger
            CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());

            //Schedule job
            scheduler.scheduleJob(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());

            //If the newly created schedulerWrapper object is not set to active
            //un-schedule it now
            if(!schedulerWrapper.getActive()) {
                disableAllTriggersForAJob(scheduler, schedulerWrapper);
                logger.info("new job schedule created - inactive and unscheduled");
            } else {
                //Add the trigger listener as this object is scheduled for execution
                scheduler.getListenerManager().addTriggerListener(customTriggerListener);
                logger.info("new job schedule created - active and scheduled");
            }
        } catch (InvalidCronExpression | SchedulerException e) {
            logger.error("error occurred while scheduling a new job", e);
            throw e;
        } catch (ClassNotFoundException e) {
            logger.error("error occurred while scheduling a new job", e);
        }
    }

    @Override
    public void updateScheduleJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression, SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            //Create the job detail
            JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);

            //Create the trigger
            CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());

            //Rescheduling the job is not working properly as the new triggers are not getting attached
            //To overcome this, delete this job and then schedule it again with new data
            //Delete this entry
            deleteScheduledJob(schedulerWrapper);

            //Re-schedule the job
            scheduler.scheduleJob(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());
            scheduler.getListenerManager().addTriggerListener(customTriggerListener);
            logger.info("job updated with new/old schedule");

            //This object is no longer set to be active
            //Un-Schedule the job - disable all the triggers associated with this jobKey
            if(!schedulerWrapper.getActive()) {
                disableAllTriggersForAJob(scheduler, schedulerWrapper);
            }
        } catch (InvalidCronExpression | SchedulerException e) {
            logger.error("error occurred while scheduling a new job", e);
            throw e;
        } catch (ClassNotFoundException e) {
            logger.error("error occurred while scheduling a new job", e);
        }
    }

    @Override
    public boolean deleteScheduledJob(SchedulerWrapper schedulerWrapper) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup());
        try {
            boolean isDeleted = scheduler.deleteJob(jobKey);
            logger.info("schedulerWrapper object with id: {} and jobKey: {} isDeleted: {}", schedulerWrapper.getId(), jobKey, isDeleted);
            return isDeleted;
        } catch (SchedulerException e) {
            logger.error("error occurred while deleting the object id: {} and jobKey: {}", schedulerWrapper.getId(), jobKey, e);
            return false;
        }
    }

    /**
     * Instantiate a JobDetailFactoryBean based on the schedulerWrapper object provided
     * @param schedulerWrapper the scheduler object
     * @return JobDetailFactoryBean instance
     * @throws ClassNotFoundException if the job class is incorrect
     */
    private JobDetailFactoryBean getJobDetailFactoryBean(SchedulerWrapper schedulerWrapper) throws ClassNotFoundException {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        try {
            jobDetailFactoryBean.setApplicationContext(context);
            jobDetailFactoryBean.setJobClass((Class<? extends QuartzJobBean>) Class.forName(schedulerWrapper.getJobClass()));
            jobDetailFactoryBean.setDurability(true);
            jobDetailFactoryBean.setGroup(schedulerWrapper.getJobGroup());
            jobDetailFactoryBean.setName(schedulerWrapper.getJobName());

            jobDetailFactoryBean.afterPropertiesSet();
            logger.debug("jobDetailFactoryBean instantiated successfully");
        } catch (Exception e) {
            logger.error("error occurred while creating a new jobDetailFactoryBean", e);
        }

        return jobDetailFactoryBean;
    }

    /**
     * Instantiate a SimpleTriggerFactoryBean based on the schedulerWrapper object provided
     * @param schedulerWrapper the scheduler object
     * @param jobDetail the job detail object for this trigger
     * @return CronTriggerFactory bean instance
     */
    private PersistCronTriggerFactoryBean getCronTriggerFactoryBean(SchedulerWrapper schedulerWrapper, JobDetail jobDetail) throws InvalidCronExpression {
        PersistCronTriggerFactoryBean cronTriggerFactoryBean = new PersistCronTriggerFactoryBean();
        try {
            cronTriggerFactoryBean.setJobDetail(jobDetail);
            cronTriggerFactoryBean.setGroup(schedulerWrapper.getJobGroup());
            cronTriggerFactoryBean.setName(schedulerWrapper.getJobName());
            cronTriggerFactoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
            cronTriggerFactoryBean.setStartTime(schedulerWrapper.getStartTime());

            //Validate and then add the passed in cron expression
            if(!CronExpression.isValidExpression(schedulerWrapper.getCronExpression()))
                throw new InvalidCronExpression(schedulerWrapper.getCronExpression());
            cronTriggerFactoryBean.setCronExpression(schedulerWrapper.getCronExpression());

            cronTriggerFactoryBean.afterPropertiesSet();
            logger.debug("persistCronTriggerFactoryBean instantiated successfully");
        } catch (InvalidCronExpression e) {
            logger.error("invalid cron expression passed for objectId: {}", schedulerWrapper.getId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("error occurred while creating a new jobDetailFactoryBean", e);
        }

        return cronTriggerFactoryBean;
    }


    /**
     * Disable each of the triggers attached to a specific jobName & jobGroup combination
     * @param scheduler
     * @param schedulerWrapper
     * @throws SchedulerException
     */
    private void disableAllTriggersForAJob(Scheduler scheduler, SchedulerWrapper schedulerWrapper) throws SchedulerException {
        //Disable all the triggers related to this job
        JobKey jobKey = new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup());
        List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
        logger.debug("JobKey: {}, FoundTriggerCount: {} to un-schedule", jobKey, triggers.size());

        for(Trigger trigger : triggers) {
            scheduler.unscheduleJob(trigger.getKey());
            logger.debug("Disabled TriggerKey: {}", trigger.getKey());
        }
        logger.debug("unscheduled all the triggers");
    }


    /**
     * If the job was set to inactive - all the triggers associated with it will be removed
     * We need to add the trigger when the job is again set to active
     * @param scheduler
     * @param schedulerWrapper
     * @param cronTriggerFactoryBean
     * @throws SchedulerException
     */
    private void rescheduleTriggersForAJob(Scheduler scheduler, SchedulerWrapper schedulerWrapper, CronTriggerFactoryBean cronTriggerFactoryBean) throws SchedulerException {
        JobKey jobKey = new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup());
        logger.debug("Rescheduling JobKey: {}", jobKey);
        logger.debug("New TriggerKey: {} to be rescheduled", cronTriggerFactoryBean.getObject().getKey().toString());
        scheduler.rescheduleJob(cronTriggerFactoryBean.getObject().getKey(), cronTriggerFactoryBean.getObject());
        logger.debug("rescheduled the triggers");
    }


    /**
     * This fetches all the scheduler objects stored in db and then tries to schedule them at startup
     */
    @Override
    public void startAllSchedulers() {
        List<SchedulerWrapper> schedulerWrapperList = schedulerWrapperRepository.findAll();
        logger.info("JobCount to schedule at startup: {}", schedulerWrapperList.size());

        if(!schedulerWrapperList.isEmpty()) {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            schedulerWrapperList.forEach(schedulerWrapper -> {
                try {
                    JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);
                    CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());

                    //Check if a schedule for the given jobKey exists, if not then create a new one
                    if(!scheduler.checkExists(new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup()))) {
                        logger.debug("Job for this SchedulerWrapper Object: {} is not present, scheduling it", schedulerWrapper.getId());
                        scheduler.scheduleJob(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());
                    }

                    //If a new schedule was created but the active state was false - disable all the triggers
                    //do not delete the job, if the user wants job deleted - they will call delete rest api
                    if(!schedulerWrapper.getActive()) {
                        logger.info("SchedulerWrapper Object with id: {} not scheduled at startup as active status is false", schedulerWrapper.getId());
                        disableAllTriggersForAJob(scheduler, schedulerWrapper);
                    } else {
                        logger.info("SchedulerWrapper Object with id: {} scheduled at startup", schedulerWrapper.getId());
                        scheduler.getListenerManager().addTriggerListener(customTriggerListener);
                    }
                } catch (ClassNotFoundException | SchedulerException | InvalidCronExpression e) {
                    logger.error("error creating quartz schedules at startup", e);
                }
            });
        }
    }

}
