package pickle;

public class Parser {

    protected Scanner        scanner;
    protected SymbolTable    symbolTable;
    protected StorageManager storageManager;

    protected boolean       bShowExpr;
    protected boolean       bShowAssign;



    public Parser(Scanner scanner, SymbolTable symbolTable) {
        this.scanner = scanner;
        this.symbolTable = symbolTable;
        this.storageManager = new StorageManager();

        this.bShowExpr = false;
        this.bShowAssign = false;
    }


    public boolean hasNext() {
        return scanner.currentToken.primClassif != Classif.EOF;
    }

    public void getNext() throws PickleException {
        // get Next Statment, meaning get all tokens for a stmt
        scanner.getNext();
        
        switch (scanner.currentToken.primClassif) {
            case OPERAND:
                if (debugStmt()) {
                    break;
                }
                assignmentStmt();
                break;
            case FUNCTION:
                functionStmt();
                break;
            case CONTROL:
                controlStmt();
                break;
            case OPERATOR:
                throw new PickleException();
            default:
                // TODO: 3/25/2021 throw exceptin of cannot evaluate statement
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
                        //res = ifStmt();
                        break;
                    case "while":
                        //res = whileStmt();
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

        res = new ResultValue(varStr, SubClassif.EMPTY);

        if (scanner.getNext().equals("=")) {
            res = expr();
        }

        // Statement does not end in a semicolon
        if (! scanner.currentToken.tokenStr.equals(";")) {
            //todo: update pickle error
            throw new PickleException();
        }

        STEntry varEntry = symbolTable.getSymbol(varStr);

        // Already in symbol table declaring again
        if (varEntry.primClassif != Classif.EMPTY) {
            //todo
            // pop old symbol off the table and create new entry with putSymbol
        }


        symbolTable.putSymbol(varStr, new STIdentifier(
                varStr,
                Classif.OPERAND,
                getDataType(declareTypeStr),
                "primitive",
                "none",
                99
        ));

        if (res.dataType != SubClassif.EMPTY) {
            res = assign(varStr, res);
        }


        //System.out.printf("Declared new variable with symbol: %s Type: %s\n", varStr, declareTypeStr);

        return res;
    }

    private ResultValue assignmentStmt() throws PickleException {
        ResultValue res;

        if (scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {

            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Operand must be of type IDENTIFIER to assign values");
        }

        String varStr = scanner.currentToken.tokenStr;

        if (scanner.nextToken.primClassif != Classif.OPERATOR || !scanner.getNext().equals("=")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Second token in assignment statement must be \"=\" Operator");
        }


        scanner.getNext();
        res = expr();

        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Assignment statment must end in ';':");
        }

        res = assign(varStr, res);




        return res;
    }


    private ResultValue expr() throws PickleException {
        /*System.out.printf("Called expr with tokenStr: %s\n", scanner.currentToken.tokenStr);*/

        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        Numeric nOp1 = null;
        Numeric nOp2 = null;

        switch (scanner.currentToken.primClassif) {
            case OPERATOR:
                if (!scanner.currentToken.tokenStr.equals("-")) {
                    // TODO: 3/25/2021 throw pickle exception non-unary minus
                }
                if (scanner.nextToken.primClassif != Classif.OPERAND) {
                    // TODO: 3/25/2021 throw pickle exception unary minus on non operand
                }
                scanner.getNext();
                if (scanner.currentToken.subClassif == SubClassif.IDENTIFIER) {
                    // TODO: 3/25/2021 collect value from storage manager and assign to numeric for unary minus
                    STEntry symbolEntry = this.symbolTable.getSymbol(scanner.currentToken.tokenStr);

                    if (symbolEntry.primClassif == Classif.EMPTY) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol does not exist:");
                    }

                    if (symbolEntry.primClassif != Classif.OPERAND) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol is not operand type:");
                    }


                    res = this.storageManager.getVariable(symbolEntry.symbol);

                    if (res.dataType != SubClassif.INTEGER && res.dataType != SubClassif.FLOAT) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot perform unary minus on non-numeric operand identifier:");
                    }

