package bytes.sync.scheduler;

import bytes.sync.repository.InMemoryCrudRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationManager {

    @Bean
    public InMemoryCrudRepository inMemoryCrudRepository() {
        return new InMemoryCrudRepository();
    }

}
