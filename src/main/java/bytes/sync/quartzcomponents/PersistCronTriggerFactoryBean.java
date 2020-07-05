package bytes.sync.quartzcomponents;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import java.text.ParseException;


/**
 * This class was created to workaround a spring boot error where
 * on setting spring.quartz.properties.org.quartz.jobStore.useProperties=true property to true
 * results in serialization of jobDetail object.
 * This is not possible as jobDetail object is not serializable, thus the below workaround
 * is used by removing the jobDetail key from the jobDataMap
 */
public class PersistCronTriggerFactoryBean extends CronTriggerFactoryBean {

    public static final String JOB_DETAIL_KEY = "jobDetail";

    @Override
    public void afterPropertiesSet() throws ParseException {
        super.afterPropertiesSet();
        //Remove jobDetail key to prevent persist related errors
        getJobDataMap().remove(JOB_DETAIL_KEY);
    }
}
