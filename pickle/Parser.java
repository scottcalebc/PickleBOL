package pickle;

import java.util.ArrayList;
import java.util.Stack;

public class Parser {


    protected Scanner                   scanner;                        // scanner pointer
    protected SymbolTable               symbolTable;                    // symbol table pointer
    protected StorageManager            storageManager;                 // storage manager
    protected Stack<ActivationRecord>   activationRecordStack;          // activation record stack

    protected boolean       bShowExpr;                                  // boolean flag for debug expressions
    protected boolean       bShowAssign;                                // boolean flag for debug assignments
    protected boolean       bPostFix;

    /**
     * Parser constructor takes scanner and symbol table
     * <p>
     *
     * </p>
     * @param scanner           scanner object
     * @param symbolTable       symbol table object
     */
    public Parser(Scanner scanner, SymbolTable symbolTable) {
        this.scanner = scanner;
        this.symbolTable = symbolTable;
        this.storageManager = new StorageManager();
        this.activationRecordStack = new Stack<ActivationRecord>();

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
    public ResultValue getNext(iExecMode execMode) throws PickleException {
        // get Next Statment, meaning get all tokens for a stmt
        scanner.getNext();
        ResultValue res = new ResultValue("", SubClassif.EMPTY);
        res.execMode = execMode;

        verifyOperand();
        
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
                return controlStmt(execMode);
            case OPERATOR:
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot evaluate starting on operator");
            default:
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Unknown token to evaluate");
        }

        return res;
    }

    public void verifyOperand() {
        if (scanner.currentToken.primClassif == Classif.OPERAND) {
            if (scanner.currentToken.subClassif == SubClassif.IDENTIFIER) {
                STEntry entry = this.symbolTable.getSymbol(scanner.currentToken.tokenStr);

                if (! this.activationRecordStack.isEmpty()) {
                    int scope = this.activationRecordStack.peek().findSymbolScope(scanner.currentToken.tokenStr);

                    if (scope != -1)
                        entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(scanner.currentToken.tokenStr);
                }

                if (entry.primClassif != Classif.EMPTY && (entry instanceof STFunction)) {
                    scanner.currentToken.primClassif = Classif.FUNCTION;
                    scanner.currentToken.subClassif = ((STFunction) entry).definedBy;
                }
            }
        }
    }

