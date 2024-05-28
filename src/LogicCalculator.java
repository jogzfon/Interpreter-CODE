import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogicCalculator {

    // Method to calculate logic based on given expressions and operator
    public boolean evaluate(String expression) throws CODEExceptions.NotExistingVariableName, CODEExceptions.JuxtapositionArithmeticException {
        // Remove unnecessary outer parentheses
        expression = removeOuterParentheses(expression);
        expression = expression.replaceAll("\"TRUE\"", 1+"");
        expression = expression.replaceAll("\"FALSE\"", 0+"");
        // Split expression into parts based on AND or OR operators
        String[] parts = expression.split("\\b(AND|OR)\\b");
        boolean result = true; // Default to true if there are no operations

        // Evaluate each part separately
        for (String part : parts) {
            part = part.trim(); // Remove leading and trailing spaces
            boolean partResult = evaluatePart(part);

            // Combine results based on AND or OR operators
            if (expression.contains(" AND ")) {
                result = result & partResult;
            } else if (expression.contains(" OR ")) {
                result = result | partResult;
            } else {
                result = partResult; // Single expression
            }
        }

        return result;
    }

    // Method to evaluate a single part of the expression
    private boolean evaluatePart(String part) throws CODEExceptions.NotExistingVariableName, CODEExceptions.JuxtapositionArithmeticException {
        // Check if it's a negated expression
        if (part.startsWith("NOT")) {
            return !evaluate(removeOuterParentheses(part.substring(3).trim()));
        } else {
            return evaluateExpression(part);
        }
    }

    // Method to remove unnecessary outer parentheses
    private String removeOuterParentheses(String expression) {
        int openingParenIndex = expression.indexOf('(');
        int closingParenIndex = expression.lastIndexOf(')');

        if (openingParenIndex == 0 && closingParenIndex == expression.length() - 1) {
            return expression.substring(1, closingParenIndex);
        }
        return expression;
    }

    // Method to evaluate arithmetic expression
    private boolean evaluateExpression(String expression) throws CODEExceptions.JuxtapositionArithmeticException {
        JuxtapositionCalculator calc = new JuxtapositionCalculator();
        double value = calc.calculate(expression);
        return value > 0;
    }

    public static void main(String[] args) throws CODEExceptions.NotExistingVariableName, CODEExceptions.JuxtapositionArithmeticException {
        String expression = "(5==0) AND (5==5)";

        LogicCalculator calculator = new LogicCalculator();
        boolean result = calculator.evaluate(expression);

        System.out.println("Result of the expression: " + result);
    }

}
