package pickle;

public class STControl extends STEntry
{
    public SubClassif subClassif;       // Sub classification (e.g. flow, end, declare)

    public STControl(String symbol, Classif primClassIf, SubClassif subClassif)
    {
        super(symbol, primClassIf);
        this.subClassif = subClassif;
    }
}
