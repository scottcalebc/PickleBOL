package pickle;

public class STEntry
{
    public String symbol;               // Name string for the symbol
    public Classif primClassif;         // Primary classification of the symbol

    public STEntry(String symbol, Classif primClassif)
    {
        this.symbol = symbol;
        this.primClassif = primClassif;
    }

}
