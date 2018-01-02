package com.wisdom.web.dao;

import java.util.Map;

public interface IArtifactDao {

	public boolean updateArtifactById(String artifactId, String classification, String supplier_name, String identify_code, 
			Double sum, int rate, Double amount, Double tax, String type, int is_fa, int is_accurate);

	public Map<String, Object> getArtifactByInvoiceId(String invoice_id);

	public boolean addArtifact(int invoiceId, String classification, String supplier, String identifyCode, Double sum,
			int rate, Double amount, Double tax, int number, String type, String forcast, int isFa, int isAccurate);

}
