package pickle;

public class BoolException extends ScannerParserException {

    /**
     * Used to indicate Bool error
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     */
    public BoolException(Token token, String sourceFileNm)
    {
        super(token, sourceFileNm, "Invalid bool representation");
    }

    /**
     * Used to indicate Bool error
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     * @param errMessageStr    error message to display
     */
    public BoolException(Token token, String sourceFileNm, String errMessageStr)
    {
        super(token, sourceFileNm, errMessageStr);
    }

}
