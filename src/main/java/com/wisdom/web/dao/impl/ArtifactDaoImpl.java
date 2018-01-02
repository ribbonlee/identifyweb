package com.wisdom.web.dao.impl;

import java.sql.Timestamp;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.wisdom.web.dao.IArtifactDao;

@Repository("artifactDao")
public class ArtifactDaoImpl implements IArtifactDao {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public boolean addArtifact(int invoiceId, String classification, String supplier, String identifyCode, Double sum,
			int rate, Double amount, Double tax, int number, String type, String forcast, int isFa, int isAccurate) {
		String sql = "insert into artifact (invoice_id, classification, supplier_name, identify_code, sum, rate, amount, tax, "
				+ "number, type, forcast, is_fa, is_accurate, created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(sql, invoiceId, classification, supplier, identifyCode, sum, rate, 
					amount, tax, number, type, forcast, isFa, isAccurate, new Timestamp(System.currentTimeMillis()));
		} catch(Exception e) {
			logger.error("addArtifact error : {}", e.toString());
		}
		return affectedRows!=0;
	}
	
	@Override
	public boolean updateArtifactById(String artifactId, String classification, String supplier_name, String identify_code,
			Double sum, int rate, Double amount, Double tax, String type, int is_fa, int is_accurate) {
		String sql = "update artifact set classification=?, supplier_name=?, identify_code=?, sum=?, rate=?, amount=?, tax=?, type=?, is_fa=?, is_accurate=? where id=?";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(sql, classification, supplier_name, identify_code, sum, rate, amount, tax, type, is_fa, is_accurate, artifactId);
		} catch(Exception e) {
			logger.error("addArtifact error : {}", e.toString());
		}
		return affectedRows!=0;
	}

	@Override
	public Map<String, Object> getArtifactByInvoiceId(String invoice_id) {
		Map<String, Object> artifactMap = null;
		String sql = "select * from artifact where invoice_id=?";
		try {
			artifactMap = jdbcTemplate.queryForMap(sql, invoice_id);
		} catch(Exception e) {
			logger.error("addArtifact error : {}", e.toString());
		}
		return artifactMap;
	}

}
