package bytes.sync.service.scheduler.contract;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.errors.InvalidCronExpression;
import org.quartz.SchedulerException;

public interface GenericSchedulerService {

    void scheduleNewJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression, SchedulerException;
    void updateScheduleJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression, SchedulerException;
    boolean deleteScheduledJob(SchedulerWrapper schedulerWrapper);

    void startAllSchedulers();
}
