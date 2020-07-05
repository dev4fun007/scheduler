package bytes.sync.jobs;

import kong.unirest.Unirest;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Random;


@Component
public class APICallJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("CronJob Start................");

        //Make api call to a different service
        String url = "https://ping-postgresql.herokuapp.com:8081/ping";
//        String url = "http://localhost:8081/ping";

        Random random = new Random();
        String jsonData = "{\"name\":\"ping_"+ random.nextInt(100) + "\"}";

        Unirest.post(url)
                .header("Content-Type", "application/json")
                .body(jsonData)
                .asEmpty();

        System.out.println("CronJob End, check a new ping object created in the ping micro-service");
    }

}
