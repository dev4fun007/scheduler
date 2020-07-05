package bytes.sync.service.scheduler.contract;

import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.errors.InvalidCronExpression;

public interface GenericSchedulerService {

    void scheduleNewJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression;
    void updateScheduleJob(SchedulerWrapper schedulerWrapper) throws InvalidCronExpression;
    boolean deleteScheduledJob(SchedulerWrapper schedulerWrapper);

    void startAllSchedulers();
}
