package com.xinyiglass.springSample.controller;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import xygdev.commons.util.TypeConvert;

import com.xinyiglass.springSample.entity.PoHeaderVO;
import com.xinyiglass.springSample.entity.PoLineVO;
import com.xinyiglass.springSample.service.PoHeaderVOService;
import com.xinyiglass.springSample.service.PoLineVOService;

@Controller
@RequestMapping("/po")
public class PoController {
	@Autowired
	PoHeaderVOService phvs;
	@Autowired
	PoLineVOService plvs;
	
	protected HttpServletRequest req; 
    protected HttpServletResponse res; 
    protected HttpSession sess; 
    
    @ModelAttribute 
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{ 
        this.req = request; 
        this.res = response; 
        this.sess = request.getSession(); 
        req.setCharacterEncoding("utf-8");
		res.setCharacterEncoding("utf-8");
		res.setContentType("text/html;charset=utf-8");  
		phvs.setSess(sess);
		plvs.setSess(sess);
    }
    
    @RequestMapping("/poOnline.do")
	public String listPoHeaderVO(){
		return "po/poOnline";
	}
    
    @RequestMapping("/getPoPage.do")
    public void getPoPage() throws Exception
	{   	
		int pageSize=Integer.parseInt(req.getParameter("pageSize"));
		int pageNo=Integer.parseInt(req.getParameter("pageNo"));
		boolean goLastPage=Boolean.parseBoolean(req.getParameter("goLastPage"));
		String orderBy=req.getParameter("orderby");
		Long userId = (Long)sess.getAttribute("USER_ID");
		String custContractNumber = req.getParameter("CUSTOMER_CONTRACT_NUMBER");
		String poNumber = req.getParameter("PO_NUMBER");
		String status = req.getParameter("STATUS");
		Long custId = TypeConvert.str2Long(req.getParameter("CUSTOMER_ID"));
		res.getWriter().print(phvs.findForPage(pageSize, pageNo, goLastPage, userId, poNumber, custContractNumber, status, custId, orderBy));
	} 
    
    @RequestMapping("/preUpdatePoHeader.do")
    public void preUpdatePoHeader() throws Exception
    {
    	Long poHeaderId = TypeConvert.str2Long(req.getParameter("PO_HEADER_ID"));
    	PoHeaderVO phVO = phvs.findForPoHeaderVOById(poHeaderId);
    	sess.setAttribute("lockPoHeaderVO", phVO);
    	res.getWriter().print(phvs.findPoHeaderByIdForJSON(poHeaderId));
    }
    
    @RequestMapping(value = "/insertPoHeader.do", method = RequestMethod.POST)
	public void insertPoHeader() throws Exception
	{ 
    	PoHeaderVO ph = new PoHeaderVO();
    	Long userId = (Long)sess.getAttribute("USER_ID");
    	Long funcId = (Long)sess.getAttribute("FUNC_ID");
    	ph.setPoNumber(req.getParameter("PO_NUMBER"));
    	ph.setCustomerContractNumber(req.getParameter("CUSTOMER_CONTRACT_NUMBER"));
    	ph.setCurrCode(req.getParameter("CURR_CODE"));
    	ph.setCustomerId(TypeConvert.str2Long(req.getParameter("CUSTOMER_ID")));
    	ph.setSalesOrgId(TypeConvert.str2Long(req.getParameter("SALES_ORG_ID")));
    	ph.setShipFromOrgId(TypeConvert.str2Long(req.getParameter("ORGANIZATION_ID")));
    	ph.setStatus("INPUT");
    	ph.setRemark(req.getParameter("REMARK")); 	
    	ph.setCreatedBy(userId);
    	res.getWriter().print(phvs.insert(ph, funcId).toJsonStr());	
	}
    
    @RequestMapping(value = "/deletePoHeader.do", method = RequestMethod.POST)
	public void deletePoHeader() throws Exception
	{ 
    	Long poHeaderId = TypeConvert.str2Long(req.getParameter("PO_HEADER_ID"));
    	res.getWriter().print(phvs.delete(poHeaderId).toJsonStr());
	}
    
