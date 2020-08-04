package bytes.sync.config;

import bytes.sync.service.restapi.impl.APIServiceImpl;
import bytes.sync.service.restapi.impl.ExecutionInfoServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralBeanConfig {

    @Bean
    public APIServiceImpl apiService() {
        return new APIServiceImpl();
    }

    @Bean
    public ExecutionInfoServiceImpl executionInfoService() { return new ExecutionInfoServiceImpl(); }

}
