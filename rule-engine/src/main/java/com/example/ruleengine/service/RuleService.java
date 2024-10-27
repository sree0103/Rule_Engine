package com.example.ruleengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ruleengine.model.Node;
import com.example.ruleengine.repository.NodeRepository;

@Service
public class RuleService {

	 @Autowired
	    private NodeRepository nodeRepository;
	 
	 
	 private static final Map<String, Boolean> associativityMap = new HashMap<>();
	    private static final Map<String, Integer> precedenceMap = new HashMap<>();

	    static {
	        precedenceMap.put("AND", 2);
	        precedenceMap.put("OR", 1);
	        precedenceMap.put(">", 3);
	        precedenceMap.put("<", 3);
	        precedenceMap.put(">=", 3);
	        precedenceMap.put("<=", 3);
	        precedenceMap.put("=", 3);

	        associativityMap.put("AND", true);
	        associativityMap.put("OR", true);
	    }

	    public Node createRule(String rule) {
	    	if(rule == null) {
    		throw new IllegalArgumentException("Rule cannot be null");
    	}
    	if(rule.trim().isEmpty()) {
    		throw new IllegalArgumentException("Rule cannot be empty");
    	}
    	
    	 if (!isValidRuleFormat(rule)) {
    	        throw new IllegalArgumentException("Invalid rule format: " + rule);
    	    }

	        Node ruleAst = buildAstFromTokens(tokenize(rule));
	        Node savedNode = nodeRepository.save(ruleAst);
//	        System.out.println("Created rule with ID: " + savedNode.getId());
	        return savedNode;
	    }
	    
	    private boolean isValidRuleFormat(String rule) {
	        if (rule == null || rule.trim().isEmpty()) {
	            return false;
	        }
	        int balance = 0;
	        for (char ch : rule.toCharArray()) {
	            if (ch == '(') {
	                balance++;
	            } else if (ch == ')') {
	                balance--;
	                if (balance < 0) {
	                    return false;
	                }
	            }
	        }
	        if (balance != 0) {
	            return false;
	        }
	        String validOperators = ">|<|=|!=|>=|<=";
	        if (!rule.matches(".*\\b(\\w+\\s*(" + validOperators + ")\\s*\\w+)\\b.*")) {
	            return false; 
	        }
	        if (!rule.matches(".*\\b(AND|OR)\\b.*") && !rule.matches(".*\\b\\w+\\s*(" + validOperators + ")\\s*\\w+.*")) {
	            return true; 
	        }
	        return true; 
	    }
	    
	    private Node buildAstFromTokens(List<String> tokens) {
	        Stack<Node> operandStack = new Stack<>();
	        Stack<String> operatorStack = new Stack<>();

	        for (String token : tokens) {
	            if ("(".equals(token)) {
	                operatorStack.push(token);
	            } else if (")".equals(token)) {
	                while (!operatorStack.isEmpty() && !"(".equals(operatorStack.peek())) {
	                    processOperator(operandStack, operatorStack);
	                }
	                operatorStack.pop();
	            } else if (isOperator(token)) {
	                while (!operatorStack.isEmpty() && getPrecedence(token) <= getPrecedence(operatorStack.peek())) {
	                    processOperator(operandStack, operatorStack);
	                }
	                operatorStack.push(token);
	            } else {
	                operandStack.push(new Node("operand", token));
	            }
	        }

	        while (!operatorStack.isEmpty()) {
	            processOperator(operandStack, operatorStack);
	        }

	        return operandStack.pop();
	    }   
	    
	    private boolean isOperator(String token) {
	        return precedenceMap.containsKey(token);
	    }

	    private void processOperator(Stack<Node> operandStack, Stack<String> operatorStack) {
	        if (operatorStack.isEmpty()) {
	            throw new IllegalStateException("Insufficient operands for operator");
	        }

	        if (operandStack.size() < 2) {
	            throw new IllegalArgumentException("Insufficient operands for operator");
	        }
	        String operator = operatorStack.pop();
	        Node right = operandStack.pop();
	        Node left = operandStack.pop();

	        Node operatorNode = new Node("operation", operator, left, right);
	        operandStack.push(operatorNode);
	    }
	    
