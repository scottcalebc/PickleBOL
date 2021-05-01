package pickle;

import java.util.ArrayList;

public class STFunction extends STEntry
{
    public SubClassif returnType;                    // Return data type
    public SubClassif definedBy;                     // Who/what defined it (e.g. user or builtin)
    public int numArgs;                              // Number of arguments (VAR_ARGS for variable length)
    public ArrayList<SubClassif> parameters;         // Reference to an ArrayList of the parameter types
    public ArrayList<String> names;                  // List of the names of all parameters
    public ArrayList<Boolean> array;                 // Determines if parameter is array or not
    public String symbol;                            // Name of function
    public Builtin function = null;                  // Reference to Builtin functions anonymous
    public int colPos = 0;                           // Col pos for user defined function
    public int lineNum = 0;                          // Line number for user defined function

    public ActivationRecord record = null;           // functions activation record

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
     */
    public STFunction(String symbol, Classif primClassIf, SubClassif returnType, SubClassif definedBy, int numArgs, int lineNum, int colPos, ArrayList<SubClassif> types, ArrayList<String> names, ArrayList<Boolean> array)
    {
        super(symbol, primClassIf);
        this.returnType = returnType;
        this.definedBy = definedBy;
        this.numArgs = numArgs;
        this.symbol = symbol;
        this.lineNum = lineNum;
        this.colPos = colPos;
        this.parameters = types;
        this.names = names;
        this.array = array;
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
        this.symbol = symbol;
        this.function = exe;
        this.parameters = types;
    }


    public void setupSymbolTable() {
        for (int i = 0; i < this.names.size(); i++) {
            String name = this.names.get(i);
            this.record.symbolTable.putSymbol(
                    name,
                    new STIdentifier(
                            name,
                            Classif.OPERAND,
                            this.parameters.get(i),
                            "primitive",
                            "ref",
                            0

                    )
            );
        }
    }

    public void setupActivationRecord(ActivationRecord parent) {
        this.record = new ActivationRecord(this.symbol);
        for (ActivationRecord rec : parent.environmentVector)
            this.record.environmentVector.add(rec);


    }

/*
    /**
     *
     * Returns an ActivationRecord object
     *
     * @return functions activations record (ActivationRecord)
     *
    public ActivationRecord getActivationRecord() {
        return this.record;
    }*/
}