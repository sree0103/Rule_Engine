package com.example.ruleengine.model;

public class CombinationResponse {

	private String status;
    private Node combinedData;
    private String error;
    
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Node getCombinedData() {
		return combinedData;
	}
	public void setCombinedData(Node combinedData) {
		this.combinedData = combinedData;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public CombinationResponse(String status, Node combinedData, String error) {
		super();
		this.status = status;
		this.combinedData = combinedData;
		this.error = error;
	}
    
}
