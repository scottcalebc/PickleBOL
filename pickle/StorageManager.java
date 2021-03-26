package pickle;

import java.util.HashMap;

public class StorageManager {

    private HashMap<String, ResultValue> variables;     // HashMap for containing variables values (in ResultValue form)

    /**
     *
     * Returns a StorageManager object that holds the variable values for the pickle code
     * <p>
     *
     */
    public StorageManager()
    {
        this.variables = new HashMap<>();
    }

    /**
     *
     * Adds/Updates the value of a given variable in the table
     * <p>
     *
     * @param name      variable name to be added/updated
     * @param value     value of said variable (ResultValue)
     */
    public void updateVariable(String name, ResultValue value)
    {
        this.variables.put(name, value);
    }

    /**
     *
     * Retrieves a value given a variables name.
     * <p>
     *
     * @param name      name of the variable to get the value from
     * @return          Returns a ResultValue object, ResultValue of data type Classif.EMPTY and string value "", if variable not found.
     */
    public ResultValue getVariable(String name)
    {
        return this.variables.containsKey(name) ? this.variables.get(name) : new ResultValue("", SubClassif.EMPTY);
    }

}