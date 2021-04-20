package pickle;

import javax.xml.parsers.ParserConfigurationException;
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
                controlStmt(true);
                break;
            case OPERATOR:
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot evaluate starting on operator:");
            default:
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Unknown token to evaluate");
        }


    }

    /**
     * Executes control statements
     * <p></p>
     *
     * @return                  generic ResultValue
     * @throws PickleException
     */
    private Result controlStmt(Boolean bExec) throws PickleException {
        Result res = new ResultValue("", SubClassif.EMPTY);

        switch (scanner.currentToken.subClassif) {
            case DECLARE:
                res = declareStmt();
                break;
            case FLOW:
                String flowStr = scanner.currentToken.tokenStr;

                switch (flowStr) {
                    case "if":
                        res = ifStmt(bExec);
                        break;
                    case "while":
                        res = whileStmt(bExec);
                        break;
                    case "for":
                        res = forStmt(bExec);
                        break;

                }
                break;
            default:
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Unknown Control token");
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
    private Result declareStmt() throws PickleException {
        ResultValue res;

        String declareTypeStr = scanner.currentToken.tokenStr;
        scanner.getNext();

        // Must Declare using an identifier
        if (scanner.currentToken.primClassif != Classif.OPERAND
         && scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {
            throw new PickleException();
        }

        String varStr = scanner.currentToken.tokenStr;

        if (scanner.nextToken.tokenStr.equals("[")) {
            scanner.getNext();
            return declareArrayStmt(varStr, declareTypeStr);
        }

        res = new ResultValue(varStr, SubClassif.EMPTY);

        // if assignment occuring grab expression into result value
        if (scanner.getNext().equals("=")) {
            scanner.getNext();
            res = (ResultValue) expr();
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
            assign(varStr, res);
        }


        return res;
    }

    private Result declareArrayStmt(String varStr, String declareTypeStr) throws PickleException {
        ResultList resList;
        ResultValue res, currRes;
        SubClassif arrType = getDataType(declareTypeStr);
        ArrayList<ResultValue> values = new ArrayList<ResultValue>();

        resList = new ResultList(this, new ArrayList<ResultValue>(), 0, arrType);

        if (scanner.getNext().equals("]")) { //if square brackets contain no value
            if (!scanner.getNext().equals("=")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm,"Excpeted assignment values for unbounded array");
            }
        }
        else {
            res = (ResultValue) expr(); //get value in square brackets
            if (res.dataType != SubClassif.INTEGER) {
                res = Utility.castNumericToInt(this, new Numeric(this, res, "+", "expr ret value"));
            }
            resList.capacity = Integer.parseInt(res.strValue);
            ResultValue empty = new ResultValue("", SubClassif.EMPTY);
            for (int i = 0; i < resList.capacity; i++) {
                resList.arrayList.add(empty);
            }
            if (scanner.currentToken.tokenStr.equals(";")) { //no assignment just declaration of bounded array
                resList.allocatedSize = 0;
                symbolTable.putSymbol(varStr, new STIdentifier(varStr, Classif.OPERAND, arrType, "array", "none", 99));
                storageManager.updateVariable(varStr, resList);
                return resList;
            }
        }

        if (!scanner.currentToken.tokenStr.equals("=")) { //we may have a problem, dont know what lamo
            throw new PickleException();
        }

        //loop though elements
        while(!scanner.getNext().equals(";")) {
            if (scanner.currentToken.tokenStr.equals(",")) { //skip comma
                continue;
            }
            currRes = new ResultValue(scanner.currentToken.tokenStr, scanner.currentToken.subClassif);

            if (scanner.currentToken.subClassif != arrType) {
             if (scanner.currentToken.subClassif == SubClassif.INTEGER && arrType == SubClassif.FLOAT) {
                 currRes = Utility.castNumericToDouble(this, new Numeric(this, currRes, "", "Casting to Float"));
             } else if (scanner.currentToken.subClassif == SubClassif.FLOAT && arrType == SubClassif.INTEGER) {
                 currRes = Utility.castNumericToInt(this, new Numeric(this, currRes, "", "Casting to Integer"));
             }
            }


            values.add(currRes);
            if (!scanner.nextToken.tokenStr.equals(",")  && !scanner.nextToken.tokenStr.equals(";")) {
                throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Expected a sperator");
            }
        }

        resList.arrayList = values;
        resList.allocatedSize = values.size();
        if (resList.capacity == 0) {
            resList.capacity = values.size();
        }

        symbolTable.putSymbol(varStr, new STIdentifier(varStr, Classif.OPERAND, arrType, "array", "none", 99));
        storageManager.updateVariable(varStr, resList);

        return resList;
    }

    /**
     * Executes assignment statements
     * <p>
     *
     * </p>
     * @return
     * @throws PickleException
     */
    private Result assignmentStmt() throws PickleException {
        Result res;

        if (scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Operand must be of type IDENTIFIER to assign values");
        }

        String varStr = scanner.currentToken.tokenStr;

        STEntry entry = symbolTable.getSymbol(scanner.currentToken.tokenStr);

        if (entry.primClassif != Classif.EMPTY && ((STIdentifier) symbolTable.getSymbol(scanner.currentToken.tokenStr)).structure.equals("array")) { // if operator is an array, branch outta here real quick like the flash ⚡⚡
            res =  assignArrayStmt(varStr);
        } else if (scanner.nextToken.primClassif == Classif.OPERATOR && scanner.getNext().equals("=")) {

            scanner.getNext();      //get next token
            res = expr();           //get expression value

        } else if (scanner.getNext().equals("[")) {


            scanner.getNext(); // advance to Expression
            ResultValue index;

            try {
                entry = symbolTable.getSymbol(varStr);

                if (entry.primClassif == Classif.EMPTY) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot index into uninitialized string");
                } else  if (((STIdentifier)entry).dclType != SubClassif.STRING) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot index into non array or non string variables");
                }

                ResultValue str = (ResultValue)storageManager.getVariable(varStr);

                index = (ResultValue) expr();

                if (index.dataType != SubClassif.INTEGER) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Index must be an Integer");
                }

                if (!scanner.currentToken.tokenStr.equals("=")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Assignment statement must be followed by '=' '");
                }

                scanner.getNext(); // advance to next expression must be a string
                res = (ResultValue)expr();



                res = Utility.assignAtIndex(this, str, (ResultValue) res, Integer.parseInt(index.strValue) );

            } catch (Exception e) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expression must be scalar value");
            }

        }

        else  {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid assignment");
        }

        // ensure assignment ends in ';'
        if (!scanner.currentToken.tokenStr.equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Assignment statement must end in ';'");
        }

        res = assign(varStr, res);  //save value to symbol
        return res;
    }

    private ResultList assignArrayStmt(String varString) throws PickleException {
        ResultList array = (ResultList) storageManager.getVariable(varString), res;
        ResultValue val, assign;

        if (scanner.getNext().equals("=")) { //total array assignment
            scanner.getNext(); //skip to asignee dude guy expr 🤵

            if (scanner.currentToken.subClassif == SubClassif.IDENTIFIER && ((STIdentifier)symbolTable.getSymbol(scanner.currentToken.tokenStr)).structure.equals("array") && scanner.nextToken.tokenStr.equals(";")) { //just an array to array
                res = Utility.assignArrayToArray(this, array, (ResultList) storageManager.getVariable(scanner.currentToken.tokenStr));
                scanner.getNext();
            }
            else {
                val = (ResultValue) expr();
                if (val.dataType != array.dataType) {
                    //TODO - fix exception
                    throw new PickleException();
                }
                res = Utility.assignScalarToArray(this, val, array.capacity);
            }
        }
        else if (scanner.currentToken.tokenStr.equals("[")) { //arr index assignment

            scanner.getNext();
            val = (ResultValue) expr(); //get index of array

            if (!scanner.currentToken.tokenStr.equals("=")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Array assignment must be followed by '=' operator");
            }

            scanner.getNext();
            assign = (ResultValue) expr();
            if (val.dataType != SubClassif.INTEGER) {
                val = Utility.castNumericToInt(this, new Numeric(this, val, "+", "value of array index"));
            }


            if (assign.dataType != SubClassif.STRING && assign.dataType != SubClassif.INTEGER && assign.dataType != SubClassif.FLOAT) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot assign data type " + assign.dataType.name() + " to array of data type " + array.dataType.name());
            }

            if (assign.dataType == SubClassif.FLOAT && array.dataType == SubClassif.INTEGER) {
                assign = Utility.castNumericToInt(this, new Numeric(this, assign, "", "Coerce to Int"));
            } else if (assign.dataType == SubClassif.INTEGER && array.dataType == SubClassif.FLOAT) {
                assign = Utility.castNumericToDouble(this, new Numeric(this, assign, "", "Coerce to Float"));
            }


            array.setItem(this, Integer.parseInt(val.strValue), assign);
            res = array;
        }
        else {
            throw new PickleException();
        }

        if (!scanner.currentToken.tokenStr.equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Assignment statement must end in ';'");
        }


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
    private Result expr() throws PickleException {
        //System.out.printf("Called expr with tokenStr: %s\n", scanner.currentToken.tokenStr);

        ArrayList<Token> out = Expr.postFixExpr(this);

        /* System.out.printf("Postfix: ");
        for(Token token : out) {
            System.out.printf("%s ", token.tokenStr);
        }
        System.out.println();*/

        Result ans = Expr.evaluatePostFix(this, out);

        // code to see postfix expression and evaluated answer




        /*System.out.printf("Evalueted to answer: %s\n", ((ResultValue)ans).strValue);*/

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
        Result res =  null;

        // check for builtin function if not throw error for identifier
        if (scanner.currentToken.subClassif == SubClassif.BUILTIN) {
            switch (scanner.currentToken.tokenStr) {
                case "print":
                    print();
                    break;
                case "LENGTH":
                case "SPACES":
                case "ELEM":
                case "MAXELEM":
                    res = expr();
                    break;

                default:
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "No function of name: ");
            }
        }


        return (ResultValue) res;
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

            Result res = expr();



            sb.append(res.printResult());
            sb.append(" ");

        }

        // ensure print function ends in ';'
        if (!scanner.currentToken.tokenStr.equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Did not reach ';' at end of function call:");
        }

        //output string to terminal
        System.out.println(sb.toString());
    }

    private ResultValue length() throws PickleException {
        scanner.getNext();
        ResultValue res;
        Token exprToken = scanner.currentToken;
        try {

            res = (ResultValue) expr();

            if (res.dataType != SubClassif.STRING) {
                throw new ScannerParserException(exprToken, scanner.sourceFileNm, "Cannot call builtin LENGTH on non-string expressions");
            }
            res = Utility.builtInLENGTH(this, res);
        } catch (PickleException p) {
            throw p;
        } catch (Exception e) {
            throw new ScannerParserException(exprToken, scanner.sourceFileNm, "Cannot call builtin Length on arrays");
        }

        return res;
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
    private Result assign(String varStr, Result res) throws PickleException {
       /* if (res.dataType == SubClassif.EMPTY) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot assign empty value to identifier:");
        }*/

        // grab identifier from table to perform coercion if necessary
        STEntry entry = this.symbolTable.getSymbol(varStr);

        if (entry.primClassif == Classif.EMPTY) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot assign value to undeclared variable");
        }


        STIdentifier symbolEntry = (STIdentifier) this.symbolTable.getSymbol(varStr);

        if (res instanceof ResultValue) {
            // conversion from specified types to declared type
            if (symbolEntry.dclType == SubClassif.FLOAT) {
                res = Utility.castNumericToDouble(this, new Numeric(this, (ResultValue) res, "", "cast to declared type"));
            }
            if (symbolEntry.dclType == SubClassif.INTEGER) {
                res = Utility.castNumericToInt(this, new Numeric(this, (ResultValue) res, "", "cast to declared type"));
            }
        }

        // store value
        this.storageManager.updateVariable(varStr, res);

        if (bShowAssign)
            System.out.printf("... Assign result into '%s' is '%s'\n", varStr, ((ResultValue) res).strValue);

        return  res;
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
                    controlStmt(false);
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

    private ResultValue forStmt(Boolean bExec) throws PickleException {
        ResultValue result = new ResultValue("", SubClassif.EMPTY);

        if (bExec) {
            scanner.getNext();
            if (scanner.currentToken.primClassif != Classif.OPERAND && scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {
                // TODO: 4/13/2021 fix exception
                throw new PickleException();
            }
            String controlVar = scanner.currentToken.tokenStr;
            scanner.getNext();
            if (scanner.currentToken.tokenStr.equals("=")) {
                result = countingFor(controlVar);
            }
            else if (scanner.currentToken.tokenStr.equals("in")) {
                //branch to either charStringFor() or itemArrayFor() depending on if controlVar is a char
                // TODO: 4/15/2021 check identifier to be String or Array


                try {
                    STIdentifier entry = (STIdentifier) this.symbolTable.getSymbol(scanner.nextToken.tokenStr);

                    if (entry.primClassif == Classif.EMPTY) {
                        throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Identifier must be declared first");
                    }

                    if (entry.dclType == SubClassif.STRING && !entry.structure.equals("array")) {
                        charStringFor(controlVar);
                    }
                    else if (entry.structure.equals("array")) {
                        itemArrayFor(controlVar);
                    } else  {
                        // TODO: 4/15/2021 cannot run for loop on any other types
                        throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Identifier must be of type String, Int, Float to use for loop");
                    }
                } catch (PickleException p) {
                    throw p;
                } catch (Exception e) {
                    throw e;
                }


                
            }
            else if (scanner.currentToken.tokenStr.equals("from")) {
                result = stringDelimiterFor(controlVar);
            }
            else {
                //TODO: fix exception - for loop type not recognized error or smth man idk
                throw new PickleException();
            }
        } else {
            Utility.skipTo(scanner, ":");
            result = statements(false);

            if (!result.terminatingString.equals("endfor")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endfor");
            }

            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';'");
            }
        }

        return result;
    }

    private ResultValue charStringFor(String controlVar) throws PickleException {
        STEntry entry = symbolTable.getSymbol(controlVar);
        ResultValue result = new ResultValue("", SubClassif.EMPTY);

        if (entry.primClassif == Classif.EMPTY) {
            symbolTable.putSymbol(controlVar,
                    new STIdentifier(controlVar,
                            Classif.OPERAND,
                            SubClassif.STRING,
                            "none",
                            "local",
                            0));


        }


        scanner.getNext();

        Result end = expr(); //get value of limit
        ResultValue limit;

        if (end instanceof ResultValue) {
            limit = (ResultValue) end;
        } else {
            // TODO: 4/15/2021 cannot iterator over array in this loop
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot use for char loop over array");
        }

        if (limit.dataType != SubClassif.STRING) {
            //TODO fix exception - limit type needs to be of a string
            throw new PickleException();
        }

        ResultValue startValue = Utility.valueAtIndex(this, limit, 0); //set up iterating char value

        if (!scanner.currentToken.tokenStr.equals(":")) {
            // TODO: fix exception - error for statement not ending in ':'
            throw new PickleException();
        }

        //save off current position to loop
        int iSavedLineNr = scanner.currentToken.iSourceLineNr;
        int iSavedColPos = scanner.currentToken.iColPos;

        storageManager.updateVariable(controlVar, startValue);

        ResultValue currPos = new ResultValue("0", SubClassif.INTEGER);
        ResultValue maxPos = new ResultValue(String.valueOf(limit.strValue.length()), SubClassif.INTEGER);

        ResultValue incrementBy = new ResultValue("1", SubClassif.INTEGER);

        Numeric nOp1, nOp2;
        nOp2 = new Numeric(this, incrementBy, "+", "Incrementing by value");

        while(Utility.lessThan(this, currPos, maxPos).strValue.equals("T")) {
            // evaluate loop statements
            result = statements(true);

            if (!result.terminatingString.equals("endfor")) {
                //TODO: fix exception - should be a for loop's terminating thingy
                throw new PickleException();
            }

            nOp1 = new Numeric(this, currPos, "+", "Incrementing char pos");


            currPos = Utility.add(this, nOp1, nOp2);

            if (Integer.parseInt(currPos.strValue) < Integer.parseInt(maxPos.strValue)) {
                startValue = Utility.valueAtIndex(this, limit, Integer.parseInt(currPos.strValue)); //increment char value
                storageManager.updateVariable(controlVar, startValue);
            }

            scanner.setPosition(iSavedLineNr, iSavedColPos);
            scanner.getNext();
        }

        result = statements(false);

        if (!result.terminatingString.equals("endfor")) {
            //TODO: fix exception - end should be here
            throw new PickleException();
        }

        if (!scanner.getNext().equals(";")) {
            //TODO: fix exception - ; should be here
            throw new PickleException();
        }

        return result;
    }

    private ResultValue itemArrayFor(String controlVar) throws  PickleException {
        STEntry entry = symbolTable.getSymbol(controlVar);
        ResultValue result = new ResultValue("", SubClassif.EMPTY);



        scanner.getNext();


        Result end = expr();
        ResultList limit;
        if (end instanceof ResultList) {
            limit = (ResultList) end;
        } else {
            throw new PickleException();
        }


        if (entry.primClassif == Classif.EMPTY) {
            symbolTable.putSymbol(controlVar,
                    new STIdentifier(controlVar,
                            Classif.OPERAND,
                            limit.dataType, //TODO - ish
                            "none",
                            "local",
                            0));


        } else { //TODO - might need to fix this if too
            //TODO fix exception - limit type needs to be the same as the array elements

            STIdentifier id = (STIdentifier) symbolTable.getSymbol(controlVar);

            if (limit.dataType != id.dclType) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot use variable of non " + limit.dataType.name() + " type");
            }

        }

        ResultValue startValue = limit.getItem(this, 0); //set up iterating char value

        if (!scanner.currentToken.tokenStr.equals(":")) {
            // TODO: fix exception - error for statement not ending in ':'
            throw new PickleException();
        }

        //save off current position to loop
        int iSavedLineNr = scanner.currentToken.iSourceLineNr;
        int iSavedColPos = scanner.currentToken.iColPos;



        ResultValue currPos = new ResultValue("0", SubClassif.INTEGER);
        ResultValue maxPos = new ResultValue(String.valueOf(limit.allocatedSize), SubClassif.INTEGER);

        ResultValue incrementBy = new ResultValue("1", SubClassif.INTEGER);


        startValue =  limit.getItem(this, Integer.parseInt(currPos.strValue));

        Numeric nOp1, nOp2;
        nOp2 = new Numeric(this, incrementBy, "+", "Incrementing by value");

        while (startValue.dataType == SubClassif.EMPTY && Integer.parseInt(currPos.strValue) < Integer.parseInt(maxPos.strValue)) {
            nOp1 = new Numeric(this, currPos, "+", "Incrementing array pos");


            currPos = Utility.add(this, nOp1, nOp2);
            startValue =  limit.getItem(this, Integer.parseInt(currPos.strValue));
        }

        storageManager.updateVariable(controlVar, startValue);

        while(Utility.lessThan(this, currPos, maxPos).strValue.equals("T")) {
            // evaluate loop statements
            result = statements(true);

            if (!result.terminatingString.equals("endfor")) {
                //TODO: fix exception - should be a for loop's terminating thingy
                throw new PickleException();
            }


            nOp1 = new Numeric(this, currPos, "+", "Incrementing array pos");


            currPos = Utility.add(this, nOp1, nOp2);
            if (Integer.parseInt(currPos.strValue) < Integer.parseInt(maxPos.strValue)) {

                startValue =  limit.getItem(this, Integer.parseInt(currPos.strValue));
                while (startValue.dataType == SubClassif.EMPTY && Integer.parseInt(currPos.strValue) < Integer.parseInt(maxPos.strValue)) {
                    nOp1 = new Numeric(this, currPos, "+", "Incrementing array pos");


                    currPos = Utility.add(this, nOp1, nOp2);
                    if (Integer.parseInt(currPos.strValue) >= Integer.parseInt(maxPos.strValue))
                        break;

                    startValue =  limit.getItem(this, Integer.parseInt(currPos.strValue));
                }

                if (Integer.parseInt(currPos.strValue) < Integer.parseInt(maxPos.strValue))
                    storageManager.updateVariable(controlVar, startValue);
            }

            scanner.setPosition(iSavedLineNr, iSavedColPos);
            scanner.getNext();
        }

        result = statements(false);

        if (!result.terminatingString.equals("endfor")) {
            //TODO: fix exception - end should be here
            throw new PickleException();
        }

        if (!scanner.getNext().equals(";")) {
            //TODO: fix exception - ; should be here
            throw new PickleException();
        }

        return result;
    }



    private ResultValue stringDelimiterFor(String controlVar) throws  PickleException {
        STEntry entry = symbolTable.getSymbol(controlVar);
        ResultValue result = new ResultValue("", SubClassif.EMPTY);
        //TODO function body
        return result;
    }

    private ResultValue countingFor(String controlVar) throws PickleException {
        STEntry entry = symbolTable.getSymbol(controlVar);
        ResultValue result;
        ResultValue startValue;
        ResultValue limit;
        ResultValue incrementBy = new ResultValue("1", SubClassif.INTEGER);


        if (entry.primClassif == Classif.EMPTY) {
            symbolTable.putSymbol(controlVar,
                    new STIdentifier(controlVar,
                            Classif.OPERAND,
                            SubClassif.INTEGER,
                            "none",
                            "local",
                            0));


        }


        scanner.getNext();
        
        if (scanner.currentToken.primClassif != Classif.OPERAND &&
                (scanner.currentToken.subClassif != SubClassif.FLOAT || scanner.currentToken.subClassif != SubClassif.INTEGER)) {
            // TODO: 4/13/2021 parser error since assigning non-numeric
            throw new PickleException();
        }

        try {
            startValue = (ResultValue) expr();

            if (!scanner.currentToken.tokenStr.equals("to")) {
                // TODO: 4/13/2021 parser error incorrect for setup
                throw new PickleException();
            }

            scanner.getNext();


            limit = (ResultValue) expr();

            if (scanner.currentToken.tokenStr.equals("by")) {
                scanner.getNext();
                incrementBy = (ResultValue) expr();

            }

        } catch (PickleException p) {
            throw p;
        } catch (Exception e) {
            // TODO: 4/15/2021 must be ResultValue
            throw new PickleException();
        }



        ResultValue zero =  new ResultValue("0", SubClassif.INTEGER);

        if (Utility.lessThan(this, incrementBy, zero).strValue.equals("T")) {
            // TODO: 4/13/2021 throw less than zero error
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Increment must be positive integer");
        }


        if (!scanner.currentToken.tokenStr.equals(":")) {
            // TODO: 4/13/2021 parse error for statement not ending in ':'
            throw new PickleException();
        }
        int iSavedLineNr = scanner.currentToken.iSourceLineNr;
        int iSavedColPos = scanner.currentToken.iColPos;

        storageManager.updateVariable(controlVar, startValue);

        Numeric nOp1;
        Numeric nOp2;

        nOp2 = new Numeric(this, incrementBy, "+", "Incrementing by value");





        while (Utility.lessThan(this, startValue, limit).strValue.equals("T")) {
            // evaluate statments
            result = statements(true);

            if (!result.terminatingString.equals("endfor")) {
                // TODO: 4/13/2021 throw parser error for loop didn't end in endfor
                throw new PickleException();
            }

            nOp1 = new Numeric(this, startValue, "+", "Incrementing startValue");


            startValue = Utility.add(this, nOp1, nOp2);
            storageManager.updateVariable(controlVar, startValue);

            scanner.setPosition(iSavedLineNr, iSavedColPos);
            scanner.getNext();
        }

        result= statements(false);

        if (!result.terminatingString.equals("endfor")) {
            // TODO: 4/13/2021 throw error
            throw new PickleException();
        }

        if (!scanner.getNext().equals(";")) {
            throw new PickleException();
        }

        return result;

    }

    private ResultValue evalCond() throws PickleException {


        ResultValue res = new ResultValue("", SubClassif.EMPTY);
        scanner.getNext();
        try {
            res = (ResultValue) expr();
        } catch (PickleException p) {
            throw p;
        } catch (Exception e) {
            // TODO: 4/15/2021 Bool are only ResultValue
            throw e;
        }

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
