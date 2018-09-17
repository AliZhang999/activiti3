package com.ali.controller;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
public class highlightProcessTracking {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @GetMapping("processtrack")
    public void processTacking(String processDefinitionId,String processInstanceId,HttpServletResponse response) throws Exception {
        response.setHeader("Pragma","No-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setDateHeader("Expires",0);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        if(!StringUtils.isEmpty(processInstanceId)){//流程实例不为空，需要对流程图做高亮处理
            getActivitiProccessImage(processInstanceId, response);
        }else{
            String name = processDefinition.getDiagramResourceName();
            System.out.println("deployment name:"+name);

            InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), name);
            byte[] b = new byte[1024];
            int len = -1;
            ServletOutputStream outputStream = response.getOutputStream();
            while ((len = resourceAsStream.read(b,0,1024)) != -1){
                outputStream.write(b,0,len);
            }
            outputStream.close();
            resourceAsStream.close();
        }
    }

    private void getActivitiProccessImage(String processInstanceId,HttpServletResponse response) throws IOException {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if(historicProcessInstance == null){
            System.out.println("获取流程对应的历史流程实例失败");
        }else{
            ProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());
            List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceId().asc().list();
            List<String> activitedIdList = new ArrayList<>();
            int idx = 1;
            for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                activitedIdList.add(activityInstance.getActivityId());
                idx++;
            }
            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
            List<String> flowIdList = getHighlightFlows(bpmnModel,processDefinition,historicActivityInstanceList);

            ProcessDiagramGenerator processDiagramGenerator = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
            InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", activitedIdList, flowIdList, "宋体", "宋体", "宋体", null, 2.0);
            response.setContentType("image/png");
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] b = new byte[8192];
            int len = 0;
            while ((len = imageStream.read(b,0,8192)) != -1){
                outputStream.write(b,0,len);
            }
            outputStream.close();
            imageStream.close();
        }
    }

    private List<String> getHighlightFlows(BpmnModel bpmnModel,ProcessDefinition processDefinition,List<HistoricActivityInstance> historicActivityInstanceList){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> highFlows = new ArrayList<>();

        for(int i=0;i<historicActivityInstanceList.size();i++){
            HistoricActivityInstance historicActivityInstancei = historicActivityInstanceList.get(i);

            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstancei.getActivityId());

            List<FlowNode> sameStartTimeNodes = new ArrayList<>();
            FlowNode flowNode1 = null;

            HistoricActivityInstance historicActivityInstancej;

            for(int j = i+1; j<historicActivityInstanceList.size()-1;j++){
                historicActivityInstancej = historicActivityInstanceList.get(j);
                if(historicActivityInstancei.getActivityType().equals("userTask") && historicActivityInstancej.getActivityType().equals("userTask") && dateFormat.format(historicActivityInstancei.getStartTime()).equals(dateFormat.format(historicActivityInstancej.getStartTime()))){

                }else{
                    flowNode1 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstancej.getActivityId());
                    break;
                }
            }
            sameStartTimeNodes.add(flowNode1);

            for(int k=i+1;k<historicActivityInstanceList.size()-1;k++){
                HistoricActivityInstance historicActivityInstancea = historicActivityInstanceList.get(k);
                HistoricActivityInstance historicActivityInstanceb = historicActivityInstanceList.get(k+1);
                if(dateFormat.format(historicActivityInstancea.getStartTime()).equals(dateFormat.format(historicActivityInstanceb.getStartTime()))){
                    FlowNode flowNode2= (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstanceb.getActivityId());
                    sameStartTimeNodes.add(flowNode2);
                }else{
                    break;
                }
            }

            List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();
            for (SequenceFlow sequenceFlow : outgoingFlows) {
                FlowNode flowNode3= (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef());
                if(sameStartTimeNodes.contains(flowNode3)){
                    highFlows.add(flowNode3.getId());
                }
            }
        }
        return highFlows;
    }
}
