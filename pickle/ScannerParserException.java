package pickle;

public class ScannerParserException extends PickleException {

    /**
     *
     * Generic ScannerParser Error during tokenizing or parsing
     * <p>
     * Mostly used as an abstract function to print standard error output for exceptions
     *
     * @param token             Token being parsed
     * @param sourceFileNm      source file name
     * @param errMessageStr     Specific error being thrown (i.e. String, Numeric, InvalidToken)
     *
     */
    public ScannerParserException(Token token, String sourceFileNm, String errMessageStr) {
        this.errMessageStr = "Line " + token.iSourceLineNr + " "
                + errMessageStr + ": "
                +   "\'" + token.tokenStr + "\', "
                + "File: " + sourceFileNm;
    }
}
