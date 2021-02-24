package pickle;

public class NumericConstantException extends ScannerParserException {

    /**
     * Used to indicate Numeric Constant error during parsing
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     */
    public NumericConstantException(Token token, String sourceFileNm)
    {
        super(token, sourceFileNm, "Invalid numeric constant");
    }

}
