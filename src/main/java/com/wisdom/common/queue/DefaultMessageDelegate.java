package com.wisdom.common.queue;

import java.io.IOException;
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
		
		List<HashMap<String, Object>> content = mapper.readValue(data.get("content").toString(), typeRef);
		if (content != null && content.size() != 0) {
			for (HashMap<String, Object> item : content) {
				if(item.isEmpty()) continue;
				String supplier = (String) item.get("supplierName");
				String identifyCode = (String) item.get("identifyCode");
				String type = (String) item.get("description");
				Double amount = 0.0;
				Double tax = 0.0;
				int number = 1;
				int isAccurate = 0;
				int isFa = 0;
				int forecast = 0;
				
				Map<String, String> companyInfoMap = VerifyNameCodeUtils.getCompanyInfo(identifyCode);
				if(companyInfoMap!=null && !companyInfoMap.isEmpty()) {
					String companyName = companyInfoMap.get("Name");
					String creditCode = companyInfoMap.get("CreditCode");
					if(identifyCode.equals(creditCode)) {
						forecast = 1;
						supplier = companyName;
					}
				}
				if(forecast != 1){
					companyInfoMap = VerifyNameCodeUtils.getCompanyInfo(supplier);
					if(companyInfoMap!=null && !companyInfoMap.isEmpty()) {
						String companyName = companyInfoMap.get("Name");
						String creditCode = companyInfoMap.get("CreditCode");
						if(companyName.equals(supplier)) {
							forecast = 1;
							identifyCode = creditCode;
						}
					}
				}
				try{
					String amount_str = (String) item.get("amount");
					if(amount_str != null && !amount_str.isEmpty() ) {
						amount = Double.parseDouble(amount_str);
					}
					String tax_str = (String) item.get("tax");
					if(tax_str != null && !tax_str.isEmpty() ) {
						tax = Double.parseDouble(tax_str);
					}
					String number_str = (String) item.get("number");
					if(number_str != null && !number_str.isEmpty() ) {
						number = Integer.parseInt(number_str);
					}
				
					boolean isAccurate_str = (boolean)item.get("isAccurate");
					if(isAccurate_str) {
						isAccurate = 1;
					}
				}catch(Exception e) {
					logger.error("handleMessage parse error : {}", e.toString());
				}
				artifactDao.addArtifact(invoiceId, supplier, identifyCode, classification, type, tax, amount, number, isFa, isAccurate, forecast);
			}
		} else {
			artifactDao.addArtifact(invoiceId, "", "", "", "", 0.0, 0.0, 1, 0, 0, 0);
		}
		invoiceDao.addInvoice(priority, name, path, company, invoiceId, companyId);
	}

}
