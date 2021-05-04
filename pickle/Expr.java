package pickle;

import java.util.ArrayList;
import java.util.Stack;

public class Expr {

    public static ArrayList<Token> postFixExpr(Parser parser) throws PickleException {
        ArrayList<Token> postfix = new ArrayList<Token>();
        Stack<Token> stack = new Stack<Token>();

        int funcBool = 0;
        int sliceI = 0;
        int expr = 0;

        int size = 0;

        while(     !(parser.scanner.currentToken.tokenStr.equals(";") && parser.scanner.currentToken.subClassif != SubClassif.STRING)
                && !(parser.scanner.currentToken.tokenStr.equals(":") && parser.scanner.currentToken.subClassif != SubClassif.STRING)
                && !(parser.scanner.currentToken.tokenStr.equals("to") && parser.scanner.currentToken.subClassif != SubClassif.STRING)
                && !(parser.scanner.currentToken.tokenStr.equals("by") && parser.scanner.currentToken.subClassif != SubClassif.STRING)
                && !(parser.scanner.currentToken.tokenStr.equals("=") && parser.scanner.currentToken.subClassif != SubClassif.STRING)) {

            if (funcBool == 0 && parser.scanner.currentToken.tokenStr.equals(",") && parser.scanner.currentToken.subClassif != SubClassif.STRING) {
                break;
            }

            switch (parser.scanner.currentToken.primClassif) {
                case OPERAND:
                    //System.out.printf("Outing operand '%s' onto stack\n", parser.scanner.currentToken.tokenStr);
                    Token temp;
                    if (parser.scanner.nextToken.tokenStr.equals("[") && parser.scanner.currentToken.subClassif == SubClassif.IDENTIFIER) {
                        parser.scanner.currentToken.tokenStr = parser.scanner.currentToken.tokenStr.concat(parser.scanner.nextToken.tokenStr);
                        parser.scanner.currentToken.operatorPrecedence = OperatorPrecedence.ARRAY;
                        stack.push(parser.scanner.currentToken);
                        Token slice = new Token();
                        slice.tokenStr = "SLICE";
                        slice.primClassif = Classif.OPERAND;
                        postfix.add(slice);
                        sliceI++;
                        parser.scanner.getNext();
                    } else {

                        if (!stack.empty() && stack.peek().tokenStr.equals("~")) {
                            postfix.add(stack.pop());
                        }

                        postfix.add(parser.scanner.currentToken);
                        size++;

                        /*if (!stack.empty() && stack.peek().primClassif == Classif.OPERATOR && parser.scanner.nextToken.primClassif != Classif.OPERATOR) {
                            postfix.add(stack.pop());
                            if (postfix.get(postfix.size()-1).operatorPrecedence != OperatorPrecedence.UNARYMINUS && postfix.get(postfix.size()-1).operatorPrecedence != OperatorPrecedence.NOT)
                                size = size - 1;
                            expr--;
                        }*/

                    }
                    break;
                case OPERATOR:
                    /*if (postfix.size() > 0 && postfix.get(postfix.size()-1).primClassif != Classif.OPERAND && expr != 0
                        && (postfix.get(postfix.size()-1).operatorPrecedence != OperatorPrecedence.UNARYMINUS
                    && postfix.get(postfix.size()-1).operatorPrecedence != OperatorPrecedence.NOT
                    && postfix.get(postfix.size()-1).operatorPrecedence != OperatorPrecedence.PARMS)) {
                        throw new ScannerParserException(postfix.get(postfix.size()-1), parser.scanner.sourceFileNm, "expected operand, found");
                    }*/


                    if (!stack.empty() && stack.peek().tokenStr.equals("~")) {
                        postfix.add(stack.pop());
                        size--;
                    }

                    while (!stack.empty()) {
                       /* System.out.printf("%s\n", stack.peek().tokenStr);
                        System.out.printf("Checking precedence:\n\t'%s'\t'%s'\n\t'%d'\t'%d'\n",
                                parser.scanner.currentToken.tokenStr,
                                stack.peek().tokenStr,
                                parser.scanner.currentToken.operatorPrecedence.tokenPrecedence,
                                stack.peek().operatorPrecedence.stackPrecedence);*/


                        if (parser.scanner.currentToken.operatorPrecedence.tokenPrecedence > stack.peek().operatorPrecedence.stackPrecedence) {
                            break;
                        }

                        Token popped = stack.pop();
                        postfix.add(popped);

                        if (popped.operatorPrecedence != OperatorPrecedence.UNARYMINUS && popped.operatorPrecedence != OperatorPrecedence.NOT)
                            size--;

                        expr--;
                        //System.out.printf("Outing operator '%s'\n", popped.tokenStr);
                    }

                    if (size == 0 && !parser.scanner.currentToken.tokenStr.equals("~") &&
                            !(parser.scanner.currentToken.operatorPrecedence == OperatorPrecedence.UNARYMINUS || parser.scanner.currentToken.operatorPrecedence == OperatorPrecedence.NOT)) {
                        throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "expected operand, found");
                    }

                    stack.push(parser.scanner.currentToken);

                    if (parser.scanner.currentToken.operatorPrecedence != OperatorPrecedence.UNARYMINUS && parser.scanner.currentToken.operatorPrecedence != OperatorPrecedence.NOT)
                        expr++;


                    break;
                case SEPARATOR:
                        switch (parser.scanner.currentToken.tokenStr) {
                            case "(":
                                if (stack.empty() && postfix.size() > 0) {
                                    throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "operator expected, found");
                                }


                                stack.push(parser.scanner.currentToken);
                                break;
                            case ")":

                                if (!stack.empty()) {
                                    Boolean parenCheck = false;
                                    while (!stack.empty() ) {
                                        Token popped = stack.pop();
                                        if (popped.tokenStr.equals("(")) {
                                            parenCheck = true;
                                            break;
                                        }


                                        if (size < 0 && popped.operatorPrecedence == OperatorPrecedence.FUNC)
                                            throw new ScannerParserException(postfix.get(postfix.size()-1), parser.scanner.sourceFileNm, "Invalid expression");

                                        else if (size < 2
                                                && !((popped.operatorPrecedence == OperatorPrecedence.NOT
                                                        || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                                                        || popped.operatorPrecedence == OperatorPrecedence.FUNC)))
                                            throw new ScannerParserException(postfix.get(postfix.size()-1), parser.scanner.sourceFileNm, "expected operand, found");

                                        else if (size < 1 &&
                                                (popped.operatorPrecedence == OperatorPrecedence.NOT
                                                        || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS))
                                            throw new ScannerParserException(postfix.get(postfix.size()-1), parser.scanner.sourceFileNm, "expected operand, found");


                                        postfix.add(popped);

                                        if (popped.operatorPrecedence != OperatorPrecedence.NOT && popped.operatorPrecedence != OperatorPrecedence.UNARYMINUS)
                                            size--;


                                        if (popped.primClassif == Classif.OPERATOR)
                                            expr--;

                                        if (popped.primClassif == Classif.FUNCTION) {
                                            parenCheck = true;
                                            size++;
                                            funcBool--;
                                            break;
                                        }
                                    }

                                    if (!parenCheck) {
                                        throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Missing '('");
                                    }
                                    if (!stack.empty() && stack.peek().primClassif == Classif.OPERATOR) {
                                        Token popped = stack.pop();


                                        if (size < 2
                                                && !((popped.operatorPrecedence == OperatorPrecedence.NOT
                                                || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                                                || popped.operatorPrecedence == OperatorPrecedence.FUNC)))
                                            throw new ScannerParserException(postfix.get(postfix.size()-1), parser.scanner.sourceFileNm, "expected operand, found");

                                        if (size < 1 &&
                                                ((popped.operatorPrecedence == OperatorPrecedence.NOT
                                                        || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                                                        || popped.operatorPrecedence == OperatorPrecedence.FUNC)))
                                            throw new ScannerParserException(postfix.get(postfix.size()-1), parser.scanner.sourceFileNm, "expected operand, found");

                                        if (popped.operatorPrecedence != OperatorPrecedence.NOT && popped.operatorPrecedence != OperatorPrecedence.UNARYMINUS)
                                            size--;

                                        postfix.add(popped);
                                        expr--;
                                    }



                                } else {
                                    throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Missing '('");
                                }
                                break;
                            case ",":
                                if (!stack.empty()) {
                                    Token popped = stack.pop();
                                    while (!stack.empty() && popped.primClassif != Classif.FUNCTION) {

                                        if (size < 2
                                                && !((popped.operatorPrecedence == OperatorPrecedence.NOT
                                                || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                                                || popped.operatorPrecedence == OperatorPrecedence.FUNC)))
                                            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid expression operation");

                                        if (size < 1 &&
                                                ((popped.operatorPrecedence == OperatorPrecedence.NOT
                                                        || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                                                        || popped.operatorPrecedence == OperatorPrecedence.FUNC)))
                                            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid expression operation");

                                        if (popped.operatorPrecedence != OperatorPrecedence.NOT && popped.operatorPrecedence != OperatorPrecedence.UNARYMINUS)
                                            size--;

                                        postfix.add(popped);




                                        popped = stack.pop();
                                    }
                                    stack.push(popped);

                                    size--;

                                }

                                break;
                            case "[":
                                if (parser.scanner.currentToken.operatorPrecedence != OperatorPrecedence.SLICE)
                                    parser.scanner.currentToken.operatorPrecedence = OperatorPrecedence.SLICE;
                                stack.push(parser.scanner.currentToken);
                                break;
                            case "]":
                                if (!stack.empty()) {
                                    Token popped = stack.pop();

                                    while(!stack.empty() && !popped.tokenStr.endsWith("[")) {
                                        if (!popped.tokenStr.equals("~") && size < 2
                                                && !((popped.operatorPrecedence == OperatorPrecedence.NOT
                                                || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                                                || popped.operatorPrecedence == OperatorPrecedence.FUNC)))
                                            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid expression operation");

                                        if (!popped.tokenStr.equals("~") &&size < 1 &&
                                                ((popped.operatorPrecedence == OperatorPrecedence.NOT
                                                        || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                                                        || popped.operatorPrecedence == OperatorPrecedence.FUNC)))
                                            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid expression operation");

                                        if (popped.operatorPrecedence != OperatorPrecedence.NOT && popped.operatorPrecedence != OperatorPrecedence.UNARYMINUS)
                                            size--;

                                        postfix.add(popped);
                                        popped = stack.pop();
                                    }
                                    if (!popped.tokenStr.equals("["))
                                        sliceI--;
                                        postfix.add(popped);


                                } else {
                                    throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Missing '['");
                                }
                                break;
                            default:
                                // TODO: 4/8/2021 throw error for invalid separator in expression
                                throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid separator in expression");
                        }

                        break;
                case FUNCTION:
                        if (!stack.empty() && stack.peek().tokenStr.equals("~")){
                            Token popped = stack.pop();
                            postfix.add(popped);
                            size--;
                        }
                        if (parser.scanner.currentToken.operatorPrecedence != OperatorPrecedence.FUNC)
                            parser.scanner.currentToken.operatorPrecedence = OperatorPrecedence.FUNC;

                        stack.push(parser.scanner.currentToken);
                        Token parms = new Token();
                        parms.tokenStr = "PARMS";
                        parms.primClassif = Classif.OPERATOR;
                        parms.operatorPrecedence = OperatorPrecedence.PARMS;
                        postfix.add(parms);

                        funcBool++;
                        if (!parser.scanner.getNext().equals("(")) {
                            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Functions must be followed by a '(' token");
                        }
                        break;
                default:
                    // TODO: 4/8/2021 throw error for invalid token in expression
                    throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid token in expression");

            }


