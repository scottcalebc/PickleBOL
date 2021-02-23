package pickle;

public class STIdentifier extends STEntry{
    public String dclType;
    public String structure;
    public String parm;
    public int nonLocal;

    public STIdentifier(String symbol, Classif primClassIf, String dclType, String structure, String parm, int nonLocal) {
        super(symbol, primClassIf);
        this.dclType = dclType;
        this.structure = structure;
        this.parm = parm;
        this.nonLocal = nonLocal;
    }
}