    @RequestMapping(value = "/updatePoHeader.do", method = RequestMethod.POST)
   	public void updatePoHeader() throws Exception
   	{
    	Long poHeaderId = TypeConvert.str2Long(req.getParameter("PO_HEADER_ID"));
       	PoHeaderVO lockPoHeaderVO = (PoHeaderVO)sess.getAttribute("lockPoHeaderVO");
       	if (lockPoHeaderVO ==null){
   			throw new RuntimeException("更新数据出错:会话数据已经无效!请返回再重新操作!");
   		}
   		if (!poHeaderId.equals(lockPoHeaderVO.getPoHeaderId())){
   			throw new RuntimeException("更新的数据无法匹配!请重新查询!");
   		}
   		PoHeaderVO ph = new PoHeaderVO();
   		Long userId = (Long)sess.getAttribute("USER_ID");
    	Long funcId = (Long)sess.getAttribute("FUNC_ID");
    	ph.setPoHeaderId(TypeConvert.str2Long(req.getParameter("PO_HEADER_ID")));
   		ph.setPoNumber(req.getParameter("PO_NUMBER"));
    	ph.setCustomerContractNumber(req.getParameter("CUSTOMER_CONTRACT_NUMBER"));
    	ph.setCurrCode(req.getParameter("CURR_CODE"));
    	ph.setCustomerId(TypeConvert.str2Long(req.getParameter("CUSTOMER_ID")));
    	ph.setSalesOrgId(TypeConvert.str2Long(req.getParameter("SALES_ORG_ID")));
    	ph.setStatus(req.getParameter("STATUS"));
    	ph.setRemark(req.getParameter("REMARK")); 	
       	res.getWriter().print(phvs.update(lockPoHeaderVO, ph, userId, funcId).toJsonStr());
   	}
    
    @RequestMapping("/getPoLine.do")
    public void getPoLine() throws Exception
	{   	
		int pageSize=Integer.parseInt(req.getParameter("pageSize"));
		int pageNo=Integer.parseInt(req.getParameter("pageNo"));
		boolean goLastPage=Boolean.parseBoolean(req.getParameter("goLastPage"));
		String orderBy=req.getParameter("orderby");
		Long poHeaderId = TypeConvert.str2Long(req.getParameter("PO_HEADER_ID"));
		res.getWriter().print(plvs.findForPage(pageSize, pageNo, goLastPage, orderBy,poHeaderId));
	} 
    
    @RequestMapping("/getAutoAddSeq.do")
    public void getAutoAddSeq() throws Exception
	{
    	Long poHeaderId = Long.parseLong(req.getParameter("PO_HEADER_ID"));
    	res.getWriter().print(plvs.findAutoAddSequence(poHeaderId));
	}
     
    @RequestMapping(value = "/insertPoLine.do", method = RequestMethod.POST)
	public void insertPoLine() throws Exception
	{ 
    	PoLineVO pl = new PoLineVO();
    	Long userId = (Long)sess.getAttribute("USER_ID");
    	Long funcId = (Long)sess.getAttribute("FUNC_ID");
    	pl.setPoHeaderId(TypeConvert.str2Long(req.getParameter("PO_HEADER_ID")));
    	pl.setShipFromOrgId(TypeConvert.str2Long(req.getParameter("SHIP_FROM_ORG_ID")));
    	pl.setLineNum(TypeConvert.str2Long(req.getParameter("LINE_NUM")));
    	pl.setThickness(TypeConvert.str2Long(req.getParameter("THICKNESS")));
    	pl.setCoatingType(req.getParameter("COATING_TYPE"));
    	pl.setInventoryItemId(TypeConvert.str2Long(req.getParameter("INVENTORY_ITEM_ID")));
    	pl.setWidth(TypeConvert.str2Long(req.getParameter("WIDTH")));
    	pl.setHeight(TypeConvert.str2Long(req.getParameter("HEIGHT")));
    	pl.setPieQuantity(TypeConvert.str2Long(req.getParameter("PIE_QUANTITY")));
    	pl.setStatus(req.getParameter("STATUS"));
    	pl.setRemark(req.getParameter("REMARK"));
    	pl.setCreatedBy(userId);
    	res.getWriter().print(plvs.insert(pl, funcId).toJsonStr());
	}
    
