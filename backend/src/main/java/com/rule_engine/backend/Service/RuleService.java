package com.rule_engine.backend.Service;
import com.rule_engine.backend.DTO.RuleInfoResponse;
import com.rule_engine.backend.Model.Rule;
import com.rule_engine.backend.Repository.RuleRepository;
import com.rule_engine.backend.Utility.ASTNode;
import com.rule_engine.backend.Utility.RuleParser;
import com.rule_engine.backend.Utility.RuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RuleService {
    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private CSVService csvFileService;
    public Rule getRuleById(Long ruleId) {
        return ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found with ID: " + ruleId));
    }
    // create rule logic start
    public List<RuleInfoResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(rule -> new RuleInfoResponse(rule.getId(), rule.getRuleString()))
                .collect(Collectors.toList());
    }
    public Rule createRule(String ruleString) {
        // Format the rule string
        String formattedRuleString = formatRuleString(ruleString);

        // Validate the formatted rule
        Rule rule = new Rule();
        RuleValidator validator=new RuleValidator();
        rule.setRuleString(formattedRuleString);
        if (!validator.validateRule(formattedRuleString)) {
            System.out.println("&&&&&&&&&&&&&");
            throw new IllegalArgumentException("Invalid rule: " + ruleString);
        }


        // Set other fields as needed (like astJson)
        return ruleRepository.save(rule);
    }

    private String formatRuleString(String ruleString) {
        // Ensure proper spacing around comparison operators (<, >, <=, >=, ==, !=)
        String formatted = ruleString.replaceAll("\\s*([<>=!]=?)\\s*", " $1 ");

        // Replace 'and' and 'or' with ' AND ' and ' OR ' (with spaces)
        formatted = formatted.replaceAll("(?i)\\band\\b", " AND ")
                .replaceAll("(?i)\\bor\\b", " OR ");

        // Remove unnecessary spaces around parentheses
        formatted = formatted.replaceAll("\\s*\\(\\s*", " ( ")
                .replaceAll("\\s*\\)\\s*", " ) ");

        // Ensure no extra spaces are left behind
        return formatted.replaceAll("\\s{2,}", " ").trim();
    }
//    private boolean isValidRule(String ruleString) {
//        // Implement validation logic to ensure the rule string is valid
//        // This can include checking for matching parentheses, valid operators, etc.
//        return true; // Replace with actual validation logic
//    }

    // rule creation logic end

    // Apply the rule to the CSV file content using AST traversal logic
    public List<Map<String, Object>> applyRule(Long ruleId, Long fileId) {
        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found"));

        List<Map<String, Object>> fileContent = csvFileService.getFileContent(fileId);
        ASTNode ast = parseRuleToAST(rule.getRuleString());

        return evaluateAST(ast, fileContent);
    }

    // Recursively evaluate the AST and return matching rows
    private List<Map<String, Object>> evaluateAST(ASTNode node, List<Map<String, Object>> content) {
        if (node.isLeaf()) {
            // For leaf nodes, filter the CSV data based on the condition
            return content.stream()
                    .filter(createPredicateFromExpression(node.getValue()))
                    .collect(Collectors.toList());
        }

        // Recursively evaluate left and right child nodes
        List<Map<String, Object>> leftResult = evaluateAST(node.getLeft(), content);
        List<Map<String, Object>> rightResult = evaluateAST(node.getRight(), content);

        // Apply the operator (AND/OR) on the results
        if (node.getValue().equals("AND")) {
            return leftResult.stream()
                    .filter(rightResult::contains)  // Intersection of both results
                    .collect(Collectors.toList());
        } else if (node.getValue().equals("OR")) {
            Set<Map<String, Object>> combined = new HashSet<>(leftResult);
            combined.addAll(rightResult);  // Union of both results
            return new ArrayList<>(combined);
        } else {
            throw new IllegalArgumentException("Invalid operator: " + node.getValue());
        }
    }

    // Create a predicate for filtering data based on a simple expression
//    private Predicate<Map<String, Object>> createPredicateFromExpression(String expression) {
//        String[] parts = expression.split("\\s+");  // Split by space (e.g., "age > 30")
//        String field = parts[0];
//        String operator = parts[1];
//        String value = parts[2];
//
//        return row -> {
//            Object fieldValue = row.get(field);
//
//            switch (operator) {
//                case ">":
//                    return Double.parseDouble(fieldValue.toString()) > Double.parseDouble(value);
//                case "<":
//                    return Double.parseDouble(fieldValue.toString()) < Double.parseDouble(value);
//                case "==":
//                    return fieldValue.toString().equals(value.replace("'", ""));
//                default:
//                    throw new IllegalArgumentException("Invalid operator in expression: " + expression);
//            }
//        };
//    }
    private Predicate<Map<String, Object>> createPredicateFromExpression(String expression) {
        expression = expression.trim();

        // Check for valid operators
        if (expression.contains(">=")) {
            String[] parts = expression.split(">=");
            String left = parts[0].trim();
            String right = parts[1].trim();
            return row -> compare(row, left, right, ">=");
        } else if (expression.contains("<=")) {
            String[] parts = expression.split("<=");
            String left = parts[0].trim();
            String right = parts[1].trim();
            return row -> compare(row, left, right, "<=");
        } else if (expression.contains(">")) {
            String[] parts = expression.split(">");
            String left = parts[0].trim();
            String right = parts[1].trim();
            return row -> compare(row, left, right, ">");
        } else if (expression.contains("<")) {
            String[] parts = expression.split("<");
            String left = parts[0].trim();
            String right = parts[1].trim();
            return row -> compare(row, left, right, "<");
        } else if (expression.contains("==")) {
            String[] parts = expression.split("==");
            String left = parts[0].trim();
            String right = parts[1].trim();
            return row -> compare(row, left, right, "==");
        } else if (expression.contains("!=")) {
            String[] parts = expression.split("!=");
            String left = parts[0].trim();
            String right = parts[1].trim();
            return row -> compare(row, left, right, "!=");
        } else {
            throw new IllegalArgumentException("Invalid operator in expression: " + expression);
        }
    }

    private boolean compare(Map<String, Object> row, String left, String right, String operator) {
        Object leftValue = row.get(left);
        Object rightValue = parseValue(right);

        switch (operator) {
            case ">=":
                return ((Comparable) leftValue).compareTo(rightValue) >= 0;
            case "<=":
                return ((Comparable) leftValue).compareTo(rightValue) <= 0;
            case ">":
                return ((Comparable) leftValue).compareTo(rightValue) > 0;
            case "<":
                return ((Comparable) leftValue).compareTo(rightValue) < 0;
            case "==":
                return leftValue.equals(rightValue);
            case "!=":
                return !leftValue.equals(rightValue);
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    private Object parseValue(String value) {
        // Add logic to parse value into correct type if necessary
        // Example: parse to Integer, Double, String, etc.
        if (value.matches("\\d+")) {
            return Integer.parseInt(value);
        }
        return value; // Treat as String if not a number
    }
    // Parse the rule string into an AST
    private ASTNode parseRuleToAST(String rule) {
        return RuleParser.parseRule(rule);  // Use the parser logic you developed earlier
    }
}

