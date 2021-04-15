package pickle;

import java.util.ArrayList;
import java.util.Stack;

public class Expr {

    public static ArrayList<Token> postFixExpr(Parser parser) throws PickleException {
        ArrayList<Token> postfix = new ArrayList<Token>();
        Stack<Token> stack = new Stack<Token>();

        while(!parser.scanner.currentToken.tokenStr.equals(",") && !parser.scanner.currentToken.tokenStr.equals(";") && !parser.scanner.currentToken.tokenStr.equals(":")) {

            switch (parser.scanner.currentToken.primClassif) {
                case OPERAND:
                    //System.out.printf("Outing operand '%s' onto stack\n", parser.scanner.currentToken.tokenStr);
                    postfix.add(parser.scanner.currentToken);
                    break;
                case OPERATOR:
                    while (!stack.empty()) {
                        System.out.printf("Checking precedence:\n\t'%s'\t'%s'\n\t'%d'\t'%d'\n",
                                parser.scanner.currentToken.tokenStr,
                                stack.peek().tokenStr,
                                parser.scanner.currentToken.operatorPrecedence.tokenPrecedence,
                                stack.peek().operatorPrecedence.stackPrecedence);

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
                                    while (!stack.empty()) {
                                        Token popped = stack.pop();
                                        if (popped.tokenStr.equals("(")) {
                                            break;
                                        }
                                        postfix.add(popped);
                                    }
                                    break;
                                }
                            default:
                                // TODO: 4/8/2021 throw error for invalid seperator in expression
                        }

                        break;
                default:
                    // TODO: 4/8/2021 throw error for invalid token in expression

            }

            parser.scanner.getNext();
        }
        while (!stack.empty()) {
            postfix.add(stack.pop());
        }

        return postfix;
    }


    public static ResultValue evaluatePostFix(Parser parser, ArrayList<Token> postFix) throws PickleException {
        Stack<ResultValue> stack = new Stack<ResultValue>();

        ResultValue res;


        for (Token token : postFix) {
            switch (token.primClassif) {
                case OPERAND:
                    stack.push(new ResultValue(token.tokenStr, token.subClassif));
                    break;
                case OPERATOR:
                    ResultValue[] resValues = new ResultValue[2];
                    int i = 0;
                    while (!stack.empty() && i < 2) {
                        resValues[i] = stack.pop();
                        if (resValues[i].dataType == SubClassif.IDENTIFIER) {
                            STEntry entry = parser.symbolTable.getSymbol(resValues[i].strValue);
                            if (entry.primClassif == Classif.EMPTY) {
                                // TODO: 4/8/2021 throw error on invalid identifier in expression
                                throw new PickleException();
                            }

                            resValues[i] = (ResultValue) parser.storageManager.getVariable(resValues[i].strValue);
                        }

                        i++;
                    }

                    // 0 = second operand
                    // 1 = first oeprand
                    Bool bOp1;
                    Bool bOp2;


                    switch (token.tokenStr) {

                            case "+":
                                res = Utility.add(parser,
                                        getNumeric(parser, resValues[1], token.tokenStr, "First Operand for addition"),
                                        getNumeric(parser, resValues[0], token.tokenStr, "Second Operand for addition"));
                                break;
                            case "-":

                                if (token.operatorPrecedence == OperatorPrecedence.UNARYMINUS) {
                                    res = Utility.unaryMinus(parser,
                                            getNumeric(parser, resValues[0], token.tokenStr, "First Operand for unary minus"));

                                    if (i >= 1) {
                                        stack.push(resValues[1]);
                                    }

                                } else {
                                    res = Utility.subtract(parser,
                                            getNumeric(parser, resValues[1], token.tokenStr, "First Operand for subtraction"),
                                            getNumeric(parser, resValues[0], token.tokenStr, "Second Operand for subtraction"));
                                }
                                break;
                            case "*":
                                res = Utility.multiply(parser,
                                        getNumeric(parser, resValues[1], token.tokenStr, "First Operand for multiply"),
                                        getNumeric(parser, resValues[0], token.tokenStr, "Second Operand for multiply"));
                                break;
                            case "/":
                                res = Utility.divide(parser,
                                        getNumeric(parser, resValues[1], token.tokenStr, "First Operand for divide"),
                                        getNumeric(parser, resValues[0], token.tokenStr, "Second Operand for divide"));
                                break;
                            case "^":
                                res = Utility.power(parser,
                                        getNumeric(parser, resValues[1], token.tokenStr, "First Operand for power"),
                                        getNumeric(parser, resValues[0], token.tokenStr, "Second Operand for power"));
                                break;
                            case ">":
                                res = Utility.greaterThan(parser, resValues[1], resValues[0]);
                                break;
                            case "<":
                                res = Utility.lessThan(parser, resValues[1], resValues[0]);
                                break;
                            case ">=":
                                res = Utility.greaterThanOrEqualTo(parser, resValues[1], resValues[0]);
                                break;
                            case "<=":
                                res = Utility.lessThanOrEqualTo(parser, resValues[1], resValues[0]);
                                break;
                            case "==":
                                res = Utility.equal(parser, resValues[1], resValues[0]);
                                break;
                            case "!=":
                                res = Utility.notEqual(parser, resValues[1], resValues[0]);
                                break;
                            case "and":
                                bOp1 = new Bool(parser, resValues[1]);
                                bOp2 = new Bool(parser, resValues[0]);
                                res = Utility.boolAnd(parser, bOp1, bOp2);
                                break;
                            case "or":
                                bOp1 = new Bool(parser, resValues[1]);
                                bOp2 = new Bool(parser, resValues[0]);
                                res = Utility.boolOr(parser, bOp1, bOp2);
                                break;
                            case "not":
                                bOp2 = new Bool(parser, resValues[0]);
                                res = Utility.boolNot(parser, bOp2);

                                if (i >= 1) {
                                    stack.push(resValues[1]);
                                }

                                break;
                            default:
                                throw new ScannerParserException(token, parser.scanner.sourceFileNm, "Cannot perform operation with invalid OPERATOR");


                    }

                    if (parser.bShowExpr) {
                        switch (token.operatorPrecedence) {
                            case UNARYMINUS:
                                break;
                            default:
                                System.out.printf("... %s %s %s is %s\n", resValues[1].strValue, token.tokenStr, resValues[0].strValue, res.strValue);
                                break;

                        }
                    }


                    stack.push(res);


            }



        }

        res = stack.pop();

        if (res.dataType == SubClassif.IDENTIFIER) {
            STEntry entry = parser.symbolTable.getSymbol(res.strValue);
            if (entry.primClassif == Classif.EMPTY) {
                // TODO: 4/8/2021 throw error on invalid identifier in expression
                throw new PickleException();
            }

            res = (ResultValue) parser.storageManager.getVariable(res.strValue);
        }

        return res;


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

