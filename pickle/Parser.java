package pickle;

import java.util.ArrayList;

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
            scanner.getNext();
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
        if (!scanner.currentToken.tokenStr.equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Assignment statement must end in ';'");
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
        //System.out.printf("Called expr with tokenStr: %s\n", scanner.currentToken.tokenStr);


        ArrayList<Token> out = Expr.postFixExpr(this);

        ResultValue ans = Expr.evaluatePostFix(this, out);

        // code to see postfix expression and evaluated answer
        /*System.out.printf("Postfix: ");
        for(Token token : out) {
            System.out.printf("%s ", token.tokenStr);
        }

        System.out.println();

        System.out.printf("Evalueted to answer: %s\n", ans.strValue);*/

        return ans;


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

        while(!scanner.currentToken.tokenStr.equals(";")) {
            // if reached ';' before end of parameters
            if (scanner.currentToken.tokenStr.equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Reached ';' before closing function ')':");
            }

            // if reached seperator skip token
            if (scanner.currentToken.tokenStr.equals(",")) {
                scanner.getNext();
                continue;
            }

            ResultValue res = expr();



            sb.append(res.strValue);
            sb.append(" ");

        }

        // ensure print function ends in ';'
        if (!scanner.currentToken.tokenStr.equals(";")) {
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


        ResultValue res = null;
        scanner.getNext();

        res = expr();

        if (!scanner.currentToken.tokenStr.equals(":")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Conditions must be followed by ':' token :");
        }


        return res;
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
