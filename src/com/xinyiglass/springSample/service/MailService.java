package com.xinyiglass.springSample.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xinyiglass.springSample.dao.MailDao;

import xygdev.commons.entity.PlsqlRetValue;
import xygdev.commons.page.PagePub;
import xygdev.commons.springjdbc.DevJdbcSubProcess;
import xygdev.commons.sqlStmt.SqlStmtPub;

@Service
@Transactional(rollbackFor=Exception.class)//指定checked的异常Exception也要回滚！
public class MailService {
	
	@Autowired
	PagePub pagePub;
	@Autowired
	MailDao mDao;
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String findForRecMail(int pageSize,int pageNo,boolean goLastPage,Map<String,Object> conditionMap,Long loginId) throws Exception{
		Map<String,Object> paramMap=new HashMap<String,Object>();
		StringBuffer sqlBuf=new StringBuffer();
		sqlBuf.append("select * FROM XYG_ALB2B_RECEIVE_V A WHERE RECEIVE_USER_ID = :1");
		sqlBuf.append(SqlStmtPub.getAndStmt("SEND_USER_ID",conditionMap.get("sendUserId"),paramMap));
		sqlBuf.append(SqlStmtPub.getAndStmt("SEND_ID",conditionMap.get("sendId"),paramMap));
		sqlBuf.append(SqlStmtPub.getAndStmt("MAIL_TITLE",conditionMap.get("mailTitle"),paramMap));
		sqlBuf.append(SqlStmtPub.getAndStmt("SEND_DATE",conditionMap.get("sendDate_F"), conditionMap.get("sendDate_T"),paramMap));
		sqlBuf.append(SqlStmtPub.getAndStmt("READ_DATE",conditionMap.get("readDate_F"), conditionMap.get("readDate_T"),paramMap));
		paramMap.put("1", conditionMap.get("userId"));
		sqlBuf.append(" ORDER BY "+conditionMap.get("orderBy"));
		return pagePub.qPageForJson(sqlBuf.toString(), paramMap, pageSize, pageNo, goLastPage);
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String findForSendMail(int pageSize,int pageNo,boolean goLastPage,Map<String,Object> conditionMap,Long loginId) throws Exception{
		Map<String,Object> paramMap=new HashMap<String,Object>();
		StringBuffer sqlBuf=new StringBuffer();
		sqlBuf.append("SELECT SEND_ID,");
		sqlBuf.append("       SEND_TITLE,");
		sqlBuf.append("       SEND_DATE,");
		sqlBuf.append("       CASE WHEN XYG_ALD_STRING_PKG.GET_CHAR_CONTENT_COUNT(RECEIVE_USER_NAME_LIST,',')>2 THEN");
		sqlBuf.append("       XYG_ALD_STRING_PKG.GET_POSCONT_INTERVAL (RECEIVE_USER_NAME_LIST,',',1,3)||'...'");
		sqlBuf.append("       ELSE");
		sqlBuf.append("       RECEIVE_USER_NAME_LIST");
		sqlBuf.append("       END REC_USER_NAME");
		sqlBuf.append("  FROM XYG_ALB2B_SEND_V");
		sqlBuf.append(" WHERE SEND_USER_ID = :1");
		sqlBuf.append(SqlStmtPub.getAndStmt("SEND_TITLE",conditionMap.get("sendTitle"),paramMap));
		sqlBuf.append(SqlStmtPub.getAndStmt("SEND_DATE",conditionMap.get("sendDate_F"), conditionMap.get("sendDate_T"),paramMap));
		paramMap.put("1", conditionMap.get("userId"));
		sqlBuf.append(" ORDER BY "+conditionMap.get("orderBy"));
		return pagePub.qPageForJson(sqlBuf.toString(), paramMap, pageSize, pageNo, goLastPage);
	}
	
	public PlsqlRetValue delRecMail(Long recid,Long loginId) throws Exception{
		PlsqlRetValue ret=mDao.deleteRecMail(recid);
		if(ret.getRetcode()!=0){
			DevJdbcSubProcess.setRollbackOnly();//该事务必须要回滚！
		}
		return ret;
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public PlsqlRetValue updateRecMail(Long recid,Long loginId) throws Exception
	{ 
		PlsqlRetValue  ret=mDao.updateRecMail(recid);
		if(ret.getRetcode()!=0){
			DevJdbcSubProcess.setRollbackOnly();//该事务必须要回滚！
		}
		return ret;
	}
	
	public PlsqlRetValue insertSendMail(Long sendUserId,String title,String content,String sendType,String recUser,Long loginId) throws Exception{
		PlsqlRetValue  ret=mDao.insertMail(sendUserId, title, content, sendType, recUser);
		//DevJdbcSubProcess.setRollbackOnly();
		if(ret.getRetcode()!=0){
			DevJdbcSubProcess.setRollbackOnly();//该事务必须要回滚！
		}
		return ret;
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String findRecMailByIdForJson(Long sendid,Long loginId) throws Exception{
		return "{\"rows\":"+mDao.findRecMailByIdForRS(sendid).toJsonStr()+"}";
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String findSendMailByIdForJson(Long sendid,Long loginId) throws Exception{
		return "{\"rows\":"+mDao.findSendMailByIdForRS(sendid).toJsonStr()+"}";
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String countUnReadMail(Long recid,Long loginId) throws Exception{
		String sql = "SELECT COUNT(*) COUNT FROM XYG_ALB2B_RECEIVE WHERE RECEIVE_USER_ID = :1 AND READ_DATE IS NULL";
		Map<String,Object> paramMap=new  HashMap<String,Object>();
		paramMap.put("1", recid);
		return pagePub.qSqlForJson(sql, paramMap);
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public String findUnReadMailForJson(Long recid,Long loginId) throws Exception{
		String sql = "SELECT R.* FROM (SELECT ROWNUM RN, INNER_Q.* FROM (  SELECT REC.* FROM XYG_ALB2B_RECEIVE_V REC WHERE REC.RECEIVE_USER_ID = :1 AND READ_DATE IS NULL ORDER BY REC.SEND_DATE DESC) INNER_Q) R WHERE R.RN <= 3";
		Map<String,Object> paramMap=new  HashMap<String,Object>();
		paramMap.put("1", recid);
		return pagePub.qSqlForJson(sql, paramMap);
	}
	

}
