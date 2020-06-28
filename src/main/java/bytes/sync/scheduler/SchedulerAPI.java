package bytes.sync.scheduler;


import bytes.sync.errors.SchedulerObjectNotFound;
import bytes.sync.model.SchedulerWrapper;
import bytes.sync.repository.InMemoryCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SchedulerAPI {

    @Autowired
    private InMemoryCrudRepository crudRepository;

    @GetMapping(path = "/schedulers", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SchedulerWrapper> getAllScheduledObjects() {
        return crudRepository.findAllSchedulerObjects();
    }

    @GetMapping(path = "/scheduler/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SchedulerWrapper getScheduledObjectById(@PathVariable("id") String id) {
        SchedulerWrapper wrapper = crudRepository.findSchedulerObjectById(id);
        if(wrapper == null) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
        return wrapper;
    }

    @PostMapping(path = "/scheduler", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createNewSchedulerObject(@RequestBody SchedulerWrapper schedulerWrapper) {
        if(!crudRepository.saveNewSchedulerObject(schedulerWrapper)) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @PutMapping(path = "/scheduler/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateScheduledObjectById(@PathVariable("id") String id, @RequestBody SchedulerWrapper schedulerWrapper) {
        if(!crudRepository.updateSchedulerObjectById(id, schedulerWrapper)) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path = "/scheduler/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteSchedulerObjectById(@PathVariable("id") String id) {
        if(!crudRepository.deleteSchedulerObjectById(id)) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

}
