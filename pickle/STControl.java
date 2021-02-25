package pickle;

public class STControl extends STEntry
{
    public SubClassif subClassif;       // Sub classification (e.g. flow, end, declare)

    /**
     * Returns a STControl object (Value in the symbol table) to more explicitly reference a control entry in the symbol table
     * <p>
     *
     * @param symbol        Name of symbol (Key in the symbol table)
     * @param primClassIf   Primary classification of the symbol
     * @param subClassif    Sub classification of the symbol
     */
    public STControl(String symbol, Classif primClassIf, SubClassif subClassif)
    {
        super(symbol, primClassIf);
        this.subClassif = subClassif;
    }
}
