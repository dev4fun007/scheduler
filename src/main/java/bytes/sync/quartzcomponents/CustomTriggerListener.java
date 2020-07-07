package bytes.sync.quartzcomponents;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.repository.SchedulerWrapperRepository;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomTriggerListener extends TriggerListenerSupport {
    private Logger logger = LoggerFactory.getLogger(CustomTriggerListener.class);
    public static final String TRIGGER_LISTENER_NAME = "CustomTriggerListener";

    @Autowired
    SchedulerWrapperRepository schedulerWrapperRepository;

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        List<SchedulerWrapper> schedulerWrapperList = schedulerWrapperRepository.findAllByJobNameAndJobGroup(context.getJobDetail().getKey().getName(),
                context.getJobDetail().getKey().getGroup());
        if(schedulerWrapperList != null && !schedulerWrapperList.isEmpty()) {
            schedulerWrapperList.forEach(schedulerWrapper -> {
                schedulerWrapper.setActivationExpression(context.getFireTime().toString());
                schedulerWrapperRepository.save(schedulerWrapper);
                logger.debug("CustomTriggerListener adding fire time to the activationExpression");
            });
        }
    }

    @Override
    public String getName() {
        return TRIGGER_LISTENER_NAME;
    }
}
