package pickle;

import java.util.ArrayList;

public class ActivationRecord {
    public ArrayList<ActivationRecord> environmentVector;
    public StorageManager storageManager;
    public SymbolTable symbolTable;

    public ActivationRecord(String name) {
        symbolTable = new SymbolTable(name);
        storageManager = new StorageManager();
        environmentVector = new ArrayList<ActivationRecord>();
        environmentVector.add(this);
    }

    /**
     * findSymbolScope will place a copy of the symbol into the activation records symbol table with the activation record offset
     *
     * @param symbol        -   Symbol to search for
     * @return                  returns an integer describing the offset of the symbol in terms of scope, returns -1 if symbol not found
     */
    public int findSymbolScope(String symbol) {
        for (int i = 0; i < environmentVector.size(); i++) {
            STIdentifier res = (STIdentifier) environmentVector.get(i).symbolTable.getSymbol(symbol);
            if (res.primClassif != Classif.EMPTY && !res.symbol.isEmpty()) {
                this.symbolTable.putSymbol(res.symbol, new STIdentifier(res.symbol, res.primClassif, res.dclType, res.structure, res.parm, i));
                return i;
            }
        }
        return -1;
    }
}
