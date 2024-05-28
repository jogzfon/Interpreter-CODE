import java.util.ArrayList;
import java.util.List;

public class ConditionalChecker {

    public void CheckIFCount(List<Token> tokens) throws Exception {
        List<Integer> indexOfIF = new ArrayList<>();
        LineNumberChecker checker = new LineNumberChecker();

        for(int index = 0; index < tokens.size(); index++){
            if (tokens.get(index).getType() == Token.TokenType.CONDITIONALS
                    && (tokens.get(index).getValue().equals("IF") || tokens.get(index).getValue().equals("ELSE"))){
                int pos = index;

                //Adds the position of the IF, ELSE IF, ELSE
                indexOfIF.add(pos);

                //Skips the other codes until end line
                while (tokens.get(index).getType()!= Token.TokenType.END_LINE){
                    index++;
                }
                index++;

                //If BEGIN IF does not exist after the IF, ELSE IF, ELSE then there is an Exception
                if (tokens.get(index).getType() != Token.TokenType.IF_BEGIN){
                    if(tokens.get(pos).getValue().equals("IF")){
                        throw new CODEExceptions.IFException("BEGIN IF not found for the IF statement on line: " + checker.find(pos, tokens));

                    }else if (tokens.get(pos).getValue().equals("ELSE") && tokens.get(pos+1).getValue().equals("IF")){
                        throw new CODEExceptions.IFException("BEGIN IF not found for the ELSE IF statement on line: " + checker.find(pos, tokens));

                    }else{
                        throw new CODEExceptions.IFException("BEGIN IF not found for the ELSE statement on line: " + checker.find(pos, tokens));
                    }
                }
            }

            //Removes the last IF,ELSE IF, ELSE Position added if END IF is encountered
            if(tokens.get(index).getType() == Token.TokenType.IF_END && !indexOfIF.isEmpty()){
                indexOfIF.remove(indexOfIF.size()-1);
            }
        }

        //This checks if there is a remaining IF position and if there is it means END IF for it is missing
        for(Integer pos: indexOfIF){
            if(tokens.get(pos).getValue().equals("IF")){
                throw new CODEExceptions.IFException("END IF not found for the IF statement on line: " + checker.find(pos, tokens));

            }else if (tokens.get(pos).getValue().equals("ELSE") && tokens.get(pos+1).getValue().equals("IF")){
                throw new CODEExceptions.IFException("END IF not found for the ELSE IF statement on line: " + checker.find(pos, tokens));

            }else{
                throw new CODEExceptions.IFException("END IF not found for the ELSE statement on line: " + checker.find(pos, tokens));
            }
        }
    }

    public void CheckWHILECount(List<Token> tokens)  throws Exception {
        List<Integer> indexOfWHILE = new ArrayList<>();
        LineNumberChecker checker = new LineNumberChecker();

        for(int index = 0; index < tokens.size(); index++){
            if (tokens.get(index).getType() == Token.TokenType.CONDITIONALS && tokens.get(index).getValue().equals("WHILE")){
                int pos = index;

                //Adds the position of the WHILE
                indexOfWHILE.add(pos);
                while (tokens.get(index).getType()!= Token.TokenType.END_LINE){
                    index++;
                }
                index++;

                //If BEGIN WHILE does not exist after the WHILE then there is an Exception
                if (tokens.get(index).getType() != Token.TokenType.WHILE_BEGIN){
                    throw new CODEExceptions.IFException("BEGIN WHILE not found for the WHILE statement on line: " + checker.find(pos, tokens));
                }
            }

            //Removes the last WHILE Position added if END WHILE is encountered
            if(tokens.get(index).getType() == Token.TokenType.WHILE_END && !indexOfWHILE.isEmpty()){
                indexOfWHILE.remove(indexOfWHILE.size()-1);
            }
        }

        //This checks if there is a remaining WHILE position and if there is it means END WHILE for it is missing
        for(Integer pos: indexOfWHILE){
            throw new CODEExceptions.IFException("END WHILE not found for the WHILE statement on line: " + checker.find(pos, tokens));
        }
    }
}
