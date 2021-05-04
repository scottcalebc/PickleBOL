package pickle;

public class DateException extends ScannerParserException{

    /**
     * Used to indicate Date error
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     */
    public DateException(Token token, String sourceFileNm)
    {
        super(token, sourceFileNm, "Invalid Date.");
    }

    /**
     * Used to indicate Date error
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     * @param errMessageStr    error message to display
     */
    public DateException(Token token, String sourceFileNm, String errMessageStr)
    {
        super(token, sourceFileNm, errMessageStr);
    }
}
