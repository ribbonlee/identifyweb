package com.wisdom.web.dao.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.wisdom.web.dao.IUserDao;

@Repository("userDao")
public class UserDaoImpl implements IUserDao {

	private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);
	
	@Autowired
	private  JdbcTemplate jdbcTemplate;
	
	@Override
	public Map<String, Object> getUserById(String id) {
		String sql = "select * from user where id=?";
		Map<String, Object> userMap = null;
		try{
			userMap = jdbcTemplate.queryForMap(sql, id);
		} catch(Exception e) {
			logger.error("getUserById error : {}", e.toString());
		}
		return userMap;
	}
	
}
