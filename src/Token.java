public class Token {
    enum TokenType {
        VARIABLE,
        VALUE,
        OPERATOR,
        DATATYPE,
        ESCAPE,
        SYMBOL,
        EQUALS,
        DISPLAY,
        STRING,
        END_LINE,
        SCAN,
        COMMA,
        IF_BEGIN,
        IF_END,
        WHILE_BEGIN,
        WHILE_END,
        CONDITIONALS,
        BEGIN_CODE,
        END_CODE,
    }
    final TokenType type;
    private String value;

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}