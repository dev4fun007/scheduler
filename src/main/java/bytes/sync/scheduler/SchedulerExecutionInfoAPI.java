package bytes.sync.scheduler;


import bytes.sync.entity.ScheduledExecutionInfo;
import bytes.sync.entity.ScheduledExecutionInfoDTO;
import bytes.sync.service.restapi.impl.ExecutionInfoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SchedulerExecutionInfoAPI {

    private final Logger logger = LoggerFactory.getLogger(SchedulerExecutionInfoAPI.class);

    @Autowired
    private ExecutionInfoServiceImpl executionInfoServiceImpl;


    @GetMapping(path = "/executions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScheduledExecutionInfoDTO> getAllScheduledExecutionInfo() {
        List<ScheduledExecutionInfo> list = executionInfoServiceImpl.getAllScheduledExecutionInfo();
        ScheduledExecutionInfoDTO executionInfoDTO = new ScheduledExecutionInfoDTO();
        executionInfoDTO.setCount(list.size());
        executionInfoDTO.setScheduledExecutionInfoList(list);
        return ResponseEntity.ok().body(executionInfoDTO);
    }


}
