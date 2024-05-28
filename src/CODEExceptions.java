public class CODEExceptions {
    //region Lexer Exceptions
    public static class BEGINCODEException extends Exception {
        public BEGINCODEException(String message) {
            super(message);
        }
    }
    public static class ENDCODEException extends Exception {
        public ENDCODEException(String message) {
            super(message);
        }
    }
    public static class BeginEndCodePresentException extends Exception {
        public BeginEndCodePresentException(String message) {
            super(message);
        }
    }
    //endregion
    //region Variable Exceptions
    //Variable Declaration Exceptions
    public static class InvalidVariableName extends Exception {
        public InvalidVariableName(String message) {
            super(message);
        }
    }
    public static class VariableNameExistsException extends Exception {
        public VariableNameExistsException(String message) {
            super(message);
        }
    }
    public static class ValueAsVariableException extends Exception {
        public ValueAsVariableException(String message) {
            super(message);
        }
    }
    public static class NoValueAddedException extends Exception {
        public NoValueAddedException(String message) {
            super(message);
        }
    }
    public static class InvalidValueAddedException extends Exception {
        public InvalidValueAddedException(String message) {
            super(message);
        }
    }
    public static class NoEqualBetweenException extends Exception {
        public NoEqualBetweenException(String message) {
            super(message);
        }
    }
    public static class CommaAsVariableException extends Exception {
        public CommaAsVariableException(String message) {
            super(message);
        }
    }
    public static class VariableAfterCommaException extends Exception {
        public VariableAfterCommaException(String message) {
            super(message);
        }
    }
    public static class BOOLDeclarationException extends Exception {
        public BOOLDeclarationException(String message) {
            super(message);
        }
    }
    public static class INTDeclarationException extends Exception {
        public INTDeclarationException(String message) {
            super(message);
        }
    }
    public static class CHARDeclarationException extends Exception {
        public CHARDeclarationException(String message) {
            super(message);
        }
    }
    public static class FLOATDeclarationException extends Exception {
        public FLOATDeclarationException(String message) {
            super(message);
        }
    }

    //Variable Checking Exceptions
    public static class NoVariableFoundException extends Exception {
        public NoVariableFoundException(String message) {
            super(message);
        }
    }
    public static class WrongVariableFormat extends Exception {
        public WrongVariableFormat(String message) {
            super(message);
        }
    }
    public static class UninitializedVariable extends Exception {
        public UninitializedVariable(String message) {
            super(message);
        }
    }
    public static class NotExistingVariableName extends Exception {
        public NotExistingVariableName(String message) {
            super(message);
        }
    }

    //Syntax Checker
    public static class BracketException extends Exception {
        public BracketException(String message) {
            super(message);
        }
    }
    public static class CommaNotFound extends Exception {
        public CommaNotFound(String message) {
            super(message);
        }
    }
    public static class StringToCharException extends Exception {
        public StringToCharException(String message) {
            super(message);
        }
    }
    public static class ConcatenationException extends Exception {
        public ConcatenationException(String message) {
            super(message);
        }
    }

    //Main Method Exceptions
    public static class SCANException extends Exception {
        public SCANException(String message) {
            super(message);
        }
    }
    public static class DISPLAYException extends Exception {
        public DISPLAYException(String message) {
            super(message);
        }
    }
    public static class IFException extends Exception {
        public IFException(String message) {
            super(message);
        }
    }
    public static class WHILEException extends Exception {
        public WHILEException(String message) {
            super(message);
        }
    }

    //Calculator Exceptions
    public static class JuxtapositionArithmeticException extends Exception {
        public JuxtapositionArithmeticException(String message) {
            super(message);
        }
    }
    public static class LogicalCaculationException extends Exception {
        public LogicalCaculationException(String message) {
            super(message);
        }
    }
    //endregion
}
