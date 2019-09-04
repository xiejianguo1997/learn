package com.billow.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.billow.base.workflow.component.WorkFlowExecute;
import com.billow.base.workflow.component.WorkFlowQuery;
import com.billow.base.workflow.vo.Page;
import com.billow.base.workflow.vo.ProcessInstanceVo;
import com.billow.system.dao.ApplyInfoDao;
import com.billow.system.feign.AdminUserFeign;
import com.billow.system.pojo.ex.UserEx;
import com.billow.system.pojo.po.ApplyInfoPo;
import com.billow.system.pojo.vo.ApplyInfoVo;
import com.billow.system.service.ApplyInfoService;
import com.billow.system.service.StartApplyProcess;
import com.billow.tools.enums.ApplyTypeEnum;
import com.billow.tools.enums.ResCodeEnum;
import com.billow.tools.resData.BaseResponse;
import com.billow.tools.utlis.ToolsUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申请信息服务
 *
 * @author liuyongtao
 * @create 2019-09-02 17:17
 */
@Slf4j
@Service
public class ApplyInfoServiceImpl implements ApplyInfoService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private Map<String, StartApplyProcess> startApplyProcessMap;
    @Autowired
    private WorkFlowExecute workFlowExecute;
    @Autowired
    private WorkFlowQuery workFlowQuery;
    @Autowired
    private ApplyInfoDao applyInfoDao;
    @Autowired
    private AdminUserFeign adminUserFeign;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void submitApplyInfo(String operator, ApplyTypeEnum applyTypeEnum, Object object) {
        // 保存申请数据
        ApplyInfoPo applyInfo = new ApplyInfoPo();
        applyInfo.setApplyData(JSONObject.toJSONString(object));
        applyInfo.setApplyType(applyTypeEnum.getApplyType());
        applyInfo.setIsEnd(false);
        applyInfo.setApplyUserCode(operator);
        applyInfo.setValidInd(true);
        applyInfo = applyInfoDao.save(applyInfo);
        // 启动相应流程
        String key = applyTypeEnum.getApplyType() + StartApplyProcess.class.getSimpleName();
        StartApplyProcess startApplyProcess = startApplyProcessMap.get(key);
        Map<String, Object> variables = new HashMap<>();
        // 启动前操作
        if (startApplyProcess != null) {
            startApplyProcess.startProcessBefore(variables, object);
        }
        // 启动流程
        ProcessInstanceVo processInstanceVo = workFlowExecute.startProcessInstance(operator,
                applyTypeEnum.getProcessKey(),
                applyInfo.getId().toString(),
                variables);

        // 更新申请信息
        applyInfo.setProcDefId(processInstanceVo.getProcessDefinitionId());
        applyInfo.setProcInstId(processInstanceVo.getProcessInstanceId());
        applyInfoDao.save(applyInfo);

        // 启动流程后操作
        if (startApplyProcess != null) {
            startApplyProcess.startProcessAfter(applyInfo);
        }
    }

    @Override
    public Page queryMyTaskList(ApplyInfoVo applyInfoVo, Integer offset, Integer pageSize) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append("r.assignee AS assignee, ");
        sql.append("r.groupId AS groupId, ");
        sql.append("r.taskId AS taskId, ");
        sql.append("r.taskName AS taskName, ");
        sql.append("r.claimStatus AS claimStatus, ");
        sql.append("r.id AS id, ");
//        sql.append("r.apply_data as applyData, ");
        sql.append("r.procDefId AS procDefId, ");
        sql.append("r.procInstId AS procInstId, ");
        sql.append("r.isEnd AS isEnd, ");
        sql.append("r.applyType AS applyType, ");
        sql.append("r.applyUserCode AS applyUserCode, ");
        sql.append("r.validInd AS validInd, ");
        sql.append("r.createTime AS createTime, ");
        sql.append("r.createCode AS createCode, ");
        sql.append("r.updateTime AS updateTime, ");
        sql.append("r.updateCode AS updateCode ");
        sql.append("FROM v_mytasklist r WHERE 1 =1 ");

        String assignee = applyInfoVo.getAssignee();
        if (ToolsUtils.isEmpty(assignee)) {
            log.error("assignee 不能为空");
            new RuntimeException("assignee 不能为空");
        }

        BaseResponse<UserEx> baseResponse = adminUserFeign.findUserInfoByUserCode(assignee);
        String resCode = baseResponse.getResCode();
        if (!resCode.equals(ResCodeEnum.OK)) {
            log.error("远程调用 AdminUser 系统异常");
            new RuntimeException("远程调用 AdminUser 系统异常");
        }

        StringBuilder sqlCondition = new StringBuilder();

        UserEx resData = baseResponse.getResData();
        if (resData != null && ToolsUtils.isNotEmpty(resData.getGroupId())) {
            sqlCondition.append("and r.assignee = '" + assignee + "' or r.groupId = '" + resData.getGroupId() + "'");
        } else {
            sqlCondition.append("and r.assignee = '" + assignee + "'");
        }

        sql.append(sqlCondition);
        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        nativeQuery.setFirstResult(offset);
        nativeQuery.setMaxResults(pageSize);
        nativeQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List<Map<String, Object>> resultList = nativeQuery.getResultList();

        StringBuilder sqlCount = new StringBuilder("SELECT count(1) from v_mytasklist r where 1=1 ");
        sqlCount.append(sqlCondition);
        nativeQuery = entityManager.createNativeQuery(sqlCount.toString());
        BigInteger o = (BigInteger) nativeQuery.getSingleResult();
        Page<Map<String, Object>> page = new Page<>(pageSize, o.longValue());
        page.setContent(resultList);
        return page;
    }
}
