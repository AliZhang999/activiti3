package com.ali.controller;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeployModel {

    @Autowired
    private RepositoryService repositoryService;

    @GetMapping("deploymodel")
    public void deployByModel() throws Exception {
        /*
        Model model = repositoryService.createModelQuery().modelName("vacation").singleResult();

        ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(model.getId()));

        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);

        byte[] bytes = new BpmnXMLConverter().convertToXML(bpmnModel, "UTF-8");

        String resourceName = model.getName() + ".bpmn20.xml";

        Deployment deploy = repositoryService.createDeployment().name(model.getName()).addString(resourceName,new String(bytes,"UTF-8")).deploy();
        */
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("processes/MutilInstanceUserTaskTestDemo.bpmn20.xml").deploy();
        String id = deploy.getId();
        String name = deploy.getName();
        String key = deploy.getKey();
        //deploy model id:2501,name:vacation,key:null
        System.out.println("deploy model id:"+id+",name:"+name+",key:"+key);

        //部署完成后，就生成一个流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leavetest").singleResult();
        String processDefinitionId = processDefinition.getId();
        String processDefinitionKey = processDefinition.getKey();
        String processDefinitionName = processDefinition.getName();
        String processDefinitionDeploymentId = processDefinition.getDeploymentId();
        System.out.println("processDefinition id:"+processDefinitionId+",key:"+processDefinitionKey+",name:"+processDefinitionName+",deployment id:"+processDefinitionDeploymentId);
    }
}
