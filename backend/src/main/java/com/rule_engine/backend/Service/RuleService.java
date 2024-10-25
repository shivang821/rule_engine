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
    public void deleteAllRules() {
        ruleRepository.deleteAll();
        System.out.println("all rules deleted");
    }
    public List<RuleInfoResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(rule -> new RuleInfoResponse(rule.getId(), rule.getRuleString()))
                .collect(Collectors.toList());
    }
    public Rule createRule(String ruleString) {
        String formattedRuleString = formatRuleString(ruleString);

        // Validate the formatted rule
        Rule rule = new Rule();
        RuleValidator validator=new RuleValidator();
        rule.setRuleString(formattedRuleString);
        if (!validator.validateRule(formattedRuleString)) {
            System.out.println("&&&&&&&&&&&&&");
            throw new IllegalArgumentException("Invalid rule: " + ruleString);
        }


        return ruleRepository.save(rule);
    }

    private String formatRuleString(String ruleString) {
        String formatted = ruleString.replaceAll("\\s*([<>=!]=?)\\s*", " $1 ");

        formatted = formatted.replaceAll("(?i)\\band\\b", " AND ")
                .replaceAll("(?i)\\bor\\b", " OR ");

        formatted = formatted.replaceAll("\\s*\\(\\s*", " ( ")
                .replaceAll("\\s*\\)\\s*", " ) ");

        return formatted.replaceAll("\\s{2,}", " ").trim();
    }

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
    private Predicate<Map<String, Object>> createPredicateFromExpression(String expression) {
        expression = expression.trim();

        // Determine the operator used in the expression
        String[] operators = {">=", "<=", ">", "<", "==", "!=","="};
        String finalExpression = expression;
        String selectedOperator = Arrays.stream(operators)
                .filter(expression::contains)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid operator in expression: " + finalExpression));

        // Split the expression based on the selected operator
        String[] parts = expression.split(Pattern.quote(selectedOperator));
        String left = parts[0].trim().toLowerCase();  // Column name, case-insensitive
        String right = parts[1].trim().replaceAll("'", "");  // Value to compare

        return row -> {
            // Convert the row's keys to lowercase for dynamic column matching
            Map<String, Object> normalizedRow = row.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toLowerCase(),  // Normalize column name to lowercase
                            Map.Entry::getValue
                    ));

            Object leftValue = normalizedRow.get(left);
            if (leftValue == null) {
                throw new IllegalArgumentException("Column not found: " + left);
            }

            return compare(leftValue.toString().toLowerCase(), right.toLowerCase(), selectedOperator);
        };
    }

    private boolean compare(String leftValue, String rightValue, String operator) {
        switch (operator) {
            case ">=":
                return leftValue.compareTo(rightValue) >= 0;
            case "<=":
                return leftValue.compareTo(rightValue) <= 0;
            case ">":
                return leftValue.compareTo(rightValue) > 0;
            case "<":
                return leftValue.compareTo(rightValue) < 0;
            case "==":
                return leftValue.equalsIgnoreCase(rightValue);// Case-insensitive comparison
            case "=":
                return leftValue.equalsIgnoreCase(rightValue);
            case "!=":
                return !leftValue.equalsIgnoreCase(rightValue);  // Case-insensitive comparison
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

