package bytes.sync.quartzcomponents;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.repository.SchedulerWrapperRepository;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CustomTriggerListener extends TriggerListenerSupport {

    private static final String TRIGGER_LISTENER_NAME = "CustomTriggerListener";

    @Autowired
    SchedulerWrapperRepository schedulerWrapperRepository;

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        List<SchedulerWrapper> schedulerWrapperList = schedulerWrapperRepository.findAllByJobNameAndJobGroup(context.getJobDetail().getKey().getName(),
                context.getJobDetail().getKey().getGroup());
        if(schedulerWrapperList != null && !schedulerWrapperList.isEmpty()) {
            schedulerWrapperList.forEach(schedulerWrapper -> {
                if(schedulerWrapper != null) {
                    schedulerWrapper.setActivationExpression(trigger.getFinalFireTime().toString());
                    schedulerWrapperRepository.save(schedulerWrapper);
                }
            });
        }
    }

    @Override
    public String getName() {
        return TRIGGER_LISTENER_NAME;
    }
}
