package pickle;

import java.util.ArrayList;
import java.util.Arrays;

public class STFunction extends STEntry
{
    public SubClassif returnType;           // Return data type
    public SubClassif definedBy;            // Who/what defined it (e.g. user or builtin)
    public String numArgs;                  // Number of arguments (VAR_ARGS for variable length)
    public ArrayList<String> parmList;      // Reference to an ArrayList of parameters
    public String symbolTable;              // Reference to the function's symbol table if it's a user def function

    public STFunction(String symbol, Classif primClassIf, SubClassif returnType, SubClassif definedBy, String numArgs)
    {
        super(symbol, primClassIf);
        this.returnType = returnType;
        this.definedBy = definedBy;
        this.numArgs = numArgs;
        this.symbolTable = null; //TODO: add symbol table reference
    }
}
