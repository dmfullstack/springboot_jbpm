package com.yczx.bee.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yczx.bee.forms.LeaveForm;
import com.yczx.bee.forms.TaskBean;
import com.yczx.bee.utils.JbpmUtils;

@Controller
public class MainController {
	@RequestMapping("apply.html")
	public String apply(LeaveForm leaveForm) {
		return "apply";
	}
	
	@RequestMapping("approve.html")
	public String approve(Model model) {
		
		QueryServicesClient queryClient = JbpmUtils.getInstance().getClient().getServicesClient(QueryServicesClient.class);
		
	    List<QueryDefinition> queryDefs = queryClient.getQueries(0, 10);
        System.out.println(queryDefs);
        
		
		TaskQueryFilterSpec filter  = new TaskQueryFilterSpec();
		QueryParam[] paramters  = new QueryParam[2];
		paramters[0] = new QueryParam();
		paramters[0].setColumn("name");
		paramters[0].setOperator("EQUALS_TO");
		List<String> nameValues  = new ArrayList<String>();
		nameValues.add("TaskManagerApprove");
		paramters[0].setValue(nameValues);

		paramters[1] = new QueryParam();
		paramters[1].setColumn("status");
		paramters[1].setOperator("EQUALS_TO");
		List<String> statusValues  = new ArrayList<String>();
		statusValues.add("Reserved");
		paramters[1].setValue(statusValues);
		filter.setParameters(paramters);
		filter.setAscending(false);
		filter.setOrderBy("createdOn");
		
		List<TaskInstance> taskInstances = queryClient .findHumanTasksWithFilters("jbpmHumanTasks", filter, 0, 1000);

		
		List<TaskBean> taskBeans  = new ArrayList<TaskBean>();
		
		for(TaskInstance taskInstance : taskInstances) {
			TaskBean taskBean  = new TaskBean();
			taskBean.setProcessName(taskInstance.getProcessId());
			taskBean.setTaskId(taskInstance.getId().toString());
			taskBean.setTaskName(taskInstance.getName());
			taskBeans.add(taskBean);
		}

		model.addAttribute("tasks", taskBeans);
		return "approve";
	}
	/**
	 * http://localhost:8080/kie-server/services/rest/server/containers/testAskLeave_1.0.0-SNAPSHOT
	 * @param leaveForm
	 * @return
	 */
	@RequestMapping("applySubmit.html")
	public String applySubmit(LeaveForm leaveForm) {
		leaveForm.setReason(leaveForm.getReason()+" ok");
		HashMap<String, Object> variables  = new HashMap<String, Object>();
		variables.put("managerApproveResult", Boolean.FALSE);
		
		String containerId = "testAskLeave_1.0.0-SNAPSHOT";
		String processId = "test.askforleave";
		ProcessServicesClient processClient = JbpmUtils.getInstance().getClient().getServicesClient(ProcessServicesClient.class);
		Long processInstanceId = processClient.startProcess(containerId, processId, variables);
		ProcessInstance  instance  =processClient.getProcessInstance(containerId, processInstanceId);
		TaskSummaryList taskSummaryList  = instance.getActiveUserTasks();
		UserTaskServicesClient userTaskClient = JbpmUtils.getInstance().getClient().getServicesClient(UserTaskServicesClient.class);
		TaskInstance mytask = null;
		TaskSummary[]  tasks  = taskSummaryList.getTasks();
		for(TaskSummary task  : tasks) {
			if(task.getName().equals("TaskCompose")){
				mytask = userTaskClient.getTaskInstance(containerId, task.getId());
				break;
			}
		}
		if(mytask!=null) {
			userTaskClient.startTask(containerId, mytask.getId(), "wbadmin");
			Map<String, Object> param =  new HashMap<String, Object>();
			userTaskClient.completeTask(containerId, mytask.getId(), "wbadmin",param);
		}
		
		
		return "apply";
	}
}
