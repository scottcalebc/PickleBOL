package pickle;

import java.util.ArrayList;
import java.util.HashMap;

public class STFunction extends STEntry
{
    public SubClassif returnType;                       // Return data type
    public SubClassif definedBy;                        // Who/what defined it (e.g. user or builtin)
    public int numArgs;                                 // Number of arguments (VAR_ARGS for variable length)
    public ArrayList<String> parmList;                  // Reference to an ArrayList of parameters TODO
    public String symbolTable;                          // Reference to the function's symbol table if it's a user def function
    protected HashMap<String, STEntry> functionST;      // Function's symbol table

    /**
     * Returns a STFunction object (Value in the symbol table) to more explicitly reference a function entry in the symbol table
     * <p>
     *
     * @param symbol        Name of symbol (Key in the symbol table)
     * @param primClassIf   Primary classification of the symbol
     * @param returnType    Return data type of function
     * @param definedBy     Who/what defined it (e.g. user or builtin)
     * @param numArgs       Number of arguments function needs
     *
     */
    public STFunction(String symbol, Classif primClassIf, SubClassif returnType, SubClassif definedBy, int numArgs)
    {
        super(symbol, primClassIf);
        this.returnType = returnType;
        this.definedBy = definedBy;
        this.numArgs = numArgs;
        this.symbolTable = definedBy == SubClassif.BUILTIN ? null : symbol;
        this.functionST = definedBy == SubClassif.BUILTIN ? null : new HashMap<>(); //TODO: depends on how Clark wants...
    }
}