package bytes.sync.service.restapi.contract;

import bytes.sync.entity.ScheduledExecutionInfo;

import java.util.List;


public interface ExecutionInfoService {

    List<ScheduledExecutionInfo> getAllScheduledExecutionInfo();

}
