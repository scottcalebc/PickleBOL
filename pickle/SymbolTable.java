package pickle;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {

	public HashMap<String, STEntry> symbolTable;		// Global symbol table
	public String scopeName;

	/**
	 *
	 * Returns a SymbolTable object that holds the global symbol table of the pickle code
	 * <p>
	 *
	 */
	public SymbolTable()
	{
		this.symbolTable = new HashMap<>();
		this.scopeName = "Global";
		initGlobal();
	}

	/**
	 *
	 * Returns a SymbolTable object that hold the symbol table for a programmer defined function
	 *
	 * @param fcnName	-	name of function for symbol table
	 */
	public SymbolTable(String fcnName)
	{
		this.scopeName = fcnName;
		this.symbolTable = new HashMap<>();
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
		return this.symbolTable.containsKey(symbol) ? this.symbolTable.get(symbol) : new STEntry("", Classif.EMPTY);
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
		this.symbolTable.put(symbol, entry);
	}

	/**
	 *
	 * Initializes the global symbol table's values
	 * <p>
	 *
	 */
	private void initGlobal()
	{

		this.symbolTable.put("return", new STControl("return", Classif.CONTROL, SubClassif.FLOW));
		this.symbolTable.put("def", new STControl("def", Classif.CONTROL, SubClassif.FLOW));
		this.symbolTable.put("enddef", new STControl("enddef", Classif.CONTROL, SubClassif.END));
		this.symbolTable.put("if", new STControl("if", Classif.CONTROL, SubClassif.FLOW));
		this.symbolTable.put("endif", new STControl("endif", Classif.CONTROL, SubClassif.END));
		this.symbolTable.put("else", new STControl("else", Classif.CONTROL, SubClassif.END));
		this.symbolTable.put("for", new STControl("for", Classif.CONTROL, SubClassif.FLOW));
		this.symbolTable.put("endfor", new STControl("endfor", Classif.CONTROL, SubClassif.END));
		this.symbolTable.put("while", new STControl("while", Classif.CONTROL, SubClassif.FLOW));
		this.symbolTable.put("endwhile", new STControl("endwhile", Classif.CONTROL, SubClassif.END));
		this.symbolTable.put("Int", new STControl("Int", Classif.CONTROL, SubClassif.DECLARE));
		this.symbolTable.put("Float", new STControl("Float", Classif.CONTROL, SubClassif.DECLARE));
		this.symbolTable.put("String", new STControl("String", Classif.CONTROL, SubClassif.DECLARE));
		this.symbolTable.put("Bool", new STControl("Bool", Classif.CONTROL, SubClassif.DECLARE));
		this.symbolTable.put("Date", new STControl("Date", Classif.CONTROL, SubClassif.DECLARE));
		
		this.symbolTable.put("and", new STEntry("and", Classif.OPERATOR));
		this.symbolTable.put("or", new STEntry("or", Classif.OPERATOR));
		this.symbolTable.put("not", new STEntry("not", Classif.OPERATOR));
		this.symbolTable.put("in", new STEntry("in", Classif.OPERATOR));
		this.symbolTable.put("notin", new STEntry("notin", Classif.OPERATOR));

		ArrayList<SubClassif> print = new ArrayList<SubClassif>();
		ArrayList<SubClassif> string = new ArrayList<SubClassif>();
		string.add(SubClassif.STRING);
		ArrayList<SubClassif> array = new ArrayList<SubClassif>();
		array.add(SubClassif.VOID);

		final int VAR_ARGS = -1;  // Value for variable args

		this.symbolTable.put("print", new STFunction("print", Classif.FUNCTION, SubClassif.VOID, SubClassif.BUILTIN, VAR_ARGS, print, new Builtin() {
			@Override
			public ResultValue execute(ArrayList<Result> param) {
				//return Utility.builtInPrint(param);
				return null;
			}
		}));
		this.symbolTable.put("LENGTH", new STFunction("LENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1, string, new Builtin() {
			@Override
			public ResultValue execute(ArrayList<Result> param) {
				return Utility.builtInLENGTH(param);
			}
		}));
		this.symbolTable.put("SPACES", new STFunction("SPACES", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1, string, new Builtin() {
			@Override
			public ResultValue execute(ArrayList<Result> param) {
				return Utility.builtInSPACES(param);
			}
		}));
		this.symbolTable.put("ELEM", new STFunction("ELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1, array, new Builtin() {
			@Override
			public ResultValue execute(ArrayList<Result> param) {
				return Utility.builtInELEM(param);
			}
		}));
		this.symbolTable.put("MAXELEM", new STFunction("MAXELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1, array,new Builtin() {
			@Override
			public ResultValue execute(ArrayList<Result> param) {
				return Utility.builtInMAXELEM(param);
			}
		}));
	}

}