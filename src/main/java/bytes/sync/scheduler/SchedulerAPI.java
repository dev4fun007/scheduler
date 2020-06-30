package bytes.sync.scheduler;


import bytes.sync.repository.SchedulerWrapperRepository;
import bytes.sync.errors.SchedulerObjectNotFound;
import bytes.sync.domain.SchedulerWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class SchedulerAPI {

    @Autowired
    private SchedulerWrapperRepository repository;

    @GetMapping(path = "/schedulers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SchedulerWrapper>> getAllScheduledObjects(@RequestParam(name = "active", required = false) boolean active) {
        try {
            List<SchedulerWrapper> list = null;
            if(!active) {
                list = repository.findAll();
            } else {
                list = repository.findByActiveTrue();
            }
            return ResponseEntity.ok().body(list);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping(path = "/scheduler/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchedulerWrapper> getScheduledObjectById(@PathVariable("id") String id) {
        Optional<SchedulerWrapper> wrapper = repository.findById(id);
        if(!wrapper.isPresent()) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
        return ResponseEntity.ok().body(wrapper.get());
    }

    @PostMapping(path = "/scheduler", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchedulerWrapper> createNewSchedulerObject(@RequestBody SchedulerWrapper schedulerWrapper) {
        try {
            SchedulerWrapper wrapper = repository.save(schedulerWrapper);
            return ResponseEntity.ok().body(wrapper);
        } catch (Exception e) {
            throw e;
        }
    }

    @PutMapping(path = "/scheduler/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchedulerWrapper> updateScheduledObjectById(@PathVariable("id") String id, @RequestBody SchedulerWrapper schedulerWrapper) {
        try {
            schedulerWrapper.setId(id);
            SchedulerWrapper wrapper = repository.save(schedulerWrapper);
            return ResponseEntity.ok().body(wrapper);
        } catch (Exception e) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
    }

    @DeleteMapping(path = "/scheduler/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchedulerWrapper> deleteSchedulerObjectById(@PathVariable("id") String id) {
        try {
            repository.deleteById(id);
        } catch (Exception e) {
            throw new SchedulerObjectNotFound("id: " + id);
        }
        return ResponseEntity.noContent().build();
    }

}
