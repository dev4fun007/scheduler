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

    private final Logger logger = LoggerFactory.getLogger(QuartzSchedulerServiceImpl.class);


    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SchedulerWrapperRepository schedulerWrapperRepository;

    @Autowired
    CustomTriggerListener customTriggerListener;


    /**
     * Create a new job in quartz for the given schedulerWrapper object
     * @param schedulerWrapper
     * @throws InvalidCronExpression
     * @throws SchedulerException
     */
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
            //Add the trigger listener as this object is scheduled for execution
            scheduler.getListenerManager().addTriggerListener(customTriggerListener);

            //If the newly created schedulerWrapper object is not set to active
            //un-schedule it now
            if(!schedulerWrapper.getActive()) {
                disableAllTriggersForAJob(scheduler, schedulerWrapper);
                logger.info("new job schedule created - inactive and unscheduled");
            } else {
                logger.info("new job schedule created - active and scheduled");
            }
        } catch (InvalidCronExpression | SchedulerException e) {
            logger.error("error occurred while scheduling a new job", e);
            throw e;
        } catch (ClassNotFoundException e) {
            logger.error("error occurred while scheduling a new job", e);
        }
    }

    /**
     * Update the job represented by the given schedulerWrapper object
     * @param schedulerWrapper
     * @throws InvalidCronExpression
     * @throws SchedulerException
     */
    @Override
    public void updateScheduleJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression, SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            //Create the job detail
            JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);

            //Create the trigger
            CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());

            //Re-schedule the job
            //Since this is an update(PUT) call - a valid non-null schedulerWrapper object must be present
            //with a valid scheduled object in quartz - no need to check if a job exists or not
            //This will handle the active status and adding/removing customTriggerListener
            rescheduleJob(scheduler, schedulerWrapper, cronTriggerFactoryBean);

            logger.info("job updated with new/old schedule");
        } catch (InvalidCronExpression | SchedulerException e) {
            logger.error("error occurred while scheduling a new job", e);
            throw e;
        } catch (ClassNotFoundException e) {
            logger.error("error occurred while scheduling a new job", e);
        }
    }

    /**
     * Delete the scheduled job represented by the given schedulerWrapper object
     * @param schedulerWrapper
     * @return
     */
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
     * This fetches all the scheduler objects stored in db and then tries to schedule them at startup
     */
    @Override
    public void startAllSchedulers() {
        List<SchedulerWrapper> schedulerWrapperList = schedulerWrapperRepository.findAll();
        logger.info("JobCount to schedule at startup: {}", schedulerWrapperList.size());

        if(!schedulerWrapperList.isEmpty()) {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            try {
                if(scheduler.getListenerManager().getTriggerListener(CustomTriggerListener.CUSTOM_TRIGGER_NAME) == null) {
                    logger.debug("no custom trigger attached to the scheduler, adding one now");
                    scheduler.getListenerManager().addTriggerListener(customTriggerListener);
                }
            } catch (SchedulerException e) {
                logger.error("error adding custom trigger listener at startup", e);
            }

            schedulerWrapperList.forEach(schedulerWrapper -> {
                try {
                    //Create jobDetailBean and cronTriggerBean
                    JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);
                    CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());

                    //Check if a schedule for the given jobKey exists, if not then create a new one
                    if(!scheduler.checkExists(new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup()))) {
                        logger.debug("Job for this SchedulerWrapper Object: {} is not present, scheduling it", schedulerWrapper.getId());

                        scheduler.scheduleJob(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());

                        //If a new schedule was created but the active state was false - disable all the triggers
                        //do not delete the job, if the user wants job deleted - they will call delete rest api
                        if(!schedulerWrapper.getActive()) {
                            logger.info("SchedulerWrapper Object with id: {} not scheduled at startup as active status is false", schedulerWrapper.getId());
                            disableAllTriggersForAJob(scheduler, schedulerWrapper);
                        } else {
                            logger.info("SchedulerWrapper Object with id: {} scheduled at startup", schedulerWrapper.getId());
                        }
                    } else {
                        rescheduleJob(scheduler, schedulerWrapper, cronTriggerFactoryBean);
                        logger.debug("scheduler exists for the objectId: {}", schedulerWrapper.getId());
                    }
                } catch (ClassNotFoundException | SchedulerException | InvalidCronExpression e) {
                    logger.error("error creating quartz schedules at startup", e);
                }
            });
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
            String jobClass = "bytes.sync.jobs." + schedulerWrapper.getJobClass();
            jobDetailFactoryBean.setJobClass((Class<? extends QuartzJobBean>) Class.forName(jobClass));
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
            cronTriggerFactoryBean.setGroup(jobDetail.getKey().getGroup());
            cronTriggerFactoryBean.setName(jobDetail.getKey().getName());
            cronTriggerFactoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
            cronTriggerFactoryBean.setStartTime(schedulerWrapper.getStartTime());

            //Validate and then add the passed in cron expression
            if(!CronExpression.isValidExpression(schedulerWrapper.getCronExpression()))
                throw new InvalidCronExpression(schedulerWrapper.getCronExpression());

            //cronExpression is valid - add it to the trigger
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
     * This does not remove the triggers - just pause them
     * @param scheduler
     * @param schedulerWrapper
     * @throws SchedulerException
     */
    private void disableAllTriggersForAJob(Scheduler scheduler, SchedulerWrapper schedulerWrapper) throws SchedulerException {
        //Disable all the triggers related to this job
        JobKey jobKey = new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup());
        List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
        logger.debug("JobKey: {}, FoundTriggerCount: {} to pause trigger", jobKey, triggers.size());

        for(Trigger trigger : triggers) {
            scheduler.pauseTrigger(trigger.getKey());
//            scheduler.getListenerManager().removeTriggerListener(trigger.getKey().toString());
            logger.debug("Paused TriggerKey: {}, Removed CustomTriggerListener", trigger.getKey());
        }
        logger.debug("paused all the triggers");
    }

    /**
     * Rescheduling the jobs - activating/pausing triggers based on the active status
     * This keeps all the triggers still attached to the job but just pause/resumes them
     * New trigger object is added to each triggerKey
     * @param scheduler
     * @param schedulerWrapper
     * @param cronTriggerFactoryBean
     * @throws SchedulerException
     */
    private void rescheduleJob(Scheduler scheduler, SchedulerWrapper schedulerWrapper, CronTriggerFactoryBean cronTriggerFactoryBean) throws SchedulerException {
        JobKey jobKey = new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup());
        List<Trigger> triggerList = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
        logger.debug("JobKey: {}, FoundTriggerCount: {} to resume/pause trigger", jobKey, triggerList.size());

        for(Trigger trigger : triggerList) {
            scheduler.rescheduleJob(trigger.getKey(), cronTriggerFactoryBean.getObject());
            logger.debug("RescheduledJob With TriggerKey: {}", trigger.getKey());
            if(schedulerWrapper.getActive()) {
                scheduler.resumeTrigger(trigger.getKey());
//                scheduler.getListenerManager().addTriggerListener(customTriggerListener);
                logger.debug("Resumed TriggerKey: {}, Added CustomTriggerListener", trigger.getKey());
            } else {
                scheduler.pauseTrigger(trigger.getKey());
//                scheduler.getListenerManager().removeTriggerListener(trigger.getKey().toString());
                logger.debug("Paused TriggerKey: {}, Removed CustomTriggerListener", trigger.getKey());
            }
        }
        logger.debug("resumed/paused all the triggers");
    }

}
