package pickle;


public class StringConstantException extends ScannerParserException {

    /**
     *
     * Used to indicate String Constant error during parsing
     * <p>
     *
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     *
     */
    public StringConstantException(Token token, String sourceFileNm)
    {
        super(token, sourceFileNm, "Invalid string constant");
    }
}
