package com.example.ruleengine.model;

public class RuleEvaluationResponse {

	private String status;
    private boolean result;
    private String error;

    public RuleEvaluationResponse(String status, boolean result, String error) {
        this.status = status;
        this.result = result;
        this.error = error;
    }

	public RuleEvaluationResponse(String status, boolean result) {
		// TODO Auto-generated constructor stub
		this.status = status;
        this.result = result;
	}

	public RuleEvaluationResponse(String status, String error) {
		// TODO Auto-generated constructor stub
		this.status = status;
		this.error = error;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
    
}
