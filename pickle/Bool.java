package pickle;

/**
 * This class handles Boolean conversions from Strings
 *
 * A Bool is a 'T' or 'F'
 */
public class Bool {

    public boolean bValue;
    public String strValue;
    public char chValue;
    public SubClassif dataType;

    /**
     * Validates if a given ResultValue is a Bool type.
     *
     * If a ResultValue cannot be parsed into a Bool,
     * an error is raised.
     *
     * @param parser   - Parser object
     * @param resValue - ResultValue that contains string to parse
     * @throws BoolException
     */
    public Bool(Parser parser, ResultValue resValue) throws BoolException
    {
        // store the given result value string
        strValue = resValue.strValue;
        dataType = SubClassif.BOOLEAN;
        // validate that the given resValue is a bool, raise exception on failure
        parseString(parser, strValue);
    }

    /**
     * Overridden toString function to print Bool Object
     *
     * @return String representation of Bool Class
     *
     */
    @Override
    public String toString()
    {
        return String.valueOf(this.chValue);
    }

    /**
     * Tries to parse a string into the correct bool value.
     *
     * <p> A valid Bool value is a single char 'T' or 'F'.
     *
     * @param parser  - Parser object
     * @param str     - String to parse.
     * @throws BoolException if the string to more than one char or
     *                       is not 'T' or 'F'.
     */
    private void parseString(Parser parser, String str) throws BoolException
    {
        if (str.length() != 1)
            throw new BoolException(parser.scanner.currentToken, parser.scanner.sourceFileNm);
        if (str.equals("T"))
        {
            bValue = true;
            this.chValue = 'T';
        }
        else if(str.equals("F"))
        {
            bValue = false;
            this.chValue = 'F';
        }
        else
            throw new BoolException(parser.scanner.currentToken, parser.scanner.sourceFileNm);
    }
}
