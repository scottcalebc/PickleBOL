package pickle;

public enum OperatorPrecedence {
    NONE(0,0),
    FUNC(16, 2),
    ARRAY(16, 0),
    PAREN(15, 2),
    UNARYMINUS(12, 12),
    POWER(11, 10),
    MULTIPLYDIVIDE(9, 9),
    ADDMINUS(8, 8),
    SLICE(1, 0),
    CONCAT(7, 7),
    BOOLEANOPS(6,6),
    INNOTIN(6, 6),
    NOT(5,5),
    ANDOR(4,4);







    public final int tokenPrecedence;
    public final int stackPrecedence;


    private OperatorPrecedence(int tokenPrecedence, int stackPrecedence) {
        this.tokenPrecedence = tokenPrecedence;
        this.stackPrecedence = stackPrecedence;
    }
}
