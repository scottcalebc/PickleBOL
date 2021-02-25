package pickle;

/**
 * The ResultValue class represents the result of an interpreter function.
 *
 */
public class ResultValue {
    // TODO: finish this class
    //type, value, structure, terminating string
    public SubClassif dataType;
    public String strValue;

    public ResultValue(String strValue, SubClassif dataType)
    {
        this.strValue = strValue;
        this.dataType = dataType;
    }
}
