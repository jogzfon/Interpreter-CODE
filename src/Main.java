import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main extends Exception{
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader("CODE/CODE.code"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            String input = sb.toString();
            Lexer lexer = new Lexer();

            //Tokenize the entire read lines
            List<Token> tokens = lexer.tokenize(input);

            /*for (Token token: tokens){
                System.out.println(token);
            }*/

            //Parser
            Parser parser = new Parser(tokens);
            parser.ParseTokens();

            /*System.out.println("============= All the Declared Variables =================");
            parser.GetAllDeclaredVariableNodes();*/

        } catch (IOException e) {
            System.err.println("Theres a problem in buffering the file!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
