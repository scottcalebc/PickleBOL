package pickle;

/**
 * The ResultValue class represents the result of an interpreter function.
 *
 */
public class ResultValue implements Result {
    // TODO: finish this class
    //type, value, structure, terminating string
    public SubClassif dataType;
    public String strValue;
    public String terminatingString;

    public ResultValue(String strValue, SubClassif dataType)
    {
        this.strValue = strValue;
        this.dataType = dataType;
    }

    public ResultValue(String strValue, SubClassif dataType, String terminatingString) {
        this.strValue = strValue;
        this.dataType = dataType;
        this.terminatingString = terminatingString;
    }
}
