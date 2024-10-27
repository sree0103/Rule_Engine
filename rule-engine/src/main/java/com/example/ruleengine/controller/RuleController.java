package com.example.ruleengine.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.ruleengine.exception.CombinationException;
import com.example.ruleengine.model.CombinationResponse;
import com.example.ruleengine.model.Node;
import com.example.ruleengine.model.RuleEvaluationResponse;
import com.example.ruleengine.model.RuleResponse;
import com.example.ruleengine.service.RuleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/rules")
public class RuleController {

    @Autowired
    private RuleService ruleService;
    @GetMapping("/create")
    public String showCreateRuleForm() {
        return "create-rule"; 
    }
    @PostMapping("/create")
    public String createRule(@RequestParam String rule, Model model) {
        Node createdNode = ruleService.createRule(rule);
        model.addAttribute("response", new RuleResponse("success", createdNode, null));
        return "rule-result";
    }
    @GetMapping("/evaluate")
    public String showEvaluateRuleForm() {
        return "evaluate-rule";
    }
    @PostMapping("/evaluate")
    public String evaluateRule(@RequestParam Long megaRuleId, 
                               @RequestParam String userData, 
                               Model model) {
        if (megaRuleId == null || megaRuleId <= 0) {
            model.addAttribute("response", new RuleEvaluationResponse("error", false, "Invalid megaRuleId."));
            return "evaluation-result";
        }
        Map<String, Object> userDataMap;
        try {
            userDataMap = new ObjectMapper().readValue(userData, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            model.addAttribute("response", new RuleEvaluationResponse("error", false, "Invalid user data format. Please provide valid JSON."));
            return "evaluation-result";
        }
        if (userDataMap.isEmpty()) {
            model.addAttribute("response", new RuleEvaluationResponse("error", false, "User data cannot be empty."));
            return "evaluation-result";
        }

        try {
            boolean result = ruleService.evaluateRule(megaRuleId, userDataMap);
            model.addAttribute("response", new RuleEvaluationResponse("success", result, null));
        } catch (IllegalArgumentException e) {
            model.addAttribute("response", new RuleEvaluationResponse("error", false, e.getMessage()));
        } catch (CombinationException e) {
            model.addAttribute("response", new RuleEvaluationResponse("error", false, "Combination error: " + e.getMessage()));
        } catch (Exception e) {
            model.addAttribute("response", new RuleEvaluationResponse("error", false, e.getMessage()));
        }
        return "evaluation-result"; 
    }
    @GetMapping("/combine")
    public String showCombineRulesForm() {
        return "combine-rules";
    }
    @PostMapping("/combine")
    public String combineRules(@RequestParam List<Long> ruleIds, Model model) {
        try {
            Node combinedNode = ruleService.combineRules(ruleIds);
            model.addAttribute("response", new CombinationResponse("success", combinedNode, null));
        } catch (CombinationException e) {
            model.addAttribute("response", new CombinationResponse("error", null, e.getMessage()));
        } catch (Exception e) {
            model.addAttribute("response", new CombinationResponse("error", null, e.getMessage()));
        }
        return "combination-result";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", "Error: " + e.getMessage());
        return "error";
    }
}
