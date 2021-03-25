package pickle;

/**
 * This class handles Boolean conversions from Strings
 *
 * A Bool is a 'T' or 'F'
 */
public class Bool {

    public boolean bValue;
    public String strValue;
    public SubClassif dataType;

    public Bool(Scanner scanner, ResultValue resValue) throws BoolException
    {
        // store the given result value string
        strValue = resValue.strValue;
        dataType = SubClassif.BOOLEAN;
        // validate that the given resValue is a bool, raise exception on failure
        parseString(scanner, strValue);
    }

    private void parseString(Scanner scanner, String str) throws BoolException
    {
        if (str.length() > 1)
            throw new BoolException(scanner.currentToken, scanner.sourceFileNm);
        if (str.equals("T"))
        {
            bValue = true;
        }
        else if(str.equals("F"))
        {
            bValue = false;
        }
        else
            throw new BoolException(scanner.currentToken, scanner.sourceFileNm);
    }
}