	    public static int getPrecedence(String operator) {
	        return precedenceMap.getOrDefault(operator, 0);
	    }

	    public List<String> tokenize(String rule) {
	        List<String> tokens = new ArrayList<>();
	        StringBuilder sb = new StringBuilder();

	        for (int i = 0; i < rule.length(); i++) {
	            char c = rule.charAt(i);
	            if (Character.isWhitespace(c)) {
	                continue;
	            }

	            if (c == '(' || c == ')') {
	                if (sb.length() > 0) {
	                    tokens.add(sb.toString()); 
	                    sb.setLength(0); 
	                }
	                tokens.add(Character.toString(c)); 
	            }
	            else if (c == '>' || c == '<' || c == '=' || c == '!') {
	                if (sb.length() > 0) {
	                    tokens.add(sb.toString()); 
	                    sb.setLength(0); 
	                }
	                if (i + 1 < rule.length() && (rule.charAt(i + 1) == '=' || (c == '!' && rule.charAt(i + 1) == '='))) {
	                    tokens.add(Character.toString(c) + rule.charAt(i + 1)); 
	                    i++; 
	                } else {
	                    tokens.add(Character.toString(c)); 
	                }
	            }
	            else if (i + 2 < rule.length() && rule.substring(i, i + 3).equalsIgnoreCase("AND")) {
	                if (sb.length() > 0) {
	                    tokens.add(sb.toString()); 
	                    sb.setLength(0);
	                }
	                tokens.add("AND"); 
	                i += 2; 
	            } else if (i + 1 < rule.length() && rule.substring(i, i + 2).equalsIgnoreCase("OR")) {
	                if (sb.length() > 0) {
	                    tokens.add(sb.toString()); 
	                    sb.setLength(0); 
	                }
	                tokens.add("OR");
	                i += 1;
	            }
	            else if (Character.isLetterOrDigit(c) || c == '\'' || c == '"') {
	                sb.append(c); 
	            }
	            else {
	                if (sb.length() > 0) {
	                    tokens.add(sb.toString()); 
	                    sb.setLength(0); 
	                }
	                tokens.add(Character.toString(c));
	            }
	        }
	        if (sb.length() > 0) {
	            tokens.add(sb.toString()); 
	        }
	        return tokens;
	    }

	   
	    public Node combineRules(List<Long> ruleIds) {
	        List<Node> asts = new ArrayList<>();
	        Map<String, Integer> operatorCount = new HashMap<>();

	        for (Long ruleId : ruleIds) {
	            Optional<Node> nodeOpt = nodeRepository.findById(ruleId);
	            if (!nodeOpt.isPresent()) {
	                throw new IllegalArgumentException("No rule found for ID: " + ruleId);
	            }
	            Node node = nodeOpt.get();
//	            System.out.println("Rule Node: " + node.getValue() + " of type: " + node.getType());
	            asts.add(node);
	            countOperators(node, operatorCount);
	        }

	        String mainOperator = getDominantOperator(operatorCount);
	        Node combinedAst = combineASTs(asts, mainOperator);
	        return nodeRepository.save(combinedAst);
	    }
	    private void countOperators(Node node, Map<String, Integer> operatorCount) {
	    	if (node == null) return;
//	        System.out.println("Visiting Node: " + node.getValue() + " of type: " + node.getType());

	        if ("operation".equals(node.getType())) {
	            operatorCount.put(node.getValue(), operatorCount.getOrDefault(node.getValue(), 0) + 1);
	            countOperators(node.getLeft(), operatorCount);
	            countOperators(node.getRight(), operatorCount);
	        }
	    }
	     private String getDominantOperator(Map<String, Integer> operatorCount) {
	    	 int orCount = operatorCount.getOrDefault("OR", 0);
	    	    int andCount = operatorCount.getOrDefault("AND", 0);

//	    	    System.out.println("OR Count: " +orCount);
//	    	    System.out.println("AND Count: " +andCount);
	    	    if (orCount > andCount) {
	    	        return "OR";
	    	    }
	    	    return "AND";
	    }
	    public Node combineASTs(List<Node> asts, String mainOperator) {
	        if (asts.isEmpty() || mainOperator == null) {
	            throw new IllegalArgumentException("Cannot combine ASTs with empty list or null operator.");
	        }
	        Node root = asts.get(0);
	        for (int i = 1; i < asts.size(); i++) {
	            Node nextNode = asts.get(i);
	            Node newRoot = new Node("operator", mainOperator);
	            newRoot.setLeft(root);
	            newRoot.setRight(nextNode);
	            root = newRoot;
	        }
	        return root;
	    }
	    
	     
	    public boolean evaluateRule(Long ruleId, Map<String, Object> userData) throws IllegalAccessException {
	        Optional<Node> ruleNodeOpt = nodeRepository.findById(ruleId);
	        if (ruleNodeOpt.isEmpty()) {
	            throw new IllegalArgumentException("Rule not found");
	        }
	        Node ruleNode = ruleNodeOpt.get();
	        return evaluateNode(ruleNode, userData);
	    }

