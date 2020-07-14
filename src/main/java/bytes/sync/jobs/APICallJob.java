package bytes.sync.jobs;

import kong.unirest.Unirest;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Component
public class APICallJob extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger(APICallJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Cron-Scheduled APICallJob Execution Started:");
/*        logger.info("TriggerKey: {}", jobExecutionContext.getTrigger().getKey().toString());
        logger.info("JobKey: {}", jobExecutionContext.getJobDetail().getKey());
        logger.info("FireTime: {}", jobExecutionContext.getFireTime());
        try {
            List<String> triggerGroups = jobExecutionContext.getScheduler().getTriggerGroupNames();
            logger.info("TriggerGroups: {}", triggerGroups);
            for(String key:triggerGroups) {
                Set<TriggerKey> triggerKeySet = jobExecutionContext.getScheduler().getTriggerKeys(GroupMatcher.triggerGroupEquals(key));
                for(TriggerKey triggerKey:triggerKeySet) {
                    logger.info("TriggerState: {}", jobExecutionContext.getScheduler().getTriggerState(triggerKey));
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }*/
    }

}
