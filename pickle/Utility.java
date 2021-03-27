package pickle;

/**
 * Utility class provides various utility functions for Scanner class.
 *
 * <p> Scanner Operations:
 *     Skip Whitespace, Skip Comments
 *
 * <p>
 * All operation and comparison functions return ResultValues containing
 * the data type and string value of the operation's or comparison's result.
 *
 * <p> Operate on Numerics:
 *     Cast to Integer, Cast to Double,
 *     Add, Subtract, Unary Minus, Multiply, Divide, and Power operations.
 *
 * <p> Compare Booleans:
 *     and, or, not
 *
 * <p> Compare Strings and Numerics:
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

    // ==================== TYPE COERCIONS =====================
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

    // ==================== NUMERIC OPERATIONS =====================
    // =============================================================
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
    public static ResultValue divide(Scanner scanner, Numeric nOp1, Numeric nOp2) throws PickleException
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

    // ===================== GENERIC COMPARISONS ====================
    // =============================================================
    /**
     * Tests if two ResultValues are equal.
     * Returns ResultValue "T" if the first ResultValue is equal
     * to the second ResultValue, "F" otherwise.
     * <p>
     *      The ResultValue returned will be of Boolean type.
     *      If the first operand is of type String, a lexicographical
     *      comparison will be assumed.
     * <p>
     *      If the first operand is a Numeric,
     *      a numeric comparison will be assumed. If the second operand
     *      has a different type than the first, and exception will be thrown.
     *
     * @param scanner   Scanner object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue equal(Scanner scanner, ResultValue resVal1, ResultValue resVal2) throws Exception {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '==' cannot be applied String and Numeric");
            // Do a lexicographical comparison
            // Set "T" if both strings are equal, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) == 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '==' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(scanner, resVal1, "==", "test equal");
            Numeric nOp2 = new Numeric(scanner, resVal2, "==", "test equal");

            // Compare the values
            boolean bResult;
            if (nOp1.dataType == nOp2.dataType) {
                // both values are integers
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue == nOp2.intValue;
                // both values are floats
                else
                    bResult = nOp1.doubleValue == nOp2.doubleValue;
            }
            else
            {
                // first value is integer, second is float
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue == nOp2.doubleValue;
                // first value is float, second is integer
                else
                    bResult = nOp1.doubleValue == nOp2.intValue;
            }

            // store comparison result into ResultValue
            res.strValue = (bResult) ? "T" : "F";
        }

        return res;
    }

    /**
     * Tests if two ResultValues are not equal.
     * Returns ResultValue "T" if the first ResultValue is not equal
     * to the second ResultValue, "F" otherwise.
     * <p>
     *      The ResultValue returned will be of Boolean type.
     *      If the first operand is of type String, a lexicographical
     *      comparison will be assumed.
     * <p>
     *      If the first operand is a Numeric,
     *      a numeric comparison will be assumed. If the second operand
     *      has a different type than the first, and exception will be thrown.
     *
     * @param scanner   Scanner object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue notEqual(Scanner scanner, ResultValue resVal1, ResultValue resVal2) throws Exception {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '!=' cannot be applied String and Numeric");
            // Set "T" if both strings are equal, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) != 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '!=' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(scanner, resVal1, "!=", "test not equal");
            Numeric nOp2 = new Numeric(scanner, resVal2, "!=", "test not equal");


            // Compare the values
            boolean bResult;
            if (nOp1.dataType == nOp2.dataType) {
                // both values are integers
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue != nOp2.intValue;
                // both values are floats
                else
                    bResult = nOp1.doubleValue != nOp2.doubleValue;
            }
            else
            {
                // first value is integer, second is float
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue != nOp2.doubleValue;
                // first value is float, second is integer
                else
                    bResult = nOp1.doubleValue != nOp2.intValue;
            }

            // store comparison result into ResultValue
            res.strValue = (bResult) ? "T" : "F";
        }

        return res;
    }

    /**
     * Tests if a ResultValue is less than another ResultValue.
     * Returns ResultValue "T" if the first ResultValue is less than
     * the second ResultValue, "F" otherwise.
     * <p>
     *      The ResultValue returned will be of Boolean type.
     *      If the first operand is of type String, a lexicographical
     *      comparison will be assumed.
     * <p>
     *      If the first operand is a Numeric,
     *      a numeric comparison will be assumed. If the second operand
     *      has a different type than the first, and exception will be thrown.
     *
     * @param scanner   Scanner object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue lessThan(Scanner scanner, ResultValue resVal1, ResultValue resVal2) throws Exception {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '<' cannot be applied String and Numeric");
            // Set "T" if resVal1 is lexicographically less than resVal2, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) < 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '<' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(scanner, resVal1, "<", "test less than");
            Numeric nOp2 = new Numeric(scanner, resVal2, "<", "test less than");

            // Compare the values
            boolean bResult;
            if (nOp1.dataType == nOp2.dataType) {
                // both values are integers
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue < nOp2.intValue;
                // both values are floats
                else
                    bResult = nOp1.doubleValue < nOp2.doubleValue;
            }
            else
            {
                // first value is integer, second is float
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue < nOp2.doubleValue;
                // first value is float, second is integer
                else
                    bResult = nOp1.doubleValue < nOp2.intValue;
            }

            // store comparison result into ResultValue
            res.strValue = (bResult) ? "T" : "F";
        }

        return res;
    }

    /**
     * Tests if a ResultValue is greater than another ResultValue.
     * Returns ResultValue "T" if the first ResultValue is greater than
     * the second ResultValue, "F" otherwise.
     * <p>
     *      The ResultValue returned will be of Boolean type.
     *      If the first operand is of type String, a lexicographical
     *      comparison will be assumed.
     * <p>
     *      If the first operand is a Numeric,
     *      a numeric comparison will be assumed. If the second operand
     *      has a different type than the first, and exception will be thrown.
     *
     * @param scanner   Scanner object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */

    public static ResultValue greaterThan(Scanner scanner, ResultValue resVal1, ResultValue resVal2) throws Exception {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '>' cannot be applied String and Numeric");
            // Set "T" if resVal1 is lexicographically greater than resVal2, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) > 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '>' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(scanner, resVal1, ">", "test greater than");
            Numeric nOp2 = new Numeric(scanner, resVal2, ">", "test greater than");

            // Compare the values
            boolean bResult;
            if (nOp1.dataType == nOp2.dataType) {
                // both values are integers
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue > nOp2.intValue;
                    // both values are floats
                else
                    bResult = nOp1.doubleValue > nOp2.doubleValue;
            }
            else
            {
                // first value is integer, second is float
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue > nOp2.doubleValue;
                    // first value is float, second is integer
                else
                    bResult = nOp1.doubleValue > nOp2.intValue;
            }

            // store comparison result into ResultValue
            res.strValue = (bResult) ? "T" : "F";
        }

        return res;
    }

    /**
     * Tests if a ResultValue is less than or equals to another ResultValue.
     * Returns ResultValue "T" if the first ResultValue is less than or equals to
     * the second ResultValue, "F" otherwise.
     * <p>
     *      The ResultValue returned will be of Boolean type.
     *      If the first operand is of type String, a lexicographical
     *      comparison will be assumed.
     * <p>
     *      If the first operand is a Numeric,
     *      a numeric comparison will be assumed. If the second operand
     *      has a different type than the first, and exception will be thrown.
     *
     * @param scanner   Scanner object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue lessThanOrEqualTo(Scanner scanner, ResultValue resVal1, ResultValue resVal2) throws Exception {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '<=' cannot be applied String and Numeric");
            // Set "T" if resVal1 is lexicographically less than or equals to resVal2, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) <= 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '<=' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(scanner, resVal1, "<=", "test less than or equal");
            Numeric nOp2 = new Numeric(scanner, resVal2, "<=", "test less than or equal");

            // Compare the values
            boolean bResult;
            if (nOp1.dataType == nOp2.dataType) {
                // both values are integers
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue <= nOp2.intValue;
                    // both values are floats
                else
                    bResult = nOp1.doubleValue <= nOp2.doubleValue;
            }
            else
            {
                // first value is integer, second is float
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue <= nOp2.doubleValue;
                    // first value is float, second is integer
                else
                    bResult = nOp1.doubleValue <= nOp2.intValue;
            }

            // store comparison result into ResultValue
            res.strValue = (bResult) ? "T" : "F";
        }

        return res;
    }

    /**
     * Tests if a ResultValue is greater than or equals to another ResultValue.
     * Returns ResultValue "T" if the first ResultValue is greater than or equals to
     * the second ResultValue, "F" otherwise.
     * <p>
     *      The ResultValue returned will be of Boolean type.
     *      If the first operand is of type String, a lexicographical
     *      comparison will be assumed.
     * <p>
     *      If the first operand is a Numeric,
     *      a numeric comparison will be assumed. If the second operand
     *      has a different type than the first, and exception will be thrown.
     *
     * @param scanner   Scanner object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue greaterThanOrEqualTo(Scanner scanner, ResultValue resVal1, ResultValue resVal2) throws Exception {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING)
        {
            // If the second operand is not a String, throw exception
            if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '>=' cannot be applied String and Numeric");
            // Set "T" if resVal1 is lexicographically greater than or equal to resVal2, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) >= 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '>=' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(scanner, resVal1, ">=", "test greater than or equal");
            Numeric nOp2 = new Numeric(scanner, resVal2, ">=", "test greater than or equal");

            // Compare the values
            boolean bResult;
            if (nOp1.dataType == nOp2.dataType) {
                // both values are integers
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue >= nOp2.intValue;
                    // both values are floats
                else
                    bResult = nOp1.doubleValue >= nOp2.doubleValue;
            }
            else
            {
                // first value is integer, second is float
                if (nOp1.dataType == SubClassif.INTEGER)
                    bResult = nOp1.intValue >= nOp2.doubleValue;
                    // first value is float, second is integer
                else
                    bResult = nOp1.doubleValue >= nOp2.intValue;
            }

            // store comparison result into ResultValue
            res.strValue = (bResult) ? "T" : "F";
        }

        return res;
    }
}
