package pickle;

public class Parser {

    protected Scanner        scanner;               // scanner pointer
    protected SymbolTable    symbolTable;           // symbol table pointer
    protected StorageManager storageManager;        // storage manager

    protected boolean       bShowExpr;              // boolean flag for debug expressions
    protected boolean       bShowAssign;            // boolean flag for debug assignments


    /**
     * Parser constructor takes scanner and symbol table
     * <p>
     *
     * </p>
     * @param scanner           scanner object
     * @param symbolTable       sybmol table object
     */
    public Parser(Scanner scanner, SymbolTable symbolTable) {
        this.scanner = scanner;
        this.symbolTable = symbolTable;
        this.storageManager = new StorageManager();

        this.bShowExpr = false;
        this.bShowAssign = false;
    }

    /**
     * Boolean check for EOF
     * <p></p>
     *
     * @return      true if tokens need to be evaluated; false if end of file
     */
    public boolean hasNext() {
        return scanner.currentToken.primClassif != Classif.EOF;
    }

    /**
     * Executes next statement
     * <p>
     *
     * @throws PickleException      exception explaining issues
     */
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
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot evaluate starting on operator:");
            default:
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Unknown token to evaluate:");
        }


    }

    /**
     * Executes control statements
     * <p></p>
     *
     * @return                  generic ResultValue
     * @throws PickleException
     */
    private ResultValue controlStmt() throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        switch (scanner.currentToken.subClassif) {
            case DECLARE:
                res = declareStmt();
            case FLOW:
                String flowStr = scanner.currentToken.tokenStr;

                switch (flowStr) {
                    case "if":
                        res = ifStmt(true);
                        break;
                    case "while":
                        res = whileStmt(true);
                        break;

                }
        }

        return res;
    }

    /**
     * Executes Declare statments
     * <p></p>
     *
     * @return
     * @throws PickleException
     */
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

        // if assignment occuring grab expression into result value
        if (scanner.getNext().equals("=")) {
            res = expr();
        }

        // Statement does not end in a semicolon
        if (! scanner.currentToken.tokenStr.equals(";")) {
            //todo: update pickle error
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expecting ';' at end of assignment:");
        }

        STEntry varEntry = symbolTable.getSymbol(varStr);



        // put symbol into table
        symbolTable.putSymbol(varStr, new STIdentifier(
                varStr,
                Classif.OPERAND,
                getDataType(declareTypeStr),
                "primitive",
                "none",
                99
        ));

        // if assignment being performed during declearation ensure to assign value before exiting
        if (res.dataType != SubClassif.EMPTY) {
            res = assign(varStr, res);
        }


        return res;
    }

    /**
     * Executes assignment statements
     * <p>
     *
     * </p>
     * @return
     * @throws PickleException
     */
    private ResultValue assignmentStmt() throws PickleException {
        ResultValue res;

        if (scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {

            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Operand must be of type IDENTIFIER to assign values");
        }

        String varStr = scanner.currentToken.tokenStr;


        if (scanner.nextToken.primClassif != Classif.OPERATOR || !scanner.getNext().equals("=")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Second token in assignment statement must be \"=\" Operator");
        }


        scanner.getNext();      //get next token
        res = expr();           //get expression value

        // ensure assignment ends in ';'
        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Assignment statment must end in ';':");
        }

        res = assign(varStr, res);  //save value to symbol




        return res;
    }


    /**
     * Parsers and executes expressions
     * <p>
     *
     * </p>
     * @return
     * @throws PickleException
     */
    private ResultValue expr() throws PickleException {
        /*System.out.printf("Called expr with tokenStr: %s\n", scanner.currentToken.tokenStr);*/

        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        Numeric nOp1 = null;
        Numeric nOp2 = null;

        switch (scanner.currentToken.primClassif) {
            case OPERATOR:
                if (!scanner.currentToken.tokenStr.equals("-")) {
                    break;
                }
                if (scanner.nextToken.primClassif != Classif.OPERAND) {
                    throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Token must be of type Operand for unary minus");
                }
                scanner.getNext();
                if (scanner.currentToken.subClassif == SubClassif.IDENTIFIER) {
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
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot convert non-numeric data to numeric:");
                }


                res = Utility.unaryMinus(this, nOp1);

                break;
            case OPERAND:
                    switch (scanner.currentToken.subClassif){
                        case IDENTIFIER:
                            // get value of Identifier and check for more expressions following
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


        //if next token is seperator then just return current result value
        if (scanner.nextToken.primClassif != Classif.SEPARATOR) {
            scanner.getNext();

            if (scanner.currentToken.primClassif != Classif.OPERATOR) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Token must be operator or ';'");
            }

            // if operator is comparator just return to evalCond
            switch (scanner.currentToken.tokenStr) {
                case ">":
                case "<":
                case ">=":
                case "<=":
                case "==":
                case "!=":
                case "and":
                case "or":
                case "not":
                    return res;
            }

            String operatorStr = scanner.currentToken.tokenStr;
            Token operatorToken = scanner.currentToken;
            scanner.getNext();
            ResultValue res2 = expr();

            if (res2.dataType != SubClassif.FLOAT && res2.dataType != SubClassif.INTEGER) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot perform numeric operation on non-numeric data type:");
            }


            nOp1 = new Numeric(scanner, res, operatorStr, "first operand");
            nOp2 = new Numeric(scanner, res2, operatorStr, "second operand");

            // perform specific numeric operation
            switch (operatorStr) {
                case "+":
                    res = Utility.add(this, nOp1, nOp2);
                    break;
                case "-":
                    res = Utility.subtract(this, nOp1, nOp2);
                    break;
                case "*":
                    res = Utility.multiply(this, nOp1, nOp2);
                    break;
                case "/":
                    res = Utility.divide(this, nOp1, nOp2);
                    break;
                case "^":
                    res = Utility.power(this, nOp1, nOp2);
                    break;
                default:
                    throw new ScannerParserException(operatorToken, scanner.sourceFileNm, "Cannot perform operation with invalid OPERATOR:");

            }

            if (bShowExpr)
                System.out.printf("...%s %s %s is %s\n", nOp1.strValue, operatorStr, nOp2.strValue, res.strValue);


        }

        // does not check for end of statement as this is only evaluating expressions
        return res;

    }

    /**
     * Executes function statements, mainly builtin
     * <p></p>
     *
     * @return
     * @throws PickleException
     */
    private ResultValue functionStmt() throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        // check for builtin function if not throw error for identifier
        if (scanner.currentToken.subClassif == SubClassif.BUILTIN) {
            switch (scanner.currentToken.tokenStr) {
                case "print":
                    print();
                    break;
                default:
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "No function of name: ");
            }
        }


        return res;
    }


    /**
     * BUILTIN print function
     * <p>
     *
     * </p>
     * @throws PickleException
     */
    private void print() throws PickleException{
        StringBuilder sb = new StringBuilder();

        // if function does not start with '(' throw error
        if (!scanner.getNext().equals("(")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Function does not start with '(' token:");
        }

        while(!scanner.getNext().equals(")")) {
            // if reached ';' before end of parameters
            if (scanner.currentToken.tokenStr.equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Reached ';' before closing function ')':");
            }

            // if reached seperator skip token
            if (scanner.currentToken.tokenStr.equals(",")) {
                continue;
            }

            ResultValue res = expr();

            sb.append(res.strValue);
            sb.append(" ");

        }

        // ensure print function ends in ';'
        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Did not reach ';' at end of function call:");
        }

        //output string to terminal
        System.out.println(sb.toString());
    }


    /**
     * Parsers and executes assign statements
     * <p></p>
     *
     * @param varStr
     * @param res
     * @return
     * @throws PickleException
     */
    private ResultValue assign(String varStr, ResultValue res) throws PickleException{
        if (res.dataType == SubClassif.EMPTY) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot assign empty value to identifier:");
        }

        // grab identifier from table to perform coercion if necessary
        STIdentifier symbolEntry = (STIdentifier) this.symbolTable.getSymbol(varStr);


        // conversion from specified types to declared type
        if (symbolEntry.dclType == SubClassif.FLOAT) {
            res = Utility.castNumericToDouble(this, new Numeric(scanner, res, "", "cast to declared type"));
        } if (symbolEntry.dclType == SubClassif.INTEGER) {
            res = Utility.castNumericToInt(this, new Numeric(scanner, res, "", "cast to declared type"));
        }

        // store value
        this.storageManager.updateVariable(varStr, res);

        if (bShowAssign)
            System.out.printf("... Assign result into '%s' is '%s'\n", varStr, res.strValue);

        return res;
    }


    /**
     * Executes debug statements setting debug flags for parser
     * <p></p>
     *
     * @return
     * @throws PickleException
     */
    private boolean debugStmt() throws PickleException{

        // check if debug type of statment
        if (scanner.currentToken.tokenStr.equals("debug")) {

            String type = scanner.getNext();

            String setting = scanner.nextToken.tokenStr;
            boolean onOff = false;

            // grab boolean setting
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

            //set flag for specific debug boolean
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

            // ensure statement ends with ';'
            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';':");
            }

            return true;
        }

        return false;
    }

    /**
     * Parses statements and executes based on execute flag
     * <p>
     *
     * </p>
     * @param bExec             Execute Flag
     * @return
     * @throws PickleException
     */
    private ResultValue statements(boolean bExec) throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);
        //exec statements
        if (bExec) {
            while (hasNext() && scanner.nextToken.subClassif != SubClassif.END) {
                getNext();
            }
        }
        else { // skip over statments and set flag to false for if and while clauses
            while (hasNext() && scanner.nextToken.subClassif != SubClassif.END) {
                scanner.getNext();

                if (scanner.currentToken.primClassif == Classif.CONTROL && scanner.currentToken.subClassif == SubClassif.FLOW) {
                    switch (scanner.currentToken.tokenStr) {
                        case "if":
                            ifStmt(bExec);
                            break;
                        case "while":
                            whileStmt(bExec);
                    }
                }
            }
        }
        
        res.terminatingString = scanner.getNext();
        return res;
    }

    /**
     * Parses and executes while statement based on execute flag
     * <p></p>
     *
     * @param bExec             Execute flag
     * @return
     * @throws PickleException
     */
    private ResultValue whileStmt(Boolean bExec) throws PickleException {

        //set while position
        Token entryPosition = scanner.currentToken;
        ResultValue result;

        //execute while statement
        if (bExec) {
            //while contition is true execute stmts
             while (evalCond().strValue.equals("T")) {
                 result = statements(true);
                 if(!result.terminatingString.equals("endwhile")) {
                     throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile:");
                 }
                 //set position back to while
                 scanner.setPosition(entryPosition.iSourceLineNr, entryPosition.iColPos);
                 scanner.getNext();
             }
             //after while loop's condition is false, get back to the endwhile token
             result = statements(false);
             if(!result.terminatingString.equals("endwhile")) {
                 throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile:");
             }
             if (!scanner.getNext().equals(";")) {
                 throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';':");
             }
        }
        else { //dont execute while
            Utility.skipTo(scanner, ":");
            result = statements(false);
            if(!result.terminatingString.equals("endwhile")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile:");
            }
            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';':");
            }
        }
        return result;
    }

    /**
     * Parses and executes
     * @param bExec
     * @return
     * @throws PickleException
     */
    private ResultValue ifStmt(Boolean bExec) throws PickleException {

        ResultValue resCond;
        ResultValue resTemp;
        
        if (bExec) {

            resCond = evalCond();
            
            if (resCond.strValue.equals("T")) {
                // execute if block statements and skip else block if present

                resTemp = statements(true);
                
                if (resTemp.terminatingString.equals("else")) {
                    if (!scanner.getNext().equals(":")) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an ':' after an 'else' token:");
                    }

                    resTemp = statements(false);
                    
                }

                // ensure if statement ends with endif token
                if (!resTemp.terminatingString.equals("endif")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an ':' after an 'else' token:");
                }
                if (!scanner.getNext().equals(";")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected a ';' after 'endif' token:");
                }


            } else {

                // skip if block and execute else statements

                resTemp = statements(false);

                if (resTemp.terminatingString.equals("else")) {
                    if (!scanner.getNext().equals(":")) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an ':' after an 'else' token:");
                    }

                    resTemp = statements(true);
                }

                // ensure if statement ends with endif token
                if (!resTemp.terminatingString.equals("endif")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an ':' after an 'else' token:");
                }
                
                if (!scanner.getNext().equals(";")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected a ';' after 'endif' token:");
                }
            }

        } else {
            // do not execute any statements for if or else block

            Utility.skipTo(scanner, ":");

            resTemp = statements(false);

            if (resTemp.terminatingString.equals("else")) {
                if (!scanner.getNext().equals(":")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an ':' after an 'else' token:");
                }
                resTemp = statements(false);


            }

            if (!resTemp.terminatingString.equals("endif")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an 'endif' after 'if' token:");
            }

            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected a ';' after 'endif' token:");
            }
        }


        return resTemp;

    }



    private ResultValue evalCond() throws PickleException {
        scanner.getNext();

        ResultValue res01 = null;
        ResultValue res02 = null;

        // if started with an operator this is the boolean "not" so don't collect first ResultValue
        if (scanner.currentToken.primClassif != Classif.OPERATOR) {
            res01 = expr();
        }

        String operatorStr = scanner.currentToken.tokenStr;
        Token operatorToken = scanner.currentToken;

        scanner.getNext();

        res02 = expr();

        if (!scanner.getNext().equals(":")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Conditions must be followed by ':' token :");
        }

        ResultValue tempResult;
        Bool bOp1;
        Bool bOp2;

        // Switch to perform specific Utility boolean check
        switch (operatorStr) {
            case ">":
                tempResult = Utility.greaterThan(this, res01, res02);
                break;
            case "<":
                tempResult = Utility.lessThan(this, res01, res02);
                break;
            case ">=":
                tempResult = Utility.greaterThanOrEqualTo(this, res01, res02);
                break;
            case "<=":
                tempResult = Utility.lessThanOrEqualTo(this, res01, res02);
                break;
            case "==":
                tempResult = Utility.equal(this, res01, res02);
                break;
            case "!=":
                tempResult = Utility.notEqual(this, res01, res02);
                break;
            case "and":
                bOp1 = new Bool(scanner, res01);
                bOp2 = new Bool(scanner, res02);
                tempResult = Utility.boolAnd(this, bOp1, bOp2);
                break;
            case "or":
                bOp1 = new Bool(scanner, res01);
                bOp2 = new Bool(scanner, res02);
                tempResult = Utility.boolOr(this, bOp1, bOp2);
                break;
            case "not":
                bOp2 = new Bool(scanner, res02);
                tempResult = Utility.boolNot(this, bOp2);
                break;
            default:
                throw new ScannerParserException(operatorToken, scanner.sourceFileNm, "Invalid comparator token");
        }

        if (bShowExpr)
            System.out.printf("...%s %s %s is %s\n", res01.strValue, operatorStr, res02.strValue, tempResult.strValue);


        return tempResult;
    }


    /**
     * Main Pickle Pickle Loop
     * <p></p>
     *
     * @throws PickleException
     */
    public void run() throws PickleException {
        while (hasNext()) {
            getNext();
        }
    }

    /**
     * Helper function to get declared type for identifer
     * <p></p>
     *
     * @param declareString
     * @return
     * @throws PickleException
     */
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
