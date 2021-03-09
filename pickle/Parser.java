package pickle;

public class Parser {

    protected Scanner       scanner;
    protected SymbolTable   symbolTable;


    public Parser(Scanner scanner, SymbolTable symbolTable) {
        this.scanner = scanner;
        this.symbolTable = symbolTable;
    }
}
