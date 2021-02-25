package pickle;

public class STIdentifier extends STEntry
{
    public String dclType;          // Declaration type of the symbol
    public String structure;        // Data structure type
    public String parm;             // Parameter type (e.g. by ref, by val)
    public int nonLocal;            // Base address reference (0 - local, 1 - surrounding, ... , k - surrounding, 99 - global)

    public STIdentifier(String symbol, Classif primClassIf, String dclType, String structure, String parm, int nonLocal)
    {
        super(symbol, primClassIf);
        this.dclType = dclType;
        this.structure = structure;
        this.parm = parm;
        this.nonLocal = nonLocal;
    }
}
