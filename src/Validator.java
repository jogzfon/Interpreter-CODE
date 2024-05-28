import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator extends CODEExceptions{
    //Checks the code if it has BEGIN CODE and END CODE
    public static void CodeStartAndEnded(List<Token> tokensList) throws CODEExceptions.BeginEndCodePresentException, ENDCODEException, BEGINCODEException {
        boolean started = false;
        boolean ended = false;

        if(tokensList.size()==0){
            return;
        }
        for(Token token: tokensList){
            if(token.getType() == Token.TokenType.BEGIN_CODE){
                if(!started){
                    started = true;
                }else{
                    throw new CODEExceptions.BeginEndCodePresentException("BEGIN CODE already exists!");
                }
            }
            if(token.getType() == Token.TokenType.END_CODE){
                if(!ended){
                    ended = true;
                }else{
                    throw new CODEExceptions.BeginEndCodePresentException("END CODE already exists!");
                }
            }
        }
        if(!started && ended){
            throw new BEGINCODEException("BEGIN CODE does not exist!");
        }
        if(!ended && started){
            throw new ENDCODEException("END CODE does not exist!");
        }
        return;
    }
    //Checks if Variable name follows the syntax
    public static boolean isValidVariableName(String name) throws CODEExceptions.InvalidVariableName {
        // Regular expression pattern to match valid variable names
        String pattern = "[a-zA-Z_][a-zA-Z_0-9]*";

        // Create a Pattern object
        Pattern regex = Pattern.compile(pattern);

        // Create a Matcher object
        Matcher matcher = regex.matcher(name);

        // Check if the name matches the pattern
        if (matcher.matches()) {
            // Check if the name is not a reserved word
            if (isReservedWord(name)) {
                throw new CODEExceptions.InvalidVariableName("Variable name cannot be a reserved word: " + name);
            }
            return true;
        } else {
            throw new CODEExceptions.InvalidVariableName("Invalid variable name: " + name);
        }
    }
    // Method to check if a variable name is a reserved word
    private static boolean isReservedWord(String name) {
        // List of reserved words
        List<String> reservedWords = new ArrayList<>();
        reservedWords.add("INT");
        reservedWords.add("FLOAT");
        reservedWords.add("CHAR");
        reservedWords.add("BOOL");
        reservedWords.add("DISPLAY");
        reservedWords.add("SCAN");
        reservedWords.add("WHILE");
        reservedWords.add("IF");
        reservedWords.add("BEGIN");
        reservedWords.add("CODE");
        reservedWords.add("ELSE");
        reservedWords.add("END");
        reservedWords.add("SCAN");

        // Add more reserved words if needed
        return reservedWords.contains(name);
    }

    public static boolean isValidBoolValue(String value) throws CODEExceptions.BOOLDeclarationException {
        // Bool value must be exactly "TRUE" or "FALSE" (case insensitive)
        if (value.equalsIgnoreCase("\"TRUE\"") || value.equalsIgnoreCase("\"FALSE\"")) {
            return true;
        } else {
            throw new CODEExceptions.BOOLDeclarationException("Validation - Invalid BOOL value: " + value);
        }
    }
    public static boolean isValidFLOATValue(String value) throws CODEExceptions.FLOATDeclarationException {

        if (value.matches("-?\\d+") || value.matches("-?\\d+(\\.\\d+)?")) {
            return true;
        } else {
            throw new CODEExceptions.FLOATDeclarationException("Validation - Invalid FLOAT value: " + value);
        }
    }
}
