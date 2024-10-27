package com.example.ruleengine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.ruleengine.model.Node;
import com.example.ruleengine.repository.NodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RuleServiceTest {

	@InjectMocks
	private RuleService ruleService;

	@Mock
	private NodeRepository nodeRepository;
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		 MockitoAnnotations.openMocks(this);
		 when(nodeRepository.save(any(Node.class))).thenAnswer(invocation -> invocation.getArgument(0));
		objectMapper = new ObjectMapper();
	}

	
	@Test
	void testCreateRule_SimpleExpression() {
		String rule = "age > 18";
		Node result = ruleService.createRule(rule);

		assertNotNull(result, "AST root node should not be null");
		assertEquals("operation", result.getType());
		assertEquals(">", result.getValue());

		assertEquals("operand", result.getLeft().getType());
		assertEquals("age", result.getLeft().getValue());

		assertEquals("operand", result.getRight().getType());
		assertEquals("18", result.getRight().getValue());
	} 
	@Test
	void testCreateRule_WithLogicalOperators() {
		String rule = "(age > 18) AND (salary >= 50000)";
		Node result = ruleService.createRule(rule);

		assertNotNull(result, "AST root node should not be null");
		assertEquals("operation", result.getType());
		assertEquals("AND", result.getValue());

		// Left operand (age > 18)
		Node leftOperand = result.getLeft();
		assertEquals("operation", leftOperand.getType());
		assertEquals(">", leftOperand.getValue());
		assertEquals("age", leftOperand.getLeft().getValue());
		assertEquals("18", leftOperand.getRight().getValue());
		Node rightOperand = result.getRight();
		assertEquals("operation", rightOperand.getType());
		assertEquals(">=", rightOperand.getValue());
		assertEquals("salary", rightOperand.getLeft().getValue());
		assertEquals("50000", rightOperand.getRight().getValue());
	}   
	@Test
    void testCreateRule_WithMultipleOperators() {
        String rule = "(age > 18) OR (salary >= 50000) AND (experience < 10)";
        Node result = ruleService.createRule(rule);

        assertNotNull(result, "AST root node should not be null");
        assertEquals("operation", result.getType());
        assertEquals("OR", result.getValue());
        Node leftOperand = result.getLeft();
        assertEquals("operation", leftOperand.getType());
        assertEquals(">", leftOperand.getValue());
        assertEquals("age", leftOperand.getLeft().getValue());
        assertEquals("18", leftOperand.getRight().getValue());
        Node rightOperand = result.getRight();
        assertEquals("operation", rightOperand.getType());
        assertEquals("AND", rightOperand.getValue());

        Node salaryOperand = rightOperand.getLeft();
        assertEquals("operation", salaryOperand.getType());
        assertEquals(">=", salaryOperand.getValue());
        assertEquals("salary", salaryOperand.getLeft().getValue());
        assertEquals("50000", salaryOperand.getRight().getValue());

        Node experienceOperand = rightOperand.getRight();
        assertEquals("operation", experienceOperand.getType());
        assertEquals("<", experienceOperand.getValue());
        assertEquals("experience", experienceOperand.getLeft().getValue());
        assertEquals("10", experienceOperand.getRight().getValue());
    }
    @Test
    void testCreateRule_NullRule() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ruleService.createRule(null);
        });
        assertEquals("Rule cannot be null", exception.getMessage());
    }  //u4
    @Test
    void testCreateRule_EmptyRule() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ruleService.createRule("");
        });
        assertEquals("Rule cannot be empty", exception.getMessage());
    }  //u3
    @Test
    void testCreateRule_InvalidFormat() {
        String rule = "age >> 18";  // invalid format
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ruleService.createRule(rule);
        });
        assertEquals("Invalid rule format: age >> 18", exception.getMessage());
    }
    @Test
    void testCreateRule_UnbalancedParentheses() {
        String rule = "(age > 18 AND (salary >= 50000)";  // unbalanced parentheses
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ruleService.createRule(rule);
        });
        assertEquals("Invalid rule format: " + rule, exception.getMessage());
    }
	
	
	@Test
	void testCombineRules_WithTwoSimpleRules() {
	    // Creating rule nodes
	    Node rule1 = new Node("operation", ">"); // Represents the condition age > 18
	    rule1.setLeft(new Node("operand", "age"));
	    rule1.setRight(new Node("operand", "18"));

	    Node rule2 = new Node("operation", "<"); // Represents the condition salary < 50000
	    rule2.setLeft(new Node("operand", "salary"));
	    rule2.setRight(new Node("operand", "50000"));

	    // Create a logical AND operation to combine both rules
	    Node logicalAnd = new Node("operation", "AND");
	    logicalAnd.setLeft(rule1);
	    logicalAnd.setRight(rule2);
	    when(nodeRepository.findById(1L)).thenReturn(Optional.of(rule1));
	    when(nodeRepository.findById(2L)).thenReturn(Optional.of(rule2));
	    when(nodeRepository.findById(3L)).thenReturn(Optional.of(logicalAnd));
	    when(nodeRepository.save(any(Node.class))).thenAnswer(invocation -> invocation.getArgument(0));

	    List<Long> ruleIds = Arrays.asList(1L, 2L); 
	    Node combinedAst = ruleService.combineRules(ruleIds);

	    assertNotNull(combinedAst);
	    assertEquals("operator", combinedAst.getType());
	    assertEquals("AND", combinedAst.getValue());
	}
	 @Test
	 void testCombineRules_WithMultipleRulesAndDominantOR() {
	     Node rule1 = new Node("operation", ">");
	     rule1.setLeft(new Node("operand", "age"));
	     rule1.setRight(new Node("operand", "18"));

	     Node rule2 = new Node("operation", "<");
	     rule2.setLeft(new Node("operand", "salary"));
	     rule2.setRight(new Node("operand", "50000"));

	     Node rule3 = new Node("operation", "OR"); 
	     rule3.setLeft(new Node("operand", "experience"));
	     rule3.setRight(new Node("operand", "10"));

	     when(nodeRepository.findById(1L)).thenReturn(Optional.of(rule1));
	     when(nodeRepository.findById(2L)).thenReturn(Optional.of(rule2));
	     when(nodeRepository.findById(3L)).thenReturn(Optional.of(rule3));
	     when(nodeRepository.save(any(Node.class))).thenAnswer(invocation -> invocation.getArgument(0));

	     List<Long> ruleIds = Arrays.asList(1L, 2L, 3L);
	     Node combinedAst = ruleService.combineRules(ruleIds);

	     assertNotNull(combinedAst);
	     assertEquals("operator", combinedAst.getType());
	     assertEquals("OR", combinedAst.getValue());
	 }
	 @Test
	    void testCombineRules_WithEqualOperators() {
	        Node rule1 = new Node("operation", ">");
	        rule1.setLeft(new Node("operand", "age"));
	        rule1.setRight(new Node("operand", "18"));

	        Node rule2 = new Node("operation", "<");
	        rule2.setLeft(new Node("operand", "salary"));
	        rule2.setRight(new Node("operand", "50000"));

	        when(nodeRepository.findById(1L)).thenReturn(Optional.of(rule1));
	        when(nodeRepository.findById(2L)).thenReturn(Optional.of(rule2));
	        when(nodeRepository.save(any(Node.class))).thenAnswer(invocation -> invocation.getArgument(0));

	        List<Long> ruleIds = Arrays.asList(1L, 2L);
	        Node combinedAst = ruleService.combineRules(ruleIds);

	        assertNotNull(combinedAst);
	        assertEquals("operator", combinedAst.getType());
	        assertEquals("AND", combinedAst.getValue()); 
	    }
	    @Test
	    void testCombineRules_WithNoOperators() {
	        Node rule1 = new Node("operation", "=");
	        rule1.setLeft(new Node("operand", "age"));
	        rule1.setRight(new Node("operand", "18"));

	        when(nodeRepository.findById(1L)).thenReturn(Optional.of(rule1));
	        when(nodeRepository.save(any(Node.class))).thenAnswer(invocation -> invocation.getArgument(0));

	        List<Long> ruleIds = Arrays.asList(1L);
	        Node combinedAst = ruleService.combineRules(ruleIds);

	        assertNotNull(combinedAst);
	        assertEquals("operation", combinedAst.getType());
	        assertEquals("=", combinedAst.getValue());
	    }
	    @Test
	    void testCombineRules_WithNonexistentRuleId() {
	        when(nodeRepository.findById(1L)).thenReturn(Optional.empty());

	        List<Long> ruleIds = Arrays.asList(1L);
	        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
	            ruleService.combineRules(ruleIds);
	        });
	        assertEquals("No rule found for ID: 1", exception.getMessage());
	    }
	    @Test
	    void testCombineRules_WithEmptyRuleIds() {
	        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
	            ruleService.combineRules(Arrays.asList());
	        });
	        assertEquals("Cannot combine ASTs with empty list or null operator.", exception.getMessage());
	    }  //c1
	
	
	@Test
	void testEvaluateRule_WithValidNumericRule() throws IllegalAccessException {
	    Node ruleNode = new Node("operation", ">");
	    ruleNode.setLeft(new Node("operand", "age")); 
	    ruleNode.setRight(new Node("operand", "18")); 

	    when(nodeRepository.findById(1L)).thenReturn(Optional.of(ruleNode));

	    Map<String, Object> userData = new HashMap<>();
	    userData.put("age", 20);

	    boolean result = ruleService.evaluateRule(1L, userData);
	    assertTrue(result);
	}
	@Test
	void testEvaluateRule_WithValidStringRule() throws IllegalAccessException {
	    Node ruleNode = new Node("operation", "=");
	    ruleNode.setLeft(new Node("operand", "name")); 
	    ruleNode.setRight(new Node("operand", "'John'")); 

	    when(nodeRepository.findById(1L)).thenReturn(Optional.of(ruleNode));

	    Map<String, Object> userData = new HashMap<>();
	    userData.put("name", "John"); 

	    boolean result = ruleService.evaluateRule(1L, userData);
	    assertTrue(result); 
	}
    @Test
    void testEvaluateRule_WithInvalidRuleId() {
        when(nodeRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ruleService.evaluateRule(1L, new HashMap<>());
        });
        assertEquals("Rule not found", exception.getMessage());
    }
    @Test
    void testEvaluateRule_WithNullRuleNode() {
        
        when(nodeRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ruleService.evaluateRule(1L, new HashMap<>());
        });
        assertEquals("Rule not found", exception.getMessage());
    }
    @Test
    void testEvaluateRule_WithNonexistentAttribute() {
        
        Node ruleNode = new Node("operation", ">");
        ruleNode.setLeft(new Node("operand", "age"));  
        ruleNode.setRight(new Node("operand", "18"));  

        when(nodeRepository.findById(1L)).thenReturn(Optional.of(ruleNode));

        Map<String, Object> userData = new HashMap<>();
        

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ruleService.evaluateRule(1L, userData);
        });

        assertEquals("User data does not contain required attribute: age", exception.getMessage());
    }
	@Test
	void testEvaluateRule_WithInvalidOperandFormat() {
	    
	    Node ruleNode = new Node("operation", "=");
	    ruleNode.setLeft(new Node("operand", "name")); 
	    ruleNode.setRight(null);

	    when(nodeRepository.findById(1L)).thenReturn(Optional.of(ruleNode));

	    Map<String, Object> userData = new HashMap<>();
	    userData.put("name", "John");
	    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
	        ruleService.evaluateRule(1L, userData);
	    });

	    assertEquals("Invalid operand format: both left and right nodes must be present", exception.getMessage());
	}
	

 @Test
    void testNodeStructure() {
        Node rootNode = new Node("operation", ">");
        Node leftNode = new Node("operand", "age");
        Node rightNode = new Node("operand", "18");

        rootNode.setLeft(leftNode);
        rootNode.setRight(rightNode);

        assertNotNull(rootNode.getLeft());
        assertNotNull(rootNode.getRight());
        assertEquals("operand", rootNode.getLeft().getType());
        assertEquals("age", rootNode.getLeft().getValue());
        assertEquals("operand", rootNode.getRight().getType());
        assertEquals("18", rootNode.getRight().getValue());
    }
 	
 
	 @Test
	    public void testSimpleComparisonRule() throws IllegalAccessException {
	        Node ruleNode = new Node("operation", ">");
	        ruleNode.setLeft(new Node("operand", "age"));
	        ruleNode.setRight(new Node("operand", "30"));

	        when(nodeRepository.findById(1L)).thenReturn(Optional.of(ruleNode));

	        Map<String, Object> userData = new HashMap<>();
	        userData.put("age", 35);

	        boolean result = ruleService.evaluateRule(1L, userData);
	        assertTrue(result);  
	    }

	 @Test
	    public void testLogicalAndOperation() throws IllegalAccessException {
	        Node ruleNode = new Node("operation", "AND");
	        ruleNode.setLeft(new Node("operation", ">", new Node("operand", "age"), new Node("operand", "30")));
	        ruleNode.setRight(new Node("operation", ">=", new Node("operand", "experience"), new Node("operand", "5")));

	        when(nodeRepository.findById(2L)).thenReturn(Optional.of(ruleNode));

	        Map<String, Object> userData = new HashMap<>();
	        userData.put("age", 35);
	        userData.put("experience", 6);

	        boolean result = ruleService.evaluateRule(2L, userData);
	        assertTrue(result);  
	    }
	
	 @Test
	    public void testLogicalOrOperation() throws IllegalAccessException {
	      
	        Node ruleNode = new Node("operation", "OR");
	        ruleNode.setLeft(new Node("operation", "<", new Node("operand", "age"), new Node("operand", "25")));
	        ruleNode.setRight(new Node("operation", ">", new Node("operand", "salary"), new Node("operand", "50000")));

	        when(nodeRepository.findById(3L)).thenReturn(Optional.of(ruleNode));

	        Map<String, Object> userData = new HashMap<>();
	        userData.put("age", 30);
	        userData.put("salary", 60000);

	        boolean result = ruleService.evaluateRule(3L, userData);
	        assertTrue(result);  
	    }

	    @Test
	    public void testMissingAttributeInUserData() {
	       
	        Node ruleNode = new Node("operation", ">");
	        ruleNode.setLeft(new Node("operand", "age"));
	        ruleNode.setRight(new Node("operand", "30"));

	        when(nodeRepository.findById(4L)).thenReturn(Optional.of(ruleNode));

	        Map<String, Object> userData = new HashMap<>();

	        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
	        	ruleService.evaluateRule(4L, userData);
	        });
	        assertEquals("User data does not contain required attribute: age", exception.getMessage());
	    }

	    @Test
	    public void testIncompatibleTypesForComparison() {
	        Node ruleNode = new Node("operation", ">");
	        ruleNode.setLeft(new Node("operand", "age"));
	        ruleNode.setRight(new Node("operand", "'thirty'"));

	        when(nodeRepository.findById(5L)).thenReturn(Optional.of(ruleNode));

	        Map<String, Object> userData = new HashMap<>();
	        userData.put("age", 35);
	        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
	            ruleService.evaluateRule(5L, userData);
	        });
	        assertTrue(exception.getMessage().contains("Incompatible types for comparison"));
	    }


	    @Test
	    public void testInvalidLogicalOperandTypes() throws IllegalAccessException {
	        
	        Node ruleNode = new Node("operation", "AND");

	        // Left operation node: age > 30
	        Node leftOperationNode = new Node("operation", ">");
	        Node leftOperand1 = new Node("operand", "age"); 
	        Node leftOperand2 = new Node("operand", "30"); 

	        // Linking left operation node
	        leftOperationNode.setLeft(leftOperand1);
	        leftOperationNode.setRight(leftOperand2);
	        Node rightOperationNode = new Node("operation", "=");
	        Node rightOperand1 = new Node("operand", "department"); 
	        Node rightOperand2 = new Node("operand", "'Sales'");
	        rightOperationNode.setLeft(rightOperand1);
	        rightOperationNode.setRight(rightOperand2);
	        ruleNode.setLeft(leftOperationNode);
	        ruleNode.setRight(rightOperationNode);
	        when(nodeRepository.findById(6L)).thenReturn(Optional.of(ruleNode));

	        // User data setup
	        Map<String, Object> userData = new HashMap<>();
	        userData.put("age", 35); 
	        userData.put("department", "Sales"); 
	        boolean result = ruleService.evaluateRule(6L, userData);
	        assertTrue(result, "Expected the evaluation to be true");
	    }

	@Test
	public void testUnsupportedOperator() {
	    Node ruleNode = new Node("operation", "^^");
	    ruleNode.setLeft(new Node("operand", "age"));
	    ruleNode.setRight(new Node("operand", "30"));

	    when(nodeRepository.findById(7L)).thenReturn(Optional.of(ruleNode));

	    Map<String, Object> userData = new HashMap<>();
	    userData.put("age", 35);
	    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
	        ruleService.evaluateRule(7L, userData);
	    });
	    assertTrue(exception.getMessage().contains("Unsupported operator"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "age", "experience", "department" })
	void testMissingAttributeInUserData(String missingAttribute) {
	    Node ruleNode = new Node("operation", ">");
	    ruleNode.setLeft(new Node("operand", missingAttribute));
	    ruleNode.setRight(new Node("operand", "30"));

	    when(nodeRepository.findById(4L)).thenReturn(Optional.of(ruleNode));
	    Map<String, Object> userData = new HashMap<>();
	    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
	        ruleService.evaluateRule(4L, userData);
	    });
	    assertEquals("User data does not contain required attribute: " + missingAttribute, exception.getMessage());
	}	
 @Test
 public void testEvaluateRule_EdgeCase_EmptyUserData() throws IllegalAccessException {
     Node operationNode = new Node("operation", "=",
             new Node("operand", "username", null, null),
             new Node("operand", "'JohnDoe'", null, null)); 
     when(nodeRepository.findById(1L)).thenReturn(Optional.of(operationNode));
     Map<String, Object> userData = new HashMap<>();

     try {
    	 ruleService.evaluateRule(1L, userData);// Fails the test if no exception is thrown
     } catch (IllegalArgumentException e) {
         assertEquals("User data does not contain required attribute: username", e.getMessage());
     }
 }
 
 @Test
	void testEvaluateRule_InvalidInput_NonExistentRule() {
	    when(nodeRepository.findById(1L)).thenReturn(Optional.empty());
	    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
	        ruleService.evaluateRule(1L, new HashMap<>());
	    });
	    assertEquals("Rule not found", exception.getMessage());
	} 
 @Test
 public void testEvaluateRule_ValidInput() throws IllegalAccessException {
     Node operationNode = new Node("operation", "AND", 
             new Node("operand", "isActive", null, null), 
             new Node("operand", "age", null, null));
     when(nodeRepository.findById(1L)).thenReturn(Optional.of(operationNode));
     Map<String, Object> userData = new HashMap<>();
     userData.put("isActive", true);
     userData.put("age", 30);
     boolean result = ruleService.evaluateRule(1L, userData);
     System.out.println("Evaluation Result: " + result);
     assertTrue(result); 
 }


}

