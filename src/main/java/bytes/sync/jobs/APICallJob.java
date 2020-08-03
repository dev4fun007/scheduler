package bytes.sync.jobs;

import bytes.sync.common.Constants;
import bytes.sync.entity.ActivationExpression;
import bytes.sync.jobs.postprocessing.JobPostProcessor;
import bytes.sync.jobs.restutil.APICallHelper;
import bytes.sync.jobs.restutil.RestOutputWrapper;
import kong.unirest.Unirest;
import org.quartz.*;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class APICallJob extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger(APICallJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Cron-Scheduled APICallJob Starting");

        //Extract url, method and payload info from the jobDataMap
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        String schedulerWrapperId = jobDataMap.getString(Constants.SCHEDULER_WRAPPER_ID_KEY);

        ActivationExpression activationExpression = getActivationExpressionFromJobDataMap(jobDataMap);
        logger.debug("ActivationExpression: {}, found for the current Job: {}", activationExpression.toString(), jobExecutionContext.getJobDetail().getKey().toString());

        APICallHelper apiCallHelper = new APICallHelper(activationExpression);
        RestOutputWrapper restOutputWrapper = apiCallHelper.executeRestCall();

        //Doing the post-process database entry
        JobPostProcessor jobPostProcessor = new JobPostProcessor();
        jobPostProcessor.process(schedulerWrapperId, jobExecutionContext.getJobDetail(), restOutputWrapper);

        logger.info("Cron-Scheduled APICallJob Executed");
    }

    private ActivationExpression getActivationExpressionFromJobDataMap(JobDataMap jobDataMap) {
        ActivationExpression activationExpression = new ActivationExpression();
        activationExpression.setMethod(jobDataMap.getString(Constants.ACTIVATION_EXPRESSION_METHOD_KEY));
        activationExpression.setPayload(jobDataMap.getString(Constants.ACTIVATION_EXPRESSION_PAYLOAD_KEY));
        activationExpression.setUrl(jobDataMap.getString(Constants.ACTIVATION_EXPRESSION_URL_KEY));
        return activationExpression;
    }

}
