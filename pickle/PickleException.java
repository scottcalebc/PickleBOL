package pickle;

public class PickleException extends Exception{

    protected String  errMessageStr;    // Error message string inherited by all children

    /**
     *
     * Generic Exception that all Pickle exceptions derive from
     * <p>
     *
     */
    public PickleException() {
        this.errMessageStr = "Uh Oh: Generic Pickle Error";
    }


    /**
     *
     * Overide of Exception's toString to return instance variable errMessageStr
     * <p>
     *
     * @return Error Message String
     *
     */
    @Override
    public String toString()
    {
        return  errMessageStr;
    }
}
