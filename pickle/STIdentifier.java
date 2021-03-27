package pickle;

public class STIdentifier extends STEntry
{
    public SubClassif dclType;          // Declaration type of the symbol
    public String structure;        // Data structure type
    public String parm;             // Parameter type (e.g. by ref, by val)
    public int nonLocal;            // Base address reference (0 - local, 1 - surrounding, ... , k - surrounding, 99 - global)

    /**
     *
     * Returns a STIdentifier object (Value in the symbol table) to more explicitly reference an identifier entry in a symbol table
     * <p>
     *
     * @param symbol        Name of symbol (Key in the symbol table)
     * @param primClassIf   Primary classification of the symbol
     * @param dclType       Declaration type of symbol
     * @param structure     Data structure type of symbol
     * @param parm          Parameter type of symbol (e.g. by ref, by val)
     * @param nonLocal      Base address reference (0 - local, 1 - surrounding, ... , k - surrounding, 99 - global)
     *
     */
    public STIdentifier(String symbol, Classif primClassIf, SubClassif dclType, String structure, String parm, int nonLocal)
    {
        super(symbol, primClassIf);
        this.dclType = dclType;
        this.structure = structure;
        this.parm = parm;
        this.nonLocal = nonLocal;
    }
}
