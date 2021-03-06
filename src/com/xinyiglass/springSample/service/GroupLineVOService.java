package com.xinyiglass.springSample.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xinyiglass.springSample.dao.GroupLineVODao;
import com.xinyiglass.springSample.entity.GroupLineVO;

import xygdev.commons.entity.PlsqlRetValue;
import xygdev.commons.page.PagePub;
import xygdev.commons.springjdbc.DevJdbcSubProcess;

@Service
@Transactional(rollbackFor=Exception.class)//指定checked的异常Exception也要回滚！
public class GroupLineVOService {
    
	@Autowired
	PagePub pagePub;
	@Autowired
	GroupLineVODao groupDao;
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String findForPage(int pageSize,int pageNo,boolean goLastPage,Map<String,Object> conditionMap,Long loginId) throws Exception{
		String sql="SELECT A.*,XYG_ALD_COMMON_PKG.GET_LKM_BY_LKCODE('XYG_ALB2B_YN',A.ENABLED_FLAG) ENABLED "
				+ " FROM XYG_ALB2B_GROUP_LINES_V A "
				+ " WHERE GROUP_ID = :1 ORDER BY "+conditionMap.get("orderby");
		Map<String,Object> paramMap=new HashMap<String,Object>();
		paramMap.put("1", conditionMap.get("groupId"));
		return pagePub.qPageForJson(sql, paramMap, pageSize, pageNo, goLastPage);
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String findAutoAddSequence(Long groupId,Long loginId) throws Exception{
		Long Seq = groupDao.autoAddSequence(groupId);
		return "{\"rows\":[{\"MENU_SEQUENCE\":\""+Seq+"\"}]}";
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String findGroupLineForJSON(Long groupId,Long groupSeq,Long loginId) throws Exception{
		return "{\"rows\":"+groupDao.findGroupLineForJSON(groupId, groupSeq).toJsonStr()+"}";
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public GroupLineVO findForGroupVOById(Long groupId,Long groupSeq,Long loginId) throws Exception{
		return groupDao.findByGroupId(groupId, groupSeq);
	}
	
	public PlsqlRetValue insert(GroupLineVO g,Long loginId) throws Exception{
		PlsqlRetValue ret=groupDao.insert(g);
		if(ret.getRetcode()!=0){
			DevJdbcSubProcess.setRollbackOnly();//该事务必须要回滚！
		}
		return ret;
	}
	
	public PlsqlRetValue update(GroupLineVO lockGroupLineVO,GroupLineVO updateGroupVO,Long loginId) throws Exception
	{ 
		PlsqlRetValue ret=groupDao.lock(lockGroupLineVO);
		if(ret.getRetcode()==0){
			ret=groupDao.update(updateGroupVO);
		}else{
			DevJdbcSubProcess.setRollbackOnly();//该事务必须要回滚！
		}
		return ret;
	}
}
