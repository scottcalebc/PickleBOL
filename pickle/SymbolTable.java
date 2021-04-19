package pickle;

import java.util.HashMap;

public class SymbolTable {

	private HashMap<String, STEntry> globalST;		// Global symbol table
	private final int VAR_ARGS = -1;				// Value for variable args

	/**
	 *
	 * Returns a SymbolTable object that holds the symbol tables of the pickle code
	 * <p>
	 *
	 */
	public SymbolTable()
	{
		this.globalST = new HashMap<>();
		initGlobal();
	}

	/**
	 *
	 * Returns the symbol table entry for a given symbol
	 * <p>
	 *
	 * @param symbol	Name of symbol in the symbol table
	 * @return			Symbol table entry (STEntry), empty STEntry object if not found
	 */
	public STEntry getSymbol(String symbol)
	{
		return this.globalST.containsKey(symbol) ? this.globalST.get(symbol) : new STEntry("", Classif.EMPTY);
	}

	/**
	 *
	 * Stores the symbol and its corresponding entry in the symbol table
	 * <p>
	 *
	 * @param symbol	Name of symbol to be stored
	 * @param entry		STEntry of symbol to be stored (data or values of symbol)
	 *
	 */
	public void putSymbol(String symbol, STEntry entry)
	{
		this.globalST.put(symbol, entry);
	}

	/**
	 *
	 * Initializes the global symbol table's values
	 * <p>
	 *
	 */
	private void initGlobal()
	{
		this.globalST.put("enddef", new STControl("enddef", Classif.CONTROL, SubClassif.END));
		this.globalST.put("if", new STControl("if", Classif.CONTROL, SubClassif.FLOW));
		this.globalST.put("endif", new STControl("endif", Classif.CONTROL, SubClassif.END));
		this.globalST.put("else", new STControl("else", Classif.CONTROL, SubClassif.END));
		this.globalST.put("for", new STControl("for", Classif.CONTROL, SubClassif.FLOW));
		this.globalST.put("endfor", new STControl("endfor", Classif.CONTROL, SubClassif.END));
		this.globalST.put("while", new STControl("while", Classif.CONTROL, SubClassif.FLOW));
		this.globalST.put("endwhile", new STControl("endwhile", Classif.CONTROL, SubClassif.END));
		this.globalST.put("Int", new STControl("Int", Classif.CONTROL, SubClassif.DECLARE));
		this.globalST.put("Float", new STControl("Float", Classif.CONTROL, SubClassif.DECLARE));
		this.globalST.put("String", new STControl("String", Classif.CONTROL, SubClassif.DECLARE));
		this.globalST.put("Bool", new STControl("Bool", Classif.CONTROL, SubClassif.DECLARE));
		this.globalST.put("pickle.Date", new STControl("pickle.Date", Classif.CONTROL, SubClassif.DECLARE));

		this.globalST.put("and", new STEntry("and", Classif.OPERATOR));
		this.globalST.put("or", new STEntry("or", Classif.OPERATOR));
		this.globalST.put("not", new STEntry("not", Classif.OPERATOR));
		this.globalST.put("in", new STEntry("in", Classif.OPERATOR));
		this.globalST.put("notin", new STEntry("notin", Classif.OPERATOR));

		this.globalST.put("print", new STFunction("print", Classif.FUNCTION, SubClassif.VOID, SubClassif.BUILTIN, VAR_ARGS));
		this.globalST.put("LENGTH", new STFunction("LENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1)); 			//TODO: acutal number?
		this.globalST.put("MAXLENGTH", new STFunction("MAXLENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1));	//TODO: acutal number?
		this.globalST.put("SPACES", new STFunction("SPACES", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 0));			//TODO: acutal number?
		this.globalST.put("ELEM", new STFunction("ELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1));				//TODO: acutal number?
		this.globalST.put("MAXELEM", new STFunction("MAXELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1));		//TODO: acutal number?
	}

}