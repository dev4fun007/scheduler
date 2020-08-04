package bytes.sync.repository;

import bytes.sync.entity.ScheduledExecutionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledExecutionInfoRepository extends JpaRepository<ScheduledExecutionInfo, String> {
    @Override
    List<ScheduledExecutionInfo> findAll();
    List<ScheduledExecutionInfo> findByExecutionId(String executionId);
    List<ScheduledExecutionInfo> findByScheduleWrapperId(String scheduleWrapperId);
}
