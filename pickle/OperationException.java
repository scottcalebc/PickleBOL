package pickle;

public class OperationException extends ScannerParserException {

    /**
     * Used to indicate Operation error
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     */
    public OperationException(Token token, String sourceFileNm)
    {
        super(token, sourceFileNm, "Operation not allowed.");
    }

    /**
     * Used to indicate Operation error
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     * @param errMessageStr    error message to display
     */
    public OperationException(Token token, String sourceFileNm, String errMessageStr)
    {
        super(token, sourceFileNm, errMessageStr);
    }

}

