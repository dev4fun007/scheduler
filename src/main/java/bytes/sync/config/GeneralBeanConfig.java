package bytes.sync.config;

import bytes.sync.quartzcomponents.CustomTriggerListener;
import bytes.sync.service.restapi.impl.APIServiceImpl;
import bytes.sync.service.scheduler.impl.QuartzSchedulerServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralBeanConfig {

    @Bean
    public APIServiceImpl apiService() {
        return new APIServiceImpl();
    }

}
