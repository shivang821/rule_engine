package com.rule_engine.backend.Utility;

import java.util.Stack;

public class RuleParser {

    public static ASTNode parseRule(String rule) {
        rule = rule.trim();

        // Remove outer parentheses if present
        if (rule.startsWith("(") && rule.endsWith(")")) {
            rule = rule.substring(1, rule.length() - 1).trim();
        }

        int operatorIndex = findOperator(rule);
        if (operatorIndex == -1) {
            return new ASTNode(rule);  // Leaf node
        }

        String operator = rule.substring(operatorIndex, operatorIndex + 3).trim();  // AND or OR
        ASTNode left = parseRule(rule.substring(0, operatorIndex).trim());
        ASTNode right = parseRule(rule.substring(operatorIndex + 3).trim());

        return new ASTNode(operator, left, right);
    }

    private static int findOperator(String rule) {
        int balance = 0;
        for (int i = 0; i < rule.length(); i++) {
            char c = rule.charAt(i);
            if (c == '(') balance++;
            if (c == ')') balance--;

            if (balance == 0) {
                if (rule.startsWith("AND", i)) return i;
                if (rule.startsWith("OR", i)) return i;
            }
        }
        return -1;
    }
}
