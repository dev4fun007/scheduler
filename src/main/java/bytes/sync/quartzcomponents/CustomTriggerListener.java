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
    private final Logger logger = LoggerFactory.getLogger(CustomTriggerListener.class);
    public static final String CUSTOM_TRIGGER_NAME = "QUARTZ_CUSTOM_TRIGGER_LISTENER";

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        logger.debug("Fired TriggerKey: {}", trigger.getKey().toString());
        logger.debug("NextFireTime: {} ", trigger.getNextFireTime().toString());
    }

    @Override
    public String getName() {
        return CUSTOM_TRIGGER_NAME;
    }
}
