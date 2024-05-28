import java.util.List;

public class LineNumberChecker {
    int find(int currentTokenIndex, List<Token> tokens){
        int lineNumber = 0;

        //The data types signifies an end of a single line
        for(int i=0; i<=tokens.size();i++){
            if(tokens.get(i).getType()== Token.TokenType.END_LINE
                    || tokens.get(i).getType()== Token.TokenType.IF_BEGIN
                    || tokens.get(i).getType()== Token.TokenType.IF_END
                    || tokens.get(i).getType()== Token.TokenType.WHILE_BEGIN
                    || tokens.get(i).getType()== Token.TokenType.WHILE_END
                    || tokens.get(i).getType()== Token.TokenType.BEGIN_CODE
                    || tokens.get(i).getType()== Token.TokenType.END_CODE)
            {
                lineNumber++;
            }

            if(i >= currentTokenIndex && (tokens.get(i).getType()== Token.TokenType.END_LINE
                    || tokens.get(i).getType()== Token.TokenType.IF_BEGIN
                    || tokens.get(i).getType()== Token.TokenType.IF_END
                    || tokens.get(i).getType()== Token.TokenType.WHILE_BEGIN
                    || tokens.get(i).getType()== Token.TokenType.WHILE_END
                    || tokens.get(i).getType()== Token.TokenType.BEGIN_CODE
                    || tokens.get(i).getType()== Token.TokenType.END_CODE))
            {
                break;
            }
        }

        return lineNumber;
    }
}
