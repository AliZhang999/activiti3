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
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacation");
        String processInstanceId = processInstance.getId();
        String processInstanceName = processInstance.getName();
        //processInstance id:5001,name:null
        System.out.println("processInstance id:"+processInstanceId+",name:"+processInstanceName);
    }
}
