package com.rule_engine.backend.Utility;

import java.util.*;
import java.util.regex.*;

public class RuleValidator {

    private final RuleParser ruleParser = new RuleParser(); // Assuming RuleParser is your AST parser.

    /**
     * Validates a rule string by converting it to an AST.
     * @param ruleString The rule string to validate.
     * @return true if the rule is valid, otherwise throws an exception.
     */
    public boolean validateRule(String ruleString) {
        try {
            // Step 1: Parse the rule into an AST
            ASTNode root = ruleParser.parseRule(ruleString);

            // Step 2: Traverse the AST to ensure correctness
            validateAST(root);

            return true; // No errors found, rule is valid
        } catch (IllegalArgumentException | NullPointerException e) {
            System.err.println("Invalid rule: " + e.getMessage());
            return false; // Rule is invalid
        }
    }

    /**
     * Validates the AST recursively to ensure proper structure.
     * @param node The current AST node to validate.
     */
    private void validateAST(ASTNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Invalid rule: AST node is null.");
        }

        if (node.isLeaf()) {
            validateComparison(node.getValue()); // Check if the comparison is valid
        } else {
            // Ensure that non-leaf nodes have valid operators
            if (!isValidOperator(node.getValue())) {
                throw new IllegalArgumentException("Invalid operator: " + node.getValue());
            }

            // Recursively validate child nodes
            validateAST(node.getLeft());
            validateAST(node.getRight());
        }
    }

    /**
     * Checks if a comparison string is valid (e.g., "salary <= 3000").
     * @param comparison The comparison string to validate.
     */
    private void validateComparison(String comparison) {
        // Regex to match valid comparisons like "age >= 30" or "department == IT"
        Pattern pattern = Pattern.compile("^(\\w+)\\s*([<>=!]+)\\s*(\\w+|'[^']+')$");
        if (!pattern.matcher(comparison).matches()) {
            throw new IllegalArgumentException("Malformed comparison found: " + comparison);
        }
    }

    /**
     * Validates if the operator is one of the allowed operators (AND, OR).
     * @param operator The operator to validate.
     * @return true if the operator is valid, otherwise false.
     */
    private boolean isValidOperator(String operator) {
        return operator.equals("AND") || operator.equals("OR");
    }

    public static void main(String[] args) {
        RuleValidator validator = new RuleValidator();

        // Test valid rule
        String validRule = "(salary <= 3000 AND department == IT)";
        System.out.println("Valid Rule Test: " + validator.validateRule(validRule)); // Should print: true

        // Test invalid rule with dangling operator
        String invalidRule = "(salary <= 3000 and)";
        System.out.println("Invalid Rule Test: " + validator.validateRule(invalidRule)); // Should print: false

        // Test invalid rule with malformed comparison
        String malformedRule = "(salary <= 3000 >)";
        System.out.println("Malformed Rule Test: " + validator.validateRule(malformedRule)); // Should print: false
    }
}
