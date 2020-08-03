package bytes.sync.repository;

import bytes.sync.entity.SchedulerWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulerWrapperRepository extends JpaRepository<SchedulerWrapper, String> {
    @Override
    List<SchedulerWrapper> findAll();
    List<SchedulerWrapper> findByActiveTrue();
    List<SchedulerWrapper> findByJobGroupAndJobName(String jobGroup, String jobName);
}
