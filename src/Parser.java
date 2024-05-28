import org.w3c.dom.Node;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    List<Token> tokens;

    LineConnector lineConnector;
    LineNumberChecker lineCheck;

    private List<VariableDeclarationNode> declarationNodes;

    //Starts at 1 to skip BEGIN CODE
    private int currentTokenIndex = 1;
    private Set<String> declaredVariableNames = new HashSet<>();
    Stack<String> comebackConditionStack = new Stack<>();
    Stack<Integer> whileComebackTokenIndex = new Stack<>();

    List<VariableDeclarationNode> ifVariables = new ArrayList<>();
    List<VariableDeclarationNode> whileVariables = new ArrayList<>();

    private List<String> ifCondition = new ArrayList<>();
    private List<String> whileCondition = new ArrayList<>();

    private boolean ifStarted = false;
    private boolean ifInside = false;
    private boolean whileStarted = false;
    private boolean whileInside = false;

    private boolean outputNull = true;



    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        lineCheck = new LineNumberChecker();
        lineConnector = new LineConnector();
        declarationNodes = new ArrayList<>();
    }

    public void ParseTokens() throws Exception {
        while (tokens.get(currentTokenIndex).getType() != Token.TokenType.END_CODE) {
            arithConnector();
            ParseStatement();
        }
        if(outputNull==true){
            System.out.println("No Error Found");
        }
    }

    private void ParseStatement() throws Exception{
        Token currentToken = tokens.get(currentTokenIndex);
        // Depending on the type of token, perform appropriate parsing
        switch (currentToken.getType()) {
            case DATATYPE:
                ParseVariableDeclaration();
                break;
            case DISPLAY:
                ParseDisplayStatement();
                break;
            case SCAN:
                ParseScanInput();
                break;
            case VARIABLE:
                ParseAssignmentStatement();
                break;
            case VALUE:
                parseValueStatement();
                break;
            case CONDITIONALS:
                if(currentToken.getValue().equals("IF")){
                    ParseIfStatement();
                }
                if(currentToken.getValue().equals("ELSE")){
                    if(!tokens.get(currentTokenIndex-1).getValue().equals("END IF")){
                        throw new CODEExceptions.IFException("IF condition does not exist before line: " + lineCheck.find(currentTokenIndex, tokens));
                    }
                    if(tokens.get(currentTokenIndex+1).getValue().equals("IF")){
                        currentTokenIndex++;
                        ParseIfStatement();
                    }
                }
                if(currentToken.getValue().equals("WHILE")){
                    whileComebackTokenIndex.add(currentTokenIndex+1);
                    comebackConditionStack.add(tokens.get(currentTokenIndex+1).getValue());

                    ParseWhileStatement();
                }
                currentTokenIndex++;
                break;
            case IF_END:
                ifStarted = false;
                currentTokenIndex++;

                if(!ifCondition.isEmpty()){
                    ifCondition.remove(ifCondition.size()-1);
                }

                while(currentTokenIndex<tokens.size()-1 && tokens.get(currentTokenIndex).getType() == Token.TokenType.CONDITIONALS
                        && (!tokens.get(currentTokenIndex).getValue().equals("IF") && !tokens.get(currentTokenIndex-1).getValue().equals("ELSE")) && !tokens.get(currentTokenIndex).getValue().equals("WHILE")){
                    while (tokens.get(currentTokenIndex).getType() != Token.TokenType.IF_END){
                        currentTokenIndex++;
                        if(currentTokenIndex>tokens.size()-1){
                            throw new CODEExceptions.IFException("END IF nonexistent after line: " + lineCheck.find(currentTokenIndex, tokens));
                        }
                    }
                    currentTokenIndex++;
                }
                ifVariables.clear();
            case WHILE_END:
                whileStarted = false;

                if(whileComebackTokenIndex.size()>1&&comebackConditionStack.size()>1){
                    LogicCalculator calc = new LogicCalculator();
                    String val = comebackConditionStack.peek();
                    boolean result = calc.evaluate(ReplaceVariable(val));

                    if(result==false){
                        currentTokenIndex++;
                        whileComebackTokenIndex.pop();
                        comebackConditionStack.pop();
                    }else{
                        tokens.get(whileComebackTokenIndex.peek()).setValue(comebackConditionStack.peek());
                        currentTokenIndex = whileComebackTokenIndex.peek();
                    }
                }
                else{
                    LogicCalculator calc = new LogicCalculator();
                    String val = comebackConditionStack.peek();
                    boolean result = calc.evaluate(ReplaceVariable(val));
                    if(result){
                        tokens.get(whileComebackTokenIndex.peek()).setValue(comebackConditionStack.peek());
                        currentTokenIndex = whileComebackTokenIndex.peek();
                    }else{
                        whileComebackTokenIndex.pop();
                        comebackConditionStack.pop();
                        currentTokenIndex++;
                    }
                }
            case END_LINE:
                currentTokenIndex++;
                break;
            case ESCAPE:
                currentTokenIndex++;
                break;
            case STRING:
                currentTokenIndex++;
                break;
            case WHILE_BEGIN:
                currentTokenIndex++;
                break;
            case IF_BEGIN:
                currentTokenIndex++;
                break;
            default:
                // Handle unexpected tokens or syntax errors
                System.err.println("Syntax error: Unexpected token " + currentToken + " at line: " + lineCheck.find(currentTokenIndex, tokens));
                break;
        }
    }
    //region MAIN METHODS
    //This function declares the variables and adds the variables to the List DeclarationNodes
    private void ParseVariableDeclaration() throws CODEExceptions.NoVariableFoundException, CODEExceptions.VariableNameExistsException, CODEExceptions.CommaNotFound, CODEExceptions.NoValueAddedException, CODEExceptions.NoEqualBetweenException, CODEExceptions.VariableAfterCommaException, CODEExceptions.CommaAsVariableException, CODEExceptions.ValueAsVariableException {
        String dataType = tokens.get(currentTokenIndex).getValue().toUpperCase();
        currentTokenIndex++;
        if(tokens.get(currentTokenIndex).getType() == Token.TokenType.END_LINE){
            throw new CODEExceptions.NoVariableFoundException("Variable name not found for datatype: "+ dataType + " at line: "+ lineCheck.find(currentTokenIndex,tokens));
        }
        if(tokens.get(currentTokenIndex).getType() == Token.TokenType.COMMA){
            throw new CODEExceptions.CommaAsVariableException("COMMA is not a allowed as VARIABLE for datatype: "+ dataType + " at line: "+ lineCheck.find(currentTokenIndex,tokens));
        }
        if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VALUE){
            throw new CODEExceptions.ValueAsVariableException("VALUE is not a allowed as VARIABLE for datatype: "+ dataType + " at line: "+ lineCheck.find(currentTokenIndex,tokens));
        }

        while (tokens.get(currentTokenIndex).getType() != Token.TokenType.END_LINE && currentTokenIndex < tokens.size()-1){
            String variableName = "";
            String value = "?";

            if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VARIABLE){
                variableName = tokens.get(currentTokenIndex).getValue();

                //Check if name follows the format
                try {
                    Validator.isValidVariableName(variableName);
                }catch (Exception e){
                    System.err.println(e.getMessage() + " at line: "+ lineCheck.find(currentTokenIndex,tokens));
                    System.exit(1);
                }

                isVariableNameUnique(variableName);

                currentTokenIndex++;
                //IF next token is a variable then throw an exception a
                if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VARIABLE){
                    throw new CODEExceptions.CommaNotFound("Comma should exist before: "+ tokens.get(currentTokenIndex).getValue() + " at line: "+ lineCheck.find(currentTokenIndex,tokens));
                }
                if((currentTokenIndex+1 < tokens.size()-1)){
                    if(tokens.get(currentTokenIndex).getType() == Token.TokenType.EQUALS
                            &&tokens.get(currentTokenIndex+1).getType() == Token.TokenType.VALUE){
                        currentTokenIndex++;
                        value = tokens.get(currentTokenIndex).getValue();
                        try{
                            if(dataType.equals("INT") || dataType.equals("FLOAT") || dataType.equals("BOOL")){
                                value = Calculate(value,dataType);
                            }
                            FormatValidator(dataType, value);
                        }catch (Exception e){
                            System.err.println("Variable: "+ variableName+" has "+e.getMessage());
                            System.exit(1);
                        }
                        currentTokenIndex++;
                    }
                    if(tokens.get(currentTokenIndex).getType() == Token.TokenType.EQUALS
                            &&tokens.get(currentTokenIndex+1).getType() != Token.TokenType.VALUE){
                        throw new CODEExceptions.NoValueAddedException(tokens.get(currentTokenIndex+1).getValue()+" is Not A Value -- Value must be added after = at line: "+ lineCheck.find(currentTokenIndex, tokens));
                    }
                    if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VALUE){
                        throw new CODEExceptions.NoEqualBetweenException("Equals( = ) must be added in between "+variableName+" and "+tokens.get(currentTokenIndex).getValue()+" at line: "+ lineCheck.find(currentTokenIndex, tokens));
                    }
                }
            }else{
                try {
                    Validator.isValidVariableName(tokens.get(currentTokenIndex).getValue());
                }catch (Exception e){
                    System.err.println(e.getMessage() + " at line: "+ lineCheck.find(currentTokenIndex,tokens));
                    System.exit(1);
                }
            }

            VariableDeclarationNode variable = null;
            if(tokens.get(currentTokenIndex).getType() == Token.TokenType.COMMA){
                if(!variableName.isEmpty()){
                    variable = new VariableDeclarationNode(dataType, variableName, value);
                }
                currentTokenIndex++;
                if(tokens.get(currentTokenIndex).getType() == Token.TokenType.END_LINE){
                    throw new CODEExceptions.VariableAfterCommaException("Comma , must have a Variable after it!... at line: "+ lineCheck.find(currentTokenIndex,tokens));
                }
            } else if (tokens.get(currentTokenIndex).getType() == Token.TokenType.END_LINE) {
                if(!variableName.isEmpty()){
                    variable = new VariableDeclarationNode(dataType, variableName, value);
                }
            }

            if(variable!=null){
                declarationNodes.add(variable);
                declaredVariableNames.add(variableName);
                if(ifStarted){
                    ifVariables.add(variable);
                }
                if(whileStarted){
                    whileVariables.add(variable);
                }
            }
        }
    }

    //This function assigns value to the variable
    private void ParseAssignmentStatement() throws Exception {
        List<String> variables = new ArrayList<>();
        while (tokens.get(currentTokenIndex).getType() != Token.TokenType.END_LINE && currentTokenIndex < tokens.size()-1){
            if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VARIABLE){
                VariableDeclarationNode variableNode = FindDeclaredNode(tokens.get(currentTokenIndex).getValue());
                if(variableNode!=null){
                    variables.add(tokens.get(currentTokenIndex).getValue());
                    currentTokenIndex++;
                }else{
                    throw new CODEExceptions.NotExistingVariableName("Variable name: "+ tokens.get(currentTokenIndex).getValue()+ " has not been declared yet!... at line: "+ lineCheck.find(currentTokenIndex,tokens));
                }
            }
            if(tokens.get(currentTokenIndex).getType() == Token.TokenType.EQUALS){
                if((currentTokenIndex+1 < tokens.size()-1)&& tokens.get(currentTokenIndex+1).getType() == Token.TokenType.VALUE){
                    currentTokenIndex++;
                    variables = DistributeValues(variables,tokens.get(currentTokenIndex).getValue());
                    currentTokenIndex++;
                }else if ((currentTokenIndex+1 < tokens.size()-1)&& tokens.get(currentTokenIndex+1).getType() == Token.TokenType.VARIABLE){
                    currentTokenIndex++;
                }else{
                    throw new CODEExceptions.InvalidValueAddedException("Invalid Value: "+ tokens.get(currentTokenIndex).getValue() +" found at: " + lineCheck.find(currentTokenIndex, tokens));
                }
            }
            if(tokens.get(currentTokenIndex).getType() == Token.TokenType.COMMA){
                if((currentTokenIndex+1 < tokens.size() - 1)&& tokens.get(currentTokenIndex+1).getType() == Token.TokenType.END_LINE)
                {
                    throw new CODEExceptions.VariableAfterCommaException("Comma , must have a Variable after it!... at line: "+ lineCheck.find(currentTokenIndex,tokens));
                }
                else if((currentTokenIndex+1 < tokens.size() - 1)&& tokens.get(currentTokenIndex+1).getType() == Token.TokenType.VARIABLE)
                {
                    currentTokenIndex++;
                }
            }
        }
    }
    private void ParseScanInput() throws Exception {
        Scanner scan = new Scanner(System.in);
        currentTokenIndex++;
        // Ensure there are tokens to process
        if (currentTokenIndex >= tokens.size()) {
            throw new CODEExceptions.SCANException("Expected variable names after SCAN at line: " + lineCheck.find(currentTokenIndex, tokens));
        }

        // Retrieve variable names
        List<String> variableNames = new ArrayList<>();
        while (currentTokenIndex < tokens.size()-1 && tokens.get(currentTokenIndex).getType() == Token.TokenType.VARIABLE) {
            variableNames.add(tokens.get(currentTokenIndex).getValue());

            VariableDeclarationNode varNode = FindDeclaredNode(tokens.get(currentTokenIndex).getValue());
            if (varNode == null) {
                throw new CODEExceptions.NotExistingVariableName("Non Existing Variable: "+ tokens.get(currentTokenIndex).getValue() +" at line: " + lineCheck.find(currentTokenIndex, tokens));
            }

            currentTokenIndex++;
            if (currentTokenIndex < tokens.size()-1 && tokens.get(currentTokenIndex).getType() == Token.TokenType.COMMA) {
                currentTokenIndex++; // Move past the comma token
                if(currentTokenIndex < tokens.size()-1 && tokens.get(currentTokenIndex).getType() != Token.TokenType.VARIABLE){
                    throw new CODEExceptions.VariableAfterCommaException("Comma , must have a Variable after it!... at line: "+ lineCheck.find(currentTokenIndex,tokens));
                }
            }
        }

        // Prompt user to input values separated by commas
        String inputLine = scan.nextLine();
        String[] inputValues = inputLine.split(",");

        if(inputValues.length < variableNames.size()){
            throw new CODEExceptions.SCANException("Missed Variable Scan for " + (variableNames.size()-inputValues.length)+" variable at line: "+ lineCheck.find(currentTokenIndex,tokens));
        }
        else if(inputValues.length > variableNames.size()){
            throw new CODEExceptions.SCANException("Invalid Scan of " + (inputValues.length-variableNames.size())+" unexistent variable at line: "+ lineCheck.find(currentTokenIndex,tokens));
        }
        // Assign each value to its corresponding variable
        for (int i = 0; i < variableNames.size(); i++) {
            String variableName = variableNames.get(i).trim();
            String value = inputValues[i].trim();

            // Check if the variable exists
            VariableDeclarationNode varNode = FindDeclaredNode(variableName);

            // Validate and update the variable value
            String dataType = varNode.getDataType();
            if(value.length()==1 && dataType.equals("CHAR")){
                value = "\'"+value+"\'";
            }
            if(dataType.equals("FLOAT")){
                Validator.isValidFLOATValue(value);

                if(!value.contains(".")){
                    value = Double.toString(Double.parseDouble(value)+0.0);
                }
            }
            value = Calculate(value, varNode.getDataType());

            FormatValidator(dataType, value);
            UpdateVariableValue(variableName,value);
        }
    }
    private void ParseDisplayStatement() throws CODEExceptions.UninitializedVariable, CODEExceptions.NotExistingVariableName, CODEExceptions.JuxtapositionArithmeticException, CODEExceptions.ConcatenationException {
        currentTokenIndex++;
        // Parse values to display
        StringBuilder displayValue = new StringBuilder();
        while (currentTokenIndex < tokens.size() &&
                tokens.get(currentTokenIndex).getType() != Token.TokenType.END_LINE) {
            String value = tokens.get(currentTokenIndex).getValue();

            if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VALUE
                    && (value.matches(".*[()+*/%\\-].*")
                    || value.matches(".*[[><]=?|==|!=|[<>]|[AND]|[OR]|[NOT]].*"))
                    && value.charAt(0) != '\"' && value.charAt(value.length()-1) != '\"'){
                JuxtapositionCalculator calculator = new JuxtapositionCalculator();
                if (value.matches(".*[[><]=?|==|!=|[<>]|[AND]|[OR]|[NOT]].*") && !value.equals("\"TRUE\"") && !value.equals("\"FALSE\"")) {
                    LogicCalculator calc = new LogicCalculator();

                    if(calc.evaluate(ReplaceVariable(value))==true){
                        value = "\"TRUE\"";
                    }else{
                        value = "\"FALSE\"";
                    }
                }else{
                    if(value.contains(".")){
                        value = Double.toString(calculator.calculate(ReplaceVariable(value)));
                    }else{
                        value = Integer.toString((int) calculator.calculate(ReplaceVariable(value)));
                    }
                }
            }

            if(tokens.get(currentTokenIndex).getType() == Token.TokenType.ESCAPE){
                value = value.substring(1, value.length() - 1);
            }
            VariableDeclarationNode declaredNode;
            if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VARIABLE &&
                    tokens.get(currentTokenIndex).getType() != Token.TokenType.ESCAPE){
                declaredNode = FindDeclaredNode(value);

                if(declaredNode==null){
                    throw new CODEExceptions.NotExistingVariableName("This variable "+ value +" has not been declared at line: " + lineCheck.find(currentTokenIndex, tokens));
                }else{
                    if(declaredNode.getValue().equals("?")){
                        throw new CODEExceptions.UninitializedVariable("Variable: "+ declaredNode.getVariableName() + " = has not been initialized at line: " + lineCheck.find(currentTokenIndex, tokens));
                    }else{
                        value = declaredNode.getValue();
                    }
                }
            }
            if (value.startsWith("\"") && value.endsWith("\"")) {
                // Remove the starting and ending double quotes
                value = value.substring(1, value.length() - 1);
            }
            if (value.startsWith("\'") && value.endsWith("\'")) {
                // Remove the starting and ending single quotes
                value = value.substring(1, value.length() - 1);
            }
            if(value.equals("$")){
                value = "\n";
            }
            // Append the current value
            displayValue.append(value);

            // Check if there's a token after the current value
            if (currentTokenIndex + 1 < tokens.size()) {
                String nextToken = tokens.get(currentTokenIndex + 1).getValue();
                if(nextToken.equals("END_LINE")){
                    break;
                }
                else if (!nextToken.equals("&")) {
                    throw new CODEExceptions.ConcatenationException("Expected '&' after each value for concatenation at line: " + lineCheck.find(currentTokenIndex, tokens));
                } else if (tokens.get(currentTokenIndex + 2).getType() == Token.TokenType.END_LINE) {
                    // If there's no token after '&', throw an error
                    throw new CODEExceptions.ConcatenationException("Expression expected after '&' at line: " + lineCheck.find(currentTokenIndex, tokens));
                }
                // Move past the concatenator token
                currentTokenIndex++;
            }

            // Move to the next token
            currentTokenIndex++;
        }
        System.out.println(displayValue);

        if(displayValue.toString()!=""){
            outputNull = false;
        }
    }
    private void ParseIfStatement() throws CODEExceptions.IFException, CODEExceptions.NotExistingVariableName, CODEExceptions.JuxtapositionArithmeticException, CODEExceptions.UninitializedVariable {
        currentTokenIndex++;
        if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VALUE
                && (tokens.get(currentTokenIndex).getValue().charAt(0) == '(' ||
                tokens.get(currentTokenIndex).getValue().charAt(tokens.get(currentTokenIndex).getValue().length()-1) == ')')){
            LogicCalculator calc = new LogicCalculator();

            String val = tokens.get(currentTokenIndex).getValue();
            val = ReplaceVariable(val);
            if(ReplaceVariable(val).contains("?")){
                throw new CODEExceptions.UninitializedVariable("Uninitialized Variable found in condition: "+ val + " at line: " + lineCheck.find(currentTokenIndex, tokens));
            }
            //We Check if it has a remaining uninitialized variable
            String regex = "\\b(?!AND\\b|OR\\b|NOT\\b)[a-z]+";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(val);
            if(matcher.find()){
                throw new CODEExceptions.NotExistingVariableName("Undeclared Variable found in: "+val+" on line: "+ lineCheck.find(currentTokenIndex, tokens));
            }
            boolean result = calc.evaluate(val);
            if(result){
                currentTokenIndex+=2;
                ifCondition.add("true");
                ifStarted = true;
            }else{
                ifCondition.add("false");
                int ifCount = ifCondition.size();
                if(ifCount > 0 && ifCondition.get(ifCount-1).equals("false")){
                    while (!ifCondition.isEmpty() &&ifCondition.size()>ifCount-1){
                        if(tokens.get(currentTokenIndex).getValue().equals("IF")){
                            ifCondition.add("false");
                        }
                        if(tokens.get(currentTokenIndex).getType() == Token.TokenType.IF_END){
                            ifCondition.remove(ifCondition.size()-1);
                        }
                        currentTokenIndex++;
                    }
                }else{
                    while (tokens.get(currentTokenIndex).getType() != Token.TokenType.IF_END){
                        currentTokenIndex++;
                    }
                }
            }
        }else{
            String line = tokens.get(currentTokenIndex).getValue();

            if ((line.charAt(0) != '(' || line.charAt(line.length()-1) != ')') && line!="END_LINE"){
                throw new CODEExceptions.IFException("IF condition: "+ tokens.get(currentTokenIndex).getValue() + " must be inside a parenthesis at line: " + lineCheck.find(currentTokenIndex, tokens));
            }
            else if(line=="END_LINE"){
                throw new CODEExceptions.IFException("IF must have a condition at line: " + lineCheck.find(currentTokenIndex, tokens));
            }else{
                throw new CODEExceptions.IFException("Not a valid condition for IF: "+ tokens.get(currentTokenIndex).getValue()+" at line: " + lineCheck.find(currentTokenIndex, tokens));
            }
        }
    }
    private void ParseWhileStatement() throws CODEExceptions.UninitializedVariable, CODEExceptions.NotExistingVariableName, CODEExceptions.JuxtapositionArithmeticException, CODEExceptions.WHILEException {
        currentTokenIndex++;
        if(tokens.get(currentTokenIndex).getType() == Token.TokenType.VALUE
                && (tokens.get(currentTokenIndex).getValue().charAt(0) == '('
                || tokens.get(currentTokenIndex).getValue().charAt(tokens.get(currentTokenIndex).getValue().length()-1) == ')')){
            LogicCalculator calc = new LogicCalculator();
            String val = tokens.get(currentTokenIndex).getValue();

            val = ReplaceVariable(val);

            if(ReplaceVariable(val).contains("?")){
                throw new CODEExceptions.UninitializedVariable("Uninitialized Variable found in condition: "+ val + " at line: " + lineCheck.find(currentTokenIndex, tokens));
            }

            //We Check if it has a remaining uninitialized variable
            String regex = "\\b(?!AND\\b|OR\\b|NOT\\b)[a-z]+";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(val);
            if(matcher.find()){
                throw new CODEExceptions.NotExistingVariableName("Undeclared Variable found in: "+val+" on line: "+ lineCheck.find(currentTokenIndex, tokens));
            }

            boolean result = calc.evaluate(val);
            if(result==true){
                if(tokens.get(currentTokenIndex+2).getType() == Token.TokenType.WHILE_BEGIN){
                    currentTokenIndex+=2;
                    whileCondition.add("true");
                    whileStarted = true;
                }
            }else{
                whileCondition.add("false");
                int whileCount = whileCondition.size();
                if(whileCondition.get(whileCount-1).equals("false")){
                    while (whileCondition.size()>whileCount-1){
                        if(tokens.get(currentTokenIndex).getValue().equals("WHILE")){
                            whileCondition.add("false");
                        }
                        if(tokens.get(currentTokenIndex).getType() == Token.TokenType.WHILE_END){
                            whileCondition.remove(whileCondition.size()-1);
                        }
                        currentTokenIndex++;
                    }
                }else {
                    while (tokens.get(currentTokenIndex).getType() != Token.TokenType.WHILE_END) {
                        currentTokenIndex++;
                    }

                    whileVariables.clear();
                }
                //System.out.println(tokens.get(currentTokenIndex-1).getValue());
            }
        }else{
            String line = tokens.get(currentTokenIndex).getValue();
            if ((line.charAt(0) != '(' || line.charAt(line.length()-1) != ')') && line!="END_LINE"){
                throw new CODEExceptions.WHILEException("WHILE condition: "+ tokens.get(currentTokenIndex).getValue() + " must be inside a parenthesis at line: " + lineCheck.find(currentTokenIndex, tokens));
            }
            else if(line=="END_LINE"){
                System.out.println(tokens.get(currentTokenIndex).getValue());
                throw new CODEExceptions.WHILEException("WHILE must have a condition at line: " + lineCheck.find(currentTokenIndex, tokens));
            }else{
                throw new CODEExceptions.WHILEException("Not a valid condition for WHILE: "+ tokens.get(currentTokenIndex).getValue()+" at line: " + lineCheck.find(currentTokenIndex, tokens));
            }
        }
    }
    //endregion

    //region SubMainFunction
    public List<String> DistributeValues(List<String> variables, String value) throws Exception {
        for (String variable: variables){
            for (VariableDeclarationNode variableNode: declarationNodes){
                if(variableNode.getVariableName().equals(variable)){
                    VariableDeclarationNode vnode = FindDeclaredNode(variable);
                    if (vnode.getDataType().equals("INT") || vnode.getDataType().equals("FLOAT") || vnode.getDataType().equals("BOOL")){
                        value = Calculate(value, vnode.getDataType());
                    }
                    FormatValidator(vnode.getDataType(), value);

                    vnode.setValue(value);
                }
            }
        }
        variables.clear();
        return variables;
    }
    //CalculatesIntOrFloatOrBOOL for a Node
    private String Calculate(String value, String dataType) throws Exception {
        if (dataType.equals("FLOAT")) {
            JuxtapositionCalculator calculator = new JuxtapositionCalculator();
            value = Double.toString(calculator.calculate(ReplaceVariable(value)));
        }
        if (dataType.equals("INT")) {
            JuxtapositionCalculator calculator = new JuxtapositionCalculator();
            value = Integer.toString((int) Math.round(calculator.calculate(ReplaceVariable(value))));
        }
        if (dataType.equals("BOOL")) {
            LogicCalculator calculator = new LogicCalculator();
            if (calculator.evaluate(ReplaceVariable(value))) {
                value = "\"TRUE\"";
            } else{
                value = "\"FALSE\"";
            }
        }
        return  value;
    }
    //endregion

    //region Validators
    public VariableDeclarationNode FindDeclaredNode(String variableName) {
        for (VariableDeclarationNode node : declarationNodes) {
            if (node.getVariableName().equals(variableName)) {
                return node;
            }
        }
        return null;
    }
    private void FormatValidator(String dataType, String value) throws CODEExceptions.WrongVariableFormat {
        // Regular expression pattern for format validation
        String pattern = "";
        switch (dataType) {
            case "INT":
                pattern = "-?\\d+";
                break;
            case "FLOAT":
                pattern = "-?\\d+(\\.\\d+)?";
                break;
            case "CHAR":
                pattern = "'.{1}'";
                break;
            case "BOOL":
                try {
                    if (value.matches(".*[[><]=?|==|!=|[<>]|[AND]|[OR]|[NOT]].*") && !value.equals("\"TRUE\"") && !value.equals("\"FALSE\"")) {
                        LogicCalculator calculator = new LogicCalculator();
                        if(value.toUpperCase().contains("TRUE")||value.toUpperCase().contains("FALSE")){
                            Validator.isValidBoolValue(value);
                        }
                        if (calculator.evaluate(ReplaceVariable(value))) {
                            value = "\"TRUE\"";
                        } else{
                            value = "\"FALSE\"";
                        }
                    }
                    Validator.isValidBoolValue(value);
                } catch (Exception e) {
                    System.err.println(e.getMessage() + " at line: " + lineCheck.find(currentTokenIndex, tokens));
                    System.exit(1);
                }
                break;
            // Add more cases for other data types if needed
            default:
                throw new CODEExceptions.WrongVariableFormat("Unknown data type: " + dataType + " at line: " + lineCheck.find(currentTokenIndex, tokens));
        }

        if (!value.matches(pattern) && !dataType.equals("BOOL")) {
            throw new CODEExceptions.WrongVariableFormat("Invalid value for " + dataType + ": " + value + " at line: " + lineCheck.find(currentTokenIndex, tokens));
        }
    }

    // Method to check if a variable name is unique
    private void isVariableNameUnique(String variableName) throws CODEExceptions.VariableNameExistsException {
        if(declaredVariableNames.contains(variableName)){
            throw new CODEExceptions.VariableNameExistsException("Variable name: "+ variableName +" already existing at line: " + lineCheck.find(currentTokenIndex, tokens));
        }
    }

    //endregion

    //region ValuesConnector
    private void arithConnector() throws Exception {
        List<Token> retoken = new ArrayList<>();

        StringBuilder expressionBuilder = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {
            if(tokens.get(i).getType() == Token.TokenType.VARIABLE && (i+1)<tokens.size()
                    && (i-1)>=0 && (tokens.get(i+1).getType() == Token.TokenType.VALUE||(tokens.get(i-1).getType() == Token.TokenType.VALUE))){
                VariableDeclarationNode varNode = FindDeclaredNode(tokens.get(i).getValue());

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
        LineConnector lineTokens = new LineConnector();
        tokens = lineTokens.ConnectLines(retoken);
    }
    //endregion

    //region Miscelaneous
    //UpdatesVariableValues
    private void UpdateVariableValue(String variableName, String value) {
        // Update the value of the variable if it exists
        for (VariableDeclarationNode variable : declarationNodes) {
            if(variable.getVariableName().equals(variableName)){
                variable.setValue(value);
            }
        }
    }

    //Replaces Variables inside the expression with their values
    private String ReplaceVariable(String expression) {
        String modifiedExpression = expression;
        VariableDeclarationNode varNode;
        for (String variableName: declaredVariableNames){
            // Use regular expression to find and replace the variable
            String regex = "\\b" + variableName + "\\b"; // Match whole word
            varNode = FindDeclaredNode(variableName);

            // Replace the variable with the replacement value
            modifiedExpression = modifiedExpression.replaceAll(regex, varNode.getValue());
        }
        return modifiedExpression;
    }
    //endregion

    private void parseValueStatement() throws Exception {
        if((tokens.get(currentTokenIndex).getValue().contains("++")|| tokens.get(currentTokenIndex).getValue().contains("--")
                || tokens.get(currentTokenIndex).getValue().contains("-=")
                || tokens.get(currentTokenIndex).getValue().contains("+=")
                || tokens.get(currentTokenIndex).getValue().contains("/=")
                || tokens.get(currentTokenIndex).getValue().contains("%="))){

            //ReplaceValue part where we find the matching variableName
            String varName = "";
            String modifiedExpression = tokens.get(currentTokenIndex).getValue();
            VariableDeclarationNode varNode;
            for (String variableName: declaredVariableNames){
                // Use regular expression to find and replace the variable
                String regex = "\\b" + variableName + "\\b"; // Match whole word
                varNode = FindDeclaredNode(variableName);

                // Replace the variable with the replacement value
                if(!modifiedExpression.equals(modifiedExpression.replaceAll(regex, varNode.getValue()))){
                    varName = varNode.getVariableName();
                    break;
                }
            }

            VariableDeclarationNode vnode = FindDeclaredNode(varName);
            String value = tokens.get(currentTokenIndex).getValue();
            if(vnode!=null){
                value = Calculate(value, vnode.getDataType());
                FormatValidator(vnode.getDataType(),value);
                UpdateVariableValue(vnode.getVariableName(), value);
            }else{
                throw new CODEExceptions.NotExistingVariableName("Variable: "+varName+ ": does not exist at line: " + lineCheck.find(currentTokenIndex, tokens));
            }
        }
        currentTokenIndex++;
    }


    //region Debugging
    //unneeded extra to check if there are nodes
    public void GetAllDeclaredVariableNodes(){
        for (VariableDeclarationNode node: declarationNodes){
            System.out.println(node);
        }
    }
    //endregion
}