    @RequestMapping(value = "/deletePoLine.do", method = RequestMethod.POST)
	public void deletePoLine() throws Exception
	{ 
    	Long poLineId = TypeConvert.str2Long(req.getParameter("PO_LINE_ID"));
    	res.getWriter().print(plvs.delete(poLineId).toJsonStr());
	}
    
    @RequestMapping("/preUpdatePoLine.do")
    public void preUpdatePoLine() throws Exception
    {
    	Long poLineId = TypeConvert.str2Long(req.getParameter("PO_LINE_ID"));
    	PoLineVO plVO = plvs.findForPoLineVOById(poLineId);
    	sess.setAttribute("lockPoLineVO", plVO);
    	res.getWriter().print(plvs.findPoLineByIdForJSON(poLineId));
    }
    
    @RequestMapping(value = "/updatePoLine.do", method = RequestMethod.POST)
   	public void updatePoLine() throws Exception
   	{
    	Long poLineId = TypeConvert.str2Long(req.getParameter("PO_LINE_ID"));
       	PoLineVO lockPoLineVO = (PoLineVO)sess.getAttribute("lockPoLineVO");
       	if (lockPoLineVO ==null){
   			throw new RuntimeException("更新数据出错:会话数据已经无效!请返回再重新操作!");
   		}
   		if (!poLineId.equals(lockPoLineVO.getPoLineId())){
   			throw new RuntimeException("更新的数据无法匹配!请重新查询!");
   		}
   		PoLineVO pl = new PoLineVO();
    	Long userId = (Long)sess.getAttribute("USER_ID");
    	Long funcId = (Long)sess.getAttribute("FUNC_ID");
    	String userType = (String)sess.getAttribute("USER_TYPE");
    	pl.setPoLineId(poLineId);
    	pl.setPoHeaderId(TypeConvert.str2Long(req.getParameter("PO_HEADER_ID")));
    	pl.setShipFromOrgId(TypeConvert.str2Long(req.getParameter("SHIP_FROM_ORG_ID")));
    	pl.setLineNum(TypeConvert.str2Long(req.getParameter("LINE_NUM")));
    	pl.setThickness(TypeConvert.str2Long(req.getParameter("THICKNESS")));
    	pl.setCoatingType(req.getParameter("COATING_TYPE"));
    	pl.setInventoryItemId(TypeConvert.str2Long(req.getParameter("INVENTORY_ITEM_ID")));
    	pl.setWidth(TypeConvert.str2Long(req.getParameter("WIDTH")));
    	pl.setHeight(TypeConvert.str2Long(req.getParameter("HEIGHT")));
    	pl.setPieQuantity(TypeConvert.str2Long(req.getParameter("PIE_QUANTITY")));
    	pl.setSqmUnitPrice(TypeConvert.str2Long(req.getParameter("SQM_UNIT_PRICE")));
    	pl.setStatus(req.getParameter("STATUS"));
    	pl.setRemark(req.getParameter("REMARK"));
    	pl.setLastUpdatedBy(userId);
    	res.getWriter().print(plvs.update(lockPoLineVO, pl, userType, funcId).toJsonStr());
   	}
    
    @RequestMapping("/changeStatus.do")
    public void changeStatus() throws Exception
	{   
    	Long poHeaderId = TypeConvert.str2Long(req.getParameter("PO_HEADER_ID"));
    	Long userId = (Long)sess.getAttribute("USER_ID");
    	String status = req.getParameter("STATUS");
    	res.getWriter().print(phvs.changeStatus(poHeaderId,status,userId).toJsonStr());
	}
}
