package com.wisdom.web.dao;

import java.util.Map;

public interface IArtifactDao {

	public boolean addArtifact(int invoice_id, String supplier, String identify_code, String classification, 
			String type, double tax, double amount, int number, int isFa,int isAccurate, int forecast);

	public boolean updateArtifactById(String artifactId, String supplier_name, String identify_code, String classification, 
			String type, Double amount, Double tax, int is_fa);

	public Map<String, Object> getArtifactByInvoiceId(String invoice_id);

}
