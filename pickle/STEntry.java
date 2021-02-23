package pickle;

public class STEntry {
    public String symbol;
    public Classif primClassif = Classif.EMPTY;

    public STEntry(String symbol, Classif primClassif) {
        this.symbol = symbol;
        this.primClassif = primClassif;
    }

}
