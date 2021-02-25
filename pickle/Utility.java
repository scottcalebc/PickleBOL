package pickle;

/**
 * Utility class provides various utility functions for Scanner class.
 *
 * <p>
 *     Skipping Comments
 *     Adding Numerics
 *     Subtracting Numerics
 */
public class Utility {

    /**
     * Finds the column position after advancing past all
     * whitespace characters and returns it.
     *
     * @param scanner   Scanner Object
     * @param textCharM Current line text
     * @return int representing column position after whitespace
     */
    public int skipWhitespace(Scanner scanner, char[] textCharM)
    {
        while (scanner.iColPos < textCharM.length
                && textCharM[scanner.iColPos] == ' '
                || textCharM[scanner.iColPos] == '\t'
                || textCharM[scanner.iColPos] == '\n')
            scanner.iColPos++;
        return scanner.iColPos;
    }

    /**
     * If the next two characters in the line are '//'
     * advance the line of the scanner and return the
     * scanner object.
     *
     * @param scanner   Scanner object
     * @param textCharM Current line text
     * @return Scanner object with next line number set
     */
    public Scanner skipComment(Scanner scanner, char[] textCharM)
    {
        if (textCharM[scanner.iColPos] == '/' && textCharM[scanner.iColPos+1] == '/')
            scanner.getNextSourceLine();
        return scanner;
    }

    /**
     * Adds the Values of two Numerics and returns a ResultValue.
     * <p>
     *     The ResultValue will have the data type of the
     *     first operator.
     *
     * @param scanner   Scanner Object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public ResultValue add(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        ResultValue res =  new ResultValue("", nOp1.dataType);

        // Do operation and store resulting string
        if (nOp1.dataType == nOp1.dataType) {
            // both values are integers
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString(nOp1.intValue + nOp2.intValue);
            // both values are floats
            else
                res.strValue = Double.toString(nOp1.doubleValue + nOp2.doubleValue);
        }
        else
        {
            // first value is integer, second is float
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString(nOp1.intValue + (int)nOp2.doubleValue);
            // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue + nOp2.doubleValue);
        }

        return res;
    }

    /**
     * Subtracts the Values of two Numerics and returns a ResultValue.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operator.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public ResultValue subtract(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        ResultValue res =  new ResultValue("", nOp1.dataType);

        // Do operation and store resulting string
        if (nOp1.dataType == nOp1.dataType) {
            // both values are integers
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString(nOp1.intValue - nOp2.intValue);
                // both values are floats
            else
                res.strValue = Double.toString(nOp1.doubleValue - nOp2.doubleValue);
        }
        else
        {
            // first value is integer, second is float
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString(nOp1.intValue - (int)nOp2.doubleValue);
                // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue - nOp2.doubleValue);
        }

        return res;
    }
}
