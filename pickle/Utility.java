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
     * @return int representing column position after whitespace
     */
    public static int skipWhitespace(Scanner scanner)
    {
        while (scanner.iColPos < scanner.textCharM.length
                && ( scanner.textCharM[scanner.iColPos] == ' '
                || scanner.textCharM[scanner.iColPos] == '\t'
                || scanner.textCharM[scanner.iColPos] == '\n') )
            scanner.iColPos++;
        return scanner.iColPos;
    }

    /**
     * Returns true if remaining text in line is comment.
     * <p>
     *     The rest of the line is a comment if
     *     the tokenStr = '/' and the next char = '/'
     *
     * @param scanner   Scanner object
     * @param tokenStr  The current token string
     * @return boolean : True if is comment, false otherwise
     */
    public static boolean skipComment(Scanner scanner, String tokenStr)
    {
        return tokenStr.equals("/") && scanner.iColPos < scanner.textCharM.length
                && scanner.textCharM[scanner.iColPos] == '/';
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
    public static ResultValue add(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        ResultValue res =  new ResultValue("", nOp1.dataType);

        // Do operation and store resulting string
        if (nOp1.dataType == nOp2.dataType) {
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
    public static ResultValue subtract(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        ResultValue res =  new ResultValue("", nOp1.dataType);

        // Do operation and store resulting string
        if (nOp1.dataType == nOp2.dataType) {
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
