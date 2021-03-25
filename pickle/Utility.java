package pickle;

/**
 * Utility class provides various utility functions for Scanner class.
 *
 * <p> Scanner Operations:
 *     Skip Whitespace, Skip Comments
 *
 * <p> Operate on Numerics:
 *     Cast to Integer, Cast to Double,
 *     Add, Subtract, Unary Minus, Multiply, Divide, and Power operations.
 *
 * <p> Compare Numerics:
 *     ==. !=. <, >, <=, >=
 *
 * <p> Compare Booleans:
 *     and, or, not
 *
 * <p> Compare String/Chars:
 *     ==. !=. <, >, <=, >=
 */
public class Utility {
    // ==================== SCANNER OPERATIONS =====================
    // =============================================================
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

    // ==================== NUMERIC OPERATIONS =====================
    // =============================================================
    /**
     * Casts a Numeric Value to an Integer and returns a Result Value
     * containing the correct type and string value.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @return ResultValue
     */
    public static ResultValue castNumericToInt(Scanner scanner, Numeric nOp1)
    {
        // result will be of type INTEGER
        ResultValue res =  new ResultValue("", SubClassif.INTEGER);
        // get value of numeric
        int iValue = (nOp1.dataType == SubClassif.INTEGER) ? nOp1.intValue : (int)nOp1.doubleValue;
        // convert to string
        res.strValue = Integer.toString(iValue);
        return res;
    }

    /**
     * Casts a Numeric Value to a Double and returns a Result Value
     * containing the correct type and string value.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @return ResultValue
     */
    public static ResultValue castNumericToDouble(Scanner scanner, Numeric nOp1)
    {
        // result will be of type FLOAT
        ResultValue res =  new ResultValue("", SubClassif.FLOAT);
        // get value of numeric
        double fValue = (nOp1.dataType == SubClassif.INTEGER) ? (double)nOp1.intValue : nOp1.doubleValue;
        // convert to string
        res.strValue = Double.toString(fValue);
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

    // ==================== NUMERIC COMPARISONS ====================
    // =============================================================
    /**
     * Tests if two Numeric Values are equal.
     * Returns ResultValue "T" if the first Numeric value is equal
     * to the second Numeric value, "F" otherwise.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue equalTo(Scanner scanner, Numeric nOp1, Numeric nOp2)
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

        // result is a ResultValue representing a Bool
        String strRes = (res) ? "T" : "F";
        return new ResultValue(strRes, SubClassif.BOOLEAN);
    }

    /**
     * Tests if two Numeric Values are not equal.
     * Returns ResultValue "T" if the first Numeric value is not equal
     * to the second Numeric value, "F" otherwise.
     * This is effectively a wrapper for the equalTo function.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue - True if nOp1 is not equal to nOp2; False otherwise
     */
    public static ResultValue notEqualTo(Scanner scanner, Numeric nOp1, Numeric nOp2)
    {
        // result is a ResultValue representing a Bool
        ResultValue res = equalTo(scanner, nOp1, nOp2);
        // the value is the opposite of what was returned by equalTo()
        return new ResultValue((res.strValue.equals("T")) ? "F" : "T", SubClassif.BOOLEAN);
    }

    /**
     * Tests if a Numeric value is less than another Numeric value.
     * Returns ResultValue "T" if the first Numeric value is less than
     * the second Numeric value, "F" otherwise.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue - True if nOp1 is less than nOp2; False otherwise
     */
    public static ResultValue lessThan(Scanner scanner, Numeric nOp1, Numeric nOp2)
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

        // result is a ResultValue representing a Bool
        String strRes = (res) ? "T" : "F";
        return new ResultValue(strRes, SubClassif.BOOLEAN);
    }

    /**
     * Tests if a Numeric value is greater than another Numeric value.
     * Returns ResultValue "T" if the first Numeric value is greater than
     * the second Numeric value, "F" otherwise.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue - True if nOp1 is greater than nOp2; False otherwise
     */
    public static ResultValue greaterThan(Scanner scanner, Numeric nOp1, Numeric nOp2)
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

