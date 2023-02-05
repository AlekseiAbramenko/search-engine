package main;

import main.model.Task;
import main.model.TaskRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping(
            value = "tasks",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<Task> post(@RequestBody Task requestTask) {
        Task task = new Task();
        task.setTitle(requestTask.getTitle());
        task.setDescription(requestTask.getDescription());
        task.setCreationTime(LocalDateTime.now());
        task.setDone(false);
        taskRepository.save(task);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("tasks")
    public Iterable<Task> getAll() {
        return taskRepository.findAll();
    }

    @GetMapping("tasks/{id}")
    public ResponseEntity<Task> getById(@PathVariable int id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        return optionalTask.map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PatchMapping(
            value = "tasks/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<Task> updateTask(
            @PathVariable(value = "id") int id,
            @RequestBody Task requestTask
            ) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if(!optionalTask.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            Task taskFromDb = optionalTask.get();
            BeanUtils.copyProperties(requestTask, taskFromDb, "id", "creationTime");
            return new ResponseEntity<>(taskRepository.save(taskFromDb), HttpStatus.OK);
        }
    }

    @PatchMapping("tasks/{id}/{done}")
    public ResponseEntity<Task> updateDone(@PathVariable int id, @PathVariable boolean done) {
        try{
            Task task = taskRepository.findById(id).get();
            task.setDone(done);
            return new ResponseEntity<>(taskRepository.save(task), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "tasks/{id}")
    public ResponseEntity<Task> delete(@PathVariable int id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if(!optionalTask.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        taskRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
