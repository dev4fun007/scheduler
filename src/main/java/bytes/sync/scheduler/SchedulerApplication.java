package bytes.sync.scheduler;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.PreDestroy;

@SpringBootApplication
@ComponentScan("bytes.sync")
@EntityScan("bytes.sync.entity")
@EnableJpaRepositories("bytes.sync.repository")
public class SchedulerApplication {

	@Autowired
	private SchedulerFactoryBean schedulerFactoryBean;

	public static void main(String[] args) {
		SpringApplication.run(SchedulerApplication.class, args);
	}

	@PreDestroy
	public void preDestroy() {
		try {
			schedulerFactoryBean.getScheduler().shutdown();
		} catch (SchedulerException e) {
			System.out.println(e);
		}
	}

}
