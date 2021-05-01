package pickle;

import java.util.ArrayList;
import java.util.Stack;

public class Expr {

    public static ArrayList<Token> postFixExpr(Parser parser) throws PickleException {
        ArrayList<Token> postfix = new ArrayList<Token>();
        Stack<Token> stack = new Stack<Token>();

        int funcBool = 0;

        while(     !parser.scanner.currentToken.tokenStr.equals(";")
                && !parser.scanner.currentToken.tokenStr.equals(":")
                && !(parser.scanner.currentToken.tokenStr.equals("to") && parser.scanner.currentToken.subClassif != SubClassif.STRING)
                && !parser.scanner.currentToken.tokenStr.equals("by")
                && !parser.scanner.currentToken.tokenStr.equals("=")) {

            if (funcBool == 0 && parser.scanner.currentToken.tokenStr.equals(",") && parser.scanner.currentToken.subClassif != SubClassif.STRING) {
                break;
            }

            switch (parser.scanner.currentToken.primClassif) {
                case OPERAND:
                    //System.out.printf("Outing operand '%s' onto stack\n", parser.scanner.currentToken.tokenStr);
                    Token temp;
                    if (parser.scanner.nextToken.tokenStr.equals("[")) {
                        parser.scanner.currentToken.tokenStr = parser.scanner.currentToken.tokenStr.concat(parser.scanner.nextToken.tokenStr);
                        parser.scanner.currentToken.operatorPrecedence = OperatorPrecedence.ARRAY;
                        stack.push(parser.scanner.currentToken);
                        parser.scanner.getNext();
                    } else {
                        postfix.add(parser.scanner.currentToken);
                    }
                    break;
                case OPERATOR:
                    while (!stack.empty()) {
                       /* System.out.printf("%s\n", stack.peek().tokenStr);
                        System.out.printf("Checking precedence:\n\t'%s'\t'%s'\n\t'%d'\t'%d'\n",
                                parser.scanner.currentToken.tokenStr,
                                stack.peek().tokenStr,
                                parser.scanner.currentToken.operatorPrecedence.tokenPrecedence,
                                stack.peek().operatorPrecedence.stackPrecedence);
*/
                        if (parser.scanner.currentToken.operatorPrecedence.tokenPrecedence > stack.peek().operatorPrecedence.stackPrecedence) {
                            break;
                        }

                        Token popped = stack.pop();
                        postfix.add(popped);
                        //System.out.printf("Outing operator '%s'\n", popped.tokenStr);
                    }
                    stack.push(parser.scanner.currentToken);
                    break;
                case SEPARATOR:
                        switch (parser.scanner.currentToken.tokenStr) {
                            case "(":
                                stack.push(parser.scanner.currentToken);
                                break;
                            case ")":
                                if (!stack.empty()) {
                                    while (!stack.empty() ) {
                                        Token popped = stack.pop();
                                        if (popped.tokenStr.equals("(")) {
                                            break;
                                        }
                                        postfix.add(popped);

                                        if (popped.primClassif == Classif.FUNCTION) {
                                            break;
                                        }
                                    }



                                    break;
                                }
                            case ",":
                                if (!stack.empty()) {
                                    Token popped = stack.pop();
                                    while (!stack.empty() && popped.primClassif != Classif.FUNCTION) {
                                        postfix.add(popped);
                                        popped = stack.pop();
                                    }
                                    stack.push(popped);
                                    funcBool--;
                                }
                                break;
                            case "]":
                                if (!stack.empty()) {
                                    Token popped = stack.pop();

                                    while(!stack.empty() && !popped.tokenStr.endsWith("[")) {
                                        postfix.add(popped);
                                        popped = stack.pop();
                                    }


                                    postfix.add(popped);


                                }
                                break;
                            default:
                                // TODO: 4/8/2021 throw error for invalid seperator in expression
                                throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid seperator in expression");
                        }

                        break;
                case FUNCTION:
                        stack.push(parser.scanner.currentToken);
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
            postfix.add(stack.pop());
        }

        return postfix;
    }


    public static Result evaluatePostFix(Parser parser, ArrayList<Token> postFix) throws PickleException {
        Stack<ResultValue> stack = new Stack<ResultValue>();

        ResultValue res;

        if (postFix.size() == 0) {
            throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Invalid expression");
        }

        for (Token token : postFix) {
            switch (token.primClassif) {
                case OPERAND:
                    ResultValue tmp = new ResultValue(token.tokenStr, token.subClassif);

                    if (token.subClassif == SubClassif.IDENTIFIER && token.tokenStr.endsWith("[")) {
                        token.tokenStr = token.tokenStr.substring(0, token.tokenStr.length()-1);
                        STEntry entry = parser.symbolTable.getSymbol(token.tokenStr);

                        if (entry.primClassif != Classif.EMPTY && ((STIdentifier)entry).structure.equals("array") ) {

                            ResultValue index = stack.pop();
                            ResultList array;
                            try {
                                array = (ResultList)parser.storageManager.getVariable(token.tokenStr);

                                if (index.dataType != SubClassif.INTEGER) {
                                    if (index.dataType == SubClassif.IDENTIFIER) {
                                        STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);

                                        if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier)subEntry).dclType == SubClassif.INTEGER) {
                                            try {
                                                index = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
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
                            } catch (PickleException p) {
                                throw p;
                            }

                            catch (Exception e) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot index in non-array varaible");
                            }




                        } else if (entry.primClassif != Classif.EMPTY &&
                                (((STIdentifier) entry).dclType == SubClassif.STRING || ((STIdentifier)entry).dclType == SubClassif.DATE)) {
                            ResultValue index = stack.pop();

                            try {


                                if (index.dataType != SubClassif.INTEGER) {
                                    if (index.dataType == SubClassif.IDENTIFIER) {
                                        STEntry subEntry = parser.symbolTable.getSymbol(index.strValue);

                                        if (subEntry.primClassif != Classif.EMPTY && ((STIdentifier)subEntry).dclType == SubClassif.INTEGER) {
                                            try {
                                                index = (ResultValue) parser.storageManager.getVariable(subEntry.symbol);
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

                                ResultValue str = (ResultValue) parser.storageManager.getVariable(entry.symbol);

                                tmp = Utility.valueAtIndex(parser, str, Integer.parseInt(index.strValue));

                            } catch (Exception e) {

                            }

                        }

                        else {
                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot index on empty array");
                        }

                    }
                    stack.push(tmp);
                    break;

                case FUNCTION:
                    ResultValue param;
                    ResultValue param2;
                    ResultList paramM;
                    switch (token.tokenStr) {
                        case "LENGTH":
                            param = stack.pop();

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);
                                }catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value must be non array type");
                                }


                                if (param.dataType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot call length on non-string  value");
                                } else {
                                    res = Utility.builtInLENGTH(parser, param);
                                }
                            } else if (param.dataType == SubClassif.STRING) {
                                res = Utility.builtInLENGTH(parser, param);
                            }
                            else {
                                token.tokenStr = param.strValue;
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Parameter must be string constant or variable");
                            }
                            break;

                        case "SPACES":
                            param = stack.pop();

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);
                                }catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value must be non array type");
                                }



                                if (param.dataType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot call length on non-string  value");
                                } else {
                                    res = Utility.builtInSPACES(parser, param);
                                }
                            } else if (param.dataType == SubClassif.STRING) {
                                res = Utility.builtInLENGTH(parser, param);
                            }
                            else {
                                token.tokenStr = param.strValue;
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Parameter must be string constant or variable");
                            }
                            break;
                        case "ELEM":
                            param = stack.pop();
                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (!id.structure.equals("array")) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot call ELEM on non-array identifier");
                                }

                                try {
                                    paramM = (ResultList) parser.storageManager.getVariable(id.symbol);
                                }catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value must be array type");
                                }

                                res = Utility.builtInELEM(parser, paramM);

                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value of variable cannot be array");
                            }
                            break;

                        case "MAXELEM":
                            param = stack.pop();
                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (!id.structure.equals("array")) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot call ELEM on non-array identifier");
                                }

                                try {
                                    paramM = (ResultList) parser.storageManager.getVariable(id.symbol);
                                }catch (Exception e) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value must be array type");
                                }

                                res = Utility.builtInMAXELEM(parser, paramM);

                            } else {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Value of variable cannot be array");
                            }
                            break;

                        case "dateDiff":
                            if ( stack.size() < 2) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Did not supply enough parameters for function");
                            }

                            param2 = stack.pop();
                            param = stack.pop();

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);

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

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param2 = (ResultValue) parser.storageManager.getVariable(id.symbol);

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

                            param2 = stack.pop();
                            param = stack.pop();

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);

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

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.INTEGER) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-integer value with dateAdj function");
                                }

                                try {
                                    param2 = (ResultValue) parser.storageManager.getVariable(id.symbol);
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

                            param2 = stack.pop();
                            param = stack.pop();

                            if (param.dataType == SubClassif.IDENTIFIER) {
                                STEntry entry = parser.symbolTable.getSymbol(param.strValue);

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param = (ResultValue) parser.storageManager.getVariable(id.symbol);

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

                                if (entry.primClassif == Classif.EMPTY) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initilized");
                                }
                                STIdentifier id = (STIdentifier) entry;

                                if (id.dclType != SubClassif.DATE && id.dclType != SubClassif.STRING) {
                                    throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot use non-date value with dateDiff function");
                                }

                                try {
                                    param2 = (ResultValue) parser.storageManager.getVariable(id.symbol);

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
                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Have not implemented function to be used in expressions");

                    }
                    stack.push(res);
                    break;

                case OPERATOR:


                    // 0 = second operand
                    // 1 = first oeprand
                    Bool bOp1;
                    Bool bOp2;

                    ResultValue operand2  = stack.pop();
                    ResultValue operand1 = new ResultValue("", SubClassif.EMPTY);

                    if (operand2.dataType == SubClassif.IDENTIFIER) {
                        STEntry entry = parser.symbolTable.getSymbol(operand2.strValue);

                        if (entry.primClassif == Classif.EMPTY) {
                            throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                        }
                        try {
                            operand2 = (ResultValue) parser.storageManager.getVariable(operand2.strValue);
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

                    } else {
                        operand1  = stack.pop();

                        if (operand1.dataType == SubClassif.IDENTIFIER) {
                            STEntry entry = parser.symbolTable.getSymbol(operand1.strValue);

                            if (entry.primClassif == Classif.EMPTY) {
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Identifier must be initialized");
                            }
                            try {
                                operand1 = (ResultValue) parser.storageManager.getVariable(operand1.strValue);
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
                                System.out.printf("... %s %s %s is %s\n", operand1.strValue, token.tokenStr, operand2.strValue, res.strValue);
                                break;

                        }
                    }



                    stack.push(res);


            }



        }

        res = stack.pop();

        Result ret = res;


        if ( res.dataType == SubClassif.IDENTIFIER) {
            STEntry entry = parser.symbolTable.getSymbol(res.strValue);
            if (entry.primClassif == Classif.EMPTY) {
                // TODO: 4/8/2021 throw error on invalid identifier in expression
                throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Identifier must be initialized");
            }

            ret = parser.storageManager.getVariable(res.strValue);


        }

        return ret;
    }

    public static Numeric getNumeric(Parser parser, ResultValue res, String operator, String desc) throws PickleException{
        Numeric n = null;
        switch (res.dataType) {
            case IDENTIFIER:
                STEntry entry = parser.symbolTable.getSymbol(res.strValue);

                if (entry.primClassif == Classif.EMPTY) {
                    // TODO: 4/8/2021 throw error on invalid identifier in expression
                    throw new PickleException();
                }

                res = (ResultValue) parser.storageManager.getVariable(res.strValue);
                if (res.dataType != SubClassif.INTEGER && res.dataType != SubClassif.FLOAT) {
                    throw new ScannerParserException(parser.scanner.currentToken, parser.scanner.sourceFileNm, "Cannot perform unary minus on non-numeric operand identifier:");
                }


                n = new Numeric(parser, res, operator, desc);
                break;
            case INTEGER:
            case FLOAT:
                n = new Numeric(parser, res, operator, desc);
        }

        return n;
    }



}

