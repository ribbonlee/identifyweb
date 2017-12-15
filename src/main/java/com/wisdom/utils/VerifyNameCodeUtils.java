package com.wisdom.utils;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.alibaba.fastjson.JSON;

public class VerifyNameCodeUtils {
	
	public static void main(String[] args) {
		for(int i=0;i<1;i++) {
			
			getCompanyInfo("913100G07653272325");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> getCompanyInfo(String param) {  
		Map<String, String> dataMap = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();  
        try {  
        	String searchUrl = "http://www.qichacha.com/search?key=" + param;
            HttpGet httpGet = new HttpGet(searchUrl);  
            configHeader(httpGet);
            CloseableHttpResponse response = httpclient.execute(httpGet);  
            HttpEntity entity = response.getEntity();  
            String result = EntityUtils.toString(entity);
            //System.out.println(result);
            Document doc = Jsoup.parse(result);
            Element ele = doc.select(".m_srchList tbody").get(0).select("a").get(0);
            String uri = ele.attr("href");
            String keyno = uri.substring(uri.indexOf("_")+1, uri.lastIndexOf("."));
            String ajaxUrl = "http://www.qichacha.com/tax_view?keyno=" + keyno + "&ajaxflag=1";
            response.close();
            httpclient.close();
            httpclient = HttpClients.createDefault();  
            HttpGet detailGet = new HttpGet(ajaxUrl);
            configHeader(detailGet);
            CloseableHttpResponse detailResponse = httpclient.execute(detailGet);  
            HttpEntity detailEntity = detailResponse.getEntity();  
            String detailResult = EntityUtils.toString(detailEntity, "utf-8");
            Map<String, Object> responseMap = (Map<String, Object>) JSON.parse(detailResult);
            dataMap = (Map<String, String>) responseMap.get("data");
            System.out.println(dataMap.toString());
        } catch (Exception e) {  
            e.printStackTrace();  
        }  finally {  
            try {
                httpclient.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        return dataMap;
    }  
	
	public static void configHeader(HttpGet httpGet) {
		
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");  
        
		httpGet.setHeader("Accept-Encoding", "gzip, deflate");  
  
		httpGet.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");  
  
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9");  
  
        httpGet.setHeader("Connection", "keep-alive");  
        
        httpGet.setHeader("Host", "www.qichacha.com"); 
        
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.62 Safari/537.36");
	}
	
}
