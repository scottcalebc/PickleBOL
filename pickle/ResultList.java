package pickle;

import java.util.ArrayList;

/**
 * This class Represents Arrays as lists of ResultValues.
 *
 * TODO: Array to Array assignment
 *       - Larger assigned to smaller copies enough to fill smaller
 *       - Smaller assigned to larger copies only used elements.
 *       Array Scalar assignment (array = 0)
 */
public class ResultList
{
    public SubClassif dataType;         // DataType of Array
    ArrayList<ResultValue> arrayList;   // List of ResultValues
    int capacity;                       // capacity of Array
    int allocatedSize;                  // currently used size of Array

    /**
     * Constructs an Array of ResultValues as a ResultList object.
     *
     * <p> Will verify that all ResultValues are Homogoneous.
     *
     * @param parser    Parser object
     * @param arrayList ArrayList of ResultValues to store as ResultList
     * @param size      Capacity of Array
     * @param dataType  DataType of Array
     * @throws ResultListException if provided ResultValue list is not homogoneous.
     */
    public ResultList(Parser parser, ArrayList<ResultValue> arrayList, int size, SubClassif dataType) throws ResultListException {
        // Verify that all items in the given list share the same data type
        checkHomogeneous(parser, arrayList, dataType);
        // Store the provided items into this ResultList object.
        this.capacity = size;
        this.allocatedSize = arrayList.size();
        this.dataType = dataType;
        this.arrayList = arrayList;
    }

    /**
     * Retrieves the ResultValue at the given index in the ResultList.
     *
     * <p> Supports negative indexing (e.g. index of -1 is last item in list)
     *
     * @param parser Parser object
     * @param index  index of ResultValue to retrieve
     * @return ResultValue at given index
     * @throws ResultListException if index is out of bounds
     */
    public ResultValue getItem(Parser parser, int index) throws ResultListException
    {
        int normalizedIndex;
        // if the index is negative translate it to the corresponding positive index
        if (index < 0) normalizedIndex = normalizeIndex(parser, index);
        else normalizedIndex = index;
        // throw error if index is out of bounds.
        if (normalizedIndex > this.capacity)
        {
            throw new ResultListException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                     "Array index Out of Bounds.");
        }
        return arrayList.get(normalizedIndex);
    }

    /**
     * Translates a provided index (assumed to be negative)
     * to the corresponding positive index for the arrayList.
     *
     * @param index the index to normalize (assumed negative)
     * @return normalized index for ResultList item
     * @throws ResultListException if the provided index is out of bounds.
     */
    private int normalizeIndex(Parser parser, int index) throws ResultListException
    {
        // if the index provided is non-negative then it is already normalized
        if (index >= 0) return index;

        // e.g. Capacity is 10, index is -1, ret = 9
        int ret = this.capacity + index;
        // if the result is negative still, index was invalid
        if (ret < 0)
        {
            throw new ResultListException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                    "Array index Out of Bounds.");
        }
        return ret;
    }

    /**
     * Ensures that a provided ResultValue array consists of all the same data types.
     * (guarantee a Homogeneous array)
     *
     * @param parser    Parser object
     * @param arrayList List of ResultValues to verify data type
     * @param dataType  Data Type that all ResultValues should be
     * @throws ResultListException if the provided index is out of bounds.
     */
    private void checkHomogeneous(Parser parser, ArrayList<ResultValue> arrayList, SubClassif dataType) throws ResultListException {
        // Verify that each item in the list is the same Data Type
        for (int i = 0; i < arrayList.size(); i++)
        {
            if (dataType != arrayList.get(i).dataType)
            {
                throw new ResultListException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                        "Array item is of incompatible type.");
            }
        }
    }
}
