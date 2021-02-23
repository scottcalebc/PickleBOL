package pickle;

public class InvalidTokenException extends  ScannerParserException {

    /**
     *
     * Used to indicate Invalid token error during parsing
     * <p>
     *
     * @param token            token being parsed
     * @param sourceFileNm     source file name for error output
     *
     */
    public InvalidTokenException(Token token, String sourceFileNm) {
        super(token, sourceFileNm, "Invalid token");
    }
}
