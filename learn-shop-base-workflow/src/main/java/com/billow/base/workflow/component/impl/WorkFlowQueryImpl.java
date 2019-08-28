package com.billow.base.workflow.component.impl;

import com.billow.base.workflow.component.WorkFlowQuery;
import com.billow.base.workflow.diagram.ActUtils;
import com.billow.base.workflow.utils.PageUtils;
import com.billow.base.workflow.vo.DeploymentVo;
import com.billow.base.workflow.vo.Page;
import com.billow.base.workflow.vo.ProcessDefinitionVo;
import com.billow.base.workflow.vo.TaskVo;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 工作流查询操作
 *
 * @author liuyongtao
 * @create 2019-08-25 10:39
 */
@Component
public class WorkFlowQueryImpl implements WorkFlowQuery {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ActUtils actUtils;

    @Override
    public Page<Deployment> queryDeployment(DeploymentVo deploymentVo, int pageNo, int pageSize) {
        DeploymentQuery query = repositoryService.createDeploymentQuery();

        if (deploymentVo != null) {
            String category = deploymentVo.getCategory();
            if (category != null) {
                query.deploymentCategory(category);
            }
            String id = deploymentVo.getId();
            if (id != null) {
                query.deploymentId(category);
            }
            String key = deploymentVo.getKey();
            if (key != null) {
                query.deploymentKeyLike(key);
            }
            String name = deploymentVo.getName();
            if (name != null) {
                query.deploymentNameLike(name);
            }
        }

        List<Deployment> list = query.listPage(pageNo, pageSize);
        long count = query.count();

        Page<Deployment> page = new Page<>(pageSize, count, list);

        return page;
    }

    @Override
    public List<ProcessDefinition> queryProcessDefinition(ProcessDefinitionVo entity) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        // 构建查询条件
        this.genProcessDefCondition(entity, query);
        return query.list();
    }

    @Override
    public Page<ProcessDefinitionVo> queryProcessDefinition(ProcessDefinitionVo entity, Integer offset, Integer pageSize) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        // 构建查询条件
        this.genProcessDefCondition(entity, query);
        List<ProcessDefinition> list = query.listPage(offset, pageSize);
        return PageUtils.converListToPage(pageSize, query.count(), list, ProcessDefinitionVo.class);
    }

    /**
     * 构建 queryProcessDefinition 的查询条件
     *
     * @param entity
     * @param query
     * @return void
     * @author LiuYongTao
     * @date 2019/8/27 19:44
     */
    private void genProcessDefCondition(ProcessDefinitionVo entity, ProcessDefinitionQuery query) {
        query.orderByProcessDefinitionVersion().desc();

        String deploymentId = entity.getDeploymentId();
        if (deploymentId != null) {
            query.deploymentId(deploymentId);
        }
        String key = entity.getKey();
        if (key != null) {
            query.processDefinitionKey(key);
        }
        String id = entity.getId();
        if (id != null) {
            query.processDefinitionId(id);
        }
        String name = entity.getName();
        if (name != null) {
            query.processDefinitionNameLike("%" + name + "%");
        }
    }

    @Override
    public void getActivitiProccessImage(String executionId, HttpServletResponse response) throws Exception {
        logger.info("[开始]-获取流程图图像");
        InputStream in = actUtils.genActivitiProccessImage(executionId);
        if (in == null) {
            return;
        }
        OutputStream out = response.getOutputStream();
        // 把图片的输入流程写入resp的输出流中
        byte[] b = new byte[1024];
        for (int len; (len = in.read(b)) != -1; ) {
            out.write(b, 0, len);
        }
        // 关闭流
        out.close();
        in.close();
        logger.info("[完成]-获取流程图图像");
    }

    @Override
    public void genOriginalProcessImage(String deploymentId, HttpServletResponse response) throws Exception {
        logger.info("[开始]-获取流程图图像");
        InputStream in = actUtils.genOriginalProcessImage(deploymentId);
        if (in == null) {
            return;
        }
        OutputStream out = response.getOutputStream();
        // 把图片的输入流程写入resp的输出流中
        byte[] b = new byte[1024];
        for (int len; (len = in.read(b)) != -1; ) {
            out.write(b, 0, len);
        }
        // 关闭流
        out.close();
        in.close();
        logger.info("[完成]-获取流程图图像");
    }

    @Override
    public Page<Task> queryTaskList(TaskVo taskVo, int pageNo, int pageSize) {
        TaskQuery query = taskService.createTaskQuery();

        String id = taskVo.getId();
        if (id != null) {
            query.taskId(id);
        }

        String assignee = taskVo.getAssignee();
        if (assignee != null) {
            query.taskAssignee(assignee);
        }

        String owner = taskVo.getOwner();
        if (owner != null) {
            query.taskOwner(owner);
        }

        List<Task> tasks = query.listPage(pageNo, pageSize);
        long count = query.count();
        return new Page<>(pageSize, count, tasks);
    }
}