            parser.scanner.getNext();
        }



        while (!stack.empty()) {
            Token popped = stack.pop();

            if (popped.tokenStr.equals("(") && funcBool > 0) {
                throw new ScannerParserException(popped, parser.scanner.sourceFileNm, "invalid function expression");
            }
            if (popped.tokenStr.equals("(")) {
                throw new ScannerParserException(popped, parser.scanner.sourceFileNm, "Missing ')'");
            }

            if (size < 2
                    && !((popped.operatorPrecedence == OperatorPrecedence.NOT
                    || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                    || popped.operatorPrecedence == OperatorPrecedence.FUNC))) {
                throw new ScannerParserException(postfix.get(postfix.size()-1), parser.scanner.sourceFileNm, "Invalid expression operation");
            }

            if (size < 1 &&
                    ((popped.operatorPrecedence == OperatorPrecedence.NOT
                            || popped.operatorPrecedence == OperatorPrecedence.UNARYMINUS
                            || popped.operatorPrecedence == OperatorPrecedence.FUNC)))
                throw new ScannerParserException(postfix.get(postfix.size()-1), parser.scanner.sourceFileNm, "Invalid expression operation");

            postfix.add(popped);

            if (popped.operatorPrecedence != OperatorPrecedence.NOT && popped.operatorPrecedence != OperatorPrecedence.UNARYMINUS)
                size--;
        }

        if (sliceI > 0)
            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid expression missing or extra ']' ");

        if (funcBool > 0)
            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid function call missing closing ')'");

        return postfix;
    }

    private static Boolean isResultType(Stack<Result> stack, int type) {
        if (!stack.empty()) {
            Result t = stack.peek();

            // result value
            if (type == 1) {
                if (t instanceof ResultValue) {
                    return true;
                } else {
                    return false;
                }
            } else if (type == 2) {
                if (t instanceof ResultList) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return false;
    }


    public static Result evaluatePostFix(Parser parser, ArrayList<Token> postFix) throws PickleException {
        Stack<Result> stack = new Stack<Result>();

        Result res;

        int sliceBool = 0;
        if (postFix.size() == 0) {
            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid expression");
        }

        for (int i = 0; i < postFix.size(); i++) {
            Token token = postFix.get(i);


            switch (token.primClassif) {
                case OPERAND:
                    Result tmp = new ResultValue(token.tokenStr, token.subClassif);

                    if (token.subClassif == SubClassif.IDENTIFIER && token.tokenStr.endsWith("[")) {
                        token.tokenStr = token.tokenStr.substring(0, token.tokenStr.length()-1);
                        STEntry entry = parser.symbolTable.getSymbol(token.tokenStr);
                        if (!parser.activationRecordStack.isEmpty()) {
                            int scope = parser.activationRecordStack.peek().findSymbolScope(token.tokenStr);
                            if (scope != -1)
                                entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(token.tokenStr);
                        }

                        ResultValue neg = new ResultValue("0", SubClassif.INTEGER);

                        if (entry.primClassif != Classif.EMPTY && ((STIdentifier)entry).structure.equals("array") ) {

                            ResultValue index;

                            if (isResultType(stack, 1)) {
                                index = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }


                            ResultList array;
                            Result arrayTemp;
                            try {
                                arrayTemp = parser.storageManager.getVariable(token.tokenStr);
                                if (!parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(token.tokenStr);
                                    if (scope != -1)
                                        arrayTemp =  parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(token.tokenStr);
                                }

                                if (!(arrayTemp instanceof ResultList)) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot index into non array type");
                                } else {
                                    array = (ResultList) arrayTemp;
                                }

                                if (index.strValue.equals("~")) {
                                    //just an lower bound
                                    ResultValue lower;

                                    if (isResultType(stack, 1)) {
                                        lower = (ResultValue) stack.pop();
                                    } else {
                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                                    }


                                    if (!((ResultValue)stack.pop()).strValue.equals("SLICE")) {
                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Invalid slice expression");
                                    }

                                    if (lower.dataType != SubClassif.INTEGER) {
                                        if (lower.dataType == SubClassif.IDENTIFIER) {
                                            STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);
                                            if (!parser.activationRecordStack.isEmpty()) {
                                                int scope = parser.activationRecordStack.peek().findSymbolScope(index.strValue);
                                                if (scope != -1)
                                                    subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(index.strValue);
                                            }

                                            if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier)subEntry).dclType == SubClassif.INTEGER) {
                                                try {
                                                    lower = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
                                                    if (!parser.activationRecordStack.isEmpty()) {
                                                        int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                        if (scope != -1)
                                                            lower = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                    }

                                               } catch (Exception e) {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                }
                                            } else {
                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                            }
                                        } else {

                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                        }
                                    }

                                    if (Utility.lessThan(parser, lower, neg).strValue.equals("T")) {
                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                    }

                                    tmp =  Utility.getArraySlice(parser, array, Integer.parseInt(lower.strValue), -1);


                                } else if (isResultType(stack, 1) && ((ResultValue)stack.peek()).strValue.equals("~")) {
                                    stack.pop();
                                    // possible upper and lower or just upper
                                    if (isResultType(stack, 1) && ((ResultValue)stack.peek()).strValue.equals("SLICE")) {
                                        //just upper bound
                                        if (isResultType(stack, 1) && !((ResultValue)stack.pop()).strValue.equals("SLICE")) {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Invalid slice expression");
                                        }
                                        if (index.dataType != SubClassif.INTEGER) {
                                            if (index.dataType == SubClassif.IDENTIFIER) {
                                                STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);
                                                if (!parser.activationRecordStack.isEmpty()) {
                                                    int scope = parser.activationRecordStack.peek().findSymbolScope(index.strValue);
                                                    if (scope != -1) {
                                                        subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(index.strValue);
                                                    }
                                                }

                                                if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                    try {
                                                        index = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
                                                        if (!parser.activationRecordStack.isEmpty()) {
                                                            int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                            if (scope != -1)
                                                                index = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                        }
                                                    } catch (Exception e) {
                                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                    }
                                                } else {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                                }
                                            } else {

                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                            }
                                        }
                                        if (Utility.lessThan(parser, index, neg).strValue.equals("T")) {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                        }

                                        tmp =  Utility.getArraySlice(parser, array, -1, Integer.parseInt(index.strValue));
                                    } else {
                                        ResultValue lower;

                                        if (isResultType(stack, 1)) {
                                            lower = (ResultValue) stack.pop();
                                        } else {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                                        }

                                        if (isResultType(stack, 1) && !((ResultValue)stack.pop()).strValue.equals("SLICE")) {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Invalid slice expression");
                                        }
                                        // check upper bound for number
                                        if (index.dataType != SubClassif.INTEGER) {
                                            if (index.dataType == SubClassif.IDENTIFIER) {
                                                STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);
                                                if (!parser.activationRecordStack.isEmpty()) {
                                                    int scope = parser.activationRecordStack.peek().findSymbolScope(index.strValue);
                                                    if (scope != -1) {
                                                        subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(index.strValue);
                                                    }
                                                }

                                                if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                    try {
                                                        index = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
                                                        if (!parser.activationRecordStack.isEmpty()) {
                                                            int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                            if (scope != -1)
                                                                index = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                        }
                                                    } catch (Exception e) {
                                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                    }
                                                } else {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                                }
                                            } else {

                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                            }
                                        }

                                        // check lower bound for number
                                        if (lower.dataType != SubClassif.INTEGER) {
                                            if (lower.dataType == SubClassif.IDENTIFIER) {
                                                STEntry subEntry = parser.symbolTable.getSymbol(lower.strValue);

                                                if (!parser.activationRecordStack.isEmpty()) {
                                                    int scope = parser.activationRecordStack.peek().findSymbolScope(lower.strValue);
                                                    if (scope != -1) {
                                                        subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(lower.strValue);
                                                    }
                                                }

                                                if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                    try {
                                                        lower = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);

                                                        if (!parser.activationRecordStack.isEmpty()) {
                                                            int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                            if (scope != -1)
                                                                lower = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                        }
                                                    } catch (Exception e) {
                                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                    }
                                                } else {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                                }
                                            } else {

                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                            }
                                        }
                                        if (Utility.lessThan(parser, index, neg).strValue.equals("T")
                                        || Utility.lessThan(parser, lower, neg).strValue.equals("T")) {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                        }

                                        tmp =  Utility.getArraySlice(parser, array, Integer.parseInt(lower.strValue), Integer.parseInt(index.strValue));

                                    }

                                }

                                // no slicing
                                else {


                                    if (index.dataType != SubClassif.INTEGER) {
                                        if (index.dataType == SubClassif.IDENTIFIER) {
                                            STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);
                                            // TODO: 5/1/2021 add check for symbol in all activation records
                                            if (!parser.activationRecordStack.isEmpty()) {
                                                int scope = parser.activationRecordStack.peek().findSymbolScope(index.strValue);
                                                if (scope != -1) {
                                                    subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(index.strValue);
                                                }
                                            }


                                            if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                try {
                                                    index = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);

                                                    if (!parser.activationRecordStack.isEmpty()) {
                                                        int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                        if (scope != -1)
                                                            index = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                    }
                                                } catch (Exception e) {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                }
                                            } else {
                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                            }
                                        } else {

                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                        }
                                    }


                                    tmp = array.getItem(parser, Integer.parseInt(index.strValue));


                                    if (isResultType(stack, 1) && ((ResultValue)stack.peek()).strValue.equals("SLICE")) {
                                        stack.pop();
                                    }
                                }
                            } catch (PickleException p) {
                                throw p;
                            }

                            catch (Exception e) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot index in non-array variable");
                            }




                        } else if (entry.primClassif != Classif.EMPTY &&
                                (((STIdentifier) entry).dclType == SubClassif.STRING || ((STIdentifier)entry).dclType == SubClassif.DATE)) {
                            ResultValue index;

                            if (isResultType(stack, 1)) {
                                index = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }

                            try {
                                ResultValue str = (ResultValue) parser.storageManager.getVariable(entry.symbol);
                                if (!parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(entry.symbol);
                                    if (scope != -1)
                                        str = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(entry.symbol);
                                }

                                if (index.strValue.equals("~")) {
                                    //just an lower bound
                                    ResultValue lower;

                                    if (isResultType(stack, 1)) {
                                        lower = (ResultValue) stack.pop();
                                    } else {
                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                                    }


                                    if (isResultType(stack, 1) && !((ResultValue)stack.pop()).strValue.equals("SLICE")) {
                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Invalid slice expression");
                                    }

                                    if (lower.dataType != SubClassif.INTEGER) {
                                        if (lower.dataType == SubClassif.IDENTIFIER) {
                                            STEntry subEntry = parser.symbolTable.getSymbol(lower.strValue);
                                            if (!parser.activationRecordStack.isEmpty()) {
                                                int scope = parser.activationRecordStack.peek().findSymbolScope(lower.strValue);
                                                if (scope != -1)
                                                    subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(lower.strValue);
                                            }

                                            if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier)subEntry).dclType == SubClassif.INTEGER) {
                                                try {
                                                    lower = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
                                                    if (!parser.activationRecordStack.isEmpty()) {
                                                        int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                        if (scope != -1)
                                                            lower = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                    }

                                                } catch (Exception e) {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                }
                                            } else {
                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                            }
                                        } else {

                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                        }
                                    }

                                    if (Utility.lessThan(parser, lower, neg).strValue.equals("T")) {
                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                    }

                                    tmp = Utility.getStringSlice(parser, str, Integer.parseInt(lower.strValue), -1);


                                } else if (isResultType(stack, 1) && ((ResultValue)stack.peek()).strValue.equals("~")) {
                                    stack.pop();
                                    // possible upper and lower or just upper
                                    if (isResultType(stack, 1) && ((ResultValue)stack.peek()).strValue.equals("SLICE")) {
                                        //just upper bound
                                        if (isResultType(stack, 1) && !((ResultValue)stack.pop()).strValue.equals("SLICE")) {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Invalid slice expression");
                                        }
                                        if (index.dataType != SubClassif.INTEGER) {
                                            if (index.dataType == SubClassif.IDENTIFIER) {
                                                STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);

                                                if (!parser.activationRecordStack.isEmpty()) {
                                                    int scope = parser.activationRecordStack.peek().findSymbolScope(index.strValue);

                                                    if (scope != -1)
                                                        subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(index.strValue);
                                                }

                                                if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                    try {
                                                        index = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);

                                                        if (!parser.activationRecordStack.isEmpty()){
                                                            int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                            if (scope != -1)
                                                                index = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                        }


                                                    } catch (Exception e) {
                                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                    }
                                                } else {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                                }
                                            } else {

                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                            }
                                        }

                                        if (Utility.lessThan(parser, index, neg).strValue.equals("T")) {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                        }

                                        tmp = Utility.getStringSlice(parser, str, -1, Integer.parseInt(index.strValue));

                                    } else {
                                        ResultValue lower;

                                        if (isResultType(stack, 1)) {
                                            lower = (ResultValue) stack.pop();
                                        } else {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                                        }


                                        if (isResultType(stack, 1) && !((ResultValue)stack.pop()).strValue.equals("SLICE")) {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Invalid slice expression");
                                        }
                                        // check upper bound for number
                                        if (index.dataType != SubClassif.INTEGER) {
                                            if (index.dataType == SubClassif.IDENTIFIER) {
                                                STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);

                                                if (!parser.activationRecordStack.isEmpty()){
                                                    int scope = parser.activationRecordStack.peek().findSymbolScope(index.strValue);
                                                    if (scope != -1)
                                                        subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(index.strValue);
                                                }

                                                if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                    try {
                                                        index = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);

                                                        if (!parser.activationRecordStack.isEmpty()){
                                                            int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                            if (scope != -1)
                                                                index = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                        }
                                                    } catch (Exception e) {
                                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                    }
                                                } else {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                                }
                                            } else {

                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                            }
                                        }

                                        // check lower bound for number
                                        if (lower.dataType != SubClassif.INTEGER) {
                                            if (lower.dataType == SubClassif.IDENTIFIER) {
                                                STEntry subEntry = parser.symbolTable.getSymbol(lower.strValue);
                                                if (!parser.activationRecordStack.isEmpty()){
                                                    int scope = parser.activationRecordStack.peek().findSymbolScope(lower.strValue);
                                                    if (scope != -1)
                                                        subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(index.strValue);
                                                }


                                                if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                    try {
                                                        lower = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);

                                                        if (!parser.activationRecordStack.isEmpty()){
                                                            int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                            if (scope != -1)
                                                                lower = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                        }
                                                    } catch (Exception e) {
                                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                    }
                                                } else {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                                }
                                            } else {

                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                            }
                                        }

                                        if (Utility.lessThan(parser, index, neg).strValue.equals("T")
                                        || Utility.lessThan(parser, lower, neg).strValue.equals("T")) {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                        }

                                        tmp = Utility.getStringSlice(parser, str, Integer.parseInt(lower.strValue), Integer.parseInt(index.strValue));

                                    }

                                } else {
                                    str = (ResultValue) parser.storageManager.getVariable(entry.symbol);
                                    if (!parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(entry.symbol);
                                        if (scope != -1)
                                            str = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(entry.symbol);
                                    }

                                    if (index.dataType != SubClassif.INTEGER) {
                                        if (index.dataType == SubClassif.IDENTIFIER) {
                                            STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);

                                            if (!parser.activationRecordStack.isEmpty()) {
                                                int scope = parser.activationRecordStack.peek().findSymbolScope(index.strValue);
                                                if (scope != -1)
                                                    subEntry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(index.strValue);
                                            }

                                            if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                try {
                                                    index = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
                                                    if (!parser.activationRecordStack.isEmpty()){
                                                        int scope = parser.activationRecordStack.peek().findSymbolScope(subEntry.symbol);
                                                        if (scope != -1)
                                                            index = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(subEntry.symbol);
                                                    }
                                                } catch (Exception e) {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                }
                                            } else {
                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                            }
                                        } else {

                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                        }
                                    }
                                    tmp = Utility.valueAtIndex(parser, str, Integer.parseInt(index.strValue));



                                    if (isResultType(stack, 1) && ((ResultValue)stack.peek()).strValue.equals("SLICE")) {
                                        stack.pop();
                                    }
                                }

                            } catch (PickleException p) {
                                throw p;
                            }
                            catch (Exception e) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Unexpected or undefined value on stack");
                            }

                        }

                        else {
                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot index on empty array");
                        }

                    }


                    if (token.tokenStr.equals("SLICE")) {
                        sliceBool += 1;
                    }
                    stack.push(tmp);
                    break;

                case FUNCTION:
                    ResultValue param;
                    ResultValue param2;
                    ResultList paramM;
                    ArrayList<Result> parameters;

                    if (token.tokenStr.equals("print")) {





                    }



                    switch (token.tokenStr) {
                        case "print":
                            parameters = new ArrayList<Result>();
                            Stack<Result> paramStack = new Stack<Result>();

                            while(!stack.empty()) {
                                if (isResultType(stack, 1)) {
                                    if (((ResultValue)stack.peek()).strValue.equals("PARMS")) {
                                        break;
                                    }
                                }
                                paramStack.push(stack.pop());
                            }

                            while(!paramStack.empty()) {
                                parameters.add(paramStack.pop());
                            }

                            parser.print(parameters);
                            res = new ResultValue("", SubClassif.VOID);
                            break;
                        case "LENGTH":
                            ArrayList<Result> paramsLen = new ArrayList<Result>();
                            if (isResultType(stack, 1)) {
                                param = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);
                                if (!parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param.strValue);
                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);
                                    if (!parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);
                                        if (scope != -1)
                                            param = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }

                                }catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value must be non array type");
                                }


                                if (param.dataType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot call length on non-string  value");
                                } else {
                                    paramsLen.add(param);
                                    res = Utility.builtInLENGTH(paramsLen);
                                }
                            } else if (param.dataType != SubClassif.STRING) {
                                param = Utility.coerce(parser, param, SubClassif.STRING);

                                if (param.dataType == SubClassif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Parameter must be string constant or variable");
                                }

                                paramsLen.add(param);
                                res = Utility.builtInLENGTH(paramsLen);
                            } else if (param.dataType == SubClassif.STRING) {
                                paramsLen.add(param);
                                res = Utility.builtInLENGTH(paramsLen);
                            }
                            else {
                                token.tokenStr = param.strValue;
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Parameter must be string constant or variable");
                            }
                            break;

                        case "SPACES":
                            ArrayList<Result> paramsSpace = new ArrayList<Result>();
                            if (isResultType(stack, 1)) {
                                param = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);
                                if (!parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param.strValue);
                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);
                                    if (!parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);
                                        if (scope != -1)
                                            param = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }
                                }catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value must be non array type");
                                }



                                if (param.dataType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot call length on non-string  value");
                                } else {
                                    paramsSpace.add(param);
                                    res = Utility.builtInSPACES(paramsSpace);
                                }
                            } else if (param.dataType == SubClassif.STRING) {
                                paramsSpace.add(param);
                                res = Utility.builtInLENGTH(paramsSpace);
                            } else if (param.dataType != SubClassif.STRING && param.dataType != SubClassif.EMPTY) {
                                param = Utility.coerce(parser, param, SubClassif.STRING);

                                paramsSpace.add(param);
                                res = Utility.builtInSPACES(paramsSpace);
                            }
                            else {
                                token.tokenStr = param.strValue;
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Parameter must be string constant or variable");
                            }
                            break;
                        case "ELEM":
                            ArrayList<Result> paramsElem = new ArrayList<Result>();
                            if (isResultType(stack, 1)) {
                                param = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }


                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);
                                if (!parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param.strValue);
                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (!id.structure.equals("array")) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot call ELEM on non-array identifier");
                                }

                                try {
                                    paramM = (ResultList) parser.storageManager.getVariable(id.symbol);
                                    if (!parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);
                                        if (scope != -1)
                                            paramM = (ResultList) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }
                                }catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value must be array type");
                                }
                                paramsElem.add(paramM);
                                res = Utility.builtInELEM(paramsElem);

                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value of variable cannot be array");
                            }
                            break;

                        case "MAXELEM":
                            ArrayList<Result> paramsMaxElem = new ArrayList<Result>();

                            if (isResultType(stack, 1)) {
                                param = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);
                                if (!parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param.strValue);
                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (!id.structure.equals("array")) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot call ELEM on non-array identifier");
                                }

                                try {
                                    Result paramTemp = parser.storageManager.getVariable(id.symbol);
                                    if (!parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);
                                        if (scope != -1)
                                            paramTemp = parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }

                                    if (!(paramTemp instanceof ResultList)) {
                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expects and array as parameters");
                                    }

                                    paramM = (ResultList) paramTemp;
                                }catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value must be array type");
                                }
                                paramsMaxElem.add(paramM);
                                res = Utility.builtInMAXELEM(paramsMaxElem);

                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value of variable cannot be array");
                            }
                            break;

                        case "dateDiff":
                            if ( stack.size() < 2) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Did not supply enough parameters for function");
                            }

                            if (isResultType(stack, 1)) {
                                param2 = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }
                            if (isResultType(stack, 1)) {
                                param = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (! parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param.strValue);

                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);

                                    if (! parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);

                                        if (scope != -1)
                                            param = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }

                                    if (id.dclType == SubClassif.STRING) {
                                        param = Date.validateDate(param.strValue);
                                    }
                                } catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use array value with dateDiff function");
                                }
                            } else if (param.dataType == SubClassif.STRING || param.dataType == SubClassif.DATE) {
                                param = Date.validateDate(param.strValue);
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value was not Date constant or variable. Could not convert to date");
                            }

                            if (param2.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param2.strValue);

                                if (! parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param2.strValue);

                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param2.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param2 = (ResultValue) parser.storageManager.getVariable(id.symbol);

                                    if (! parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);

                                        if (scope != -1)
                                            param2 = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }


                                    if (id.dclType == SubClassif.STRING) {
                                        param2 = Date.validateDate(param2.strValue);
                                    }
                                } catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use array value with dateDiff function");
                                }
                            } else if (param2.dataType == SubClassif.STRING || param2.dataType == SubClassif.DATE) {
                                param2 = Date.validateDate(param2.strValue);
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value was not Date constant or variable. Could not convert to date");
                            }

                            res = Date.dateDiff(param, param2);

                            break;
                        case "dateAdj":
                            if ( stack.size() < 2) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Did not supply enough parameters for function");
                            }

                            if (isResultType(stack, 1)) {
                                param2 = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }
                            if (isResultType(stack, 1)) {
                                param = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (! parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param.strValue);

                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param.strValue);

                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);

                                    if (! parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);

                                        if (scope != -1)
                                            param = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }

                                    if (id.dclType == SubClassif.STRING) {
                                        param = Date.validateDate(param.strValue);
                                    }
                                } catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use array value with dateDiff function");
                                }
                            } else if (param.dataType == SubClassif.STRING || param.dataType == SubClassif.DATE) {
                                param = Date.validateDate(param.strValue);
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value was not Date constant or variable. Could not convert to date");
                            }

                            if (param2.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param2.strValue);

                                if (! parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param2.strValue);

                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param2.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.INTEGER) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-integer value with dateAdj function");
                                }

                                try {
                                    param2 = (ResultValue) parser.storageManager.getVariable(id.symbol);

                                    if (! parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);

                                        if (scope != -1)
                                            param2 = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }
                                } catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use array value with dateDiff function");
                                }
                            } else if (param2.dataType != SubClassif.INTEGER) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value was not Integer constant or variable");
                            }

                            res = Date.dateAdj(param, param2);
                            break;

                        case "dateAge":
                            if ( stack.size() < 2) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Did not supply enough parameters for function");
                            }

                            if (isResultType(stack, 1)) {
                                param2 = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }
                            if (isResultType(stack, 1)) {
                                param = (ResultValue) stack.pop();
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Expected a primitive type or identifier");
                            }

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (! parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param.strValue);

                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);

                                    if (! parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);

                                        if (scope != -1)
                                            param = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }

                                    if (id.dclType == SubClassif.STRING) {
                                        param = Date.validateDate(param.strValue);
                                    }
                                } catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use array value with dateDiff function");
                                }
                            } else if (param.dataType == SubClassif.STRING || param.dataType == SubClassif.DATE) {
                                param = Date.validateDate(param.strValue);
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value was not Date constant or variable. Could not convert to date");
                            }

                            if (param2.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param2.strValue);

                                if (! parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(param2.strValue);

                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(param2.strValue);
                                }

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param2 = (ResultValue) parser.storageManager.getVariable(id.symbol);

                                    if (! parser.activationRecordStack.isEmpty()) {
                                        int scope = parser.activationRecordStack.peek().findSymbolScope(id.symbol);

                                        if (scope != -1)
                                            param2 = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(id.symbol);
                                    }

                                    if (id.dclType == SubClassif.STRING) {
                                        param2 = Date.validateDate(param2.strValue);
                                    }
                                } catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use array value with dateDiff function");
                                }
                            } else if (param2.dataType == SubClassif.STRING || param2.dataType == SubClassif.DATE) {
                                param2 = Date.validateDate(param2.strValue);
                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value was not Date constant or variable. Could not convert to date");
                            }

                            res = Date.dateAge(param, param2);

                            break;

                        default:
                            try {
                                STEntry entry =  parser.symbolTable.getSymbol(token.tokenStr);

                                if (! parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(token.tokenStr);

                                    if (scope != -1)
                                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(token.tokenStr);
                                }

                                if (entry.primClassif == Classif.EMPTY || !(entry instanceof STFunction)) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot find function in any scope");
                                }

                                STFunction funcName = (STFunction) entry;

                                parameters = new ArrayList<Result>();
                                for (String names : funcName.names)
                                    parameters.add(new ResultValue("", SubClassif.EMPTY));

                                for(int j = funcName.numArgs - 1; j >= 0; j--) {
                                    if (!stack.empty()) {
                                        parameters.set(j, stack.pop());
                                    } else {
                                        throw  new ScannerParserException(token, parser.scanner.sourceFileNm, "Invalid number of parameters passed");
                                    }
                                }

                                res = parser.callUserFunction(funcName, parameters);

                            } catch (PickleException p) {
                                throw p;
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Have not implemented function to be used in expressions");
                            }


                    }
                    if (isResultType(stack, 1)) {
                        if (((ResultValue)stack.peek()).strValue.equals("PARMS"))
                            stack.pop();
                    }


                    stack.push(res);
                    break;

                case OPERATOR:

                    if (token.operatorPrecedence == OperatorPrecedence.PARMS) {
                        stack.push(new ResultValue(token.tokenStr, SubClassif.VOID));
                        break;
                    }


                    // 0 = second operand
                    // 1 = first operand
                    Bool bOp1;
                    Bool bOp2;
                    ResultValue operand2;
                    ResultValue operand1;

                    if (stack.empty()) {
                        operand2 = new ResultValue("", SubClassif.EMPTY);
                    } else {
                        if (isResultType(stack, 1)) {
                            operand2 = (ResultValue) stack.pop();
                        } else {
                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use arrays in expressions");
                        }
                    }

                    operand1 = new ResultValue("", SubClassif.EMPTY);

                    if (operand2.dataType == SubClassif.IDENTIFIER) {
                        STEntry entry = parser.symbolTable.getSymbol(operand2.strValue);
                        if (!parser.activationRecordStack.isEmpty()) {
                            int scope = parser.activationRecordStack.peek().findSymbolScope(operand2.strValue);
                            if (scope != -1)
                                entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(operand2.strValue);
                        }

                        if (entry.primClassif == Classif.EMPTY) {
                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                        }
                        try {
                            operand2 = (ResultValue) parser.storageManager.getVariable(entry.symbol);
                            if (!parser.activationRecordStack.isEmpty()) {
                                int scope = parser.activationRecordStack.peek().findSymbolScope(entry.symbol);
                                if (scope != -1)
                                    operand2 = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(entry.symbol);
                            }
                        } catch (Exception e) {
                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Operand for operator must be non array type");
                        }
                    }


                    if (token.tokenStr.equals("-") && token.operatorPrecedence == OperatorPrecedence.UNARYMINUS) {
                        res = Utility.unaryMinus(parser,
                                getNumeric(parser, operand2, token.tokenStr, "First Operand for unary minus"));

                    } else if (token.tokenStr.equals("not")) {
                        bOp2 = new Bool(parser, operand2);
                        res = Utility.boolNot(parser, bOp2);

                    } else if (token.tokenStr.equals("~")) {
                        if (sliceBool > 0) {
                            stack.push(operand2);
                            res = new ResultValue(token.tokenStr, SubClassif.EMPTY);
                            sliceBool -= 1;
                        } else {
                            ResultValue empty = new ResultValue("-1", SubClassif.INTEGER);
                            ArrayList<ResultValue>bounds = new ArrayList<ResultValue>();

                            // if stack is not empty than possible upper and lower bound
                            ResultValue lower = operand2;

                            if (lower.dataType != SubClassif.EMPTY) {
                                // if end of postFix then just lower bound
                                if (lower.dataType != SubClassif.INTEGER) {
                                    if (lower.dataType == SubClassif.IDENTIFIER) {
                                        STEntry subEntry = parser.symbolTable.getSymbol(lower.strValue);

                                        if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                            try {
                                                lower = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
                                            } catch (Exception e) {
                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                            }
                                        } else {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                        }
                                    } else {

                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                    }
                                }
                                if (Utility.lessThanOrEqualTo(parser, lower, empty).strValue.equals("T")) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                }


                                if (i + 1 >= postFix.size()) {

                                    bounds.add(lower);
                                    bounds.add(empty);

                                } else {
                                    // get upper bound

                                    ResultValue upper = (ResultValue) Expr.evaluatePostFix(parser, new ArrayList<Token>(postFix.subList(i+1, postFix.size())));
                                    if (upper.dataType != SubClassif.INTEGER) {
                                        if (upper.dataType == SubClassif.IDENTIFIER) {
                                            STEntry subEntry = parser.symbolTable.getSymbol(upper.strValue);

                                            if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                                try {
                                                    upper = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
                                                } catch (Exception e) {
                                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                                }
                                            } else {
                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                            }
                                        } else {

                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                        }
                                    }
                                    if (Utility.lessThanOrEqualTo(parser, upper, empty).strValue.equals("T")) {
                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                    }


                                    bounds.add(lower);
                                    bounds.add(upper);

                                }
                            } else {
                                if (i >= postFix.size()) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Invalid indices for slice operation");
                                }


                                ResultValue upper = (ResultValue) Expr.evaluatePostFix(parser, new ArrayList<Token>(postFix.subList(i+1, postFix.size())));
                                if (upper.dataType != SubClassif.INTEGER) {
                                    if (upper.dataType == SubClassif.IDENTIFIER) {
                                        STEntry subEntry = parser.symbolTable.getSymbol(upper.strValue);

                                        if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier) subEntry).dclType == SubClassif.INTEGER) {
                                            try {
                                                upper = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
                                            } catch (Exception e) {
                                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be primitive type");
                                            }
                                        } else {
                                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be an Integer");
                                        }
                                    } else {

                                        throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Index must be integer value");
                                    }
                                }
                                if (Utility.lessThanOrEqualTo(parser, upper, empty).strValue.equals("T")) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Slice index cannot be negative");
                                }



                                bounds.add(empty);
                                bounds.add(upper);
                            }
                            // if stack is empty then just upper bound

                            //should only hit during string slice assigning
                            return new ResultList(parser, bounds, 2, SubClassif.INTEGER);
                        }
                    } else {
                        if (isResultType(stack, 1)) {
                            operand1 = (ResultValue) stack.pop();
                        } else {
                            if (stack.empty() && (i + 1) >= postFix.size()){
                                throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "expected operand, found");
                            }

                            if (stack.empty() && (i+1) < postFix.size()) {
                                Token t = postFix.get(++i);
                                if (postFix.get(i).tokenStr.equals("("))
                                    t = parser.scanner.currentToken;

                                throw new ScannerParserException(t, parser.scanner.sourceFileNm, "Expected operand found");
                            }


                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use arrays in expressions");
                        }

                        if (operand1.dataType == SubClassif.IDENTIFIER) {
                            STEntry entry = parser.symbolTable.getSymbol(operand1.strValue);
                            if (!parser.activationRecordStack.isEmpty()) {
                                int scope = parser.activationRecordStack.peek().findSymbolScope(operand1.strValue);
                                if (scope != -1)
                                    entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(operand1.strValue);
                            }

                            if (entry.primClassif == Classif.EMPTY) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                            }
                            try {
                                operand1 = (ResultValue) parser.storageManager.getVariable(entry.symbol);
                                if (!parser.activationRecordStack.isEmpty()) {
                                    int scope = parser.activationRecordStack.peek().findSymbolScope(entry.symbol);
                                    if (scope != -1)
                                        operand1 = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(entry.symbol);
                                }
                            } catch (Exception e) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Operand for operator must be non array type");
                            }
                        }


                        switch (token.tokenStr) {
                            case "+":
                                res = Utility.add(parser,
                                        getNumeric(parser, operand1, token.tokenStr, "First Operand for addition"),
                                        getNumeric(parser, operand2, token.tokenStr, "Second Operand for addition"));
                                break;
                            case "-":

                                res = Utility.subtract(parser,
                                        getNumeric(parser, operand1, token.tokenStr, "First Operand for subtraction"),
                                        getNumeric(parser, operand2, token.tokenStr, "Second Operand for subtraction"));

                                break;
                            case "*":
                                res = Utility.multiply(parser,
                                        getNumeric(parser, operand1, token.tokenStr, "First Operand for multiply"),
                                        getNumeric(parser, operand2, token.tokenStr, "Second Operand for multiply"));
                                break;
                            case "/":
                                res = Utility.divide(parser,
                                        getNumeric(parser, operand1, token.tokenStr, "First Operand for divide"),
                                        getNumeric(parser, operand2, token.tokenStr, "Second Operand for divide"));
                                break;
                            case "^":
                                res = Utility.power(parser,
                                        getNumeric(parser, operand1, token.tokenStr, "First Operand for power"),
                                        getNumeric(parser, operand2, token.tokenStr, "Second Operand for power"));
                                break;
                            case ">":
                                res = Utility.greaterThan(parser, operand1, operand2);
                                break;
                            case "<":
                                res = Utility.lessThan(parser, operand1, operand2);
                                break;
                            case ">=":
                                res = Utility.greaterThanOrEqualTo(parser, operand1, operand2);
                                break;
                            case "<=":
                                res = Utility.lessThanOrEqualTo(parser, operand1, operand2);
                                break;
                            case "==":
                                res = Utility.equal(parser, operand1, operand2);
                                break;
                            case "!=":
                                res = Utility.notEqual(parser, operand1 , operand2);
                                break;
                            case "and":
                                bOp1 = new Bool(parser, operand1);
                                bOp2 = new Bool(parser, operand2);
                                res = Utility.boolAnd(parser, bOp1, bOp2);
                                break;
                            case "or":
                                bOp1 = new Bool(parser, operand1);
                                bOp2 = new Bool(parser, operand2);
                                res = Utility.boolOr(parser, bOp1, bOp2);
                                break;
                            case "#":
                                res = Utility.concatenateString(parser, operand1, operand2);
                                break;
                            default:
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot perform operation with invalid OPERATOR");


                        }

                    }


                    if (parser.bShowExpr) {
                        switch (token.operatorPrecedence) {
                            case UNARYMINUS:
                            case NOT:
                                break;
                            default:
                                if (res instanceof ResultValue) {
                                    System.out.printf("... %s %s %s is %s\n", operand1.strValue, token.tokenStr, operand2.strValue, ((ResultValue) res).strValue);
                                }
                                break;

                        }
                    }



                    stack.push(res);


            }



        }

        res = stack.pop();

        if (!stack.empty()) {
            if (res instanceof ResultValue) {
                parser.scanner.currentToken.tokenStr = ((ResultValue) res).strValue;
                throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "operand was not expected, found");
            } else if (res instanceof ResultList) {
                throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "operand was not expecting array");
            }
        }

        Result ret = res;

        if (res instanceof ResultValue) {
            if (((ResultValue) res).dataType == SubClassif.IDENTIFIER) {
                STEntry entry = parser.symbolTable.getSymbol(((ResultValue) res).strValue);
                if (!parser.activationRecordStack.isEmpty()) {
                    int scope = parser.activationRecordStack.peek().findSymbolScope(((ResultValue) res).strValue);
                    if (scope != -1)
                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(((ResultValue) res).strValue);
                }
                if (entry.primClassif == Classif.EMPTY) {
                    throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Identifier must be initialized");
                }

                ret = parser.storageManager.getVariable(((ResultValue) res).strValue);
                if (!parser.activationRecordStack.isEmpty()) {
                    int scope = parser.activationRecordStack.peek().findSymbolScope(((ResultValue) res).strValue);
                    if (scope != -1)
                        ret = parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(((ResultValue) res).strValue);
                }

            }
        }

        return ret;
    }

    public static Numeric getNumeric(Parser parser, ResultValue res, String operator, String desc) throws PickleException{
        Numeric n = null;
        switch (res.dataType) {
            case IDENTIFIER:
                STEntry entry = parser.symbolTable.getSymbol(res.strValue);
                if (!parser.activationRecordStack.isEmpty()) {
                    int scope = parser.activationRecordStack.peek().findSymbolScope(res.strValue);
                    if (scope != -1)
                        entry = parser.activationRecordStack.peek().environmentVector.get(scope).symbolTable.getSymbol(res.strValue);
                }

                if (entry.primClassif == Classif.EMPTY) {
                    // TODO: 4/8/2021 throw error on invalid identifier in expression
                    throw new PickleException();
                }

                res = (ResultValue) parser.storageManager.getVariable(res.strValue);
                if (!parser.activationRecordStack.isEmpty()) {
                    int scope = parser.activationRecordStack.peek().findSymbolScope(res.strValue);
                    if (scope != -1)
                        res = (ResultValue) parser.activationRecordStack.peek().environmentVector.get(scope).storageManager.getVariable(res.strValue);
                }

                if (res.dataType != SubClassif.INTEGER && res.dataType != SubClassif.FLOAT) {
                    throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Cannot perform unary minus on non-numeric operand identifier:");
                }


                n = new Numeric(parser, res, operator, desc);
                break;
            case INTEGER:
            case FLOAT:
                n = new Numeric(parser, res, operator, desc);
                break;
            default:
                    n = new Numeric(parser, Utility.coerce(parser, res, SubClassif.INTEGER), operator, desc);
        }

        return n;
    }



}

