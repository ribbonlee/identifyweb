package com.wisdom.common.queue;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.wisdom.web.dao.IInvoiceDao;


@Service
public class DeleteMessageDelegate implements MessageDelegate {
	
	private static final Logger logger = LoggerFactory.getLogger(DeleteMessageDelegate.class);
	
	@Autowired
	private IInvoiceDao invoiceDao;
	
	@Override
	public synchronized void handleMessage(String message) throws JsonParseException, JsonMappingException, IOException {
		logger.debug("handleMessage message : {}", message);
		long invoiceId = Long.parseLong(message);
		Map<String, Object> invoiceMap = invoiceDao.getInvoiceByInvoiceId(invoiceId);
		int id = Integer.valueOf((String)invoiceMap.get("id"));
		logger.debug("handleMessage id : {}", id);
		invoiceDao.updateInvoiceStatusById(id, "DELETED");
	}

}

