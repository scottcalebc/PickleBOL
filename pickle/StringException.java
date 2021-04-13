package pickle;

public class StringException extends ScannerParserException
{
    /**
     * Used to indicate String errors
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     */
    public StringException(Token token, String sourceFileNm)
    {
        super(token, sourceFileNm, "Invalid numeric constant");
    }

    /**
     * Used to indicate String errors
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     * @param errMessageStr    error message to display
     */
    public StringException(Token token, String sourceFileNm, String errMessageStr)
    {
        super(token, sourceFileNm, errMessageStr);
    }

}
