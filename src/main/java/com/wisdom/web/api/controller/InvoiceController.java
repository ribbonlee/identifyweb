package com.wisdom.web.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.wisdom.web.dao.IArtifactDao;
import com.wisdom.web.dao.IInvoiceDao;

import net.sf.json.JSONArray;
import redis.clients.jedis.Jedis;

@Controller
public class InvoiceController {

	private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);
	
	@Autowired
	private IInvoiceDao invoiceDao;
	
	@Autowired
	private IArtifactDao artifactDao;
	
	@RequestMapping("/invoice/getAllInvoice")
	@ResponseBody
	public Map<String, Object> getAllInvoice(HttpServletRequest request) {
		Map<String, Object> retMap = new HashMap<>();
		String start = request.getParameter("start");
		String length = request.getParameter("length");
		String draw = request.getParameter("draw");
		logger.debug("getAllInvoice start:{}, length:{}", start, length, draw);
		int recordsTotal = invoiceDao.getRecordsTotal();
		int recordsFiltered = recordsTotal;
		List<Map<String, Object>> retList = invoiceDao.getInvoice(start, length);
		retMap.put("draw", draw);
		retMap.put("recordsTotal", recordsTotal);
		retMap.put("recordsFiltered", recordsFiltered);
		retMap.put("data", retList);
		return retMap;
	}
	
	@RequestMapping("/invoice/getInvoiceDetailByInvoiceId")
	@ResponseBody
	public Map<String, Object> getInvoiceDetailByInvoiceId(HttpServletRequest request) {
		Map<String, Object> retMap = new HashMap<>();
		String invoice_id = request.getParameter("id");
		logger.debug("getAllInvoice invoice_id:{}", invoice_id);
		retMap = invoiceDao.getInvoiceDetailByInvoiceId(Integer.valueOf(invoice_id));
		return retMap;
	}
	
	@RequestMapping("/invoice/getInvoiceNumData")
	@ResponseBody
	public Map<String, Object> getInvoiceNumData() {
		Map<String, Object> retMap = new HashMap<>();
		int newInvoiceNum = invoiceDao.getNewInvoiceNum();
		int invoiceTotalNum = invoiceDao.getRecordsTotal();
		int identifiedNum = invoiceDao.getNumByStatus("RECOGNIZED");
		int nonIndentifiedNum = invoiceDao.getNumByStatus("UNRECOGNIZED_INVOICE");
		retMap.put("newInvoiceNum", newInvoiceNum);
		retMap.put("invoiceTotalNum", invoiceTotalNum);
		retMap.put("identifiedNum", identifiedNum);
		retMap.put("nonIndentifiedNum", nonIndentifiedNum);
		return retMap;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/invoice/updateInvoiceContentByInvoiceId")
	@ResponseBody
	public Map<String, Object> updateInvoiceContentByInvoiceId(HttpServletRequest request) {
		Map<String, Object> retMap = new HashMap<>();
		String data = request.getParameter("data");
		String FA = request.getParameter("FA");
		String invoice_id = request.getParameter("id");
		//update invoice
		Map<String, String> dataMap = (Map<String, String>)JSON.parse(data);
		try {
			String supplier_name = dataMap.get("supplier_name");
			String identify_code = dataMap.get("identify_code");
			String classification = dataMap.get("classification");
			String type = dataMap.get("type");
			Double amount = Double.parseDouble(dataMap.get("amount"));
			Double tax = Double.parseDouble(dataMap.get("tax"));
			int is_fa = 0;
			if (FA.equals("yes")) {
				is_fa = 1;
			}
			Map<String, Object> artifactMap = artifactDao.getArtifactByInvoiceId(invoice_id);
			if(artifactMap!=null && !artifactMap.isEmpty()) {
				String artifactId = (String)artifactMap.get("id");
				artifactDao.updateArtifactById(artifactId, supplier_name, identify_code, classification, type, amount, tax, is_fa);
			} else {
				artifactDao.addArtifact(Integer.valueOf(invoice_id), supplier_name, identify_code, classification, type, tax, amount, 1, is_fa, 1, 0);
			}
			invoiceDao.updateInvoiceStatusByInvoiceId(invoice_id, "RECOGNIZED");
		} catch (Exception e) {
			logger.debug(e.toString());
			retMap.put("error_msg", "更新失败，请稍后重试！");
			retMap.put("error_code", "-1");
			return retMap;
		} 
		
		Map<String, String> exportedData = new HashMap<>();
		exportedData.put("fa", FA);
		exportedData.put("id", invoice_id);
		exportedData.put("data", data);
		String exportDataStr = JSONArray.fromObject(exportedData).toString();
		System.out.println(exportDataStr);
		Jedis jedis = new Jedis("139.196.40.99", 6379);
		jedis.auth("T4729VT95%XsIvM");
		logger.debug("begin publish recognizedInvoive");
		long k_ = jedis.publish("RECOGNIZE_COMPLETE", exportDataStr);
		logger.debug("end publish recognized invoive, publish return value : {}", k_);
		jedis.close();
		
		retMap.put("error_msg", "修改成功！");
		retMap.put("error_code", "0");
		return retMap;
	}
	
	@RequestMapping("/invoice/updateInvoiceStatusByInvoiceId")
	@ResponseBody
	public Map<String, String>updateInvoiceStatusByInvoiceId(HttpServletRequest request){
		Map<String, String> retMap = new HashMap<>();
		String invoice_id = request.getParameter("id");
		String status = request.getParameter("status");
		if(invoiceDao.updateInvoiceStatusByInvoiceId(invoice_id, status)){
			retMap.put("status", "ok");
		}
		else{
			retMap.put("status", "nok");
		}
		
		Jedis jedis = new Jedis("139.196.40.99", 6379);
		jedis.auth("T4729VT95%XsIvM");
		logger.debug("begin publish INVALID_INVOICE");
		long k_ = jedis.publish("INVALID_INVOICE", invoice_id);
		logger.debug("end publish INVALID_INVOICE, publish return value : {}", k_);
		jedis.close();
		
		return retMap;
	}
}
