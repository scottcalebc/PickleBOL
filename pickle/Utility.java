package pickle;

/**
 * Utility class provides various utility functions for Scanner class.
 *
 * <p> Scanner Operations:
 *     Skip Whitespace, Skip Comments
 *
 * <p> Operate on Numerics:
 *     Add, Subtract, Unary Minus, Multiply, Divide, and Power operations.
 *
 * <p> Compare Numerics:
 *     ==. !=. <, >, <=, >=
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
	scanner.getNextValidLine();
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
     *     first operand.
     *
     * @param scanner   Scanner Object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue add(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        // result has data type of first operand
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
                res.strValue = Integer.toString((int)(nOp1.intValue + nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue + nOp2.intValue);
        }

        return res;
    }

    /**
     * Subtracts the Values of two Numerics and returns a ResultValue.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue subtract(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        // result has data type of first operand
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
                res.strValue = Integer.toString((int)(nOp1.intValue - nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue - nOp2.intValue);
        }

        return res;
    }

    /**
     * Applies a unary minus to a Numeric value.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @return ResultValue
     */
    public static ResultValue unaryMinus(Scanner scanner, Numeric nOp1)
    {
        // result has data type of first operand
        ResultValue res =  new ResultValue("", nOp1.dataType);

        // apply unary minus to Numeric
        if (nOp1.dataType == SubClassif.INTEGER)
            res.strValue = Integer.toString(-nOp1.intValue);
        else if (nOp1.dataType == SubClassif.FLOAT)
            res.strValue = Double.toString(-nOp1.doubleValue);

        return res;
    }

    /**
     * Multiplies the Values of two Numerics and returns a ResultValue.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue multiply(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        // result has data type of first operand
        ResultValue res =  new ResultValue("", nOp1.dataType);

        // Do operation and store resulting string
        if (nOp1.dataType == nOp2.dataType) {
            // both values are integers
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString(nOp1.intValue * nOp2.intValue);
            // both values are floats
            else
                res.strValue = Double.toString(nOp1.doubleValue * nOp2.doubleValue);
        }
        else
        {
            // first value is integer, second is float
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString((int)(nOp1.intValue * nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue * nOp2.intValue);
        }

        return res;
    }

    /**
     * Divides the Values of two Numerics and returns a ResultValue.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue divide(Scanner scanner, Numeric nOp1, Numeric nOp2) throws Exception
    {
        // check for divide by zero
        if ((nOp2.dataType == SubClassif.INTEGER && nOp2.intValue == 0) ||
            (nOp2.dataType == SubClassif.FLOAT && nOp2.doubleValue == 0))
            // cannot divide by zero
            throw new NumericConstantException(scanner.currentToken, scanner.sourceFileNm,
                    "Cannot Divide by Zero");

        // result has data type of first operand
        ResultValue res =  new ResultValue("", nOp1.dataType);

        // Do operation and store resulting string
        if (nOp1.dataType == nOp2.dataType) {
            // both values are integers
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString(nOp1.intValue / nOp2.intValue);
            // both values are floats
            else
                res.strValue = Double.toString(nOp1.doubleValue / nOp2.doubleValue);
        }
        else
        {
            // first value is integer, second is float
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString((int)(nOp1.intValue / nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue / nOp2.intValue);
        }

        return res;
    }

    /**
     * Raises a Numeric to the Power of another Numeric and returns a ResultValue.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue power(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        // result has data type of first operand
        ResultValue res =  new ResultValue("", nOp1.dataType);

        // Do operation and store resulting string
        if (nOp1.dataType == nOp2.dataType) {
            // both values are integers
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString((int) Math.pow(nOp1.intValue, nOp2.intValue));
            // both values are floats
            else
                res.strValue = Double.toString(Math.pow(nOp1.doubleValue, nOp2.doubleValue));
        }
        else
        {
            // first value is integer, second is float
            if (nOp1.dataType == SubClassif.INTEGER)
                res.strValue = Integer.toString((int) Math.pow(nOp1.intValue, nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(Math.pow(nOp1.doubleValue, nOp2.intValue));
        }

        return res;
    }

    /**
     * Tests if two Numeric Values are equal.
     * Returns true if the first Numeric value is equal to the second Numeric value.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return boolean - True if nOp1 is equal to nOp2; False otherwise
     */
    public static boolean equalTo(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        boolean res;

        // Do Test
        if (nOp1.dataType == nOp2.dataType) {
            // both values are integers
            if (nOp1.dataType == SubClassif.INTEGER)
                res = nOp1.intValue == nOp2.intValue;
            // both values are floats
            else
                res = nOp1.doubleValue == nOp2.doubleValue;
        }
        else
        {
            // first value is integer, second is float
            if (nOp1.dataType == SubClassif.INTEGER)
                res = nOp1.intValue == nOp2.doubleValue;
            // first value is float, second is integer
            else
                res = nOp1.doubleValue == nOp2.intValue;
        }

        return res;
    }

    /**
     * Tests if two Numeric Values are not equal.
     * Returns true if the first Numeric value is not equal to the second Numeric value.
     * This is effectively a wrapper for the equalTo function.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return boolean - True if nOp1 is not equal to nOp2; False otherwise
     */
    public static boolean notEqualTo(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        return !(equalTo(scanner, nOp1, nOp2));
    }

    /**
     * Tests if a Numeric value is less than another Numeric value.
     * Returns true if the first Numeric is less than the second Numeric.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return boolean - True if nOp1 is less than nOp2; False otherwise
     */
    public static boolean lessThan(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        boolean res;

        // Do Test
        if (nOp1.dataType == nOp2.dataType) {
            // both values are integers
            if (nOp1.dataType == SubClassif.INTEGER)
                res = nOp1.intValue < nOp2.intValue;
            // both values are floats
            else
                res = nOp1.doubleValue < nOp2.doubleValue;
        }
        else
        {
            // first value is integer, second is float
            if (nOp1.dataType == SubClassif.INTEGER)
                res = nOp1.intValue < nOp2.doubleValue;
            // first value is float, second is integer
            else
                res = nOp1.doubleValue < nOp2.intValue;
        }

        return res;
    }

    /**
     * Tests if a Numeric value is greater than another Numeric value.
     * Returns true if the first Numeric is greater than the second Numeric.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return boolean - True if nOp1 is greater than nOp2; False otherwise
     */
    public static boolean greaterThan(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        boolean res;

        // Do Test
        if (nOp1.dataType == nOp2.dataType) {
            // both values are integers
            if (nOp1.dataType == SubClassif.INTEGER)
                res = nOp1.intValue > nOp2.intValue;
            // both values are floats
            else
                res = nOp1.doubleValue > nOp2.doubleValue;
        }
        else
        {
            // first value is integer, second is float
            if (nOp1.dataType == SubClassif.INTEGER)
                res = nOp1.intValue > nOp2.doubleValue;
            // first value is float, second is integer
            else
                res = nOp1.doubleValue > nOp2.intValue;
        }

        return res;
    }

    /**
     * Tests if a Numeric value is less than or equal to another Numeric value.
     * Returns true if the first Numeric is less than or equal to the second Numeric.
     * This is effectively a wrapper for the equalTo() and lessThan() functions.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return boolean - True if nOp1 is greater than or equal to nOp2; False otherwise
     */
    public static boolean lessThanOrEqualTo(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        return (lessThan(scanner, nOp1, nOp2) || equalTo(scanner, nOp1, nOp2));
    }

    /**
     * Tests if a Numeric value is greater than or equal to another Numeric value.
     * Returns true if the first Numeric is greater than or equal to the second Numeric.
     * This is effectively a wrapper for the equalTo() and greaterThan() functions.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return boolean - True if nOp1 is greater than or equal to nOp2; False otherwise
     */
    public static boolean greaterThanOrEqualTo(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        return (greaterThan(scanner, nOp1, nOp2) || equalTo(scanner, nOp1, nOp2));
    }
}