	    private boolean evaluateNode(Node node, Map<String, Object> userData) throws IllegalAccessException {
	        // Base case for operand evaluation
	        if ("operand".equals(node.getType())) {
	            Object value = evaluateOperandValue(node, userData);
	            return value != null;
	        }
	        String operation = node.getValue();
	        if (node.getLeft() == null || node.getRight() == null) {
	            throw new IllegalArgumentException("Invalid operand format: both left and right nodes must be present");
	        }

	        if ("AND".equals(operation)) {
	            boolean leftResult = evaluateNode(node.getLeft(), userData);
	            boolean rightResult = evaluateNode(node.getRight(), userData);
	            return leftResult && rightResult;
	        } else if ("OR".equals(operation)) {
	            boolean leftResult = evaluateNode(node.getLeft(), userData);
	            boolean rightResult = evaluateNode(node.getRight(), userData);
	            return leftResult || rightResult;
	        } else if ("=".equals(operation)) {
	            Object leftValue = evaluateOperandValue(node.getLeft(), userData);
	            Object rightValue = evaluateOperandValue(node.getRight(), userData);
	            return leftValue.equals(rightValue);
	        } else if (">".equals(operation)) {
	            return compareValues(node, userData, ">");
	        } else if ("<".equals(operation)) {
	            return compareValues(node, userData, "<");
	        } else if (">=".equals(operation)) {
	            return compareValues(node, userData, ">=");
	        } else if ("<=".equals(operation)) {
	            return compareValues(node, userData, "<=");
	        } else if ("!=".equals(operation)) {
	            Object leftValue = evaluateOperandValue(node.getLeft(), userData);
	            Object rightValue = evaluateOperandValue(node.getRight(), userData);
	            return !leftValue.equals(rightValue);
	        } else {
	            throw new IllegalArgumentException("Unsupported operator: " + operation);
	        }
	    }

	    private boolean compareValues(Node node, Map<String, Object> userData, String operator) throws IllegalAccessException {
	        Object leftValue = evaluateOperandValue(node.getLeft(), userData);
	        Object rightValue = evaluateOperandValue(node.getRight(), userData);

	        // Type checking for comparison
	        if (leftValue instanceof Number && rightValue instanceof Number) {
	            double leftNum = ((Number) leftValue).doubleValue();
	            double rightNum = ((Number) rightValue).doubleValue();

	            switch (operator) {
	                case ">":
	                    return leftNum > rightNum;
	                case "<":
	                    return leftNum < rightNum;
	                case ">=":
	                    return leftNum >= rightNum;
	                case "<=":
	                    return leftNum <= rightNum;
	                default:
	                    throw new IllegalArgumentException("Unsupported comparison operator: " + operator);
	            }
	        } else {
	            throw new IllegalArgumentException("Incompatible types for comparison: " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName());
	        }
	    }


