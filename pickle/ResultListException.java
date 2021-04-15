package pickle;

public class ResultListException extends ScannerParserException{
    /**
     * Used to indicate Array ResultList errors
     * @param token            token where error was generated
     * @param sourceFileNm     source file name for error output
     */
    public ResultListException(Token token, String sourceFileNm)
    {
        super(token, sourceFileNm, "Invalid Array Operation");
    }

    /**
     * Used to indicate Array ResultList errors
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     * @param errMessageStr    error message to display
     */
    public ResultListException(Token token, String sourceFileNm, String errMessageStr)
    {
        super(token, sourceFileNm, errMessageStr);
    }
}
