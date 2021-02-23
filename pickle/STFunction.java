package pickle;

import java.util.ArrayList;
import java.util.Arrays;

public class STFunction extends STEntry{
    public SubClassif returnType;
    public SubClassif definedBy;
    public int numArgs;
    public ArrayList<String> parmList;
    public String symbolTable;

    public STFunction(String symbol, Classif primClassIf, SubClassif returnType, SubClassif definedBy, String... args) {
        super(symbol, primClassIf);
        this.returnType = returnType;
        this.definedBy = definedBy;
        this.parmList = (ArrayList<String>)Arrays.asList(args);
        this.numArgs = args.length;
        this.symbolTable = null; //TODO
    }
}
