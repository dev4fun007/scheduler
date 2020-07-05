package bytes.sync.scheduler;


import bytes.sync.errors.SchedulerObjectNotFound;
import bytes.sync.entity.SchedulerWrapper;
import bytes.sync.service.restapi.impl.APIServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SchedulerAPI {

    @Autowired
    private APIServiceImpl apiService;


    @GetMapping(path = "/schedulers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SchedulerWrapper>> getAllScheduledObjects(@RequestParam(name = "active", required = false) boolean active) {
        List<SchedulerWrapper> list = apiService.getAllSchedulerWrappers(active);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(path = "/scheduler/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchedulerWrapper> getScheduledObjectById(@PathVariable("id") String id) {
        try {
            SchedulerWrapper wrapper = apiService.getSchedulerWrapperById(id);
            return ResponseEntity.ok().body(wrapper);
        } catch (SchedulerObjectNotFound e) {
            throw e;
        }

    }

    @PostMapping(path = "/scheduler", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchedulerWrapper> createNewSchedulerObject(@RequestBody SchedulerWrapper schedulerWrapper) throws Exception {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(apiService.createNewSchedulerWrapper(schedulerWrapper));
        } catch (Exception e) {
            throw e;
        }
    }

    @PutMapping(path = "/scheduler/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchedulerWrapper> updateScheduledObjectById(@PathVariable("id") String id, @RequestBody SchedulerWrapper schedulerWrapper) {
        try {
            SchedulerWrapper wrapper = apiService.updateExistingSchedulerWrapper(id, schedulerWrapper);
            return ResponseEntity.ok().body(wrapper);
        } catch (Exception e) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
    }

    @DeleteMapping(path = "/scheduler/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchedulerWrapper> deleteSchedulerObjectById(@PathVariable("id") String id) {
        try {
            apiService.deleteSchedulerWrapperById(id);
        } catch (Exception e) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
        return ResponseEntity.noContent().build();
    }

}
