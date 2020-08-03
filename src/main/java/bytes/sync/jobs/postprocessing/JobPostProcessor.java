package bytes.sync.jobs.postprocessing;

import bytes.sync.jobs.restutil.RestOutputWrapper;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobPostProcessor.class);

    public void process(String id, JobDetail jobDetail, RestOutputWrapper restOutputWrapper) {
        //Find schedulerWrapper based on the jobGroup and jobName, should be just one entry as their combination will be unique
        logger.debug("ID: {}", id);
    }

}
