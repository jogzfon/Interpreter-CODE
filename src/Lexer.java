import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public static List<Token> tokenize(String code) throws Exception {
        List<Token> tokenList = new ArrayList<>();
        boolean insideCodeBlock = false;

        // Regular expressions for tokenization
        Pattern pattern = Pattern.compile("\\b(INT|CHAR|BOOL|FLOAT|DISPLAY|END_CODE)\\b|\"[^\"]*\"|'[^']*'|\\b[\\w']+\\b|\\S");

        String[] lines = code.split("\n");

        for (String line : lines) {
            switch (line.trim()){
                case "BEGIN CODE":
                    tokenList.add(new Token(Token.TokenType.BEGIN_CODE, "BEGIN CODE"));
                    insideCodeBlock = true;
                    continue;
                case "END CODE":
                    tokenList.add(new Token(Token.TokenType.END_CODE, "END CODE"));
                    insideCodeBlock = false;
                    continue;
                case "BEGIN IF":
                    tokenList.add(new Token(Token.TokenType.IF_BEGIN, "BEGIN IF"));
                    continue;
                case "END IF":
                    tokenList.add(new Token(Token.TokenType.IF_END, "END IF"));
                    continue;
                case "BEGIN WHILE":
                    tokenList.add(new Token(Token.TokenType.WHILE_BEGIN, "BEGIN WHILE"));
                    continue;
                case "END WHILE":
                    tokenList.add(new Token(Token.TokenType.WHILE_END, "END WHILE"));
                    continue;
            }

            if (!line.trim().startsWith("#") && insideCodeBlock) {
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String token = matcher.group();

                    if (!token.isEmpty()) {
                        Token.TokenType type;

                        if (token.matches("\\b(SCAN)\\b")) {
                            tokenList.add(new Token(Token.TokenType.SCAN, token));
                            continue;
                        }
                        else if (token.matches("\\b(DISPLAY)\\b")) {
                            type = Token.TokenType.DISPLAY;
                        }else if (token.matches("\\b(INT|CHAR|BOOL|FLOAT)\\b")) {
                            type = Token.TokenType.DATATYPE;
                        }else if (token.matches("\\b(IF|WHILE|ELSE)\\b")) {
                            type = Token.TokenType.CONDITIONALS;
                        }else if (token.matches("[()+*/%><=]|[-]")) {
                            if (token.equals("=")){
                                type = Token.TokenType.EQUALS;
                            }else{
                                type = Token.TokenType.OPERATOR;
                            }
                        } else if (token.matches("(AND|OR|NOT)")) {
                            type = Token.TokenType.VALUE;
                        } else if (token.matches("[<>]=?|[=][=]|[<>]]")) {
                            type = Token.TokenType.OPERATOR;
                        }else if (token.matches("[:]|[$]|[&]")) {
                            type = Token.TokenType.SYMBOL;
                        }else if (token.matches("[,]")) {
                            type = Token.TokenType.COMMA;
                        } else if (token.matches("[\\[]|[\\]]")){
                            type = Token.TokenType.ESCAPE;
                        } else if (token.matches("'.+'")|| token.matches("\\.")) {
                            type = Token.TokenType.VALUE;
                        }else if (token.matches("\".+\"")||token.matches("\"\"")) {
                            type = Token.TokenType.STRING;
                        }else {
                            type = Token.TokenType.VARIABLE;
                        }
                        tokenList.add(new Token(type, token));
                    }
                }
            }
            //Adds an END LINE after each line
            if(insideCodeBlock){
                tokenList.add(new Token(Token.TokenType.END_LINE, "END_LINE"));
            }
        }

        //This is a Validator checks if program has Began and Ended Successfully
        Validator validator = new Validator();
        validator.CodeStartAndEnded(tokenList);

        //This checker checks whether there is an equal amount of -- BEGIN IF and END IF :: BEGIN WHILE and END WHILE
        ConditionalChecker conditionalChecker = new ConditionalChecker();
        conditionalChecker.CheckIFCount(tokenList);
        conditionalChecker.CheckWHILECount(tokenList);

        //This will connect DISPLAY & SCAN with their :
        //Connects [ escape ]
        //Connects expressions
        LineConnector lineConnector = new LineConnector();
        tokenList = lineConnector.ConnectLines(tokenList);
        return tokenList;
    }
}

