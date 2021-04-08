package pickle;

import java.util.ArrayList;
import java.util.Stack;

public class Expr {

    public static ArrayList<Token> postFixExpr(Parser parser) throws PickleException {
        ArrayList<Token> postfix = new ArrayList<Token>();
        Stack<Token> stack = new Stack<Token>();

        while(!parser.scanner.currentToken.tokenStr.equals(",") && !parser.scanner.currentToken.tokenStr.equals(";")) {

            switch (parser.scanner.currentToken.primClassif) {
                case OPERAND:
                    //System.out.printf("Outing operand '%s' onto stack\n", parser.scanner.currentToken.tokenStr);
                    postfix.add(parser.scanner.currentToken);
                    break;
                case OPERATOR:
                    while (!stack.empty()) {
                        System.out.printf("Checking precedence:\n\t'%s'\t'%s'\n\t'%d'\t'%d'\n", parser.scanner.currentToken.tokenStr, stack.peek().tokenStr, parser.scanner.currentToken.precedence, stack.peek().stkPrecedence);
                        if (parser.scanner.currentToken.precedence > stack.peek().stkPrecedence) {
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
                                Token popped = stack.pop();
                                while (!popped.tokenStr.equals("(")) {
                                    postfix.add(popped);
                                    popped = stack.pop();
                                }
                                break;
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



}
