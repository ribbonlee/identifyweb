package com.wisdom.common.queue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisdom.utils.VerifyNameCodeUtils;
import com.wisdom.web.dao.IArtifactDao;
import com.wisdom.web.dao.IInvoiceDao;

import net.sf.json.JSONArray;
import redis.clients.jedis.Jedis;


@Service
public class DefaultMessageDelegate implements MessageDelegate {

	@Autowired
	private static final Logger logger = LoggerFactory.getLogger(DefaultMessageDelegate.class);
	
	@Autowired
	private IInvoiceDao invoiceDao;

	@Autowired
	private IArtifactDao artifactDao;
	
	@Override
	public synchronized void handleMessage(String message) throws JsonParseException, JsonMappingException, IOException {
		logger.debug("handleMessage message : {}", message);
		
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		TypeReference<List<HashMap<String, Object>>> typeRef = new TypeReference<List<HashMap<String, Object>>>() {
		};

		List<HashMap<String, Object>> list = mapper.readValue(message, typeRef);
		HashMap<String, Object> data = list.get(0);
		logger.debug("handleMessage data : {}", data);
		
		String path = (String) data.get("path");
		String name = (String) data.get("path");
		String company = (String) data.get("company");
		Integer priority = 10;
		
		int invoiceId = Integer.valueOf((String) data.get("invoice_id"));
		long companyId = Long.parseLong((String) data.get("company_id"));
		
		String classification = (String) data.get("classification");
		try {
			String contentStr = data.get("content").toString();
			TypeReference<HashMap<String, Object>> mapRef = new TypeReference<HashMap<String, Object>>() {
			};
			Map<String, Object> contentMap = mapper.readValue(contentStr, mapRef);
			
			publishDetail(String.valueOf(invoiceId), contentMap);
			
			String supplier = (String) contentMap.get("supplierName");
			//String buyerName = (String) item.get("buyerName");
			String type = (String) contentMap.get("description");
			String identifyCode = (String) contentMap.get("identifyCode");
			String forcast = (String) contentMap.get("result_status");
			String invoice_type = (String) contentMap.get("invoice_type");
			Double sum = 0.0;
			Double amount = 0.0;
			Double tax = 0.0;
			int number = 1;
			int rate = 0;
			int isFa = 0;
			int isAccurate = 0;
			
			try{
				String sum_str = (String) contentMap.get("sum");
				if(sum_str != null && !sum_str.isEmpty() ) {
					sum = Double.parseDouble(sum_str);
				}
				String rate_str = (String) contentMap.get("rate");
				if(rate_str != null && !rate_str.isEmpty() ) {
					rate = Integer.parseInt(rate_str);
				}
				String amount_str = (String) contentMap.get("amount");
				if(amount_str != null && !amount_str.isEmpty() ) {
					amount = Double.parseDouble(amount_str);
				}
				String tax_str = (String) contentMap.get("tax");
				if(tax_str != null && !tax_str.isEmpty() ) {
					tax = Double.parseDouble(tax_str);
				}
				String number_str = (String) contentMap.get("number");
				if(number_str != null && !number_str.isEmpty() ) {
					number = Integer.parseInt(number_str);
				}
			}catch(Exception e) {
				logger.error("handleMessage parse error : {}", e.toString());
			}
			artifactDao.addArtifact(invoiceId, classification, supplier, identifyCode, sum, rate, amount, tax, number, type, forcast, isFa, isAccurate, invoice_type);
		}catch(Exception e) {
			logger.error("handleMessage error :{}", e.toString());
			artifactDao.addArtifact(invoiceId, "", "", "", 0.0, 0, 0.0, 0.0, 1, "", "ALL_FAIL", 0, 0, "unknown");
		}
		invoiceDao.addInvoice(priority, name, path, company, invoiceId, companyId);
	}

	
	public long publishDetail(String invoice_id, Map<String, Object> contentMap) {
		
		List<Map<String, Object>> exportedDataList = new ArrayList<>();
		
		Map<String, Object> exportedData = new HashMap<>();
		exportedData.put("fa", "no");
		exportedData.put("id", invoice_id);
		List<Map<String, Object>> contentList = new ArrayList<>();
		contentMap.put("description", contentMap.get("description"));
		contentMap.put("supplier", contentMap.get("supplierName"));
		contentMap.put("number", "1");
		contentList.add(contentMap);
		String contentListStr = JSONArray.fromObject(contentList).toString();
		exportedData.put("data", contentListStr);
		
		exportedDataList.add(exportedData);
		
		String exportDataStr = JSONArray.fromObject(exportedData).toString();
		System.out.println(exportDataStr);
		Jedis jedis = new Jedis("139.196.40.99", 6379);
		jedis.auth("T4729VT95%XsIvM");
		logger.debug("begin publish recognizedInvoive");
		long k_ = jedis.publish("RECOGNIZE_COMPLETE", exportDataStr);
		logger.debug("end publish recognized invoive, publish return value : {}", k_);
		jedis.close();
		return k_;
	}
	
}
