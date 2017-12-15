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
	public boolean addArtifact(int invoice_id, String supplier, String identify_code, String classification, 
			String type, double tax, double amount, int number, int isFa, int isAccurate, int forecast) {
		
		String sql = "insert into artifact (invoice_id, supplier_name, identify_code, classification, amount, tax, "
				+ "number, type, is_fa, is_accurate, forcast, created_time) values (?,?,?,?,?,?,?,?,?,?,?,?)";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(sql, invoice_id, supplier, identify_code, classification, 
					amount, tax, number, type, isFa, isAccurate, forecast, new Timestamp(System.currentTimeMillis()));
		} catch(Exception e) {
			logger.error("addArtifact error : {}", e.toString());
		}
		return affectedRows!=0;
	}

	@Override
	public boolean updateArtifactById(String artifactId, String supplier_name, String identify_code, String classification,
			String type,  Double amount, Double tax, int is_fa) {
		String sql = "update artifact set supplier_name=?, identify_code=?, classification=?, amount=?, tax=?, type=?, is_fa=?, is_accurate=? where id=?";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(sql, supplier_name, identify_code, classification, amount, tax, type, is_fa, 1, new Timestamp(System.currentTimeMillis()));
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
