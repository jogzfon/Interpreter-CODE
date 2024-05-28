public class VariableDeclarationNode{
    private String variableName;
    private String dataType;
    private String value;

    public VariableDeclarationNode(String dataType, String variableName, String value){
        this.dataType = dataType;
        this.variableName = variableName;
        this.value = value;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VariableDeclarationNode{" +
                "variableName='" + variableName + '\'' +
                ", dataType='" + dataType + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