	    private Object evaluateOperandValue(Node operandNode, Map<String, Object> userData) throws IllegalAccessException {
	        if ("operand".equals(operandNode.getType())) {
	            String operandKey = operandNode.getValue();
	            try {
	                double numericValue = Double.parseDouble(operandKey);
	                return numericValue;
	            } catch (NumberFormatException e) {
	               
	            }
	            if (operandKey.startsWith("'") && operandKey.endsWith("'")) {
	                return operandKey.substring(1, operandKey.length() - 1); 
	            }
	            if (!userData.containsKey(operandKey)) {
	                throw new IllegalArgumentException("User data does not contain required attribute: " + operandKey);
	            }

	            Object value = userData.get(operandKey);
	            if (value instanceof Number || value instanceof String || value instanceof Boolean) {
	                return value;
	            } else {
	                throw new IllegalArgumentException("Invalid attribute type for operand: " + operandKey + " with type " + value.getClass().getSimpleName());
	            }
	        } else {
	            throw new IllegalArgumentException("Invalid node type for operand: " + operandNode.getType());
	        }
	    }

	    private boolean isComparisonOperator(String operator) {
	        return operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=") ||
	               operator.equals("=") || operator.equals("!=");
	    }

	    private boolean isStringOperator(String operator) {
	        return operator.equals("=") || operator.equals("!=");
	    }

	    private boolean applyComparisonOperator(String operator, double userValue, double operandValue) {
	        switch (operator) {
	            case ">":
	                return userValue > operandValue;
	            case "<":
	                return userValue < operandValue;
	            case ">=":
	                return userValue >= operandValue;
	            case "<=":
	                return userValue <= operandValue;
	            case "=":
	                return userValue == operandValue;
	            case "!=":
	                return userValue != operandValue;
	            default:
	                throw new IllegalArgumentException("Unsupported comparison operator: " + operator);
	        }
	    }

	    private boolean applyStringOperator(String operator, String userValue, String operandValue) {
	        switch (operator) {
	            case "=":
	                return userValue.equals(operandValue);
	            case "!=":
	                return !userValue.equals(operandValue);
	            default:
	                throw new IllegalArgumentException("Unsupported operator for String: " + operator);
	        }
	    }



	    // The evaluation for comparison and string operators remains the same as you provided above
	    private boolean evaluateComparisonOrStringOperator(Node ruleNode, Object leftValue, Object rightValue) {
	        // Check for numeric comparisons
	        if (leftValue instanceof Number && rightValue instanceof Number) {
	            return applyComparisonOperator(ruleNode.getValue(), ((Number) leftValue).doubleValue(), ((Number) rightValue).doubleValue());
	        } 
	        // Check for string comparisons
	        else if (leftValue instanceof String && rightValue instanceof String) {
	            return applyStringOperator(ruleNode.getValue(), (String) leftValue, (String) rightValue);
	        } 
	        // If leftValue is a String (like "Sales") and rightValue is a String (like the user data "Sales")
	        else if (leftValue instanceof String && rightValue instanceof String) {
	            return leftValue.equals(rightValue);
	        } 
	        else {
	            throw new IllegalArgumentException("Incompatible types for comparison: " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName());
	        }
	    }

	    private boolean applyLogicalOperator(String operator, boolean leftResult, boolean rightResult) {
	        switch (operator) {
	            case "AND":
	                return leftResult && rightResult;
	            case "OR":
	                return leftResult || rightResult;
	            default:
	                throw new IllegalArgumentException("Unsupported logical operator: " + operator);
	        }
	    }

	    private void validateOperator(String operator) {
	        if (!("AND".equals(operator) || "OR".equals(operator) || isComparisonOperator(operator) || isStringOperator(operator))) {
	            throw new IllegalArgumentException("Unsupported operator: " + operator);
	        }
	    }
	    
	 
}

