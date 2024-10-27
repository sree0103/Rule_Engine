package com.example.ruleengine.model;

public class RuleResponse {

	private String status; // "success" or "error"
    private Node node; 
    private String error;

    public RuleResponse(String status, Node node,String error) {
        this.status = status;
        this.node = node;
        this.error = error;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