        // result is a ResultValue representing a Bool
        String strRes = (res) ? "T" : "F";
        return new ResultValue(strRes, SubClassif.BOOLEAN);
    }

    /**
     * Tests if a Numeric value is less than or equal to another Numeric value.
     * Returns ResultValue "T" if the first Numeric value is less than or equal to
     * the second Numeric value, "F" otherwise.
     * This is effectively a wrapper for the equalTo() and lessThan() functions.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue - True if nOp1 is greater than or equal to nOp2; False otherwise
     */
    public static ResultValue lessThanOrEqualTo(Scanner scanner, Numeric nOp1, Numeric nOp2) throws BoolException {
        // Get result of equalTo() and LessThan()
        ResultValue res1 = equalTo(scanner, nOp1, nOp2);
        ResultValue res2 = lessThan(scanner, nOp1, nOp2);

        // Convert result into Bool objects
        Bool bOp1 = new Bool(scanner, res1);
        Bool bOp2 = new Bool(scanner, res1);

        // Or both Bools, return result
        return boolOr(scanner, bOp1, bOp2);
    }

    /**
     * Tests if a Numeric value is greater than or equal to another Numeric value.
     * Returns ResultValue "T" if the first Numeric value is greater than or equal to
     * the second Numeric value, "F" otherwise.
     * This is effectively a wrapper for the equalTo() and greaterThan() functions.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param scanner   Scanner object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue - True if nOp1 is greater than or equal to nOp2; False otherwise
     */
    public static ResultValue greaterThanOrEqualTo(Scanner scanner, Numeric nOp1, Numeric nOp2) throws BoolException {
        // Get result of equalTo() and LessThan()
        ResultValue res1 = equalTo(scanner, nOp1, nOp2);
        ResultValue res2 = greaterThan(scanner, nOp1, nOp2);

        // Convert result into Bool objects
        Bool bOp1 = new Bool(scanner, res1);
        Bool bOp2 = new Bool(scanner, res1);

        // Or both Bools, return result
        return boolOr(scanner, bOp1, bOp2);
    }

    // ====================== BOOL OPERATIONS ======================
    // =============================================================
    /**
     * Boolean AND test on two Bool Operands.
     * Returns a ResultValue containing the result of the test.
     *
     * @param scanner   Scanner object
     * @param bOp1      Bool Operand 1
     * @param bOp2      Bool Operand 2
     * @return ResultValue
     */
    public static ResultValue boolAnd(Scanner scanner, Bool bOp1, Bool bOp2)
    {
        ResultValue res =  new ResultValue("", bOp1.dataType);
        res.strValue = (bOp1.bValue && bOp2.bValue) ? "T" : "F";
        return res;
    }

    /**
     * Boolean OR test on two Bool Operands.
     * Returns a ResultValue containing the result of the test.
     *
     * @param scanner   Scanner object
     * @param bOp1      Bool Operand 1
     * @param bOp2      Bool Operand 2
     * @return ResultValue
     */
    public static ResultValue boolOr(Scanner scanner, Bool bOp1, Bool bOp2)
    {
        ResultValue res =  new ResultValue("", bOp1.dataType);
        res.strValue = (bOp1.bValue || bOp2.bValue) ? "T" : "F";
        return res;
    }

    /**
     * Boolean NOT test on two Bool Operands.
     * Returns a ResultValue containing the result of the test.
     *
     * @param scanner   Scanner object
     * @param bOp1      Bool Operand 1
     * @return ResultValue
     */
    public static ResultValue boolNot(Scanner scanner, Bool bOp1)
    {
        ResultValue res =  new ResultValue("", bOp1.dataType);
        res.strValue = (!bOp1.bValue) ? "T" : "F";
        return res;
    }

    // ===================== STRING COMPARISONS ====================
    // =============================================================
    //TODO: String Comparison Functions
    public static boolean strEqual(Scanner scanner, ResultValue resVal1, ResultValue resVal2)
    {
        return true;
    }
    public static boolean strNotEqual(Scanner scanner, ResultValue resVal1, ResultValue resVal2)
    {
        return true;
    }
    public static boolean strLessThan(Scanner scanner, ResultValue resVal1, ResultValue resVal2)
    {
        return true;
    }
    public static boolean strGreaterThan(Scanner scanner, ResultValue resVal1, ResultValue resVal2)
    {
        return true;
    }
    public static boolean strLessThanOrEqualTo(Scanner scanner, ResultValue resVal1, ResultValue resVal2)
    {
        return true;
    }
    public static boolean strGreaterThanOrEqualTo(Scanner scanner, ResultValue resVal1, ResultValue resVal2)
    {
        return true;
    }
}
