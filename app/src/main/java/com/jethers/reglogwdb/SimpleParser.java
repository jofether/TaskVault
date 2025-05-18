package com.jethers.reglogwdb;

import java.util.Stack;

public class SimpleParser {

    public static String evaluateExpression(String expression) {
        String[] tokens = expression.trim().split(" ");
        Stack<Double> numbers = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            if (isNumeric(token)) {
                numbers.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                // Check for unary minus (negative numbers)
                if (token.equals("-") && (i == 0 || isOperator(tokens[i - 1]))) {
                    String nextToken = tokens[++i];
                    numbers.push(-Double.parseDouble(nextToken));
                } else {
                    // Handle operator precedence
                    while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token)) {
                        double b = numbers.pop();
                        double a = numbers.pop();
                        String op = operators.pop();
                        numbers.push(applyOperation(a, b, op));
                    }
                    operators.push(token);
                }
            }
        }

        // Final evaluation of remaining operators
        while (!operators.isEmpty()) {
            double b = numbers.pop();
            double a = numbers.pop();
            String op = operators.pop();
            numbers.push(applyOperation(a, b, op));
        }

        // Return final result as a string
        return String.valueOf(numbers.pop());
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isOperator(String str) {
        return str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/");
    }

    private static int precedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                return 0;
        }
    }

    private static double applyOperation(double a, double b, String operator) {
        switch (operator) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) throw new UnsupportedOperationException("Cannot divide by zero");
                return a / b;
            default:
                throw new IllegalArgumentException("Invalid operator");
        }
    }
}
