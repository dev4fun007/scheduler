package bytes.sync.service.scheduler.impl;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.errors.InvalidCronExpression;
import bytes.sync.quartzcomponents.CustomTriggerListener;
import bytes.sync.quartzcomponents.PersistCronTriggerFactoryBean;
import bytes.sync.repository.SchedulerWrapperRepository;
import bytes.sync.service.scheduler.contract.GenericSchedulerService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.*;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;


@Service
public class QuartzSchedulerServiceImpl implements GenericSchedulerService {

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
                System.out.println("new job schedule created - inactive and unscheduled");
            } else {
                //Add the trigger listener as this object is scheduled for execution
                scheduler.getListenerManager().addTriggerListener(customTriggerListener);
                System.out.println("new job schedule created - active and scheduled");
            }
        } catch (InvalidCronExpression | SchedulerException e) {
          throw e;
        } catch (ClassNotFoundException | ParseException e) {
            System.out.println(e);
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
            System.out.println("job updated by deleting and then re-creating it");

            //This object is no longer set to be active
            //Un-Schedule the job - disable all the triggers associated with this jobKey
            if(!schedulerWrapper.getActive()) {
                disableAllTriggersForAJob(scheduler, schedulerWrapper);
            }
        } catch (InvalidCronExpression | SchedulerException e) {
          throw e;
        } catch (ClassNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteScheduledJob(SchedulerWrapper schedulerWrapper) {
        try {
            System.out.println("job deleted");
            return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup()));
        } catch (SchedulerException e) {
            System.out.println(e);
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

        } catch (Exception e) {
            System.out.println(e);
        }

        return jobDetailFactoryBean;
    }

    /**
     * Instantiate a SimpleTriggerFactoryBean based on the schedulerWrapper object provided
     * @param schedulerWrapper the scheduler object
     * @param jobDetail the job detail object for this trigger
     * @return CronTriggerFactory bean instance
     */
    private PersistCronTriggerFactoryBean getCronTriggerFactoryBean(SchedulerWrapper schedulerWrapper, JobDetail jobDetail) throws InvalidCronExpression, ParseException {
        PersistCronTriggerFactoryBean cronTriggerFactoryBean = new PersistCronTriggerFactoryBean();
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
        System.out.println("JobKey: " + jobKey + ", FoundTriggerCount: " + triggers.size() + " to un-schedule");

        for(Trigger trigger : triggers) {
            scheduler.unscheduleJob(trigger.getKey());
            System.out.println("Disabled TriggerKey: " + trigger.getKey());
        }
        System.out.println("unscheduled all the triggers");
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
        System.out.println("Rescheduling JobKey: " + jobKey);
        System.out.println("New TriggerKey: " + cronTriggerFactoryBean.getObject().getKey().toString() + " to be rescheduled");
        scheduler.rescheduleJob(cronTriggerFactoryBean.getObject().getKey(), cronTriggerFactoryBean.getObject());
        System.out.println("rescheduled the triggers");
    }


    /**
     * This fetches all the scheduler objects stored in db and then tries to schedule them at startup
     */
    @Override
    public void startAllSchedulers() {
        List<SchedulerWrapper> schedulerWrapperList = schedulerWrapperRepository.findAll();
        System.out.println("JobCount to schedule at startup: " + schedulerWrapperList.size());

        if(!schedulerWrapperList.isEmpty()) {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            schedulerWrapperList.forEach(schedulerWrapper -> {
                try {
                    JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);
                    CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());

                    //Check if a schedule for the given jobKey exists, if not then create a new one
                    if(!scheduler.checkExists(new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup()))) {
                        scheduler.scheduleJob(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());
                    }

                    //If a new schedule was created but the active state was false - disable all the triggers
                    //do not delete the job, if the user wants job deleted - they will call delete rest api
                    if(!schedulerWrapper.getActive()) {
                        disableAllTriggersForAJob(scheduler, schedulerWrapper);
                    } else {
                        System.out.println("Job with id: " + schedulerWrapper.getId() + " scheduled at startup");
                        scheduler.getListenerManager().addTriggerListener(customTriggerListener);
                    }
                } catch (ClassNotFoundException | SchedulerException | InvalidCronExpression | ParseException e) {
                    System.out.println(e);
                }
            });
        }
    }

}
