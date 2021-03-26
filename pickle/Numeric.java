package pickle;

import java.text.DecimalFormat;

/**
 * This class handles Numeric conversions from Strings
 */
public class Numeric {

    public SubClassif dataType;         // INTEGER or FLOAT
    public String     strValue;         // String representation of Numeric Value
    public double     doubleValue;      // Value as a FLOAT, only valid if dataType is FLOAT
    public int        intValue;         // Value as an INT, only valid if dataType is INT

    /**
     * Validates if a given ResultValue is an integer or float.
     *
     * If a ResultValue cannot be parsed into an integer or float,
     * an error is raised via scanner.error()
     *
     * @param scanner       Scanner object
     * @param resValue      ResultValue that contains string to parse
     * @param operator      The operator string associated with this value
     * @param operandDesc   The description of the Operand ("1st Operand"/"2nd Operand")
     *
     * @throws NumericConstantException if the string is not an int or float
     *                                  or the value cannot be parsed.
     */
    public Numeric(Scanner scanner, ResultValue resValue, String operator, String operandDesc)
            throws NumericConstantException
    {
        // store the given result value string
        strValue = resValue.strValue;
        // validate that the given resValue is a numeric, raise exception on failure
        parseString(scanner, strValue);
    }

    /**
     * Overridden toString function to print Numeric Object
     *
     * @return String representation of Numeric Class
     *
     */
    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.00");
        return (this.dataType == SubClassif.INTEGER) ?
                Integer.toString(this.intValue) : df.format(this.doubleValue);
    }

    /**
     * Determines if a given string represents a Numeric value.
     * <p>
     *     1. If a string is one or more digits, it is considered
     *     an integer.
     * <p>
     *     2. If a string is one or more digits, a '.', and one or
     *     more digits, it is considered a float.
     *
     * @param scanner   Scanner object
     * @param str       String to parse
     *
     * @throws NumericConstantException if the string is not an int or float
     *                                  or the value cannot be parsed.
     */
    private void parseString(Scanner scanner, String str) throws NumericConstantException
    {
        // string is one or more digits
        if (str.matches("\\d+"))
        {
            dataType = SubClassif.INTEGER;
            storeValue(scanner, str, dataType);
        }
        // string is one or more digits, a '.', and one or more digits
        else if (str.matches("\\d+\\.?\\d+"))
        {
            dataType = SubClassif.FLOAT;
            storeValue(scanner, str, dataType);
        }
        // string is not an int or float
        else
        {
            throw new NumericConstantException(scanner.currentToken, scanner.sourceFileNm);
        }
    }

    /**
     * Stores the given string value depending on the data type.
     *
     * @param str       String representation of Int or Flaot
     * @param dataType  Subclassif.INTEGER or Subclassif.FLOAT
     *
     * @throws NumericConstantException if the int or float cannot be parsed
     */
    private void storeValue(Scanner scanner, String str, SubClassif dataType) throws NumericConstantException
    {
        if (dataType == SubClassif.INTEGER)
        {
            // Try to parse the string into an integer
            try {
                intValue = Integer.parseInt(str);
            }
            catch (Exception ex)
            {
                // this likely means the integer in the string
                // is too large for the int data type
                throw new NumericConstantException(scanner.currentToken, scanner.sourceFileNm,
                                                    "Invalid Integer value");
            }
        }
        else if (dataType == SubClassif.FLOAT)
        {
            // Try to parse the string into a float
            try {
                doubleValue = Double.parseDouble(str);
            }
            catch (Exception ex)
            {
                throw new NumericConstantException(scanner.currentToken, scanner.sourceFileNm,
                                                    "Invalid Float value");
            }
        }
    }
}
