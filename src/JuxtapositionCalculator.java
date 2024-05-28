import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JuxtapositionCalculator {
    private int index = 0;
    private String expression;
    private String wholeExpression;

    // Method to parse and evaluate the expression
    public double calculate(String expression) throws CODEExceptions.JuxtapositionArithmeticException {
        wholeExpression = expression;
        if (expression == null || expression.isEmpty()) {
            throw new CODEExceptions.JuxtapositionArithmeticException("Expression is empty or null: "+ expression);
        }

        this.expression = expression.replaceAll("\\s", ""); // Remove whitespace
        this.index = 0; // Reset index

        return parseExpression();
    }

    // Method to parse an expression
    private double parseExpression() throws CODEExceptions.JuxtapositionArithmeticException {
        double result = parseComparison();

        while (index < expression.length()) {
            char operator = expression.charAt(index);
            if (operator == '+' || operator == '-') {
                index++;
                double nextTerm = parseComparison();
                if (operator == '+') {
                    result += nextTerm;
                } else {
                    result -= nextTerm;
                }
            } else {
                break;
            }
        }

        return result;
    }

    // Method to parse a comparison expression
    private double parseComparison() throws CODEExceptions.JuxtapositionArithmeticException, CODEExceptions.JuxtapositionArithmeticException {
        double leftOperand = parseTerm();

        while (index < expression.length()) {
            char operator = expression.charAt(index);
            if (operator == '>' || operator == '<' || operator == '=' || operator == '!'
                    || operator == '+' || operator == '-' || operator == '/'|| operator == '%') {
                index++;
                char nextChar = (index < expression.length()) ? expression.charAt(index) : 0;
                if (nextChar == '=') {
                    index++;
                    switch (operator) {
                        case '>':
                            return leftOperand >= parseTerm() ? 1 : 0;
                        case '<':
                            return leftOperand <= parseTerm() ? 1 : 0;
                        case '=':
                            return leftOperand == parseTerm() ? 1 : 0;
                        case '!':
                            return leftOperand != parseTerm() ? 1 : 0;
                        case '+':
                            return leftOperand += parseTerm();
                        case '-':
                            return leftOperand -= parseTerm();
                        case '/':
                            return leftOperand /= parseTerm();
                        case '%':
                            return leftOperand %= parseTerm();
                        default:
                            throw new CODEExceptions.JuxtapositionArithmeticException("Invalid operator: " + operator + " in: "+ expression);
                    }
                } else if (nextChar == '>') {
                    index++;
                    switch (operator) {
                        case '<':
                            return leftOperand != parseTerm() ? 1 : 0;
                        default:
                            throw new CODEExceptions.JuxtapositionArithmeticException("Invalid operator: " + operator + " in: "+ expression);
                    }
                } else if(nextChar == '-'){
                    // Handle decrement operation
                    index++;
                    if (operator == '-') {
                        leftOperand--;
                    } else {
                        throw new CODEExceptions.JuxtapositionArithmeticException("Invalid operator: " + operator + " in: "+ expression);
                    }
                } else if(nextChar == '+'){
                    // Handle increment operation
                    index++;
                    if (operator == '+') {
                        leftOperand = leftOperand + 1;
                    } else {
                        throw new CODEExceptions.JuxtapositionArithmeticException("Invalid operator: " + operator + " in: "+ expression);
                    }
                } else {
                    switch (operator) {
                        case '>':
                            return leftOperand > parseTerm() ? 1 : 0;
                        case '<':
                            return leftOperand < parseTerm() ? 1 : 0;
                        case '!':
                            return leftOperand != parseTerm() ? 1 : 0;
                        case '+':
                            return leftOperand + parseTerm();
                        default:
                            throw new CODEExceptions.JuxtapositionArithmeticException("Invalid operator: " + operator + " in: "+ expression);
                    }
                }
            } else{
                break;
            }
        }

        return leftOperand;
    }

    // Method to parse a term
    private double parseTerm() throws CODEExceptions.JuxtapositionArithmeticException, CODEExceptions.JuxtapositionArithmeticException {
        double result = parseFactor();

        while (index < expression.length()) {
            char operator = expression.charAt(index);
            if (operator == 'x' || operator == '*') {
                index++;
                result *= parseFactor();
            } else if (operator == '/') {
                index++;
                double divisor = parseFactor();
                if (divisor == 0) {
                    throw new CODEExceptions.JuxtapositionArithmeticException("Division by zero: " + expression);
                }
                result /= divisor;
            } else if (operator == '%') {
                index++;
                double divisor = parseFactor();
                if (divisor == 0) {
                    throw new CODEExceptions.JuxtapositionArithmeticException("Modulo by zero: " + expression);
                }
                result %= divisor;
            } else {
                break;
            }
        }

        return result;
    }

    // Method to parse a factor
    private double parseFactor() throws CODEExceptions.JuxtapositionArithmeticException, CODEExceptions.JuxtapositionArithmeticException {
        if (index >= expression.length()) {
            throw new CODEExceptions.JuxtapositionArithmeticException("Invalid Expression: "+ expression);
        }

        char firstChar = expression.charAt(index);
        if (Character.isDigit(firstChar) || firstChar == '-') {
            Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
            Matcher matcher = pattern.matcher(expression.substring(index));
            if (matcher.find()) {
                String numberStr = matcher.group();
                index += numberStr.length();
                return Double.parseDouble(numberStr);
            } else {
                throw new CODEExceptions.JuxtapositionArithmeticException("Invalid number format: "+ expression);
            }
        } else if (firstChar == '(' || firstChar == '[') {
            char closingBracket = (firstChar == '(') ? ')' : ']';
            index++;
            double result = parseExpression();
            if (index >= expression.length() || expression.charAt(index) != closingBracket) {
                throw new CODEExceptions.JuxtapositionArithmeticException("Missing closing bracket in: " + expression);
            }
            index++; // Move past closing bracket
            return result;
        } else {
            //throw new CodeExceptions.JuxtapositionArithmeticException("Undeclared variable found in: " + expression);
            return 0.0;
        }
    }
}


class Cloco {
    public static void main(String[] args) throws CODEExceptions.JuxtapositionArithmeticException {
        String expression = "((23==23)+(42>=42))";

        JuxtapositionCalculator calculator = new JuxtapositionCalculator();
        double result = calculator.calculate(expression);

        System.out.println("Result of the expression: " + result);
    }
}
