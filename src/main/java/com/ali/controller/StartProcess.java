package com.ali.controller;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartProcess {

    @Autowired
    private RuntimeService runtimeService;

    @GetMapping("startprocess")
    public void startProcess(){

        /*List<String> assigneeList=new ArrayList<>(); //分配任务的人员
        assigneeList.add("tom");
        assigneeList.add("jeck");
        assigneeList.add("mary");
        Map<String, Object> vars = new HashMap<>(); //参数
        vars.put("assigneeList", assigneeList);*/


        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leavetest");
        String processInstanceId = processInstance.getId();
        String processInstanceName = processInstance.getName();
        //processInstance id:5001,name:null
        System.out.println("processInstance id:"+processInstanceId+",name:"+processInstanceName);
    }
}
