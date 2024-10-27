package com.example.ruleengine.model;

public class GeneralErrorResponse {

	private String error;

    public GeneralErrorResponse(String error) {
        this.error = error;
    }

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
    
}
