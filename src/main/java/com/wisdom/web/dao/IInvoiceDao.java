package com.wisdom.web.dao;

import java.util.List;
import java.util.Map;

public interface IInvoiceDao {

	boolean addInvoice(Integer priority, String name, String path, String company, 
			int invoiceId, long companyId);

	Map<String, Object> getInvoiceByInvoiceId(long invoiceId);

	boolean updateInvoiceStatusById(int id, String status);

	int getRecordsTotal();

	List<Map<String, Object>> getInvoice(String start, String length);

	Map<String, Object> getInvoiceDetailByInvoiceId(int invoice_id);

	boolean updateInvoiceByInvoiceId(int invoice_id, String supplier_name, String amount, String tax,
			String type, int is_fa);

	boolean updateInvoiceStatusByInvoiceId(String invoice_id, String status);

	int getNewInvoiceNum();

	int getNumByStatus(String status);

}
