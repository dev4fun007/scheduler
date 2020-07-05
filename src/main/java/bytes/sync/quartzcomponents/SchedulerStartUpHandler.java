package bytes.sync.quartzcomponents;

import bytes.sync.service.scheduler.impl.QuartzSchedulerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SchedulerStartUpHandler implements ApplicationRunner {

    @Autowired
    private QuartzSchedulerServiceImpl schedulerService;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("Schedule all new scheduler jobs at app startup - starting");
        try {
            schedulerService.startAllSchedulers();
            System.out.println("Schedule all new scheduler jobs at app startup - complete");
        } catch (Exception ex) {
            System.out.println("Schedule all new scheduler jobs at app startup - error");
        }
    }
}
