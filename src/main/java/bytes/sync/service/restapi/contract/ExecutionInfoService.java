package bytes.sync.service.restapi.contract;

import bytes.sync.entity.ScheduledExecutionInfoDTO;
import org.springframework.http.ResponseEntity;


public interface ExecutionInfoService {

    ResponseEntity<ScheduledExecutionInfoDTO> getAllScheduledExecutionInfo();

}
