package com.wisdom.web.dao.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.wisdom.web.dao.IInvoiceDao;

@Repository("invoiceDao")
public class InvoiceDaoImpl implements IInvoiceDao {
	
	private static final Logger logger = LoggerFactory.getLogger(ArtifactDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public boolean addInvoice(Integer priority, String name, String path, String company, 
			int invoiceId, long companyId) {
		String uuid = UUID.randomUUID().toString();
		String sql = "insert into invoice (id, name, created_time, priority, path, company, "
				+ "exported, uid, document, status, invoice_id, company_id) values (?,?,?,?,?,?,?,?,?,?,?,?)";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(sql, uuid, name, new Timestamp(System.currentTimeMillis()), priority, 
					path, company, 0, null, null, "UNRECOGNIZED_INVOICE", invoiceId, companyId);
		} catch(Exception e) {
			logger.error("addArtifact error : {}", e.toString());
		}
		return affectedRows!=0;
	}

	@Override
	public Map<String, Object> getInvoiceByInvoiceId(long invoiceId) {
		String sql = "select * from invoice where invoice_id=?";
		Map<String, Object> retMap = null;
		try{
			retMap = jdbcTemplate.queryForMap(sql);
		} catch(Exception e) {
			logger.error("getInvoiceByInvoiceId query error : {}", e.toString());
		}
		return retMap;
	}

	@Override
	public boolean updateInvoiceStatusById(int id, String status) {
		String sql = "update invoice set status=？, modified_time=? where id=?";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(sql, status, new Timestamp(System.currentTimeMillis()), id);
		} catch(Exception e) {
			logger.error("updateInvoiceStatusById error : {}", e.toString());
		}
		return affectedRows!=0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getRecordsTotal() {
		String sql = "select count(*) from invoice";
		int recordsTotal = 0;
		try {
			recordsTotal = jdbcTemplate.queryForInt(sql);
		} catch(Exception e) {
			logger.error("getRecordsTotal error : {}", e.toString());
		}
		return recordsTotal;
	}

	@Override
	public List<Map<String, Object>> getInvoice(String start, String length) {
		String sql = "select invoice.invoice_id, invoice.status, company, supplier_name, "
				+ "sum(amount) AS amount, sum(tax) AS tax, group_concat(type) AS type, "
				+ "artifact.forcast, artifact.identify_code, artifact.classification "
				+ "from invoice left join artifact on invoice.invoice_id = artifact.invoice_id "
				+ "group by artifact.invoice_id order by invoice.created_time desc limit " + start + "," + length;
		List<Map<String, Object>> retList = null;
		try {
			retList = jdbcTemplate.queryForList(sql);
		} catch(Exception e) {
			logger.error("getInvoice error : {}", e.toString());
		}
		return retList;
	}

	@Override
	public Map<String, Object> getInvoiceDetailByInvoiceId(int invoice_id) {
		Map<String, Object> retMap = null;
		String sql = "select name, company, supplier_name, identify_code, classification, amount, tax, type from invoice left "
				+ "join (select invoice_id, supplier_name, identify_code, classification, sum(amount) as amount, "
				+ "sum(tax) as tax, group_concat(type) as type from artifact group by "
				+ "artifact.invoice_id) tmp on invoice.invoice_id=tmp.invoice_id where invoice.invoice_id=" + invoice_id;
		try {
			retMap = jdbcTemplate.queryForMap(sql);
		} catch(Exception e) {
			logger.error("getInvoiceDetailByInvoiceId error : {}", e.toString());
		}
		return retMap;
	}

	@Override
	public boolean updateInvoiceByInvoiceId(int invoice_id, String supplier_name, String amount, String tax,
			String type, int is_fa) {
		String updateInvoiceSql = "update invoice set modified_time=?, status=? where invoice_id=?";
		String updateArtifactSql = "update artifact set supplier_name=?, amount=?, tax=?, type=?, is_fa=? where invoice_id=?";
		int updateInvoiceAffectedRows = 0;
		int updateArtifactAffectedRows = 0;
		try {
			updateInvoiceAffectedRows = jdbcTemplate.update(updateInvoiceSql, new Timestamp(System.currentTimeMillis()), "RECOGNIZED", invoice_id);
			updateArtifactAffectedRows = jdbcTemplate.update(updateArtifactSql, supplier_name, amount, tax, type, is_fa, invoice_id);
		} catch(Exception e) {
			logger.error("getInvoice error : {}", e.toString());
		}
		return updateInvoiceAffectedRows!=0 && updateArtifactAffectedRows!=0;
	}

	@Override
	public boolean updateInvoiceStatusByInvoiceId(String invoice_id, String status) {
		String sql = "update invoice set status=?, modified_time=? where invoice_id=?";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(sql, status, new Timestamp(System.currentTimeMillis()), invoice_id);
		} catch(Exception e) {
			logger.error("getInvoiceDetailByInvoiceId error : {}", e.toString());
		}
		return affectedRows!=0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getNewInvoiceNum() {
		String sql = "select count(*) from invoice where date(created_time)=curdate()";
		int total = 0;
		try {
			total = jdbcTemplate.queryForInt(sql);
		} catch(Exception e) {
			logger.error("getNewInvoiceNum error : {}", e.toString());
		}
		return total;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getNumByStatus(String status) {
		String sql = "select count(*) from invoice where status=?";
		int total = 0;
		try {
			total = jdbcTemplate.queryForInt(sql, status);
		} catch(Exception e) {
			logger.error("getNumByStatus error : {}", e.toString());
		}
		return total;
	}
	
}
