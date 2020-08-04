package bytes.sync.jobs.postprocessing;

import bytes.sync.entity.ScheduledExecutionInfo;
import bytes.sync.jobs.restutil.RestOutputWrapper;
import bytes.sync.repository.ScheduledExecutionInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class JobPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobPostProcessor.class);

    @Autowired
    private ScheduledExecutionInfoRepository repository;

    public void process(String id, RestOutputWrapper restOutputWrapper) {
        if(repository == null) {
            logger.debug("ScheduledExecutionInfoRepository is null, check @Autowired not working");
            return;
        }

        ScheduledExecutionInfo scheduledExecutionInfo = new ScheduledExecutionInfo();
        scheduledExecutionInfo.setScheduleWrapperId(id);
        scheduledExecutionInfo.setResponseHttpStatus(restOutputWrapper.getHttpStatus());
        scheduledExecutionInfo.setResponseMessage(restOutputWrapper.getResponseMessage());
        scheduledExecutionInfo.setTimeToExecuteInMillis(restOutputWrapper.getTimeToExecuteInMillis());
        scheduledExecutionInfo.setExecutionTimestamp(Timestamp.valueOf(LocalDateTime.now()));

        //Save this info to the db
        try {
            ScheduledExecutionInfo response = repository.saveAndFlush(scheduledExecutionInfo);
            logger.debug("ScheduledExecutionInfoSaved with executionId: {}, for schedulerWrapperId: {}", response.getExecutionId(), id);
        } catch (Exception e) {
            logger.error("error saving executionInfo object for schedulerWrapperId: {}", id);
        }

    }

}
