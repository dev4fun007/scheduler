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


    @Override
    public void scheduleNewJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression {
        //This schedulerWrapper is active - create a new job for the same
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            //Create the job detail
            JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);

            //Create the trigger
            CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());

            //Schedule job
            scheduler.scheduleJob(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());

            //If the newly created schedulerWrapper object is not set to active
            //un-schedule it now
            if(!schedulerWrapper.getActive()) {
                scheduler.unscheduleJob(TriggerKey.triggerKey(cronTriggerFactoryBean.getObject().getKey().getName()));
                System.out.println("new job schedule created - inactive and unscheduled");
            } else {
                //Add the trigger listener as this object is scheduled for execution
                scheduler.getListenerManager().addTriggerListener(new CustomTriggerListener());
                System.out.println("new job schedule created - active and scheduled");
            }
        } catch (InvalidCronExpression e) {
          throw e;
        } catch (ClassNotFoundException | SchedulerException | ParseException e) {
            System.out.println(e);
        }
    }

    @Override
    public void updateScheduleJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            //Create the job detail
            JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);

            //Create the trigger
            CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());

            if(!schedulerWrapper.getActive()) {
                //This object is no longer set to be active
                //Un-Schedule the job
                if(scheduler.checkExists(new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup()))) {
                    //This job exists - should pause it
                    scheduler.unscheduleJob(TriggerKey.triggerKey(cronTriggerFactoryBean.getObject().getKey().getName()));
                    System.out.println("new job schedule updated - now inactive and unscheduled");
                }
            } else {
                //This object is now set to be active
                //Re-Schedule this job
                scheduler.scheduleJob(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());
                scheduler.getListenerManager().addTriggerListener(new CustomTriggerListener());
                System.out.println("new job schedule updated - now active and scheduled");
            }
        } catch (InvalidCronExpression e) {
          throw e;
        } catch (SchedulerException | ClassNotFoundException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean deleteScheduledJob(SchedulerWrapper schedulerWrapper) {
        try {
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

        if(!CronExpression.isValidExpression(schedulerWrapper.getCronExpression()))
            throw new InvalidCronExpression(schedulerWrapper.getCronExpression());
        cronTriggerFactoryBean.setCronExpression(schedulerWrapper.getCronExpression());

        cronTriggerFactoryBean.afterPropertiesSet();

        return cronTriggerFactoryBean;
    }


    @Override
    public void startAllSchedulers() {
        List<SchedulerWrapper> schedulerWrapperList = schedulerWrapperRepository.findAll();
        if(schedulerWrapperList != null && !schedulerWrapperList.isEmpty()) {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            schedulerWrapperList.forEach(schedulerWrapper -> {
                try {
                    JobDetailFactoryBean jobDetailFactoryBean = getJobDetailFactoryBean(schedulerWrapper);
                    CronTriggerFactoryBean cronTriggerFactoryBean = getCronTriggerFactoryBean(schedulerWrapper, jobDetailFactoryBean.getObject());
                    if(!scheduler.checkExists(new JobKey(schedulerWrapper.getJobName(), schedulerWrapper.getJobGroup()))) {
                        scheduler.scheduleJob(jobDetailFactoryBean.getObject(), cronTriggerFactoryBean.getObject());
                        if(!schedulerWrapper.getActive()) {
                            scheduler.unscheduleJob(TriggerKey.triggerKey(cronTriggerFactoryBean.getObject().getKey().getName()));
                            System.out.println("Job with id: " + schedulerWrapper.getId() + " inactive & unscheduled during startup");
                        } else {
                            System.out.println("Job with id: " + schedulerWrapper.getId() + " scheduled at startup");
                        }
                    }
                    scheduler.getListenerManager().addTriggerListener(new CustomTriggerListener());
                } catch (ClassNotFoundException | SchedulerException | InvalidCronExpression | ParseException e) {
                    System.out.println(e);
                }
            });
        }
    }

}
