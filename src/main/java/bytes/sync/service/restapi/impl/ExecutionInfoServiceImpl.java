package bytes.sync.service.restapi.impl;

import bytes.sync.entity.ScheduledExecutionInfo;
import bytes.sync.repository.ScheduledExecutionInfoRepository;
import bytes.sync.service.restapi.contract.ExecutionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class ExecutionInfoServiceImpl implements ExecutionInfoService {

    private Logger logger = LoggerFactory.getLogger(ExecutionInfoServiceImpl.class);

    @Autowired
    private ScheduledExecutionInfoRepository scheduledExecutionInfoRepository;

    @Override
    public List<ScheduledExecutionInfo> getAllScheduledExecutionInfo() {
        List<ScheduledExecutionInfo> scheduledExecutionInfoList = new LinkedList<>();
        try {
            scheduledExecutionInfoList = scheduledExecutionInfoRepository.findAll();
        } catch (Exception e) {
            logger.error("error fetching all the scheduledExecutionInfo objects: {}", e.getMessage());
        }
        return scheduledExecutionInfoList;
    }

}
