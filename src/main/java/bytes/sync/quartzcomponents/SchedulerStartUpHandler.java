package bytes.sync.quartzcomponents;

import bytes.sync.service.scheduler.impl.QuartzSchedulerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SchedulerStartUpHandler implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(SchedulerStartUpHandler.class);

    @Autowired
    private QuartzSchedulerServiceImpl schedulerService;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Schedule all new scheduler jobs at app startup - starting");
        try {
            schedulerService.startAllSchedulers();
            logger.info("Schedule all new scheduler jobs at app startup - complete");
        } catch (Exception ex) {
            logger.error("Schedule all new scheduler jobs at app startup - error");
        }
    }
}
