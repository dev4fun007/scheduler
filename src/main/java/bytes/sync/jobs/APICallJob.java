package bytes.sync.jobs;

import kong.unirest.Unirest;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Random;


@Component
public class APICallJob extends QuartzJobBean {

    private Logger logger = LoggerFactory.getLogger(APICallJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Cron-Scheduled APICallJob Executing");

        //Make api call to a different service
        String url = "https://ping-postgresql.herokuapp.com/ping";
//        String url = "http://localhost:8081/ping";

        Random random = new Random();
        String jsonData = "{\"name\":\"ping_"+ random.nextInt(100) + "\"}";

        Unirest.post(url)
                .header("Content-Type", "application/json")
                .body(jsonData)
                .asEmpty();

        logger.info("Cron-Scheduled APICallJob Execution Ends");
        logger.info("Check a new ping object created in the ping micro-service");
    }

}
