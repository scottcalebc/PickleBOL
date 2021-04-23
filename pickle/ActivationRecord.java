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

    public int findSymbolScope(String symbol) {
        for (int i = 0; i < environmentVector.size(); i++) {
            STEntry res = environmentVector.get(i).symbolTable.getSymbol(symbol);
            if (res.primClassif != Classif.EMPTY && !res.symbol.isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
