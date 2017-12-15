package com.wisdom.web.api.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wisdom.utils.GenerateMD5;
import com.wisdom.web.dao.IUserDao;

@Controller
public class UserController {
	
	@Autowired
	private IUserDao userDao;
    
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@RequestMapping("/login")
	@ResponseBody
	public Map<String, String> login(HttpSession httpSession, HttpServletRequest request){
		logger.debug("enter Login");
		Map<String, String> retMap = new HashMap<>();
		String id = request.getParameter("id");
		String password = request.getParameter("password");
		if (id==null || id.equals("") || password == null || password.equals("")){
			retMap.put("error_code", "1");
			retMap.put("error_msg", "用户名或密码不能为空!");
			return retMap;
		}
		Map<String, Object> userMap = userDao.getUserById(id);
		if(userMap==null || userMap.isEmpty()){
			retMap.put("error_code", "2");
			retMap.put("error_msg", "用户名不存在!");
			return retMap;
		}
		String cryptedPwd = GenerateMD5.generateMD5(password);
		if(!cryptedPwd.equals(userMap.get("password"))) {
			retMap.put("error_code", "3");
			retMap.put("error_msg", "密码错误，请重新输入!");
			return retMap;
		}
		httpSession.setAttribute("uid", id);
		retMap.put("error_code", "0");
		retMap.put("redirect_url", "/views/webviews/index.html");
		return retMap;
	}
	
	@RequestMapping("/logout")
	public String logout(HttpSession httpSession, HttpServletRequest request){
		Map<String, String> retMap = new HashMap<>();
		try {
			httpSession.removeAttribute("uid");
			retMap.put("status", "ok");
		}catch(Exception e){
			retMap.put("status", "nok");
		}
		return "redirect:/views/frontviews/login.html";
	}
	
}
