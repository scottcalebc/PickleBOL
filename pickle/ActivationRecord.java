package pickle;

import java.util.ArrayList;

public class ActivationRecord implements Cloneable {
    public ArrayList<ActivationRecord> environmentVector;
    public StorageManager storageManager;
    public SymbolTable symbolTable;
    public Result returnVal = null;

    /**
     * returns and Activation record for the newly defined function
     *  Note: this will also create the functions:
     *      1. Symbol Table
     *      2. Storage Manager
     *      3. Environment Vector
     *
     * @param name      -   name of defined function
     */
    public ActivationRecord(String name) {
        this.environmentVector = new ArrayList<ActivationRecord>();
        this.environmentVector.add(this);
        this.storageManager = new StorageManager();
        this.symbolTable = new SymbolTable(name);
    }

    public ActivationRecord(ActivationRecord record) {
        this.environmentVector = new ArrayList<ActivationRecord>();
        this.environmentVector.add(this);
        this.storageManager = new StorageManager();
        this.symbolTable = record.symbolTable.clone();
    }


    /**
     * findSymbolScope will look for a copy of the symbol in the activation records symbol table with the activation record offset
     *
     * @param symbol        -   Symbol to search for
     * @return                  returns an integer describing the offset of the symbol in terms of scope, returns -1 if symbol not found
     */
    public int findSymbolScope(String symbol) {
        for (int i = 0; i < environmentVector.size(); i++) {
            STEntry res =  environmentVector.get(i).symbolTable.getSymbol(symbol);
            if (res.primClassif != Classif.EMPTY && !res.symbol.isEmpty()) {
                /*f (res instanceof STIdentifier) {
                    STIdentifier id = (STIdentifier)res;
                    //this.symbolTable.putSymbol(res.symbol, new STIdentifier(id.symbol, id.primClassif, id.dclType, id.structure, id.parm, i));
                    return i;
                } if (res instanceof STFunction) {
                    return i;
                }*/
                return i;
            }
        }
        return -1;
    }

}
