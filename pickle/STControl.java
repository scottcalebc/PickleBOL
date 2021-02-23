package pickle;

public class STControl extends STEntry {
    public SubClassif subClassif = SubClassif.EMPTY;

    public STControl(String symbol, Classif primClassIf, SubClassif subClassif) {
        super(symbol, primClassIf);
        this.subClassif = subClassif;
    }
}
