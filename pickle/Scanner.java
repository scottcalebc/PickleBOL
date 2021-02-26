package pickle;

import java.util.ArrayList;
import java.util.Arrays;


public class Scanner {

    private final static String delimiters = " \t:;()'\"=!<>+-*/[]#,^\n";   // Terminate a token
    private final static String operators = "+-*/<>=!^";                    // All operators
    private final static String separators = "(),:;[]";                     // All seperators

    protected ArrayList<String> sourceLineM;                                // List of all source file lines
    protected char[]            textCharM;
    protected SymbolTable       symbolTable;
    protected String            sourceFileNm;                               // Source file name
    protected int               iSourceLineNr;                              // Source line number
    protected int               iColPos;                                    // Source column position

    public  Token               currentToken;                               // Current evaluated Token
    public  Token               nextToken;                                  // Next evaluated Token


    /**
     *
     * Returns a Scanner object to read pickle source code and tokenize the input.
     * <p>
     *
     * @param sourceFileNm  File name of pickle source file
     * @param symbolTable   Symbol Table object holding global symbols
     *
     * @throws PickleException      Some Error during file open or tokenization
     *
     */
    public Scanner(String sourceFileNm, SymbolTable symbolTable)
            throws PickleException {

        // Set all local variables
        this.sourceFileNm = sourceFileNm;
        this.symbolTable = symbolTable;
        this.currentToken = new Token();
        this.nextToken = new Token();
        this.sourceLineM = new ArrayList<String>();
        this.textCharM = new char[0];

        // Attempt to read the all lines into sourceLineM
        try {
            java.io.File file = new java.io.File(sourceFileNm);
            java.util.Scanner scanner = new java.util.Scanner(file);

            while (scanner.hasNextLine()) {
                sourceLineM.add(scanner.nextLine());
            }
        } catch (java.io.FileNotFoundException e) {
            throw new FileNotFoundException(sourceFileNm);
        }

        // Pre-set all line and column positions
        iSourceLineNr = 0;
        iColPos = 0;

        getToken();         // Load first token into next token
    }


    /**
     *
     * Set position in the source file to begin reading from
     * <p>
     *
     * @param iSourceLineNr         Source Line Number to set
     * @param iColPos               Column position to set
     *
     */
    public void setPosition(int iSourceLineNr, int iColPos) throws PickleException
    {
        this.iSourceLineNr = iSourceLineNr;     //sets current source line
        this.iColPos = iColPos;                 // sets current column position

        textCharM = sourceLineM.get(iSourceLineNr - 1).toCharArray();

        // resets tokens
        currentToken = new Token();
        nextToken = new Token();

        // preload nextToken
        getToken();
    }


    /**
     *
     * Sets the given token object properties
     * <p>
     * Helper function to set token properties
     *
     * @param token     Token object to modify
     * @param tokenStr  Token string to set in token
     *
     */
    public void setToken(Token token, String tokenStr)
    {
        token.tokenStr = tokenStr;
        token.iSourceLineNr = iSourceLineNr;
        token.iColPos = iColPos - tokenStr.length();
    }


    public boolean checkStringArray(String tokenStr, String[] checkArray) {
        return Arrays.stream(checkArray).anyMatch(tokenStr::matches);
    }



    /**
     *
     * Helper function to get the next source line from array list
     * <p>
     * Grabs the next source line and places it into instance variable textCharM
     * also resets column position for getNext() and prints debug info of current line
     *
     */
    public void getNextSourceLine()
    {
        if (iSourceLineNr < sourceLineM.size())
        {
            String currLine = sourceLineM.get(iSourceLineNr++);         //Get Next Line and Increment iSourceLineNr
            textCharM = currLine.toCharArray();                         //Convert current line to char[]
            iColPos = 0;


            System.out.printf("\t%d %s\n", iSourceLineNr, currLine);    //Print Debug info of current line
        }

    }

