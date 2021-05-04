package pickle;

import java.util.ArrayList;
/**
 * This class Represents Arrays as lists of ResultValues.
 *
 * <p> Provides the following methods:
 *      getItem - retrieve a ResultValue from the ResultList
 *      setItem - set a ResultValue to the ResultList
 */
public class ResultList implements Result
{
    public SubClassif dataType;         // DataType of Array
    ArrayList<ResultValue> arrayList;   // List of ResultValues
    int capacity;                       // capacity of Array
    int allocatedSize;                  // currently used size of Array
    boolean unbounded;                  // flag for unbounded Array, T = unbounded

    /**
     * Constructs an Array of ResultValues as a ResultList object.
     *
     * <p> Will verify that all ResultValues are Homogeneous.
     *
     * <p> If size is -1 array is treated as unbounded.
     *     Unbounded arrays capacity and allocated size will always match.
     *
     * @param parser    Parser object
     * @param arrayList ArrayList of ResultValues to store as ResultList
     * @param size      Capacity of Array
     * @param dataType  DataType of Array
     * @throws ResultListException if provided ResultValue list is not homogeneous.
     */
    public ResultList(Parser parser, ArrayList<ResultValue> arrayList, int size, SubClassif dataType) throws ResultListException
    {
        // Verify that all items in the given list share the same data type
        checkHomogeneous(parser, arrayList, dataType);
        // Store the provided items into this ResultList object.
        unbounded = size == -1;
        this.capacity = unbounded ? arrayList.size() : size;
        this.allocatedSize = arrayList.size();
        this.dataType = dataType;
        this.arrayList = arrayList;
        // assign the rest of the ArrayList to be Empty ResultValues
        fillEmptyValues(parser);
    }

    @Override
    public String toString() {
        String arrayListString = "";
        for(int i = 0; i < capacity; i++) {
            arrayListString += "\n\t\t" + arrayList.get(i).toString();
        }

        return "ResultList{" +
                "\n\tdataType=" + dataType +
                ", \n\tarrayList=" + arrayListString +
                ", \n\tcapacity=" + capacity +
                ", \n\tallocatedSize=" + allocatedSize +
                "\n}";
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
        if (normalizedIndex > this.capacity-1)
        {
            parser.scanner.currentToken.tokenStr = String.valueOf(index);
            throw new ResultListException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                                     "Array index Out of Bounds");
        }
        return arrayList.get(normalizedIndex);
    }

    /**
     * Assigns the ResultValue at the given index in the ResultList.
     *
     * <p> Supports negative indexing (e.g. index of -1 is last item in list)
     *
     * @param parser Parser object
     * @param index  index of ResultValue to retrieve
     * @param value  ResultValue to assign to index
     * @return ResultList that is new List
     * @throws ResultListException if index is out of bounds
     */
    public ResultList setItem(Parser parser, int index, ResultValue value) throws ResultListException
    {
        int normalizedIndex;
        // if the index is negative translate it to the corresponding positive index
        if (index < 0) normalizedIndex = normalizeIndex(parser, index);
        else normalizedIndex = index;
        // throw error if index is out of bounds.
        if (normalizedIndex > capacity-1 && !unbounded)
        {
            throw new ResultListException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                    "Array index Out of Bounds.");
        }
        // Assign the ResultValue to the given index
        // unbounded arrays can have values set beyond the allocated size, increasing the capacity as needed.
        if (normalizedIndex > capacity-1 && unbounded)
        {
            // update capacity of array, is new index + 1
            capacity = normalizedIndex + 1;
            // add new empty values to fill any newly created empty spaces
            fillEmptyValues(parser);
            // set the value at the index given
        }
        arrayList.set(normalizedIndex, value);

        // Verify the ResultList
        checkHomogeneous(parser, arrayList, dataType);
        // Update the ResultLists allocated size
        updateAllocatedSize(parser);
        // Return this ResultList with the newly assigned Value
        return this;
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
     * All items in an Array must share the same data type or be EMPTY.
     *
     * @param parser    Parser object
     * @param arrayList List of ResultValues to verify data type
     * @param dataType  Data Type that all ResultValues should be
     * @throws ResultListException if the provided index is out of bounds.
     */
    private void checkHomogeneous(Parser parser, ArrayList<ResultValue> arrayList, SubClassif dataType) throws ResultListException
    {
        // Verify that each item in the list is the same Data Type
        for (int i = 0; i < arrayList.size(); i++)
        {
            // if arrayList[i] datatype is not EMPTY or the same type as the Array
            if (dataType != arrayList.get(i).dataType && arrayList.get(i).dataType != SubClassif.EMPTY)
            {
                throw new ResultListException(parser.scanner.currentToken, parser.scanner.sourceFileNm,
                        "Array item is of incompatible type.");
            }
        }
    }

    /**
     * Fill a ResultList's array with emptyValues.
     * This ensures that any non-allocated indexes are Empty.
     *
     * @param parser Parser object
     * @throws ResultListException on failure to set item in array.
     */
    private void fillEmptyValues(Parser parser) throws ResultListException
    {
        // do nothing if array is full, unless it is unbounded
        if (allocatedSize == capacity && !unbounded) return;
        ResultValue emptyValue = new ResultValue("", SubClassif.EMPTY);
        while(arrayList.size() != capacity)
        {
            this.arrayList.add(emptyValue);
        }
    }

    /**
     * Updates this ResultLists allocated size by finding the subscript
     * of the last non-empty element.
     *
     * @param parser
     */
    private void updateAllocatedSize(Parser parser)
    {
        int i;
        for (i = arrayList.size()-1; i>=0; i--)
        {
            if (arrayList.get(i).dataType != SubClassif.EMPTY) break;
        }
        allocatedSize = i+1;
    }

    public String printResult() {
        StringBuilder sb = new StringBuilder();
        for (ResultValue res : arrayList) {
            if (res.dataType != SubClassif.EMPTY) {
                if (res == arrayList.get(arrayList.size() - 1))
                    sb.append(res.printResult());
                else
                    sb.append(res.printResult() + " ");
            }
        }
        return sb.toString();
    }
}
