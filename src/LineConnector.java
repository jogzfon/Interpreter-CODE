import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineConnector {
    LineNumberChecker lineCheck;
    public LineConnector(){
        lineCheck = new LineNumberChecker();
    }

    public List<Token> ConnectLines(List<Token> tokenList) throws Exception {
        List<Token> retoken = new ArrayList<>();

        StringBuilder expressionBuilder = new StringBuilder();

        boolean isOpenEscape = false;

        LineNumberChecker lineCheck = new LineNumberChecker();

        for(int i=0;i<tokenList.size();i++){
            //This will check if [ is present and if it is then isOpenEscape true and it will append all the strings after it until ]
            if(tokenList.get(i).getType() == Token.TokenType.ESCAPE){
                if(tokenList.get(i).getValue().equals("[")){
                    isOpenEscape = true;
                }
                //if ] is present then it will isOpenEscape false and tokenize the string
                else if(tokenList.get(i).getValue().equals("]") && (i+1)<tokenList.size()
                        && tokenList.get(i+1).getType() != Token.TokenType.ESCAPE && isOpenEscape){
                    expressionBuilder.append(tokenList.get(i).getValue());
                    retoken.add(new Token(Token.TokenType.ESCAPE, expressionBuilder.toString()));
                    expressionBuilder.setLength(0); // Reset the StringBuilder
                    isOpenEscape = false;
                    continue;
                }
                //if ] happens without a [ then it will throw an exception
                else if(tokenList.get(i).getValue().equals("]") && !isOpenEscape){
                    throw new CODEExceptions.BracketException("Open Bracket [ is missing!");
                }

            }
            //Checks whether DISPLAY is followed by : and if is not, an exception will be thrown
            else if (tokenList.get(i).getType() == Token.TokenType.DISPLAY && !tokenList.get(i).getValue().contains(":")) {
                if((i+1 < tokenList.size()) && tokenList.get(i+1).getValue().equals(":")){
                    retoken.add(new Token(Token.TokenType.DISPLAY, tokenList.get(i).getValue()+tokenList.get(i+1).getValue()));
                    i++;
                    continue;
                }else{
                    throw new CODEExceptions.DISPLAYException("DISPLAY has no \":\" symbol at line: " + lineCheck.find(i, tokenList));
                }

            }
            //Checks whether SCAN is followed by : and if is not, an exception will be thrown
            else if (tokenList.get(i).getType() == Token.TokenType.SCAN && !tokenList.get(i).getValue().contains(":")) {
                if((i+1 < tokenList.size()) && tokenList.get(i+1).getValue().equals(":")){
                    retoken.add(new Token(Token.TokenType.SCAN, tokenList.get(i).getValue()+tokenList.get(i+1).getValue()));
                    i++;
                    continue;
                }else{
                    throw new CODEExceptions.DISPLAYException("SCAN has no \":\" symbol at line: " + lineCheck.find(i, tokenList));
                }
            }

            //If is Open Escape like [---- all the tokens will be appended until it is closed
            if(isOpenEscape){
                expressionBuilder.append(tokenList.get(i).getValue());
            }
            // Check if the token's type is VARIABLE, OPERATOR, or VALUE and append them into a string
            else if (tokenList.get(i).getType() == Token.TokenType.OPERATOR ||
                    tokenList.get(i).getType() == Token.TokenType.VALUE
                    || isNumeric(tokenList.get(i).getValue())) {
                // Concatenate the token's value to the expression
                if(tokenList.get(i).getValue().equals("AND")||tokenList.get(i).getValue().equals("OR")){
                    expressionBuilder.append(" "+tokenList.get(i).getValue() + " ");
                }else{
                    expressionBuilder.append(tokenList.get(i).getValue());
                }
            }
            // Check if the stringbuilder have a string and if it does then consider it as a value
            else {
                if (expressionBuilder.length() > 0) {
                    retoken.add(new Token(Token.TokenType.VALUE, expressionBuilder.toString()));
                    expressionBuilder.setLength(0); // Reset the StringBuilder
                }

                // Add the token directly to retoken
                retoken.add(tokenList.get(i));
            }
        }
        //If [ exist and the there is no ] in the list of tokens and exception will be thrown
        if(isOpenEscape){
            throw new CODEExceptions.BracketException("Closing Bracket ] is missing!");
        }
        return retoken;
    }

    //Connects all the values from variables to values datatype
    //Called whenever a calculation of the value is required
    public List<Token> ArithmeticLineConnector(Parser parser, List<Token> tokens) throws Exception {

        List<Token> retoken = new ArrayList<>();

        StringBuilder expressionBuilder = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {
            if(tokens.get(i).getType() == Token.TokenType.VARIABLE && (i+1)<tokens.size()
                    && (i-1)>=0 && (tokens.get(i+1).getType() == Token.TokenType.VALUE||(tokens.get(i-1).getType() == Token.TokenType.VALUE))){
                VariableDeclarationNode varNode = parser.FindDeclaredNode(tokens.get(i).getValue());

                if(varNode!=null){
                    if(varNode.getValue()!="?"){
                        expressionBuilder.append(tokens.get(i).getValue());
                    }
                    else{
                        retoken.add(new Token(Token.TokenType.VALUE, expressionBuilder.toString()));
                        expressionBuilder.setLength(0); // Reset the StringBuilder
                        retoken.add(tokens.get(i));
                    }
                }else{
                    if (expressionBuilder.length() > 0) {
                        retoken.add(new Token(Token.TokenType.VALUE, expressionBuilder.toString()));
                        expressionBuilder.setLength(0); // Reset the StringBuilder
                    }
                    // Add the token directly to retoken
                    retoken.add(tokens.get(i));
                }
            }else if((tokens.get(i).getType() == Token.TokenType.EQUALS
                    || tokens.get(i).getValue().equals("<")
                    || tokens.get(i).getValue().equals(">")
                    || tokens.get(i).getValue().equals("!")
                    || tokens.get(i).getValue().equals("+")
                    || tokens.get(i).getValue().equals("*")
                    || tokens.get(i).getValue().equals("-")
                    || tokens.get(i).getValue().equals("/")
                    || tokens.get(i).getValue().equals("%"))
                    && (i+1)<tokens.size() && tokens.get(i+1).getType() == Token.TokenType.EQUALS ) {
                retoken.add(new Token(Token.TokenType.VALUE, tokens.get(i).getValue() + tokens.get(i+1).getValue()));
                i++;
            } else if((i+1)<tokens.size() && (tokens.get(i).getValue().equals("+") && tokens.get(i+1).getValue().equals("+"))
                    || (tokens.get(i).getValue().equals("-") && tokens.get(i+1).getValue().equals("-"))) {
                retoken.add(new Token(Token.TokenType.VALUE, tokens.get(i).getValue() + tokens.get(i+1).getValue()));
                i++;
            }else if(tokens.get(i).getValue().equals("IF")&&tokens.get(i).getValue().equals("WHILE")){
                while (tokens.get(i).getType()==Token.TokenType.VARIABLE || tokens.get(i).getType()==Token.TokenType.VALUE){
                    expressionBuilder.append(tokens.get(i).getValue() + " ");
                    i++;
                }
                retoken.add(new Token(Token.TokenType.VALUE, expressionBuilder.toString()));
                expressionBuilder.setLength(0); // Reset the StringBuilder
                retoken.add(tokens.get(i));
            }else if(tokens.get(i).getValue().equals("(")){
                String val = tokens.get(i).getValue();
                while ( val.charAt(val.length()-1) != ')' && tokens.get(i).getType()!=Token.TokenType.END_LINE){
                    expressionBuilder.append(tokens.get(i).getValue() + " ");
                    i++;
                }
                retoken.add(new Token(Token.TokenType.VALUE, expressionBuilder.toString()));
                expressionBuilder.setLength(0); // Reset the StringBuilder
                retoken.add(tokens.get(i));
            }else{
                if (expressionBuilder.length() > 0) {
                    retoken.add(new Token(Token.TokenType.VALUE, expressionBuilder.toString()));
                    expressionBuilder.setLength(0); // Reset the StringBuilder
                }
                // Add the token directly to retoken
                retoken.add(tokens.get(i));
            }
        }
        return retoken;
    }

    //Checks if it is a number
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
