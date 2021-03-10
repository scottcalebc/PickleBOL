package pickle;

public class Parser {

    protected Scanner       scanner;
    protected SymbolTable   symbolTable;


    public Parser(Scanner scanner, SymbolTable symbolTable) {
        this.scanner = scanner;
        this.symbolTable = symbolTable;
    }


    public boolean hasNext() {
        return scanner.currentToken.primClassif != Classif.EOF;
    }

    public void getNext() throws PickleException {
        // get Next Statment, meaning get all tokens for a stmt
        scanner.getNext();

        //scanner.currentToken.printToken();

        switch (scanner.currentToken.primClassif) {
            case OPERAND:
                assignmentStmt();
                break;
            case FUNCTION:
                //functionStmt();
                break;
            case CONTROL:
                controlStmt();
                break;
            case OPERATOR:
                throw new PickleException();
        }


    }


    private ResultValue controlStmt() throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        switch (scanner.currentToken.subClassif) {
            case DECLARE:
                res = declareStmt();
            case FLOW:
                String flowStr = scanner.currentToken.tokenStr;

                switch (flowStr) {
                    case "if":
                        //ifStmt();
                        break;
                    case "while":
                        //whileStmt();
                        break;

                }
        }

        return res;
    }

    private ResultValue declareStmt() throws PickleException {
        ResultValue res;

        String declareTypeStr = scanner.currentToken.tokenStr;
        scanner.getNext();

        // Must Declare using an identifier
        if (scanner.currentToken.primClassif != Classif.OPERAND
         && scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {
            throw new PickleException();
        }

        String varStr = scanner.currentToken.tokenStr;

        // Statement does not end in a semicolon
        if (! scanner.getNext().equals(";") ) {
            throw new PickleException();
        }

        STEntry varEntry = symbolTable.getSymbol(varStr);

        // Already in symbol table declaring again
        if (varEntry.primClassif != Classif.EMPTY) {
            throw new PickleException();
        }

        symbolTable.putSymbol(varStr, new STIdentifier(
                varStr,
                Classif.OPERAND,
                declareTypeStr,
                "primitive",
                "none",
                99
        ));


        res = new ResultValue(varStr, SubClassif.EMPTY);

        System.out.printf("Declared new variable with symbol: %s Type: %s\n", varStr, declareTypeStr);

        return res;
    }

    private ResultValue assignmentStmt() throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        if (scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {
            throw new PickleException();
        }

        String varStr = scanner.currentToken.tokenStr;
        scanner.getNext();

        if (scanner.currentToken.primClassif != Classif.OPERATOR) {
            throw new PickleException();
        }

        String opStr = scanner.currentToken.tokenStr;

        ResultValue res01;
        ResultValue res02;
        Numeric nOp1;
        Numeric nOp2;

        switch (opStr) {
            case "=" :
                res02 = expr();
                //res = assign(varStr, res02);
                System.out.printf("Assigning value '%s' for operand '%s'\n", res02.strValue, varStr);
                break;

        }





        return res;
    }


    private ResultValue expr() throws PickleException {
        scanner.getNext();

        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        Numeric nOp1;
        Numeric nOp2;

        switch (scanner.currentToken.primClassif) {
            case SEPARATOR:

                break;
            case OPERAND:
                    switch (scanner.currentToken.subClassif){
                        case IDENTIFIER:
                            // get value of Identifier and check for more expressions following
                            break;
                        case FLOAT:
                            res = new ResultValue(scanner.currentToken.tokenStr, SubClassif.FLOAT);
                            break;
                        case INTEGER:
                            // get value of number and check for more values later
                            res = new ResultValue(scanner.currentToken.tokenStr, SubClassif.INTEGER);
                            break;
                        case STRING:
                            return new ResultValue(scanner.currentToken.tokenStr, SubClassif.STRING);
                        case BOOLEAN:
                            return new ResultValue(scanner.currentToken.tokenStr, SubClassif.BOOLEAN);
                    }

        }

        if (scanner.getNext() != ";") {

            if (scanner.currentToken.primClassif != Classif.OPERATOR) {
                throw new PickleException();
            }

            String operatorStr = scanner.currentToken.tokenStr;
            ResultValue res2 = expr();

            if (res2.dataType != SubClassif.FLOAT && res2.dataType != SubClassif.INTEGER) {
                throw new PickleException();
            }

            nOp1 = new Numeric(scanner, res, operatorStr, "first operand");
            nOp2 = new Numeric(scanner, res, operatorStr, "second operand");

            switch (operatorStr) {
                case "+":
                    return Utility.add(scanner, nOp1, nOp2);
                case "-":
                    return Utility.subtract(scanner, nOp1, nOp2);
            }

        }


        return res;

    }



    public SubClassif getDataType(String declareString) throws PickleException {
        switch (declareString) {
            case "Float":
                return SubClassif.FLOAT;
            case "Int":
                return SubClassif.INTEGER;
            case "Bool":
                return SubClassif.BOOLEAN;
            case "String":
                return SubClassif.STRING;
            case "Date":
                return SubClassif.DATE;
            default:
                throw new PickleException();
        }
    }
}
