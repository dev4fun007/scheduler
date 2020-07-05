package bytes.sync.quartzcomponents;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import java.text.ParseException;

public class PersistCronTriggerFactoryBean extends CronTriggerFactoryBean {

    public static final String JOB_DETAIL_KEY = "jobDetail";

    @Override
    public void afterPropertiesSet() throws ParseException {
        super.afterPropertiesSet();
        //Remove jobDetail key to prevent persist related errors
        getJobDataMap().remove(JOB_DETAIL_KEY);
    }
}