                    nOp1 = new Numeric(scanner, res, "-", "first operand for unary minus");
                } else if (scanner.currentToken.subClassif == SubClassif.INTEGER) {
                    
                    nOp1 = new Numeric(scanner, new ResultValue(scanner.currentToken.tokenStr, SubClassif.INTEGER), "-", "first operand for unary minus");
                } else if (scanner.currentToken.subClassif == SubClassif.FLOAT) {
                    nOp1 = new Numeric(scanner, new ResultValue(scanner.currentToken.tokenStr, SubClassif.FLOAT), "-", "first operand for unary minus");
                } else {
                    // TODO: 3/25/2021 throw pickle exception for unary minus on non-numeric data
                }

                
                // TODO: 3/25/2021 use new utility function for unary operation
                res = Utility.unaryMinus(scanner, nOp1);

                break;
            case OPERAND:
                    switch (scanner.currentToken.subClassif){
                        case IDENTIFIER:
                            // get value of Identifier and check for more expressions following
                            // TODO: 3/26/2021 fix to pull value from SymbolTable and StorageManager
                            STEntry symbolEntry = this.symbolTable.getSymbol(scanner.currentToken.tokenStr);

                            if (symbolEntry.primClassif == Classif.EMPTY) {
                                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol does not exist:");
                            }

                            if (symbolEntry.primClassif != Classif.OPERAND) {
                                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol is not operand type:");
                            }


                            res = this.storageManager.getVariable(symbolEntry.symbol);
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



        if (scanner.nextToken.primClassif != Classif.SEPARATOR) {
            scanner.getNext();

            if (scanner.currentToken.primClassif != Classif.OPERATOR) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Token must be operator or ';'");
            }

            String operatorStr = scanner.currentToken.tokenStr;
            Token operatorToken = scanner.currentToken;
            scanner.getNext();
            ResultValue res2 = expr();

            if (res2.dataType != SubClassif.FLOAT && res2.dataType != SubClassif.INTEGER) {
                throw new PickleException();
            }

            /*System.out.printf("Result 1: %s\n", res.strValue);
            System.out.printf("Result 2: %s\n", res2.strValue);
            System.out.printf("Operator: %s\n", operatorStr);*/

            nOp1 = new Numeric(scanner, res, operatorStr, "first operand");
            nOp2 = new Numeric(scanner, res2, operatorStr, "second operand");

            switch (operatorStr) {
                case "+":
                    res = Utility.add(scanner, nOp1, nOp2);
                    break;
                case "-":
                    res = Utility.subtract(scanner, nOp1, nOp2);
                    break;
                case "*":
                    res = Utility.multiply(scanner, nOp1, nOp2);
                    break;
                case "/":
                    res = Utility.divide(scanner, nOp1, nOp2);
                    break;
                case "^":
                    res = Utility.power(scanner, nOp1, nOp2);
                    break;
                default:
                    throw new ScannerParserException(operatorToken, scanner.sourceFileNm, "Cannot perform operation with invalid OPERATOR:");

            }

        }


        return res;

    }

    private ResultValue functionStmt() throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        if (scanner.currentToken.subClassif == SubClassif.BUILTIN) {
            switch (scanner.currentToken.tokenStr) {
                case "print":
                    print();
                    break;
                default:
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "No BUILTIN function of name: ");
            }
        }


        return res;
    }


    private void print() throws PickleException{
        StringBuilder sb = new StringBuilder();

        if (!scanner.getNext().equals("(")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Function does not start with '(' token:");
        }

        while(!scanner.getNext().equals(")")) {
            if (scanner.currentToken.tokenStr.equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Reached ';' before closing function ')':");
            }
            if (scanner.currentToken.tokenStr.equals(",")) {
                continue;
            }

            ResultValue res = expr();

            sb.append(res.strValue);
            sb.append(" ");



        }

        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Did not reach ';' at end of function call:");
        }

        System.out.println(sb.toString());
    }


    private ResultValue assign(String varStr, ResultValue res) throws PickleException{
        if (res.dataType == SubClassif.EMPTY) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot assign empty value to identifier:");
        }

        STIdentifier symbolEntry = (STIdentifier) this.symbolTable.getSymbol(varStr);



        if (symbolEntry.dclType == SubClassif.FLOAT) {
            res = Utility.castNumericToDouble(scanner, new Numeric(scanner, res, "", "cast to declared type"));
        } if (symbolEntry.dclType == SubClassif.INTEGER) {
            res = Utility.castNumericToInt(scanner, new Numeric(scanner, res, "", "cast to declared type"));
        }


        this.storageManager.updateVariable(varStr, res);

        if (bShowAssign)
            System.out.printf("... Assign result into '%s' is '%s'\n", varStr, res.strValue);

        return res;
    }


    private boolean debugStmt() throws PickleException{
        if (scanner.currentToken.tokenStr.equals("debug")) {

            String type = scanner.getNext();

            String setting = scanner.nextToken.tokenStr;
            boolean onOff = false;

            switch (setting) {
                case "on":
                    onOff = true;
                    break;
                case "off":
                    onOff = false;
                    break;
                default:
                    throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Value following debug type must be 'on' or 'off': ");
            }


            switch (type) {
                case "Token":
                    scanner.bShowToken = onOff;
                    break;
                case "Expr":
                    bShowExpr = onOff;
                    break;
                case "Assign":
                    bShowAssign = onOff;
                    break;
                case "Stmt":
                    scanner.bShowStmt = onOff;
                    break;
                default:
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid debugType:");
            }

            scanner.getNext();

            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';':");
            }

            return true;
        }

        return false;
    }

    private ResultValue statements(boolean bExec) throws PickleException {
        ResultValue res = null;
        //exec statements
        if (bExec) {
            while (hasNext() && scanner.nextToken.subClassif != SubClassif.END) {
                getNext();
            }
        }
        else { // dont lmao
            while (hasNext() && scanner.nextToken.subClassif != SubClassif.END) {
                scanner.getNext();
            }
        }
        
        res.terminatingString = scanner.getNext();
        return res;
    }

    private void skipto(String token) throws PickleException {
        while (!scanner.getNext().equals(token));
    }

    private ResultValue whileStmt(Boolean bExec) throws PickleException {

        //set while position
        Token entryPosition = scanner.currentToken;
        ResultValue result;

        //execute while statement
        if (bExec) {
            //while contition is true execute stmts
             while (evalCond().strValue.equals("T")) {
                 result = statments(true);
                 if(!result.terminatingString.equals("endwhile")) {
                     throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile:");
                 }
                 //set position back to while
                 scanner.setPosition(entryPosition.iSourceLineNr, entryPosition.iColPos);
             }
             //after while loop's condition is false, get back to the endwhile token
             skipto(":");
             result = statments(false);
             if(!result.terminatingString.equals("endwhile")) {
                 throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile:");
             }
             if (!scanner.getNext().equals(";")) {
                 throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';':");
             }
        }
        else { //dont execute while
            skipto(":");
            result = statments(false);
            if(!result.terminatingString.equals("endwhile")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile:");
            }
            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';':");
            }
        }
        return result;
    }

    private ResultValue ifStmt(Boolean bExec) throws PickleException {
        
        if (bExec) {

            ResultValue resCond = evalCond();
            
            if (resCond.strValue.equals("T")) {
                ResultValue resTemp = statments(true);
                
                if (resTemp.terminatingString.equals("else")) {
                    if (!scanner.getNext().equals(":")) {
                        // TODO: 3/26/2021 throw error  
                    }
                    
                    resTemp = statements(false);
                    
                }
                if (!resTemp.terminatingString.equals("endif")) {
                    // TODO: 3/26/2021 throw error 
                }
                if (!scanner.getNext().equals(";")) {
                    // TODO: 3/26/2021 throw error 
                }


            } else {
                ResultValue resTemp = statements(false);

                if (resTemp.terminatingString.equals("else")) {
                    if (!scanner.getNext().equals(":")) {
                        // TODO: 3/26/2021 throw error
                    }

                    resTemp = statements(true);
                }

                if (!resTemp.terminatingString.equals("endif")) {
                    
                }
            }
        }


        return res;

    }












    public void run() throws PickleException {
        while (hasNext()) {
            getNext();
        }
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