    /**
     *
     * Gets the next valid line into textCharM
     * <p>
     * This method will continue to call getNextSourceLine() until a line with characters other
     * than just spaces, tabs, or new lines is present in textCharM
     *
     */
    public void getNextValidLine()
    {
        while (iSourceLineNr < sourceLineM.size() && iColPos >= textCharM.length)
        {
            getNextSourceLine();
            Utility.skipWhitespace(this);
        }
    }


    public String convertStringEscapeCharacters(String tokenStr) throws ScannerParserException {
        char currCharM[] = tokenStr.toCharArray();
        char retCharM[] = new char[currCharM.length];
        int iRet = 0;

        for(int i = 0; i < currCharM.length; i++) {
            if (currCharM[i] == '\\' && i+1 < currCharM.length) {
                String escapeCode = "" + currCharM[i] + currCharM[i+1];

                switch (escapeCode) {
                    case "\\\"":
                        retCharM[iRet] = '"';
                        break;
                    case "\\'":
                        retCharM[iRet] = '\'';
                        break;
                    case "\\\\":
                        retCharM[iRet] = '\\';
                        break;
                    case "\\t":
                        retCharM[iRet] = 0x09;
                        break;
                    case "\\n":
                        retCharM[iRet] = 0x0A;
                        break;
                    case "\\a":
                        retCharM[iRet] = 0x07;
                        break;
                    default:
                        setToken(nextToken, escapeCode);
                        throw new ScannerParserException(nextToken, sourceFileNm, "Invalid escape character");
                }

                iRet++;
                i++;
                continue;
            }

            retCharM[iRet] = currCharM[i];
            iRet++;

        }

        return String.valueOf(retCharM, 0, iRet);
    }

    /**
     * Retrieves a string constant out of textCharM
     * <p>
     *
     * @param type                      ' or " ; determines when to stop scanning textCharM
     * @return                          token string gathered from textCharM
     * @throws StringConstantException
     */
    public String getStringConstant(char type) throws ScannerParserException
    {
        String tokenStr = "";

        while (iColPos < textCharM.length &&
                (textCharM[iColPos] != type || textCharM[iColPos - 1] == '\\') )
        {
            tokenStr += textCharM[iColPos++];
        }

        // if reached end of line and last character does not close string constant throw error
        if (iColPos >= textCharM.length && textCharM[iColPos - 1] != type)
        {
            setToken(nextToken, tokenStr);
            throw new StringConstantException(nextToken, sourceFileNm);
        }

        iColPos++;                                  // Skips past ' or " left in textCharM
        nextToken.subClassif = SubClassif.STRING;   // sets tokens sub classification

        return convertStringEscapeCharacters(tokenStr);
    }

    /**
     *
     * Validates that token string contains an Integer or Float
     * <p>
     *
     * @param tokenStr                      tokenStr containing integer or float
     * @throws NumericConstantException
     *
     */
    public void getIntegerFloatConstant(String tokenStr) throws NumericConstantException
    {
        boolean floatConstant = false;                  // flag to verify single '.' in float constant

        // loop through entire tokenStr
        for ( int i = 1; i < tokenStr.length(); i++ )
        {

            // Used to validate float constants
            if ( tokenStr.charAt(i) == '.' && !floatConstant )
            {
                floatConstant = true;
                continue;
            }

            // if character is not a digit, throw numeric error
            if ( ! Character.isDigit(tokenStr.charAt(i)) )
            {
                setToken(nextToken, tokenStr);
                throw new NumericConstantException(nextToken, sourceFileNm);

            }
        }

        // assign subclassification
        nextToken.subClassif = (floatConstant) ? SubClassif.FLOAT : SubClassif.INTEGER;

    }

