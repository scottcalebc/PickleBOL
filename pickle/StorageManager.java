package pickle;

import java.util.HashMap;

public class StorageManager {

    private HashMap<String, Result> variables;     // HashMap for containing variables values (in Result form)

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
     * @param value     value of said variable (Result obj)
     */
    public void updateVariable(String name, Result value)
    {
        this.variables.put(name, value);
    }

    /**
     *
     * Retrieves a value given a variables name.
     * <p>
     *
     * @param name      name of the variable to get the value from
     * @return          Returns a Result obj, Result of data type Classif.EMPTY and string value "", if variable not found.
     */
    public Result getVariable(String name)
    {
        return this.variables.containsKey(name) ? this.variables.get(name) : new ResultValue("", SubClassif.EMPTY);
    }

}