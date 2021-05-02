package pickle;

import java.util.ArrayList;

/**
 * Utility class provides various utility functions for Scanner class.
 *
 * <p> Scanner Operations:
 *     Skip Whitespace, Skip Comments
 *
 * <p> Data Typing Operations:
 *     Coerce
 *
 * <p>
 * All operation and comparison functions return ResultValues containing
 * the data type and string value of the operation's or comparison's result.
 *
 * <p> Array Operations:
 *      Array to Array Assignment.
 *      Array scalar Assignment.
 *
 * <p> String Operations:
 *     Concatenate String
 *     Get Character at Subscript
 *     Assign String to String starting at index
 *     Assign Char to String at index
 *     Built-In LENGTH()
 *     Built-In SPACES()
 *
 * <p> Numeric Operations:
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

    /**
     * Helper function to skip to end of if control block
     * <p>
     *
     * </p>
     * @param token token
     * @throws PickleException if scanner.getNext() failes
     */
    public static void skipTo(Scanner scanner, String token) throws PickleException
    {
        while (!scanner.getNext().equals(token));
    }

    // ================== DATA TYPING OPERATIONS ===================
    // =============================================================

    /**
     * Coerces a ResultValue object into the provided SubClassif Data Type.
     *
     * <p> If the ResultValue's data type is the same as the target date type, nothing happens.
     *
     * <p> Target data type must be INTEGER, FLOAT, BOOL, STRING or DATE
     *
     * <p> When coercing a ResultValue, the value will be validated before coercion.
     *     An error will be thrown if the coercion is invalid.
     *
     * <p> This method will properly classify a ResultValue of EMPTY type to the target type
     *     if the coercion is valid.
     *
     * @param parser         Parser object.
     * @param value          ResultValue to be coerced.
     * @param targetDataType SubClassif Data Type to coerce ResultValue to.
     * @return ResultValue of target SubClassif Data Type
     * @throws PickleException If coercion is invalid.
     */
    public static ResultValue coerce(Parser parser, ResultValue value, SubClassif targetDataType) throws PickleException
    {
        // if source and target is same data type, return immediately, assumed that it is valid value
        if (value.dataType == targetDataType)
            return value;

        // if target data type is not INTEGER, FLOAT, BOOL, STRING or DATE throw error
        if (targetDataType != SubClassif.INTEGER && targetDataType != SubClassif.FLOAT
            && targetDataType != SubClassif.BOOLEAN && targetDataType != SubClassif.STRING
            && targetDataType != SubClassif.DATE)
        {
            throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                    "Invalid casting operation.");
        }

        String res = "";
        Numeric numeric;
        Bool bool;
        ResultValue resultValue;

        // coercion is different for each source data type to each target data type
        switch(value.dataType)
        {
            case INTEGER: // INTEGER can be coerced into FLOAT or STRING
                if (targetDataType != SubClassif.FLOAT && targetDataType != SubClassif.STRING)
                {
                    throw new NumericConstantException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                            "Invalid casting of Int.");
                }
                // Check for invalid numeric
                numeric = new Numeric(parser, value, "", "");
                // if target type is FLOAT, convert INTEGER into FLOAT
                if (targetDataType == SubClassif.FLOAT) res = Double.toString(numeric.intValue);
                // if target type is STRING, convert INTEGER into STRING
                else res = numeric.strValue;
                break;

            case FLOAT: // FLOAT can be coerced into INTEGER or STRING
                if (targetDataType != SubClassif.INTEGER && targetDataType != SubClassif.STRING)
                {
                    throw new NumericConstantException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                            "Invalid casting of Float.");
                }
                // Check for invalid numeric
                numeric = new Numeric(parser, value, "", "");
                // if target type is INTEGER, convert FLOAT into INTEGER
                if (targetDataType == SubClassif.INTEGER) res = Integer.toString((int) numeric.doubleValue);
                // if target type is STRING, convert FLOAT into STRING
                else res = numeric.strValue;
                break;

            case BOOLEAN: // BOOLEAN can be coerced into STRING
                if (targetDataType != SubClassif.STRING)
                {
                    throw new BoolException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                            "Invalid casting of Bool.");
                }
                // Check for invalid bool
                bool = new Bool(parser, value);
                // convert Bool into String
                res = bool.strValue;
                break;

            case STRING: // STRING can be coerced into INTEGER, FLOAT, BOOLEAN OR DATE
            case EMPTY:  // EMPTY sub-classification could be anything...
                switch (targetDataType)
                {
                    case INTEGER:
                    case FLOAT:
                        // Check for invalid numeric
                        numeric = new Numeric(parser, value, "", "");
                        res  = numeric.strValue;
                        break;

                    case BOOLEAN:
                        // Check for invalid bool
                        bool = new Bool(parser, value);
                        res = bool.strValue;
                        break;

                    case DATE:
                        // Check for invalid date
                        resultValue = Date.validateDate(value.strValue);
                        res = resultValue.strValue;
                        break;

                    default:
                        // Must be converting EMPTY to STRING
                        res = value.strValue;

                }
                break;

            case DATE:
                // DATE can be coerced into a STRING
                if (targetDataType != SubClassif.STRING)
                {
                    // TODO: change this to DateException
                    throw new NumericConstantException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                            "Invalid casting of Date.");
                }
                // check for invalid Date
                resultValue = Date.validateDate(value.strValue);
                // convert date into string
                res = resultValue.strValue;

                break;
        }

        // TODO delete this if statement since it should never be true anyways
        //      just here for debugging purposes.
        if (res.equals(""))
            throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                    "This should not happen please check coerce function lamo.");

        return new ResultValue(res, targetDataType);
    }

    // ====================== ARRAY FUNCTIONS ======================
    // =============================================================

    /**
     * Returns the subscript of the highest populated element + 1 as a ResultValue.
     * This is the ResultLists allocated size.
     *
     * @param parser Parser Object
     * @param array  ResultList Array
     * @return ResultValue that is the ELEM index
     */
    public static ResultValue builtInELEM(ArrayList<Result> params)
    {
        return new ResultValue(Integer.toString(((ResultList) params.get(0)).allocatedSize), SubClassif.INTEGER);
    }

    /**
     * Returns the value of the declared number of elements as a ResultValue.
     * This is the ResultLists capacity.
     *
     * @param params  Result Array
     * @return ResultValue that is the MAXELEM index
     */
    public static ResultValue builtInMAXELEM(ArrayList<Result> params)
    {
        return new ResultValue(Integer.toString(((ResultList) params.get(0)).capacity), SubClassif.INTEGER);
    }

    /**
     * Returns a ResultValue of Type Bool that is true if a ResultValue is present in a ResultList.
     *
     *
     * @param parser Parser Object.
     * @param value  ResultValue value to find in list.
     * @param array  ResultList to look for value in.
     * @return ResultValue of Type Bool
     * @throws PickleException
     */
    public static ResultValue builtInIN(Parser parser, ResultValue value, ResultList array) throws PickleException
    {
        String res = "";
        Numeric valueNum;
        Numeric elementNum;

        switch(value.dataType)
        {
            case STRING:  // If the value to check is a String or Boolean compare the string values
            case BOOLEAN: // of the array to find a match
                // iterate over entire list
                for (int i = 0; i < array.arrayList.size(); i++)
                {
                    // if the array element is not empty and matches the value we are looking for
                    if (array.arrayList.get(i).dataType != SubClassif.EMPTY
                            && (array.arrayList.get(i).strValue.equals(value.strValue)))
                    {
                        return new ResultValue("T", SubClassif.BOOLEAN);
                    }
                }
                break;
            case FLOAT: // If the value to check is a Float then we can only compare using numeric comparisons
                // verify the value is a numeric, and get object for comparison
                valueNum = new Numeric(parser, value, "", "");

                switch (array.dataType)
                {
                    case DATE:
                    case BOOLEAN:
                    case STRING:  // Value List is of incompatible type
                        throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                "Cannot compare Numeric to value list.");
                    case INTEGER:
                    case FLOAT:  // Value List is of compatible type
                        // iterate over entire list
                        for (int i = 0; i < array.arrayList.size(); i++)
                        {
                            // if the array element is not empty
                            if (array.arrayList.get(i).dataType != SubClassif.EMPTY)
                            {
                                // coerce the element into a Float
                                elementNum = new Numeric(parser, array.arrayList.get(i), "", "");
                                double test = (array.dataType == SubClassif.INTEGER) ? (double)elementNum.intValue : elementNum.doubleValue;
                                if (test == valueNum.doubleValue)
                                {
                                    return new ResultValue("T", SubClassif.BOOLEAN);
                                }
                            }
                        }
                        return new ResultValue("F", SubClassif.BOOLEAN);
                }
                break;
            case INTEGER:
                // If the value to check is an Integer then we can only compare using numeric comparisons
                valueNum = new Numeric(parser, value, "", "");

                switch (array.dataType)
                {
                    case DATE:
                    case BOOLEAN:
                    case STRING: // Value List is of incompatible type
                        throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                "Cannot compare Numeric to value list.");
                    case INTEGER:
                    case FLOAT:  // Value List is of compatible type
                        // iterate over entire list
                        for (int i = 0; i < array.arrayList.size(); i++)
                        {
                            // if the array element is not empty
                            if (array.arrayList.get(i).dataType != SubClassif.EMPTY)
                            {
                                // coerce the element into an Integer
                                elementNum = new Numeric(parser, array.arrayList.get(i), "", "");
                                double test = (array.dataType == SubClassif.FLOAT) ? (int)elementNum.doubleValue : elementNum.intValue;
                                if (test == valueNum.intValue)
                                {
                                    return new ResultValue("T", SubClassif.BOOLEAN);
                                }
                            }
                        }
                        return new ResultValue("F", SubClassif.BOOLEAN);
                }
                break;
        }
        return new ResultValue(res, SubClassif.BOOLEAN);
    }

    /**
     * Returns a ResultValue of Type Bool that is true if a ResultValue is NOT present in a ResultList.
     *
     * <p> Negated return of builtInIn.
     *
     * @param parser Parser Object.
     * @param value  ResultValue value to find in list.
     * @param array  ResultList to look for value in.
     * @return ResultValue of Type Bool
     * @throws PickleException
     */
    public static ResultValue builtInNOTIN(Parser parser, ResultValue value, ResultList array) throws PickleException {
        return boolNot(parser, new Bool(parser, builtInIN(parser, value, array)));
    }

    /**
     * Assigns an Array to an Array.
     *
     * <p> If a larger array is copied into a smaller array,
     * the smaller array is filled with the corresponding elements of the larger array.
     *
     * <p> If a smaller array is copied into a larger array,
     * the larger array is filled with the corresponding elements of the smaller array,
     * and the remaining portion of the larger array is filled with empty values.
     *
     * @param parser      Parser Object
     * @param targetArray ResultList that is target
     * @param sourceArray ResultList to copy to target.
     * @return the Target ResultList with the newly assigned values
     * @throws ResultListException
     */
    public static ResultList assignArrayToArray(Parser parser, ResultList targetArray, ResultList sourceArray) throws ResultListException
    {
        ResultValue emptyValue = new ResultValue("", SubClassif.EMPTY);
        ArrayList<ResultValue> arrayList = new ArrayList<ResultValue>();

        // copy items from source to target until:
        //         target array is full
        //      or source array has no more items
        for (int i = 0; i < targetArray.capacity; i++)
        {
            // if the sourceArray has more items, copy value
            if (i < sourceArray.allocatedSize)
            {
                // assign source value at index i to target index
                arrayList.add(sourceArray.getItem(parser, i));
            }
            // sourceArray has no more items, fill rest of target with empty values
            else
            {
                arrayList.add(emptyValue);
            }
        }

        return new ResultList(parser, arrayList, targetArray.capacity, targetArray.dataType);
    }

    /**
     * Assigns a scalar to an Array.
     *
     * <p> Creates an array in which every element is filled with the same value.
     *
     * @param parser Parser Object
     * @param value  ResultValue to assign to every element
     * @param size   Size of the array to be created
     * @return ResultList that is the created array
     * @throws ResultListException if the Result List could not be created.
     */
    public static ResultList assignScalarToArray(Parser parser, ResultValue value, int size) throws ResultListException {
        // Create List of ResultValues to become ResultList
        ArrayList<ResultValue> arrayList = new ArrayList<ResultValue>(size);
        // Assign same value to all indexes of list
        for (int i = 0; i < size; i++) {
            arrayList.add(value);
        }
        // return the ResultList
        return new ResultList(parser, arrayList, size, value.dataType);

    }

    /**
     * Returns the sub-array (slice) of a ResultList object given a lowerbound and upperbound index.
     * The lowerbound is inclusive and upperbound is exclusive.
     *
     * <p> Either of the lowerbound or upperbound indexes can be excluded by providing a negative index.
     *
     * <p> e.g. 1:
     *     given array = [1, 2, 3, 4, 5]
     *     newArray = (parser, array, 2, -1)
     *     newArray = [3, 4, 5]
     *
     * <p> e.g. 2:
     *     given array = [1, 2, 3, 4, 5]
     *     newArray = (parser, array, -1, 3)
     *     newArray = [1, 2, 3]
     *
     * <p> e.g. 3:
     *     given array = [1, 2, 3, 4, 5]
     *     newArray = (parser, array, 2, 4)
     *     newArray = [3, 4]
     *
     * @param parser     Parser object
     * @param array      ResultList to get slice of
     * @param lowerBound Lowerbourd index of slice (inclusive)
     * @param upperBound Upperbound index of slice (exclusive)
     * @return ResultList that is the sub-array from lowerbound (inclusive) to upperbound (exclusive)
     * @throws ResultListException if provided indexes are out of bounds
     */
    public static ResultList getArraySlice(Parser parser, ResultList array, int lowerBound, int upperBound) throws ResultListException
    {
        // translate any negative indexes into the proper index
        // and calculate the size of the new array
        int lb, ub, size;
        lb = Math.max(lowerBound, 0);
        ub = upperBound < 0 ? array.allocatedSize : upperBound;
        size = ub - lb;

        // verify that the provided upperbound and lowerbounds are valid
        // lb cannot be greater than ub, lb and ub must be within arrays allocated size
        if (lb > ub || lb > array.allocatedSize || ub > array.allocatedSize)
        {
            throw new ResultListException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                    "Array slice invalid, index(es) out of bounds.");
        }

        // Create List of ResultValues to become ResultList
        ArrayList<ResultValue> arrayList = new ArrayList<ResultValue>(size);

        // Copy the values from the given array to the new list
        int j = lb;
        for (int i = 0; i < size; i++) {
            arrayList.add(array.getItem(parser, j));
            j++;
        }

        // return the ResultList
        return new ResultList(parser, arrayList, size, array.dataType);
    }

    // ==================== STRING OPERATIONS =====================
    // =============================================================

    /**
     * Concatenates two strings together and returns a Result Value.
     *
     * @param parser Parser Object
     * @param op1    String 1 ResultValue
     * @param op2    String 2 ResultValue
     * @return ResultValue of type String
     */
    public static ResultValue concatenateString(Parser parser, ResultValue op1, ResultValue op2)
    {
        return new ResultValue(op1.strValue + op2.strValue, SubClassif.STRING);
    }

    /**
     * Returns the char at a given index of a string as a ResultValue
     *
     * <p> Supports negative indexing of strings (e.g. -1 is last char)
     *
     * @param parser      Parser Object
     * @param resultValue String as ResultValue
     * @param index       index of string to retrieve
     * @return ResultValue of type String
     * @throws StringException if index is out of bounds
     */
    public static ResultValue valueAtIndex(Parser parser, ResultValue resultValue, int index) throws StringException
    {
        // Normalize the index if it is negative
        int normalizedIndex;
        if (index < 0)
        {
            normalizedIndex = resultValue.strValue.length() + index;
        }
        else normalizedIndex = index;

        // verify boundaries of string
        if (normalizedIndex < 0 || normalizedIndex >= resultValue.strValue.length())
        {
            throw new StringException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                      "String subscript out of bounds.");
        }
        return new ResultValue(Character.toString(resultValue.strValue.charAt(normalizedIndex)), SubClassif.STRING);
    }

    /**
     * Assigns a String value starting at a given index to a String.
     * Returns the newly assigned string as a ResultValue
     *
     * <p> Supports negative indexing of strings (e.g. -1 is last char)
     *
     * @param parser Parser object
     * @param str1   String ResultValue to assign to
     * @param str2   String ResultValue to be assigned at the index
     * @param index  Starting position to assign the String
     * @return ResultValue of type string
     * @throws StringException if index is out of bounds
     */
    public static ResultValue assignAtIndex(Parser parser, ResultValue str1, ResultValue str2, int index) throws StringException
    {
        // Normalize the index if it is negative
        int normalizedIndex;
        if (index < 0)
        {
            normalizedIndex = str1.strValue.length() + index;
        }
        else normalizedIndex = index;

        // verify boundaries of string
        if (normalizedIndex < 0 || normalizedIndex >= str1.strValue.length())
        {
            throw new StringException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                    "String subscript out of bounds.");
        }
        // replace each of the chars in the string with the new strings chars
        StringBuilder newStringBuild = new StringBuilder(str1.strValue);
        int iStr1Pos;
        int iStr2Pos = 0;
        // for each index of string 1
        for(iStr1Pos = normalizedIndex; iStr1Pos < str1.strValue.length() ; iStr1Pos++)
        {
            // replace the str1[iStr1Pos] char with str2[iStr2Pos] char
            newStringBuild.setCharAt(iStr1Pos, str2.strValue.charAt(iStr2Pos));
            iStr2Pos++; // increment string 2 position
            // if reached end of str2 end loop
            if (iStr2Pos == str2.strValue.length()) break;
        }
        // if there are remaining chars to add from string 2
        if(iStr2Pos < str2.strValue.length())
        {
            newStringBuild.append(str2.strValue.substring(iStr2Pos));
        }
        // return the newly constructed string
        return new ResultValue(newStringBuild.toString(), SubClassif.STRING);
    }

    /**
     * Returns the number of characters in a given string as a ResultValue of type Integer.
     *
     * @param parser      Parser Object
     * @param resultValue String as a ResultValue
     * @return ResultValue of Type INTEGER which is the length of the string.
     */
    public static ResultValue builtInLENGTH(ArrayList<Result> param)
    {
        return new ResultValue(Integer.toString(((ResultValue) param.get(0)).strValue.length()), SubClassif.INTEGER);
    }

    /**
     * Returns "T" if the given ResultValue of Type String is empty or contains only spaces.
     * Otherwise returns "F". Value is returned as a ResultValue of type Boolean.
     *
     * @param parser      Parser Object
     * @param resultValue String as a ResultValue
     * @return A ResultValue of type BOOLEAN
     */
    public static ResultValue builtInSPACES(ArrayList<Result> param)
    {
        if (((ResultValue) param.get(0)).strValue.trim().isEmpty() || ((ResultValue) param.get(0)).strValue == null)
            return new ResultValue("T", SubClassif.BOOLEAN);
        else return new ResultValue("F", SubClassif.BOOLEAN);
    }

    /**
     * Returns the substring (slice) of a ResultValue string object given a lowerbound and upperbound index.
     * The lowerbound is inclusive and upperbound is exclusive.
     *
     * <p> Either of the lowerbound or upperbound indexes can be excluded by providing a negative index.
     *
     * <p> e.g. 1:
     *     given string = "goodbye"
     *     newString = (parser, string, 0, 4)
     *     newString = "good"
     *
     * <p> e.g. 2:
     *     given string = [1, 2, 3, 4, 5]
     *     newString = (parser, string, -1, 4)
     *     newString = "good"
     *
     * <p> e.g. 3:
     *     given string = [1, 2, 3, 4, 5]
     *     newString = (parser, string, 4, -1)
     *     newString = "bye"
     *
     * @param parser     Parser object
     * @param value      ResultValue String to be sliced
     * @param lowerBound Lowerbound of slice (inclusive)
     * @param upperBound Upperbound of slice (exclusive)
     * @return new ResultValue string that is the slice of the given string from lowerbound (inclusive)
     *         to upperbound (exclusive)
     * @throws StringException if given index(es) are out of bounds
     */
    public static ResultValue getStringSlice(Parser parser, ResultValue value, int lowerBound, int upperBound) throws StringException
    {
        // translate any negative indexes into the proper index
        // and calculate the size of the new array
        int lb, ub, size;
        lb = Math.max(lowerBound, 0);
        ub = upperBound < 0 ? value.strValue.length() : upperBound;
        size = ub - lb;

        // verify that the provided upperbound and lowerbounds are valid
        // lb cannot be greater than ub, lb and ub must be within arrays allocated size
        if (lb > ub || lb > value.strValue.length() || ub > value.strValue.length())
        {
            throw new StringException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                    "String slice invalid, index(es) out of bounds.");
        }

        // Create new string
        StringBuilder newStringBuild = new StringBuilder("");

        // Copy the values from the given string to the new string
        int j = lb;
        for (int i = 0; i < size; i++) {
            newStringBuild.append(value.strValue.charAt(j));
            j++;
        }

        // return the ResultList
        return new ResultValue(newStringBuild.toString(), SubClassif.STRING);
    }

    /**
     * Creates a new ResultValue string that is the result of replacing a slice of a string with a given value
     * given a lowerbound and upperbound index. The lowerbound is inclusive and upperbound is exclusive.
     *
     * <p> see StringBuilder.replace()
     *
     * @param parser     Parser object
     * @param target     ResultValue String to be sliced
     * @param lowerBound Lowerbound of slice (inclusive)
     * @param upperBound Upperbound of slice (exclusive)
     * @param value      ResultValue String that is the value to be inserted
     * @return new ResultValue string that is the slice of the given string from lowerbound (inclusive)
     *         to upperbound (exclusive)
     * @throws StringException if given index(es) are out of bounds
     */
    public static ResultValue getStringSliceAssign(Parser parser, ResultValue target, int lowerBound, int upperBound, ResultValue value)
            throws StringException
    {
        // translate any negative indexes into the proper index
        // and calculate the size of the new array
        int lb, ub, size;
        lb = Math.max(lowerBound, 0);
        ub = upperBound < 0 ? target.strValue.length() : upperBound;
        size = ub - lb;

        // verify that the provided upperbound and lowerbounds are valid
        // lb cannot be greater than ub, lb and ub must be within arrays allocated size
        if (lb > ub || lb > target.strValue.length() || ub > target.strValue.length())
        {
            throw new StringException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                    "String slice invalid, index(es) out of bounds.");
        }

        // Create new string
        StringBuilder newStringBuild = new StringBuilder(target.strValue);

        // replace the target string with the new value from lb to ub
        newStringBuild.replace(lb, ub, value.strValue);

        // return the ResultList
        return new ResultValue(newStringBuild.toString(), SubClassif.STRING);
    }

    // ==================== TYPE COERCIONS =====================
    // =============================================================
    /**
     * Casts a Numeric Value to an Integer and returns a Result Value
     * containing the correct type and string value.
     *
     * @param parser    Parser object
     * @param nOp1      Numeric Operand 1
     * @return ResultValue
     */
    public static ResultValue castNumericToInt(Parser parser, Numeric nOp1)
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
     * @param parser    Parser object
     * @param nOp1      Numeric Operand 1
     * @return ResultValue
     */
    public static ResultValue castNumericToDouble(Parser parser, Numeric nOp1)
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
     * @param parser    Parser object
     * @param nOp1      Numeric Operand 1
     * @return ResultValue
     */
    public static ResultValue unaryMinus(Parser parser, Numeric nOp1)
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
     * @param parser    Parser Object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue add(Parser parser, Numeric nOp1, Numeric nOp2)
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
     * @param parser    Parser object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue subtract(Parser parser, Numeric nOp1, Numeric nOp2)
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
                res.strValue = Integer.toString((nOp1.intValue - (int)nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue - (double)nOp2.intValue);
        }

        return res;
    }

    /**
     * Multiplies the Values of two Numerics and returns a ResultValue.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param parser    Parser object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue multiply(Parser parser, Numeric nOp1, Numeric nOp2)
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
                res.strValue = Integer.toString((nOp1.intValue * (int)nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue * (double)nOp2.intValue);
        }

        return res;
    }

    /**
     * Divides the Values of two Numerics and returns a ResultValue.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param parser    Parser object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue divide(Parser parser, Numeric nOp1, Numeric nOp2) throws PickleException
    {
        // check for divide by zero
        if ((nOp2.dataType == SubClassif.INTEGER && nOp2.intValue == 0) ||
            (nOp2.dataType == SubClassif.FLOAT && nOp2.doubleValue == 0))
            // cannot divide by zero
            throw new NumericConstantException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
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
                res.strValue = Integer.toString((nOp1.intValue / (int)nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(nOp1.doubleValue / (double)nOp2.intValue);
        }

        return res;
    }

    /**
     * Raises a Numeric to the Power of another Numeric and returns a ResultValue.
     * <p>
     *      The ResultValue will have the data type of the
     *      first operand.
     *
     * @param parser    Parser object
     * @param nOp1      Numeric Operand 1
     * @param nOp2      Numeric Operand 2
     * @return ResultValue
     */
    public static ResultValue power(Parser parser, Numeric nOp1, Numeric nOp2)
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
                res.strValue = Integer.toString( (int)Math.pow(nOp1.intValue, (int)nOp2.doubleValue));
            // first value is float, second is integer
            else
                res.strValue = Double.toString(Math.pow(nOp1.doubleValue, (double)nOp2.intValue));
        }

        return res;
    }

    // ====================== BOOL OPERATIONS ======================
    // =============================================================
    /**
     * Boolean AND test on two Bool Operands.
     * Returns a ResultValue containing the result of the test.
     *
     * @param parser    Parser object
     * @param bOp1      Bool Operand 1
     * @param bOp2      Bool Operand 2
     * @return ResultValue
     */
    public static ResultValue boolAnd(Parser parser, Bool bOp1, Bool bOp2)
    {
        ResultValue res =  new ResultValue("", bOp1.dataType);
        res.strValue = (bOp1.bValue && bOp2.bValue) ? "T" : "F";
        return res;
    }

    /**
     * Boolean OR test on two Bool Operands.
     * Returns a ResultValue containing the result of the test.
     *
     * @param parser    Parser object
     * @param bOp1      Bool Operand 1
     * @param bOp2      Bool Operand 2
     * @return ResultValue
     */
    public static ResultValue boolOr(Parser parser, Bool bOp1, Bool bOp2)
    {
        ResultValue res =  new ResultValue("", bOp1.dataType);
        res.strValue = (bOp1.bValue || bOp2.bValue) ? "T" : "F";
        return res;
    }

    /**
     * Boolean NOT test on two Bool Operands.
     * Returns a ResultValue containing the result of the test.
     *
     * @param parser    Parser object
     * @param bOp1      Bool Operand 1
     * @return ResultValue
     */
    public static ResultValue boolNot(Parser parser, Bool bOp1)
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
     * @param parser    Parser object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue equal(Parser parser, ResultValue resVal1, ResultValue resVal2) throws PickleException {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            /*if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '==' cannot be applied String and Numeric");*/
            // Do a lexicographical comparison
            // Set "T" if both strings are equal, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) == 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                              "Operator '==' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(parser, resVal1, "==", "test equal");
            Numeric nOp2 = new Numeric(parser, resVal2, "==", "test equal");

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
     * @param parser    Parser object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue notEqual(Parser parser, ResultValue resVal1, ResultValue resVal2) throws PickleException {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            /*if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '!=' cannot be applied String and Numeric");*/
            // Set "T" if both strings are equal, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) != 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                              "Operator '!=' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(parser, resVal1, "!=", "test not equal");
            Numeric nOp2 = new Numeric(parser, resVal2, "!=", "test not equal");


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
     * @param parser    Parser object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue lessThan(Parser parser, ResultValue resVal1, ResultValue resVal2) throws PickleException {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            /*if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '<' cannot be applied String and Numeric");*/
            // Set "T" if resVal1 is lexicographically less than resVal2, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) < 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                              "Operator '<' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(parser, resVal1, "<", "test less than");
            Numeric nOp2 = new Numeric(parser, resVal2, "<", "test less than");

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
     * @param parser    Parser object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */

    public static ResultValue greaterThan(Parser parser, ResultValue resVal1, ResultValue resVal2) throws PickleException {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            /*if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '>' cannot be applied String and Numeric");*/
            // Set "T" if resVal1 is lexicographically greater than resVal2, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) > 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                              "Operator '>' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(parser, resVal1, ">", "test greater than");
            Numeric nOp2 = new Numeric(parser, resVal2, ">", "test greater than");

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
     * @param parser    Parser object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue lessThanOrEqualTo(Parser parser, ResultValue resVal1, ResultValue resVal2) throws PickleException {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING) {
            // If the second operand is not a String, throw exception
            /*if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '<=' cannot be applied String and Numeric");*/
            // Set "T" if resVal1 is lexicographically less than or equals to resVal2, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) <= 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                              "Operator '<=' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(parser, resVal1, "<=", "test less than or equal");
            Numeric nOp2 = new Numeric(parser, resVal2, "<=", "test less than or equal");

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
     * @param parser    Parser object
     * @param resVal1   ResultValue Operand 1
     * @param resVal2   ResultValue Operand 2
     * @return ResultValue
     */
    public static ResultValue greaterThanOrEqualTo(Parser parser, ResultValue resVal1, ResultValue resVal2) throws PickleException {
        // ResultValue will be of type boolean
        ResultValue res =  new ResultValue("", SubClassif.BOOLEAN);

        // Test is based on data type of left operand
        // left operand is STRING
        if (resVal1.dataType == SubClassif.STRING)
        {
            // If the second operand is not a String, throw exception
            /*if (resVal2.dataType != SubClassif.STRING)
                throw new OperationException(scanner.currentToken, scanner.sourceFileNm,
                                              "Operator '>=' cannot be applied String and Numeric");*/
            // Set "T" if resVal1 is lexicographically greater than or equal to resVal2, else "F"
            res.strValue = (resVal1.strValue.compareTo(resVal2.strValue) >= 0) ? "T" : "F";
        }
        // left operand is INTEGER or FLOAT
        else if (resVal1.dataType == SubClassif.INTEGER || resVal1.dataType == SubClassif.FLOAT)
        {
            // If the second operand is not a Numeric, throw exception
            if (resVal2.dataType != SubClassif.INTEGER && resVal2.dataType != SubClassif.FLOAT)
                throw new OperationException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                              "Operator '>=' cannot be applied Numeric and String");

            // Convert both result values into Numerics
            // if they cannot be parsed a NumericConstantException will be thrown
            Numeric nOp1 = new Numeric(parser, resVal1, ">=", "test greater than or equal");
            Numeric nOp2 = new Numeric(parser, resVal2, ">=", "test greater than or equal");

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
