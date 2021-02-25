package pickle;

public class STEntry
{
    public String symbol;               // Name string for the symbol
    public Classif primClassif;         // Primary classification of the symbol

    /**
     *
     * Returns a STEntry object (Value in the symbol table) to reference an entry in a symbol table
     * <p>
     *
     * @param symbol        Name of symbol (Key in the symbol table)
     * @param primClassif   Primary classification of the symbol
     *
     */
    public STEntry(String symbol, Classif primClassif)
    {
        this.symbol = symbol;
        this.primClassif = primClassif;
    }

}
