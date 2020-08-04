package bytes.sync.service.restapi.impl;

import bytes.sync.entity.ScheduledExecutionInfo;
import bytes.sync.entity.ScheduledExecutionInfoDTO;
import bytes.sync.repository.ScheduledExecutionInfoRepository;
import bytes.sync.service.restapi.contract.ExecutionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionInfoServiceImpl implements ExecutionInfoService {

    private Logger logger = LoggerFactory.getLogger(ExecutionInfoServiceImpl.class);

    @Autowired
    private ScheduledExecutionInfoRepository scheduledExecutionInfoRepository;

    @Override
    public ResponseEntity<ScheduledExecutionInfoDTO> getAllScheduledExecutionInfo() {
        ScheduledExecutionInfoDTO executionInfoDTO = new ScheduledExecutionInfoDTO();
        try {
            List<ScheduledExecutionInfo> scheduledExecutionInfoList = scheduledExecutionInfoRepository.findAll();
            executionInfoDTO.setScheduledExecutionInfoList(scheduledExecutionInfoList);
            executionInfoDTO.setCount(scheduledExecutionInfoList.size());
        } catch (Exception e) {
            logger.error("error fetching all the scheduledExecutionInfo objects: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(executionInfoDTO);
    }

}