    /**
     * Executes control statements
     * <p></p>
     *
     * @return                  generic ResultValue
     * @throws                  PickleException
     */
    private ResultValue controlStmt(iExecMode execMode) throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);
        res.execMode = execMode;

        switch (scanner.currentToken.subClassif) {
            case DECLARE:
                res = declareStmt();
                res.execMode = execMode;
                break;
            case FLOW:
                String flowStr = scanner.currentToken.tokenStr;

                switch (flowStr) {
                    case "if":
                        res = ifStmt(execMode);
                        break;
                    case "while":
                        res = whileStmt(execMode);
                        break;
                    case "for":
                        res = forStmt(execMode);
                        break;
                    case "def":
                        userFunction(execMode);
                        break;
                    case "break":
                        res.execMode = iExecMode.BREAK_EXEC;
                        if (!scanner.getNext().equals(";")) {
                            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Break statement must be followed by ';' ");
                        }
                        break;
                    case "continue":
                        res.execMode = iExecMode.CONTINUE_EXEC;
                        if (!scanner.getNext().equals(";")) {
                            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Continue statement must be followed by ';' ");
                        }
                        break;
                    case "return":
                        if (execMode == iExecMode.EXECUTE) {
                            scanner.getNext();
                            if (!scanner.currentToken.tokenStr.equals(";")){
                                Result returnVal = expr();
                                this.activationRecordStack.peek().returnVal = returnVal;
                            }

                            if (!scanner.currentToken.tokenStr.equals(";")) {
                                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Return statement must end with ';'");
                            }

                            res.execMode = iExecMode.RETURN;
                        } else {
                            Utility.skipTo(scanner, ";");
                        }
                        break;
                    case "select":
                        res = selectStmt(execMode);
                        break;
                    default:
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Unknown Control token");

                }
                break;
            default:
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Unknown Control token");
        }

        return res;
    }

    /**
     * Executes Declare statements
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

        if (scanner.nextToken.tokenStr.equals("[")) {
            scanner.getNext();
            declareArrayStmt(varStr, declareTypeStr);
            return new ResultValue(varStr, SubClassif.IDENTIFIER);
        }

        res = new ResultValue(varStr, SubClassif.EMPTY);

        // if assignment occurring grab expression into result value
        if (scanner.getNext().equals("=")) {
            scanner.getNext();
            res = (ResultValue) expr();

            if (declareTypeStr.equals("Date")) {
                res = Date.validateDate(res.strValue);
            }
        }

        // Statement does not end in a semicolon
        if (! scanner.currentToken.tokenStr.equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expecting ';' at end of assignment:");
        }

        if (this.activationRecordStack.isEmpty()) { //in global scope
            STEntry varEntry = symbolTable.getSymbol(varStr);

            if (varEntry.symbol.isEmpty()) {
                // put symbol into table
                symbolTable.putSymbol(varStr, new STIdentifier(
                        varStr,
                        Classif.OPERAND,
                        getDataType(declareTypeStr),
                        "primitive",
                        "none",
                        0
                ));
            }
            else { //if var already exists
                if (getDataType(declareTypeStr) != ((STIdentifier) varEntry).dclType) {
                    ((STIdentifier) varEntry).dclType = getDataType(declareTypeStr);
                }
                //throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol \"" + varStr + "\" already declared");
            }
        }
        else { //in a function's scope
            STEntry varEntry = this.activationRecordStack.peek().symbolTable.getSymbol(varStr);

            if (varEntry.symbol.isEmpty()) {
                this.activationRecordStack.peek().symbolTable.putSymbol(varStr, new STIdentifier(
                        varStr,
                        Classif.OPERAND,
                        getDataType(declareTypeStr),
                        "primitive",
                        "none",
                        0
                ));
            }
            else { //if var exits already
                if (getDataType(declareTypeStr) != ((STIdentifier) varEntry).dclType) {
                    ((STIdentifier) varEntry).dclType = getDataType(declareTypeStr);
                }
                //throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol \"" + varStr + "\" already declared");
            }
        }

        // if assignment being performed during declaration ensure to assign value before exiting
        if (res.dataType != SubClassif.EMPTY) {
            assign(varStr, res);
        }

        return new ResultValue(varStr, SubClassif.IDENTIFIER);
    }

    private Result declareArrayStmt(String varStr, String declareTypeStr) throws PickleException {
        ResultList resList;
        ResultValue res, currRes;
        SubClassif arrType = getDataType(declareTypeStr);
        ArrayList<ResultValue> values = new ArrayList<ResultValue>();

        resList = new ResultList(this, new ArrayList<ResultValue>(), 0, arrType);

        if (scanner.nextToken.tokenStr.equals("]")) { //if square brackets contain no value
            scanner.getNext();
            if (!scanner.getNext().equals("=")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm,"Expected assignment values for array");
            }
        }
        else {
            if (scanner.nextToken.tokenStr.equals("unbound")) {
                scanner.nextToken.tokenStr = "-1";
                scanner.nextToken.primClassif = Classif.OPERAND;
                scanner.nextToken.subClassif = SubClassif.INTEGER;
                resList = new ResultList(this, new ArrayList<ResultValue>(), -1, arrType);
            }
                res = (ResultValue) expr(); //get value in square brackets
            //res = (ResultValue) expr(); //get value in square brackets
            if (res.dataType != SubClassif.INTEGER) {
                res = Utility.castNumericToInt(this, new Numeric(this, res, "+", "expr ret value"));
            }
            if (!res.strValue.equals("-1"))
                resList.capacity = Integer.parseInt(res.strValue);
            ResultValue empty = new ResultValue("", SubClassif.EMPTY);
            for (int i = 0; i < resList.capacity; i++) {
                resList.arrayList.add(empty);
            }
            if (scanner.currentToken.tokenStr.equals(";")) { //no assignment just declaration of bounded array
                resList.allocatedSize = 0;
                if (this.activationRecordStack.isEmpty()) { //in global scope
                    if (!symbolTable.getSymbol(varStr).symbol.isEmpty()) {
                       // throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol \"" + varStr + "\" already declared");
                    }

                    symbolTable.putSymbol(varStr, new STIdentifier(varStr, Classif.OPERAND, arrType, "array", "none", 0));
                    storageManager.updateVariable(varStr, resList);
                }
                else {
                    if (!this.activationRecordStack.peek().symbolTable.getSymbol(varStr).symbol.isEmpty()) {
                        //throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol \"" + varStr + "\" already declared");
                    }

                    this.activationRecordStack.peek().symbolTable.putSymbol(varStr, new STIdentifier(varStr, Classif.OPERAND, arrType, "array", "none", 0));
                    this.activationRecordStack.peek().storageManager.updateVariable(varStr, resList);
                }

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
            //currRes = new ResultValue(scanner.currentToken.tokenStr, scanner.currentToken.subClassif);
            Result tmp = expr();
            if (!(tmp instanceof ResultValue)) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected primitive, found list");
            }

            currRes = (ResultValue) tmp;

            if (currRes.dataType != arrType) {
             if (currRes.dataType == SubClassif.INTEGER && arrType == SubClassif.FLOAT) {
                 currRes = Utility.castNumericToDouble(this, new Numeric(this, currRes, "", "Casting to Float"));
             } else if (currRes.dataType == SubClassif.FLOAT && arrType == SubClassif.INTEGER) {
                 currRes = Utility.castNumericToInt(this, new Numeric(this, currRes, "", "Casting to Integer"));
             }
            }

            values.add(currRes);
            if (!scanner.currentToken.tokenStr.equals(",")  && !scanner.currentToken.tokenStr.equals(";")) {
                throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Expected a separator");
            }
            if (scanner.currentToken.tokenStr.equals(";")) {
                break;
            }
        }

        resList.arrayList = values;
        resList.allocatedSize = values.size();
        if (resList.capacity == 0) {
            resList.capacity = values.size();
        }

        if (this.activationRecordStack.isEmpty()) { //in global scope
            if (!symbolTable.getSymbol(varStr).symbol.isEmpty()) {
                //throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol \"" + varStr + "\" already declared");
            }

            symbolTable.putSymbol(varStr, new STIdentifier(varStr, Classif.OPERAND, arrType, "array", "none", 0));
            storageManager.updateVariable(varStr, resList);
        }
        else {
            if (!this.activationRecordStack.peek().symbolTable.getSymbol(varStr).symbol.isEmpty()) {
                //throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Symbol \"" + varStr + "\" already declared");
            }

            this.activationRecordStack.peek().symbolTable.putSymbol(varStr, new STIdentifier(varStr, Classif.OPERAND, arrType, "array", "none", 0));
            this.activationRecordStack.peek().storageManager.updateVariable(varStr, resList);
        }

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
        Token varToken = scanner.currentToken;
        String assignStr = scanner.getNext();

        STEntry entry = symbolTable.getSymbol(varStr);

        if (!this.activationRecordStack.isEmpty()) { //if scope is not global
            int scope = this.activationRecordStack.peek().findSymbolScope(varStr);
            if (scope != -1)
                entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(varStr);
        }

        if (entry.primClassif != Classif.EMPTY && ((STIdentifier) entry).structure.equals("array")){ // if operator is an array, branch outta here real quick like the flash ⚡⚡
            res =  assignArrayStmt(varStr);
        } else if (scanner.currentToken.primClassif == Classif.OPERATOR &&
                ( scanner.currentToken.tokenStr.equals("=") || scanner.currentToken.tokenStr.equals("+=")
                        || scanner.currentToken.tokenStr.equals("-=") ) ) {

            scanner.getNext();      //get next token
            res = expr();           //get expression value

            Result oldType =  this.storageManager.getVariable(varStr);

            if (!this.activationRecordStack.isEmpty()) {
                int scope = this.activationRecordStack.peek().findSymbolScope(varStr);
                if (scope != -1) {
                    oldType = this.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(varStr);
                }
            }

            if (!(oldType instanceof ResultValue)) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot perform assignment of array into primitive");
            }
            ResultValue old = (ResultValue) oldType;

            if (!assignStr.equals("=") && old.dataType == SubClassif.EMPTY) {
                throw new ScannerParserException(varToken, scanner.sourceFileNm, "Cannot use operator '" + assignStr + "' on uninitialized variable");
            }

            switch (assignStr) {
                case "+=":
                    res = Utility.add(this
                            , new Numeric(this, old, "+", "add old value with new value")
                            , new Numeric(this, (ResultValue) res, "+", "add old value with new value"));
                    break;
                case "-=":
                    res = Utility.subtract(this
                            , new Numeric(this, old, "-", "subtract old value with new value")
                            , new Numeric(this, (ResultValue) res, "-", "subtract old value with new value"));
                    break;
            }

        } else if (scanner.currentToken.tokenStr.equals("[")) {
            scanner.getNext(); // advance to Expression
            Result indexT;

            ResultValue index = new ResultValue("", SubClassif.EMPTY);
            ResultValue lower = new ResultValue("", SubClassif.EMPTY);
            ResultValue upper = new ResultValue("", SubClassif.EMPTY);

            try {
                entry = symbolTable.getSymbol(varStr);

                if (!this.activationRecordStack.isEmpty()) { //if scope is not global
                    int scope = this.activationRecordStack.peek().findSymbolScope(varStr);
                    if (scope != -1)
                        entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(varStr);
                }

                if (entry.primClassif == Classif.EMPTY) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot index into uninitialized string");
                } else  if (((STIdentifier)entry).dclType != SubClassif.STRING && ((STIdentifier)entry).dclType != SubClassif.DATE) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot index into non array or non string variables");
                }
                ResultValue str;
                if (this.activationRecordStack.isEmpty()) {
                    str = (ResultValue) storageManager.getVariable(varStr);
                }
                else {
                    int scope = this.activationRecordStack.peek().findSymbolScope(varStr);
                    if (scope != -1)
                        str = (ResultValue) this.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(varStr);
                    else
                        str = (ResultValue) storageManager.getVariable(varStr);
                }

                indexT = expr();

                if (indexT instanceof ResultValue) {
                    index = (ResultValue) indexT;
                    if (index.dataType != SubClassif.INTEGER) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Index must be an Integer");
                    }

                    if (!scanner.currentToken.tokenStr.equals("=")) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Assignment statement must be followed by '=' '");
                    }
                } else if (indexT instanceof ResultList) {
                    lower = ((ResultList) indexT).getItem(this, 0);
                    upper = ((ResultList) indexT).getItem(this, 1);

                }

                scanner.getNext(); // advance to next expression must be a string
                res = (ResultValue) expr();


                if (indexT instanceof ResultValue) {
                    res = Utility.assignAtIndex(this, str, (ResultValue) res, Integer.parseInt(index.strValue) );
                } else if (indexT instanceof ResultList) {
                    res = Utility.getStringSliceAssign(this, str, Integer.parseInt(lower.strValue), Integer.parseInt(upper.strValue), (ResultValue) res);
                }


            } catch (PickleException p) {
                throw p;
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
        ResultList array, res;
        ResultValue val, assign;
        Result slice;

        if (this.activationRecordStack.isEmpty()) {
            array = (ResultList) storageManager.getVariable(varString);
        }
        else {
            int scope = this.activationRecordStack.peek().findSymbolScope(varString);
            if (scope != -1)
                array = (ResultList) this.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(varString);
            else
                array = (ResultList) storageManager.getVariable(varString);
        }

        if (scanner.currentToken.tokenStr.equals("=")) { //total array assignment
            scanner.getNext(); //skip to assignee dude guy expr 🤵

            if (scanner.currentToken.subClassif == SubClassif.IDENTIFIER && ((STIdentifier)symbolTable.getSymbol(scanner.currentToken.tokenStr)).structure.equals("array") && scanner.nextToken.tokenStr.equals(";")) { //just an array to array
                if (this.activationRecordStack.isEmpty()) {
                    res = Utility.assignArrayToArray(this, array, (ResultList) storageManager.getVariable(scanner.currentToken.tokenStr));
                }
                else {
                    int scope = this.activationRecordStack.peek().findSymbolScope(scanner.currentToken.tokenStr);
                    if (scope != -1)
                        res = Utility.assignArrayToArray(this, array, (ResultList) this.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(scanner.currentToken.tokenStr));
                    else
                        res = Utility.assignArrayToArray(this, array, (ResultList) storageManager.getVariable(scanner.currentToken.tokenStr));
                }
                scanner.getNext();
            }
            else {
                slice = expr();

                if (slice instanceof ResultValue) {
                    val = (ResultValue) slice;
                    if (val.dataType != array.dataType) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot scalar assign incorrect data type");
                    }
                    res = Utility.assignScalarToArray(this, val, array.capacity);
                } else if (slice instanceof ResultList) {
                    res = Utility.assignArrayToArray(this, array, (ResultList) slice);
                } else {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid assign expression for array");
                }

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
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid operator for array assignment");
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

        if (bPostFix) {
            System.out.printf("Postfix: ");
            for (Token token : out) {
                System.out.printf("%s ", token.tokenStr);
            }
            System.out.println();
        }

        Result ans = Expr.evaluatePostFix(this, out);

        // code to see postfix expression and evaluated answer
        // /*System.out.printf("Evaluated to answer: %s\n", ((ResultValue)ans).strValue);*/

        return ans;


    }

    /**
     * Executes function statements
     * <p></p>
     *
     * @return
     * @throws PickleException
     */
    private ResultValue functionStmt() throws PickleException {
        Result res =  null;

        STEntry entry = this.symbolTable.getSymbol(scanner.currentToken.tokenStr);

        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(scanner.currentToken.tokenStr);
            if (scope != -1)
                entry =  this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(scanner.currentToken.tokenStr);
        }

        if (entry.primClassif == Classif.EMPTY || !(entry instanceof STFunction)) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Can not find function in any scope");
        }

        STFunction func = (STFunction) entry;

        // check for builtin function if not throw error for identifier
        if (func.definedBy == SubClassif.BUILTIN) {
            switch (scanner.currentToken.tokenStr) {
                case "print":
                    res = expr();
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
        else { //user func
            // get parameters into a parameter list, add parameters to function SymbolTable/StorageManager, execute function

            //sets up functions activation record on the stack and its environment vector


            Result p = expr();

            if (!scanner.currentToken.tokenStr.equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Function call must be followed by ';'");
            }

        }

        return (ResultValue) res;
    }

    public Result callUserFunction(STFunction function, ArrayList<Result>parameters) throws PickleException {
        ActivationRecord newAcc = new ActivationRecord(function.record);
        // verify parameter types

        for(int i = 0; i < parameters.size(); i++) {
            // coerce
            STEntry entry;

            SubClassif paramType = function.parameters.get(i);
            Result argT = parameters.get(i);
            Result value;

            // passed identifier or some expression
            if (argT instanceof ResultValue ) {
                ResultValue argI = (ResultValue) argT;
                if (argI.dataType == SubClassif.IDENTIFIER) {
                    // get scoping
                    if (!this.activationRecordStack.isEmpty()) {
                        int scope = this.activationRecordStack.peek().findSymbolScope(argI.strValue);
                        if (scope != -1) {
                            value = this.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(argI.strValue);
                        } else {
                            value = this.storageManager.getVariable(argI.strValue);
                        }
                    } else {
                        value = this.storageManager.getVariable(argI.strValue);
                    }


                    // assign paramaters into activation record storagemanager
                    if ((value instanceof ResultValue) && !function.array.get(i)) {
                        // coerce
                        if (((ResultValue) value).dataType != paramType) {
                            if (((ResultValue) value).dataType == SubClassif.EMPTY) {
                                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid parameter type should be " + paramType.name());
                            }
                            value = Utility.coerce(this, (ResultValue) value, paramType);
                        }
                        newAcc.storageManager.updateVariable(function.names.get(i), value);
                    }
                    if ((value instanceof ResultList) && function.array.get(i)) {
                        // coerce
                        if (((ResultList) value).dataType != paramType) {
                            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid parameter type should be " + paramType.name());
                        }

                        ResultList newList = new ResultList(this, (ArrayList<ResultValue>) ((ResultList) value).arrayList.clone(), ((ResultList) value).capacity, ((ResultList) value).dataType);

                        newAcc.storageManager.updateVariable(function.names.get(i), newList);
                    }


                } else {
                    if (argI.dataType != paramType) {
                        argI = Utility.coerce(this, argI, paramType);
                    }

                    if (argI.dataType == SubClassif.EMPTY) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Was not expecting empty value");
                    }

                    newAcc.storageManager.updateVariable(function.names.get(i), argI);
                }
            } else {
                // passed a Result list implying an array type that needs to be set
                if (!function.array.get(i)) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Was not expecting array for parameter " + function.names.get(i));
                }

                newAcc.storageManager.updateVariable(function.names.get(i), argT);

            }

        }

        //run user function
        int savedSourceLineNr = scanner.currentToken.iSourceLineNr;
        int savedColPos = scanner.currentToken.iColPos;

        this.activationRecordStack.push(newAcc);

        scanner.setPosition(function.lineNum, function.colPos);

        ResultValue returnStatus = statements(iExecMode.EXECUTE);
        Result returnValue = new ResultValue("", SubClassif.EMPTY);

        // end user function
        scanner.setPosition(savedSourceLineNr, savedColPos);
        scanner.getNext();

        // grab values of parameters and assign into whichever scope they need
        newAcc = this.activationRecordStack.pop();

        if (returnStatus.execMode == iExecMode.RETURN) {
            if (function.returnType == SubClassif.VOID && newAcc.returnVal != null) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot return");
            } else if (newAcc.returnVal != null) {

                if (newAcc.returnVal instanceof ResultValue) {
                    if (function.returnArray) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an array to be returned");
                    }
                    if (((ResultValue) newAcc.returnVal).dataType != function.returnType) {
                        returnValue = Utility.coerce(this, (ResultValue) newAcc.returnVal, function.returnType);
                    } else {
                        returnValue = newAcc.returnVal;
                    }
                } else {
                    if (!function.returnArray) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Can not return array as primitive type " + function.returnType);
                    }

                    if ( ((ResultList)newAcc.returnVal).dataType != function.returnType) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Can not return array of different type " + function.returnType);
                    }

                    returnValue = newAcc.returnVal;
                }
            } else if (function.returnType != SubClassif.VOID && newAcc != null ) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Function return type: " + function.returnType.name());
            }
        }
        else if (returnStatus.execMode != iExecMode.RETURN && function.returnType != SubClassif.VOID) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Return Statement not found");
        }



        for (int i =0 ; i < function.numArgs; i++) {
            Result argT = parameters.get(i);

            if (argT instanceof ResultValue) {
                ResultValue arg = (ResultValue) argT;

                if (arg.dataType == SubClassif.IDENTIFIER && function.passing.get(i).equals("Reference")) {
                    Result newValue = newAcc.storageManager.getVariable(function.names.get(i));

                    if (!this.activationRecordStack.isEmpty()) {
                        int scope = this.activationRecordStack.peek().findSymbolScope(arg.strValue);
                        Result value;

                        if (scope != -1) {
                            this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(arg.strValue, newValue);
                        } else {
                            this.storageManager.updateVariable(arg.strValue, newValue);
                        }

                    } else {
                        this.storageManager.updateVariable(arg.strValue, newValue);
                    }

                }
            }
        }


        return returnValue;
    }


    public void print(ArrayList<Result> parameters) throws PickleException {
        StringBuilder sb = new StringBuilder();

        for (Result val : parameters) {
            if (val instanceof ResultValue) {
                if (((ResultValue) val).dataType == SubClassif.IDENTIFIER) {
                    STEntry entry = this.symbolTable.getSymbol(((ResultValue) val).strValue);

                    if (!this.activationRecordStack.isEmpty()) {
                        int scope = this.activationRecordStack.peek().findSymbolScope(((ResultValue) val).strValue);
                        if (scope != -1)
                            entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(((ResultValue) val).strValue);
                    }

                    if (entry.primClassif == Classif.EMPTY) {
                        scanner.currentToken.tokenStr = ((ResultValue) val).strValue;
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot call print on unknown identifier");
                    }

                    val = this.storageManager.getVariable(entry.symbol);

                    if (!this.activationRecordStack.isEmpty()) {
                        int scope = this.activationRecordStack.peek().findSymbolScope(entry.symbol);
                        if (scope != -1)
                            val = this.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(entry.symbol);
                    }

                }
            }




            sb.append(val.printResult());
            sb.append(" ");
        }

        System.out.println(sb.toString());
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

            // if reached separator skip token
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
            ArrayList<Result> param = new ArrayList<Result>();
            param.add(res);
            res = Utility.builtInLENGTH(param);
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

        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(varStr);
            if (scope != -1)
                entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(varStr);
        }

        if (entry.primClassif == Classif.EMPTY) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot assign value to undeclared variable");
        }

        //STIdentifier symbolEntry = (STIdentifier) this.symbolTable.getSymbol(varStr);  caleb you good??? stop calling *MY* symbol table twice for same string, could have just (see below lmao) 😒
        STIdentifier symbolEntry = (STIdentifier) entry;

        if (res instanceof ResultValue) {
            // conversion from specified types to declared type
            if (symbolEntry.dclType == SubClassif.FLOAT) {
                res = Utility.castNumericToDouble(this, new Numeric(this, (ResultValue) res, "", "cast to declared type"));
            }
            if (symbolEntry.dclType == SubClassif.INTEGER) {
                res = Utility.castNumericToInt(this, new Numeric(this, (ResultValue) res, "", "cast to declared type"));
            }

            if (symbolEntry.dclType != ((ResultValue) res).dataType)
                res = Utility.coerce(this, (ResultValue) res, symbolEntry.dclType);
        }

        // store value
        if (this.activationRecordStack.isEmpty()) {
            this.storageManager.updateVariable(varStr, res);
        }
        else {
            int scope = this.activationRecordStack.peek().findSymbolScope(varStr);
            if (scope != -1)
                this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(varStr, res);
            else
                this.storageManager.updateVariable(varStr, res);
        }


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

        // check if debug type of statement
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
                case "Postfix":
                    bPostFix = onOff;
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
     * @param execMode             Execute Flag
     * @return
     * @throws PickleException
     */
    private ResultValue statements(iExecMode execMode) throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);
        res.execMode = execMode;
        //exec statements
        if (res.execMode == iExecMode.EXECUTE) {
            while (hasNext() && scanner.nextToken.subClassif != SubClassif.END && res.execMode == iExecMode.EXECUTE) {
                res = getNext(execMode);
            }

            if (res.execMode != execMode) {
                execMode = res.execMode;
                res = statements(iExecMode.IGNORE_EXEC);
                res.execMode = execMode;
                return res;
            }

            res.terminatingString = scanner.getNext();
        }
        else {

            while (hasNext() && scanner.currentToken.subClassif != SubClassif.END) {

                if (scanner.currentToken.primClassif == Classif.SEPARATOR)
                    scanner.getNext();

                if (scanner.currentToken.subClassif == SubClassif.END)
                    break;

                if (scanner.currentToken.primClassif == Classif.CONTROL && scanner.currentToken.subClassif == SubClassif.FLOW) {
                    controlStmt(execMode);
                } else {
                    Utility.skipTo(scanner, ";");
                }
            }

            res.terminatingString = scanner.currentToken.tokenStr;
        }
        

        return res;
    }

    private void userFunction(iExecMode execMode) throws PickleException {
        //current token is def, so next token will be return type
        int currLineNum = scanner.iSourceLineNr;
        int colPos = scanner.iColPos;
        int numArgs = 0;
        boolean returnArray = false;

        scanner.getNext(); //skip the 'def' token, current token now should be the return value
        SubClassif returnValue = getDataType(scanner.currentToken.tokenStr);

        if (scanner.nextToken.tokenStr.equals("[")) {
            scanner.getNext();
            if (!scanner.getNext().equals("]")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid Return type");
            }
            returnArray = true;
        }

        String functionName = scanner.getNext(); //advance token to function name and set string accordingly

        if (!scanner.getNext().equals("(")) { //if the next token is not the start of the parameter list, well thats no good bubby 🤷‍
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "missing '('");
        }

        ArrayList<SubClassif> types = new ArrayList<SubClassif>();
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Boolean> array = new ArrayList<Boolean>();
        ArrayList<String> passing = new ArrayList<String>();
        //get all the parameters

        // starts at (
        while (!scanner.getNext().equals(":")) {

            if (scanner.currentToken.tokenStr.equals(")")) {
                continue;
            }

            array.add(false);
            numArgs++;
            types.add(paramHelper(names, array, passing));
        }

        if (execMode == iExecMode.EXECUTE) {
            STFunction newUserFcn = new STFunction(functionName, Classif.FUNCTION, returnValue, returnArray, SubClassif.USER, numArgs, currLineNum, colPos, types, names, array, passing);

            //set up functions activation record
            if (!this.activationRecordStack.isEmpty()) {
                newUserFcn.setupActivationRecord(this.activationRecordStack.peek());
            } else {
                newUserFcn.record = new ActivationRecord(newUserFcn.symbol);
            }

            newUserFcn.setupSymbolTable();

            if (this.activationRecordStack.isEmpty()) {
                this.symbolTable.putSymbol(functionName, newUserFcn);
            } else {
                this.activationRecordStack.peek().symbolTable.putSymbol(functionName, newUserFcn);

            }

            //parse out the function body

            newUserFcn.lineNum = scanner.iSourceLineNr;
            newUserFcn.colPos = 0;
            ResultValue res = statements(iExecMode.IGNORE_EXEC); // dont execute the statements on function definition

            if (!res.terminatingString.equals("enddef")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing enddef");
            }

            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';'");
            }
        } else {
            ResultValue rest = statements(execMode);

            if (!rest.terminatingString.equals("enddef")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Function missing enddef statement");
            }

            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "'enddef' msut be followed by ';'");
            }
        }

    }

    private SubClassif paramHelper(ArrayList<String> names, ArrayList<Boolean> array, ArrayList<String> passing) throws PickleException {
        SubClassif paramType = SubClassif.EMPTY;
        //the parameter is either a primitive or an array with a pair of square brackets
        // current token should be an identifier

        //Could be 'Ref', 'Value' or and Identifier
        if (scanner.currentToken.tokenStr.equals("Value") || scanner.currentToken.tokenStr.equals("Ref")) {
            if (scanner.currentToken.tokenStr.equals("Value")) {
                //do something
                passing.add("Value");
            }
            else {
                passing.add("Reference");
            }
            scanner.getNext(); //if it was 'Value' or 'Ref' then skip
        }
        else {
            passing.add("Reference");
        }

        //if there is no type for the parameter
        if (scanner.currentToken.primClassif != Classif.CONTROL || scanner.currentToken.subClassif != SubClassif.DECLARE) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected Identifier, found: " + scanner.currentToken.tokenStr);
        }
        paramType = getDataType(scanner.currentToken.tokenStr);

        scanner.getNext(); //advance to next token, should be a variable name

        //if there is no identifier name for the var
        if (scanner.currentToken.primClassif != Classif.OPERAND || scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected variable name, found: " + scanner.currentToken.tokenStr);
        }

        names.add(scanner.currentToken.tokenStr);

        //if the variable is an array
        if (scanner.nextToken.tokenStr.equals("[")) {

            int i = array.size() -1;
            array.set(i, true);

            scanner.getNext(); // move token to '['
            if (!scanner.getNext().equals("]")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "expected ']', found: " + scanner.currentToken.tokenStr);
            }
            //else if (scanner.nextToken.tokenStr.equals((","))) {
            else if (scanner.getNext().equals((",")) || scanner.currentToken.tokenStr.equals(")")) {
                return paramType;
            }
            else {
                throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "expected separator, found: " + scanner.nextToken.tokenStr);
            }

        }
        //else if (scanner.nextToken.equals(",")) {
        else if (scanner.getNext().equals((",")) || scanner.currentToken.tokenStr.equals(")")) {
            return paramType;
        }
        else {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "expected separator, found: " + scanner.currentToken.tokenStr);
        }
    }

    /**
     * Executes select statements
     * <p></p>
     *
     * @param execMode
     * @return  ResultValue
     */
    private ResultValue selectStmt(iExecMode execMode) throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);
        ResultValue returnMode = new ResultValue("", SubClassif.EMPTY);
        returnMode.execMode = execMode;

        scanner.getNext(); // skip to the control variable

        Result var = expr(); //get the value of the control variable

        if (!(var instanceof ResultValue)) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Control variable for select statement cannot be a list");
        }

        ResultValue controlVar = (ResultValue) var;

        if (!scanner.currentToken.tokenStr.equals(":")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "select statement missing ':' ");
        }

        scanner.getNext(); //skip to first when

        //loop through all the selects until a default or endselect is hit
        while (scanner.currentToken.tokenStr.equals("when")) {
            res = whenStmt(execMode, controlVar);
            if (res.execMode != iExecMode.IGNORE_EXEC) {
                execMode = iExecMode.IGNORE_EXEC;
                returnMode.execMode = res.execMode;
            }
        }

        if (!scanner.currentToken.tokenStr.equals("default") && !scanner.currentToken.tokenStr.equals("endselect")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "expected endselect or default statement");
        }

        if (scanner.currentToken.tokenStr.equals("default")) {
            res = defaultStmt(execMode);
            if (res.execMode != execMode) {
                returnMode.execMode = res.execMode;
            }
            if (!res.terminatingString.equals("endselect")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "expected endselect statement");
            }
        }

        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing ';'");
        }

        res.execMode = returnMode.execMode;
        return res;
    }

    /**
     * Executes when statements
     * <p></p>
     *
     * @param execMode
     * @return ResultValue
     */
    private ResultValue whenStmt(iExecMode execMode, ResultValue controlVar) throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        scanner.getNext(); //skip to the value to compare

        while (!scanner.currentToken.tokenStr.equals(":")) {
            Result cond = expr();
            if (!(cond instanceof ResultValue)) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "when statements must not include a list");
            }

            ResultValue compare = (ResultValue) cond;

            if (compare.dataType != controlVar.dataType) {
                compare = Utility.coerce(this, compare, controlVar.dataType);
            }

            if (compare.dataType == SubClassif.EMPTY) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Error coercing data types");
            }

            if (Utility.equal(this, compare, controlVar).strValue.equals("T")) {
                if (!scanner.currentToken.tokenStr.equals(":"))
                    Utility.skipTo(scanner,":");
                res = statements(execMode);
                res.dataType = SubClassif.VOID;
                break;
            }

            if (scanner.currentToken.tokenStr.equals(",")) {
                scanner.getNext();
            }
            else if (!scanner.currentToken.tokenStr.equals(":")){
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "err");
            }

        }

        if (res.dataType == SubClassif.EMPTY) {
            res = statements(iExecMode.IGNORE_EXEC);
        }

        if (!res.terminatingString.equals("default") && !res.terminatingString.equals("when") && !res.terminatingString.equals("endselect")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "expected select statement end flow, found");
        }

        return res;
    }

    /**
     * Executes default statements
     * <p></p>
     *
     * @param execMode
     * @return ResultValue
     */
    private ResultValue defaultStmt(iExecMode execMode) throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);

        if (!scanner.getNext().equals(":")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected ':', found");
        }

        res = statements(execMode);

        return res;
    }

    /**
     * Parses and executes while statement based on execute flag
     * <p></p>
     *
     * @param execMode             Execute flag
     * @return
     * @throws PickleException
     */
    private ResultValue whileStmt(iExecMode execMode) throws PickleException {

        //set while position
        Token entryPosition = scanner.currentToken;
        ResultValue result = new ResultValue("", SubClassif.EMPTY);
        result.execMode = execMode;

        //execute while statement
        if (execMode == iExecMode.EXECUTE) {
            //while contition is true execute stmts
             while (evalCond().strValue.equals("T") &&
                     (result.execMode == iExecMode.EXECUTE || result.execMode == iExecMode.CONTINUE_EXEC) ) {
                 result = statements(execMode);

                 /*if (result.execMode == iExecMode.BREAK_EXEC) {
                     break;
                 } else if (result.execMode == iExecMode.CONTINUE_EXEC && !result.terminatingString.equals("endwhile")) {
                     result = statements(iExecMode.IGNORE_EXEC);
                 }*/


                 if(!result.terminatingString.equals("endwhile")) {
                     throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile:");
                 }



                 //set position back to while
                 scanner.setPosition(entryPosition.iSourceLineNr, entryPosition.iColPos);
                 scanner.getNext();
             }
             //after while loop's condition is false, get back to the endwhile token

            // get rest of while loop body
            // consume break or continue execute and return execute mode passed
            result = statements(iExecMode.IGNORE_EXEC);
            result.execMode = execMode;

            if (!result.terminatingString.equals("endwhile")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile :");
            }
            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';':");
            }

        }
        else { //dont execute while
            Utility.skipTo(scanner, ":");
            result = statements(iExecMode.IGNORE_EXEC);
            if(!result.terminatingString.equals("endwhile")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endwhile:");
            }
            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';':");
            }
        }

        //reset exec mode in event break or continue was returned on last iteration of while loop to consume break/continue
        result.execMode = execMode;


        return result;
    }

    /**
     * Parses and executes
     * @param execMode
     * @return
     * @throws PickleException
     */
    private ResultValue ifStmt(iExecMode execMode) throws PickleException {

        ResultValue resCond;
        ResultValue resTemp;

        ResultValue returnExec = new ResultValue("", SubClassif.EMPTY);
        returnExec.execMode = execMode;
        
        if (execMode == iExecMode.EXECUTE) {

            resCond = evalCond();
            
            if (resCond.strValue.equals("T")) {
                // execute if block statements and skip else block if present

                resTemp = statements(execMode);

                if (resTemp.execMode != iExecMode.EXECUTE) {
                    returnExec.execMode = resTemp.execMode;

                    if (!resTemp.terminatingString.equals("else") && !resTemp.terminatingString.equals("endif"))
                        resTemp = statements(resTemp.execMode);

                }
                
                if (resTemp.terminatingString.equals("else")) {
                    if (!scanner.getNext().equals(":")) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an ':' after an 'else' token:");
                    }

                    resTemp = statements(iExecMode.IGNORE_EXEC);
                    
                }

                // ensure if statement ends with endif token


            } else {

                // skip if block and execute else statements

                resTemp = statements(iExecMode.IGNORE_EXEC);

                if (resTemp.terminatingString.equals("else")) {
                    if (!scanner.getNext().equals(":")) {
                        throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an ':' after an 'else' token:");
                    }

                    resTemp = statements(execMode);

                    if (resTemp.execMode != execMode) {
                        returnExec.execMode = resTemp.execMode;

                        resTemp = statements(resTemp.execMode);

                    }
                }

                // ensure if statement ends with endif token

            }
            if (!resTemp.terminatingString.equals("endif")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an 'endif' after ':' ");
            }

        } else {
            // do not execute any statements for if or else block

            Utility.skipTo(scanner, ":");

            resTemp = statements(execMode);

            if (resTemp.terminatingString.equals("else")) {
                if (!scanner.getNext().equals(":")) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an ':' after an 'else' token:");
                }
                resTemp = statements(execMode);


            }

            if (!resTemp.terminatingString.equals("endif")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected an 'endif' after 'if' token:");
            }

        }
        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected a ';' after 'endif' token");
        }


        return returnExec;

    }

    private ResultValue forStmt(iExecMode execMode) throws PickleException {
        ResultValue result = new ResultValue("", SubClassif.EMPTY);

        if (execMode == iExecMode.EXECUTE) {
            scanner.getNext();
            if (scanner.currentToken.primClassif != Classif.OPERAND && scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected operand, found");
            }
            String controlVar = scanner.currentToken.tokenStr;
            scanner.getNext();
            if (scanner.currentToken.tokenStr.equals("=")) {
                result = countingFor(controlVar, execMode);
            }
            else if (scanner.currentToken.tokenStr.equals("in")) {
                //branch to either charStringFor() or itemArrayFor() depending on if controlVar is a char
                try {
                    STEntry entry = this.symbolTable.getSymbol(scanner.nextToken.tokenStr);
                    if (!this.activationRecordStack.isEmpty()) {
                        int scope = this.activationRecordStack.peek().findSymbolScope(scanner.nextToken.tokenStr);
                        if (scope != -1)
                            entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(scanner.nextToken.tokenStr);
                    }

                    if (scanner.nextToken.subClassif == SubClassif.IDENTIFIER) {
                        //STIdentifier entry = (STIdentifier) this.symbolTable.getSymbol(scanner.nextToken.tokenStr);
                        //STEntry entry = this.symbolTable.getSymbol(scanner.nextToken.tokenStr);

                        if (entry.primClassif != Classif.EMPTY) {
                            STIdentifier id = (STIdentifier) entry;
                            if ((id.dclType == SubClassif.STRING || id.dclType == SubClassif.DATE) && !id.structure.equals("array")) {
                                result = charStringFor(controlVar, execMode);
                            } else if (id.structure.equals("array")) {
                                result = itemArrayFor(controlVar, execMode);
                            } else {
                                throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Identifier must be of type String, Int, Float to use for loop");
                            }
                        } else {
                            throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Cannot find value for Identifier");
                        }
                    }else if (scanner.nextToken.subClassif == SubClassif.STRING || scanner.nextToken.subClassif == SubClassif.DATE) {
                        result = charStringFor(controlVar, execMode);
                    } else {
                        throw new ScannerParserException(scanner.nextToken, scanner.sourceFileNm, "Cannot run for loop on constant value " + scanner.nextToken.subClassif.name());
                    }

                } catch (PickleException p) {
                    throw p;
                } catch (Exception e) {
                    throw e;
                }

            }
            else if (scanner.currentToken.tokenStr.equals("from")) {
                result = stringDelimiterFor(controlVar, execMode);
            }
            else {
                //TODO: fix exception - for loop type not recognized error or smth man idk
                throw new PickleException();
            }
        } else {
            Utility.skipTo(scanner, ":");
            result = statements(execMode);

            if (!result.terminatingString.equals("endfor")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Missing endfor");
            }

            if (!scanner.getNext().equals(";")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Statement must end in ';'");
            }
        }

        return result;
    }

    private ResultValue charStringFor(String controlVar, iExecMode execMode) throws PickleException {
        STEntry entry = symbolTable.getSymbol(controlVar);

        ResultValue result = new ResultValue("", SubClassif.EMPTY);
        result.execMode = execMode;

        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);
            if (scope != -1)
                entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(controlVar);
        }

        if (entry.primClassif == Classif.EMPTY) {
            if (!this.activationRecordStack.isEmpty()) {
                this.activationRecordStack.peek().symbolTable.putSymbol(
                        controlVar,
                        new STIdentifier(controlVar,
                                Classif.OPERAND,
                                SubClassif.STRING,
                                "none",
                                "local",
                                0)
                );
            } else {
                symbolTable.putSymbol(controlVar,
                        new STIdentifier(controlVar,
                                Classif.OPERAND,
                                SubClassif.STRING,
                                "none",
                                "local",
                                0));
            }
        }

        scanner.getNext();

        Result end = expr(); //get value of limit
        ResultValue limit;

        if (end instanceof ResultValue) {
            limit = (ResultValue) end;
        } else {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot use for char loop over array");
        }

        /*if (limit.dataType != SubClassif.STRING) {
            //TODO fix exception - limit type needs to be of a string
            throw new PickleException();
        }*/

        ResultValue startValue = Utility.valueAtIndex(this, limit, 0); //set up iterating char value

        if (!scanner.currentToken.tokenStr.equals(":")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected ':', found");
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

        while(Utility.lessThan(this, currPos, maxPos).strValue.equals("T") &&
                (result.execMode == iExecMode.EXECUTE || result.execMode == iExecMode.CONTINUE_EXEC)) {
            // evaluate loop statements
            result = statements(execMode);

            if (!result.terminatingString.equals("endfor")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected 'endfor', found");
            }

            nOp1 = new Numeric(this, currPos, "+", "Incrementing char pos");


            currPos = Utility.add(this, nOp1, nOp2);

            if (Integer.parseInt(currPos.strValue) < Integer.parseInt(maxPos.strValue)) {
                startValue = Utility.valueAtIndex(this, limit, Integer.parseInt(currPos.strValue)); //increment char value
                if (this.activationRecordStack.isEmpty()) {
                    storageManager.updateVariable(controlVar, startValue);
                }
                else {
                    int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);
                    if (scope != -1)
                        this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(controlVar, startValue);
                    else
                        storageManager.updateVariable(controlVar, startValue);
                }
            }

            scanner.setPosition(iSavedLineNr, iSavedColPos);
            scanner.getNext();
        }

        result = statements(iExecMode.IGNORE_EXEC);
        result.execMode = execMode;

        if (!result.terminatingString.equals("endfor")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected 'endfor', found");
        }

        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected ';', found");
        }

        return result;
    }

    private ResultValue itemArrayFor(String controlVar, iExecMode execMode) throws  PickleException {
        STEntry entry = this.symbolTable.getSymbol(controlVar);
        ResultValue result = new ResultValue("", SubClassif.EMPTY);
        result.execMode = execMode;
        ResultValue startValue; //set up iterating char value
        ResultList limit;

        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);
            if (scope != -1)
                entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(controlVar);
        }

        scanner.getNext();

        Result end = expr();

        if (end instanceof ResultList) {
            limit = (ResultList) end;
        } else {
            throw new PickleException();
        }

        if (entry.primClassif == Classif.EMPTY) {
            if (!this.activationRecordStack.isEmpty()) {
                this.activationRecordStack.peek().symbolTable.putSymbol(
                            controlVar,
                            new STIdentifier(controlVar,
                                    Classif.OPERAND,
                                    limit.dataType,
                                    "none",
                                    "local",
                                    0)
                    );
            } else {
                symbolTable.putSymbol(controlVar,
                        new STIdentifier(controlVar,
                                Classif.OPERAND,
                                limit.dataType,
                                "none",
                                "local",
                                0));
            }
        } else {

            //STIdentifier id = (STIdentifier) symbolTable.getSymbol(controlVar);
            STIdentifier id = (STIdentifier) entry;

            if (limit.dataType != id.dclType) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot use variable of non " + limit.dataType.name() + " type");
            }
        }

        if (!scanner.currentToken.tokenStr.equals(":")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected ':', found");
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

        if (this.activationRecordStack.isEmpty()) {
            storageManager.updateVariable(controlVar, startValue);
        }
        else {
            int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);
            if (scope != -1)
                this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(controlVar, startValue);
            else
                storageManager.updateVariable(controlVar, startValue);
        }

        while(Utility.lessThan(this, currPos, maxPos).strValue.equals("T") &&
                (result.execMode == iExecMode.EXECUTE || result.execMode == iExecMode.CONTINUE_EXEC)) {
            // evaluate loop statements
            result = statements(execMode);

            if (!result.terminatingString.equals("endfor")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected 'endfor', found");
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

                if (Integer.parseInt(currPos.strValue) < Integer.parseInt(maxPos.strValue)) {
                    if (this.activationRecordStack.isEmpty()) {
                        storageManager.updateVariable(controlVar, startValue);
                    }
                    else {
                        int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);
                        if (scope != -1)
                            this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(controlVar, startValue);
                        else
                            storageManager.updateVariable(controlVar, startValue);
                    }
                }
            }

            scanner.setPosition(iSavedLineNr, iSavedColPos);
            scanner.getNext();
        }

        result = statements(iExecMode.IGNORE_EXEC);
        result.execMode = execMode;

        if (!result.terminatingString.equals("endfor")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected 'endfor', found");
        }

        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected ':', found");
        }

        return result;
    }

    private ResultValue stringDelimiterFor(String controlVar, iExecMode execMode) throws  PickleException {
        STEntry entry = symbolTable.getSymbol(controlVar);

        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);

            if (scope != -1)
                entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(controlVar);
        }


        ResultValue result = new ResultValue("", SubClassif.EMPTY);
        result.execMode = execMode;

        if (entry.primClassif == Classif.EMPTY) {
            if (!this.activationRecordStack.isEmpty()) {
                this.activationRecordStack.peek().symbolTable.putSymbol(
                        controlVar,
                        new STIdentifier(controlVar,
                                Classif.OPERAND,
                                SubClassif.STRING,
                                "none",
                                "local",
                                0)
                );
            } else {
                symbolTable.putSymbol(controlVar,
                        new STIdentifier(controlVar,
                                Classif.OPERAND,
                                SubClassif.STRING,
                                "none",
                                "local",
                                0));
            }
        }

        scanner.getNext(); //skips the 'from' token

        Result string = expr();

        //string to tokenize err checking
        if (!(string instanceof ResultValue)) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Variable to tokenize must be a String");
        }
        if (((ResultValue) string).dataType != SubClassif.STRING) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Variable to tokenize must be a String");
        }

        if (!scanner.currentToken.tokenStr.equals("by")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected \"by\", found " + scanner.currentToken.tokenStr);
        }

        scanner.getNext(); //skips the 'by' token

        Result delimiter = expr();

        if (delimiter instanceof ResultValue) {
            if (((ResultValue) delimiter).dataType != SubClassif.STRING) {
                delimiter = Utility.coerce(this,(ResultValue) delimiter, SubClassif.STRING);
            }

            if (((ResultValue) delimiter).dataType == SubClassif.EMPTY) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Cannot use empty string for delimiter");
            }
        } else {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid delimiter");
        }



        if (!scanner.currentToken.tokenStr.equals(":")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected \":\" found " + scanner.currentToken.tokenStr);
        }

        int iSavedLineNr = scanner.currentToken.iSourceLineNr;
        int iSavedColPos = scanner.currentToken.iColPos;

        String s = ((ResultValue) string).strValue;
        String d = ((ResultValue) delimiter).strValue;

        String[] tokens = s.split(d);

        ResultValue limit = new ResultValue(Integer.toString(tokens.length), SubClassif.INTEGER);
        ResultValue itr = new ResultValue("0", SubClassif.INTEGER);

        ResultValue update = new ResultValue(tokens[0], SubClassif.STRING);
        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);

            if (scope != -1)
                this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(controlVar, update);
            else
                this.storageManager.updateVariable(controlVar, update);
        } else {
            this.storageManager.updateVariable(controlVar, update);
        }

        Numeric nOp1;
        Numeric nOp2 = new Numeric(this, new ResultValue("1", SubClassif.INTEGER), "+", "Incrementer for the for loop");

        while (Utility.lessThan(this, itr, limit).strValue.equals("T") &&
                (result.execMode == iExecMode.EXECUTE || result.execMode == iExecMode.CONTINUE_EXEC)) {

            update =  new ResultValue(tokens[Integer.parseInt(itr.strValue)], SubClassif.STRING);

            if (!this.activationRecordStack.isEmpty()) {
                int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);

                if (scope != -1)
                    this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(controlVar, update);
                else
                    this.storageManager.updateVariable(controlVar, update);
            } else {
                this.storageManager.updateVariable(controlVar, update);
            }

            result = statements(execMode);

            if (!result.terminatingString.equals("endfor")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected 'endfor', found");
            }

            nOp1 = new Numeric(this, itr, "+", "Incrementing itr");
            itr = Utility.add(this, nOp1, nOp2);

            scanner.setPosition(iSavedLineNr, iSavedColPos);
            scanner.getNext();
        }

        result = statements(iExecMode.IGNORE_EXEC);
        result.execMode = execMode;

        if (!result.terminatingString.equals("endfor")) {
            throw new PickleException();
        }

        if (!scanner.getNext().equals(";")) {
            throw new PickleException();
        }

        return result;
    }

    private ResultValue countingFor(String controlVar, iExecMode execMode) throws PickleException {
        STEntry entry = symbolTable.getSymbol(controlVar);
        ResultValue result = new ResultValue("", SubClassif.EMPTY);
        result.execMode = execMode;
        ResultValue startValue;
        ResultValue limit;
        ResultValue incrementBy = new ResultValue("1", SubClassif.INTEGER);

        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);
            if (scope != -1)
                entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(controlVar);
        }

        if (entry.primClassif == Classif.EMPTY) {
            if (!this.activationRecordStack.isEmpty()) {
                this.activationRecordStack.peek().symbolTable.putSymbol(
                        controlVar,
                        new STIdentifier(controlVar,
                                Classif.OPERAND,
                                SubClassif.INTEGER,
                                "none",
                                "local",
                                0)
                );
            } else {
                symbolTable.putSymbol(controlVar,
                        new STIdentifier(controlVar,
                                Classif.OPERAND,
                                SubClassif.INTEGER,
                                "none",
                                "local",
                                0));
            }
        }

        scanner.getNext();
        
        if (scanner.currentToken.primClassif != Classif.OPERAND &&
                (scanner.currentToken.subClassif != SubClassif.FLOAT || scanner.currentToken.subClassif != SubClassif.INTEGER)) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Error attempted to assign non-numeric");
        }

        try {
            startValue = (ResultValue) expr();

            if (!scanner.currentToken.tokenStr.equals("to")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected 'to', found");
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
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Increment must be positive integer");
        }


        if (!scanner.currentToken.tokenStr.equals(":")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected ':', found");
        }
        int iSavedLineNr = scanner.currentToken.iSourceLineNr;
        int iSavedColPos = scanner.currentToken.iColPos;

        if (this.activationRecordStack.isEmpty()) {
            storageManager.updateVariable(controlVar, startValue);
        }
        else {
            int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);
            if (scope != -1)
                this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(controlVar, startValue);
            else
                storageManager.updateVariable(controlVar, startValue);
        }


        Numeric nOp1;
        Numeric nOp2;

        nOp2 = new Numeric(this, incrementBy, "+", "Incrementing by value");


        while (Utility.lessThan(this, startValue, limit).strValue.equals("T") &&
                (result.execMode == iExecMode.EXECUTE || result.execMode == iExecMode.CONTINUE_EXEC)) {
            // evaluate statments
            result = statements(execMode);

            if (!result.terminatingString.equals("endfor")) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected 'endfor', found");
            }

            nOp1 = new Numeric(this, startValue, "+", "Incrementing startValue");


            startValue = Utility.add(this, nOp1, nOp2);

            if (this.activationRecordStack.isEmpty()) {
                storageManager.updateVariable(controlVar, startValue);
            }
            else {
                int scope = this.activationRecordStack.peek().findSymbolScope(controlVar);
                if (scope != -1)
                    this.activationRecordStack.peek().environmentVector.get(scope).storageManager.updateVariable(controlVar, startValue);
                else
                    storageManager.updateVariable(controlVar, startValue);
            }

            scanner.setPosition(iSavedLineNr, iSavedColPos);
            scanner.getNext();
        }

        // get rest of for loop body
        // consume whether break or continue hit and pass execMode out to caller
        result = statements(iExecMode.IGNORE_EXEC);
        result.execMode = execMode;

        if (!result.terminatingString.equals("endfor")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected 'endfor', found");
        }

        if (!scanner.getNext().equals(";")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected ';', found");
        }

        return result;

    }

    private ResultValue inNotIn() throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);
        if (scanner.currentToken.primClassif != Classif.OPERAND || scanner.currentToken.subClassif != SubClassif.IDENTIFIER) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected Identifier found " + scanner.currentToken.tokenStr);
        }

        String identifier = scanner.currentToken.tokenStr;

        STEntry entry = this.symbolTable.getSymbol(identifier);
        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(identifier);
            if (scope != -1)
                entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(identifier);
        }

        if (entry.primClassif == Classif.EMPTY) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Identifier not found");
        }

        if (!(entry instanceof STIdentifier)) {
            throw new PickleException();
        }

        Result value = this.storageManager.getVariable(identifier);
        if (!this.activationRecordStack.isEmpty()) {
            int scope = this.activationRecordStack.peek().findSymbolScope(identifier);
            if (scope != -1)
                value = this.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(identifier);
        }

        if (!(value instanceof ResultValue)) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected primitive, found");
        }

        String inORout = scanner.getNext(); // set string for later utility usage

        if (!inORout.equals("IN") && !inORout.equals("NOTIN")) {
            throw new PickleException();
        }

        scanner.getNext();

        ResultList list = new ResultList(this, new ArrayList<ResultValue>(), 0, scanner.nextToken.subClassif);
        ArrayList<ResultValue> arrayList = new ArrayList<ResultValue>();

        if (scanner.currentToken.primClassif == Classif.OPERAND && scanner.currentToken.subClassif == SubClassif.IDENTIFIER && !scanner.currentToken.tokenStr.equals("{")) { //TODO '{' is OPERAND IDENTIFIER?
            entry = this.symbolTable.getSymbol(scanner.currentToken.tokenStr);
            if (!this.activationRecordStack.isEmpty()) {
                int scope = this.activationRecordStack.peek().findSymbolScope(scanner.currentToken.tokenStr);
                if (scope != -1)
                    entry = this.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(scanner.currentToken.tokenStr);
            }

            if (entry.primClassif == Classif.EMPTY) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "valueList not initialized");
            }

            Result l = this.storageManager.getVariable(entry.symbol);
            if (!this.activationRecordStack.isEmpty()) {
                int scope = this.activationRecordStack.peek().findSymbolScope(entry.symbol);
                if (scope != -1)
                    l = this.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(entry.symbol);
            }

            if (l instanceof ResultValue) {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "value must be a list");
            }
            else if (l instanceof ResultList) {
                list = (ResultList) l;
            }
            else {
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "value must be a list");
            }

        }
        else if (!scanner.currentToken.tokenStr.equals("{")) {
            throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Expected start of list (\"{\") found");
        }
        else {
            //now we hit that list bby
            //loop through 'em
            while (!scanner.currentToken.tokenStr.equals("}")) {
                scanner.getNext();

                if (scanner.currentToken.primClassif != Classif.OPERAND || scanner.currentToken.subClassif != list.dataType) {
                    throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "List element data type conflict");
                }

                arrayList.add(new ResultValue(scanner.currentToken.tokenStr, scanner.currentToken.subClassif));

                scanner.getNext();
            }

            list.arrayList = arrayList;
            list.allocatedSize = arrayList.size();
            list.capacity = arrayList.size();
        }
        if (inORout.equals("IN")) {
            res = Utility.builtInIN(this, (ResultValue) value, list);
        }
        else {
            res = Utility.builtInNOTIN(this, (ResultValue) value, list);
        }

        scanner.getNext(); //skip to the ':' token

        return res;
    }

    private ResultValue evalCond() throws PickleException {
        ResultValue res = new ResultValue("", SubClassif.EMPTY);
        scanner.getNext();
        try {
            if (scanner.nextToken.tokenStr.equals("IN") || scanner.nextToken.tokenStr.equals("NOTIN")) {
                res = inNotIn();
            }
            else {
                res = (ResultValue) expr();
            }
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
            getNext(iExecMode.EXECUTE);
        }
    }

    /**
     * Helper function to get declared type for identifier
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
            case "Void":
                return SubClassif.VOID;
            default:
                throw new ScannerParserException(scanner.currentToken, scanner.sourceFileNm, "Invalid Declaration type");
        }
    }
}
