package pickle;

import java.util.HashMap;

public class SymbolTable {

	private HashMap<String, HashMap<String, STEntry>> tables;		// HashMap to hold all symbol tables
	private HashMap<String, STEntry> globalST;						// Global symbol table

	public SymbolTable() {
		this.tables = new HashMap<>();
		this.globalST = new HashMap<>();
		initGlobal();
		this.tables.put("global", globalST);
	}

	public STEntry getSymbol(String symbol) {
		//check correct symbol table(s?) for requested symbol

		return null;
	}

	public void putSymbol(String symbol, STEntry entry) {

	}

	private void addSymbolTable(String tableName, HashMap<String, STEntry> entry) {
		tables.put(tableName, entry);
	}
	
	private void initGlobal() {
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
		this.globalST.put("Date", new STControl("Date", Classif.CONTROL, SubClassif.DECLARE));

		this.globalST.put("and", new STEntry("and", Classif.OPERATOR));
		this.globalST.put("or", new STEntry("or", Classif.OPERATOR));
		this.globalST.put("not", new STEntry("not", Classif.OPERATOR));
		this.globalST.put("in", new STEntry("in", Classif.OPERATOR));
		this.globalST.put("notin", new STEntry("notin", Classif.OPERATOR));

		this.globalST.put("print", new STFunction("print", Classif.FUNCTION, SubClassif.VOID, SubClassif.BUILTIN));
		this.globalST.put("LENGTH", new STFunction("LENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN));
		this.globalST.put("MAXLENGTH", new STFunction("MAXLENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN));
		this.globalST.put("SPACES", new STFunction("SPACES", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN));
		this.globalST.put("ELEM", new STFunction("ELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN));
		this.globalST.put("MAXELEM", new STFunction("MAXELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN));
	}

}