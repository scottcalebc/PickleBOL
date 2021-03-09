package pickle;

import java.util.HashMap;

public class StorageManager {

    private HashMap<String, Object> variables;

    /**
     *
     * Returns a StorageManager object that holds the variable values for the pickle code
     * <p>
     *
     */
    public StorageManager() {
        this.variables = new HashMap<>();
    }

    /**
     *
     * Adds/Updates the value of a given variable in the table
     * <p>
     *
     * @param name      variable name to be added/updated
     * @param value     value of said variable
     */
    public void updateVariable(String name, Object value) {
        this.variables.put(name, value);
    }

    /**
     *
     * Retrieves a value given a variables name.
     * <p>
     *
     * @param name      name of the variable to get the value from
     * @return          Return a string of the variable's value, Classif.EMPTY if variable not found.
     */
    public Object getVariable(String name) {
        return this.variables.containsKey(name) ? this.variables.get(name).toString() : Classif.EMPTY;
    }

    /**
     *
     * Retrieves a value given a variable name. Adds a generic return type via a type parameter.
     * <p>
     *
     * @param name          name of the variable to get the value from
     * @param type          Return type of variable. E.g. Integer.class, Boolean.class, etc.
     * @param <T>           Generic type
     * @return              Return a string of the variable's value, Classif.EMPTY if variable not found.
     * @throws Exception
     */
    public <T> T getVariable(String name, Class<T> type) throws Exception {
        try {
            return this.variables.containsKey(name) ?  type.cast(this.variables.get(name)) : (T) Classif.EMPTY;
        }
        catch(java.lang.ClassCastException e) {
            throw new Exception("Error casting return type: Invalid type supplied for \"" + name + "\"!\n\t" +
                    name + "'s value: " + this.variables.get(name).toString()); //compile err so using regular Exception Class
        }
        catch(Exception e) {
            return (T) Classif.EMPTY;
        }
    }

}