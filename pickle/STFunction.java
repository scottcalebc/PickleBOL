package pickle;

import java.util.ArrayList;

public class STFunction extends STEntry
{
    public SubClassif returnType;                    // Return data type
    public SubClassif definedBy;                     // Who/what defined it (e.g. user or builtin)
    public int numArgs;                              // Number of arguments (VAR_ARGS for variable length)
    public ArrayList<SubClassif> parameters;         // Reference to an ArrayList of the parameter types
    public String symbolTable;                       // Reference to the function's symbol table if it's a user def function
    public Builtin function = null;                  // Reference to Builtin functions anonymous
    public int colPos = 0;                           // Col pos for user defined function
    public int lineNum = 0;                          // Line number for user defined function
    public ActivationRecord record;                  // functions activation record

    /**
     * Returns a STFunction object (Value in the symbol table) to more explicitly reference a function entry in the symbol table
     * <p>
     *
     * @param symbol        Name of symbol (Key in the symbol table)
     * @param primClassIf   Primary classification of the symbol
     * @param returnType    Return data type of function
     * @param definedBy     Who/what defined it (e.g. user or builtin)
     * @param numArgs       Number of arguments function needs, if -1 then VAR_ARGS
     * @param lineNum       Line number of defined function
     * @param colPos        Column position of defined function
     * @param types         List of parameter types
     * @param parent        Activation record of parent scope, set to null if being defined in global scope
     */
    public STFunction(String symbol, Classif primClassIf, SubClassif returnType, SubClassif definedBy, int numArgs, int lineNum, int colPos, ArrayList<SubClassif> types, ActivationRecord parent)
    {
        super(symbol, primClassIf);
        this.returnType = returnType;
        this.definedBy = definedBy;
        this.numArgs = numArgs;
        this.symbolTable = symbol;
        this.lineNum = lineNum;
        this.colPos = colPos;
        this.parameters = types;
        this.record = new ActivationRecord(symbol);
        if (parent != null) {
            for (ActivationRecord rec : parent.environmentVector) {
                this.record.environmentVector.add(rec);
            }
        }
    }

    /**
     * Returns a STFunction object (Value in the symbol table) to more explicitly reference a function entry in the symbol table
     * <p>
     *
     * @param symbol        Name of symbol (Key in the symbol table)
     * @param primClassIf   Primary classification of the symbol
     * @param returnType    Return data type of function
     * @param definedBy     Who/what defined it (e.g. user or builtin)
     * @param numArgs       Number of arguments function needs
     * @param exe           BuiltIn interface acting as a function pointer to a builtin Utility function
     *
     */
    public STFunction(String symbol, Classif primClassIf, SubClassif returnType, SubClassif definedBy, int numArgs,  ArrayList<SubClassif> types, Builtin exe)
    {
        super(symbol, primClassIf);
        this.returnType = returnType;
        this.definedBy = definedBy;
        this.numArgs = numArgs;
        this.symbolTable = symbol;
        this.function = exe;
        this.parameters = types;
    }

    /**
     *
     * Returns an ActivationRecord object
     *
     * @return functions activations record (ActivationRecord) or null if builtin
     */
    public ActivationRecord getActivationRecord() {
        if (this.function == null) {
            return null;
        }
        return this.record;
    }
}