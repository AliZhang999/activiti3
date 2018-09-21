package com.ali.controller;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompleteUserTask {

    @Autowired
    private TaskService taskService;

    @GetMapping("completeusertask")
    public void completeUserTask(String processInstanceId,String username){
        //Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(username).singleResult();
        String taskId = task.getId();
        String taskName = task.getName();
        String taskAssignee = task.getAssignee();

        System.out.println("task id:"+taskId+",name:"+taskName+",assignee:"+taskAssignee);

        taskService.complete(taskId);
    }
}