    /**
     *
     * Grabs the next token from the source file
     * <p>
     *
     * @throws ScannerParserException               String or Numeric Constant error
     *
     */
    public void getToken() throws ScannerParserException
    {
        nextToken = new Token();    // create new token
        String tokenStr = "";       // create new tokenStr to assign to token later

        getNextValidLine();

        // if reached end of source file, exit
        if (iSourceLineNr >= sourceLineM.size() && iColPos >= textCharM.length)
            return;

        // Todo: use utility skipWhiteSpace
        Utility.skipWhitespace(this);

        // Continues to add non-delimeter characters to tokenStr
        while (iColPos < textCharM.length
                && delimiters.indexOf(textCharM[iColPos]) < 0)
        {
            tokenStr += textCharM[iColPos++];
        }

        // if character at textCharM[iColPos] was a delimiter tokenStr would be empty
        // this sets the tokenStr to the delimeter to verify type
        if (tokenStr.length() == 0)
            tokenStr = Character.toString(textCharM[iColPos++]);


        // first check if comment before classifying
        if (Utility.skipComment(this, tokenStr))
        {
            getNextValidLine();
            getToken();                     // get the next token
            return;                         // exit current getToken routine as previous routine performed classification
        }


        tokenStr = classifyPrimary(tokenStr);


        setToken(nextToken, tokenStr);

    }


    /**
     *
     * Get next token from source file
     * <p>
     *
     * @return                          Token String parsed from source file
     * @throws ScannerParserException   Error in token
     *
     */
    public String getNext() throws ScannerParserException
    {

        currentToken = nextToken;       // Gets the current token
        getToken();                     // Get Next token

        return currentToken.tokenStr;
    }


    /**
     *
     * Sets the primary classification for the token being parsed
     * <p>
     *
     * @param tokenStr                  Current parsed token string
     * @return                          Modified token string to be set to token
     * @throws ScannerParserException   Error on Token
     *
     */
    public String classifyPrimary(String tokenStr) throws ScannerParserException
    {
        STEntry entry = symbolTable.getSymbol(tokenStr);
        if (separators.contains(tokenStr))
        {
            nextToken.primClassif = Classif.SEPARATOR;

            if (iSourceLineNr >= sourceLineM.size() && iColPos >= textCharM.length)
            {
                nextToken.primClassif = Classif.EOF;
            }

            // todo: classify separator?
        }

        else if ( operators.contains(tokenStr) )
        {
            if (iColPos < textCharM.length && operators.indexOf(textCharM[iColPos]) > 0) {
                tokenStr += textCharM[iColPos++];
            }


            nextToken.primClassif = Classif.OPERATOR;

            //todo: classify operator
        }

        else if ( entry.primClassif != Classif.EMPTY ) {
            nextToken.primClassif = entry.primClassif;

            if (entry instanceof STControl) {
                nextToken.subClassif = ((STControl) entry).subClassif;
            } else if (entry instanceof  STFunction) {
                nextToken.subClassif = SubClassif.BUILTIN; // ???
            }
        }

        else
        {

            nextToken.primClassif = Classif.OPERAND;

            tokenStr = classifyOperand(tokenStr);

        }

        return tokenStr;

    }

    /**
     *
     * Sets the sub-classification for the Operand Primary Classification
     * <p>
     *
     * @param tokenStr                  Token String being parsed
     * @return                          Modified token string
     * @throws ScannerParserException   Error on token
     *
     */
    public String classifyOperand(String tokenStr) throws ScannerParserException
    {
        // if first character is begin of string constant, validate string constant
        switch (tokenStr.charAt(0)) // Check first character to determine type of Operand
        {
            case '\"':
                tokenStr = getStringConstant('\"');
                break;
            case '\'':
                tokenStr = getStringConstant('\'');
                break;
            case 'T':
            case 'F':
                nextToken.subClassif = SubClassif.BOOLEAN;
                break;
            default:
                // What if token is delimiter not in seperators or operators?
                if (delimiters.contains(tokenStr)) {
                    setToken(nextToken, tokenStr);
                    throw new InvalidTokenException(nextToken, sourceFileNm);
                }
                // If string is not malformed & not a delimiter check for numeric constant
                else if (Character.isDigit(tokenStr.charAt(0)))
                {
                    getIntegerFloatConstant(tokenStr);
                }

                // Finally no errors should result in simply an Identifier
                else
                {
                    nextToken.subClassif = SubClassif.IDENTIFIER;
                }
        }

        // return current token string after any modifications to attach to token
        return tokenStr;
    }


}



