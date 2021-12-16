package pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uppaal.Automaton;
import uppaal.Location;
import uppaal.NTA;
import uppaal.Queries;
import uppaal.Query;

/**
 * This represents the ***first*** step in our process

 * This class is used to add a formula to a jUPPAAL model
 * If an observer is necessary, this class will return that information
 * 
 * @author Marc Carwehl
 *
 */

public class Formula {

	private String scope;
	private String pattern;
	private String [] formulas;
	private NTA observer;
	private String templatePath;
	private String timed;
	private Queries queries;
	private final static String QUERIES_FILE = "/state-based-queries.properties";
	private final static String QUERIES_FILE_FLAG = "/state-based-queries-flag.properties";
	private Properties stateBasedQueriesFormula;
	private Properties stateBasedQueriesFlag;
	private int lowerTimebound;
	private int upperTimebound;
	private final String globalClockName = "gc";
	
	private boolean observerNeeded;
	private boolean flagsNeeded;


	
	/**
	 * Constructor 
	 * Note the upper/lower cases, as we use those to find the observer's path
	 * 
	 * @param scope - the scope, must be in {After, Before, Between, Until, Globally}
	 * @param pattern - the pattern, must be in the set of supported patterns, 
	 * 		see safespec wiki for more information 
	 * @param templatePath - path where the observer templates can be found
	 * @param timed - true for "Timed", false for "Untimed"
	 * @param lowerTimebound - default: -1
	 */
	public Formula(String scope, String pattern, String templatePath, boolean timed, int lowerTimebound, int upperTimebound) {
		//this.upperTimebound = -1;
		//this.lowerTimebound = -1;
		this.observerNeeded = false;
		this.flagsNeeded = false;
		this.lowerTimebound = lowerTimebound;
		this.upperTimebound = upperTimebound;
		
		this.observer = null;
		
		this.stateBasedQueriesFormula = new Properties();
		InputStream in = getClass().getResourceAsStream(QUERIES_FILE);
		try {
			this.stateBasedQueriesFormula.load(in);
			//in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.stateBasedQueriesFlag = new Properties();
		in = getClass().getResourceAsStream(QUERIES_FILE_FLAG);
		try {
			this.stateBasedQueriesFlag.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.scope = scope;
		this.pattern = pattern;
		this.templatePath = templatePath;
		//TODO test whether values are permitted
		if(timed)
			this.timed = "Timed";
		else
			this.timed = "Untimed";
	}
	/**
	 * This method is only meant as a fix because one pattern (Existence untimed after) needs another query: Absence globally untimed
	 * @param scope
	 * @param pattern
	 * @param templatePath
	 */
	private void addFormula(String q) {
		
	}
	
	/**
	 * method grabs the respective formula that can then be added to the model. 
	 * 
	 * This method also checks whether an observer is necessary and if so 
	 * loads it as NTA model
	 * 
	 */
	public void grabFormulas() {
		//First look through patterns that don't require observers
		String key = getPSPKey();
		String queryFormula = this.stateBasedQueriesFormula.getProperty(key);

		if(queryFormula != null) {
			this.formulas = new String[1];
			this.formulas[0] = queryFormula;
			this.observerNeeded = false;
			setQueries();
			return;
		} 
		
		queryFormula = this.stateBasedQueriesFlag.getProperty(key);
		if(queryFormula != null) {
			this.formulas = new String[1];
			this.formulas[0] = queryFormula;
			this.observerNeeded = false;
			this.flagsNeeded = true;
			setQueries();
			//the existence after untimed is a flag based combination. It requires another formula: The absence (globally) of $Q
			if(this.pattern.equals("Existence")) {
				String[] f = new String[2];
				f[0] = this.formulas[0];
				f[1] = "A[] not $Q";
				this.formulas = f.clone();
			}
		

		}

		else {
			//Then go through the observer templates and find the right one
			//Load it as jUPPAAL-model
			String path = this.templatePath + 
					pattern + 
					"_" + "State" + "_" +
					this.scope + "_" +
					this.timed + ".xml";
			this.observer = new NTA(path);
			this.observerNeeded = true;
			//Now load its queries
			this.queries = this.observer.getQueries();
			List<Query> queryList = this.queries.getQueries();
			//Initialize the class variable array with the correct size
			this.formulas = new String[queryList.size()];
			//loop through the queries and write each one to the class array
			int i = 0;
			for(Query query : queryList){
				this.formulas[i++] = query.getFormula();
			}
		}
	}
	//this is only done when we don't need an observer
	private void setQueries() {
		this.queries = new Queries();
		for(int i = 0; i < this.formulas.length; i++) {
			queries.getQueries().add(new Query());
			queries.getQueries().get(i).setFormula(this.formulas[i]);
		}
		
	}
	
	public void replaceFormula(Map<String, String> properties, NTA system) {
		for(String defaultValue: properties.keySet()) {
			String replacedBy = properties.get(defaultValue);
			for(Automaton a :system.getAutomata()) {
				for(Location l : a.getLocations()) {
					if(l.getName() != null && l.getName().getName().equals(replacedBy)) {
						replacedBy = a.getName().getName() + "." + replacedBy;
					}
				}
			}
			for(Query q : this.queries.getQueries()) {
				String query = q.getFormula();
				query = query.replace("$"+defaultValue, replacedBy);
				q.setFormula(query);
			}
		}
	}
	
	/**
	 * 
	 * @return the formulas as Strings that fit pattern and scope
	 */
	public String[] getFormulas() {
		return this.formulas;
	}
	
	/**
	 * 
	 * @return - NTA of the jUPPAAL model of the observer that fits pattern and scope
	 */
	public NTA getObserver() {
		return this.observer;
	}
	/***
	 * This method sets the queries that were fetched in grabFormulas() to the system model that is 
	 * given as parameter. 
	 * Make sure to call grabFormulas() first!!
	 * 
	 * 
	 * @param system - NTA model of the system
	 */
	public void addFormulasToNTA(NTA system) {
		if(this.queries != null)
			system.setQueries(this.queries);
		else 
			System.out.println("no queries fetched");
	}
	
	/**
	 * method to check whether one of the formulas need a global clock definition
	 * if so, use Observer.addGlobalClock(NTA system) to add the definition of a global clock gc
	 * @return true iff at least one of the formulas needs a global clock
	 */
	public boolean globalClockNecessary() {
		for(int i = 0; i < this.formulas.length; i++) {
			if(this.formulas[i].contains("gc"))
				return true;
		}
		return false;
	}
	
	/***
	 *  adds the definition clock gc; to the system definition
	 * 
	 * @param system - NTA model of the system
	 * 
	 */
	public void addGlobalClock(NTA system) {
		system.getSystemDeclaration().addDeclaration("clock " + this.globalClockName + ";");
	}
	
	private String getPSPKey() {
		String key = this.pattern + "_State_" + this.scope +"_"+ this.timed;
		return key;
	}
	

	public int getLowerTimebound() {
		return this.lowerTimebound;
	}

	public int getUpperTimebound() {
		return this.upperTimebound;
	}
	
	public boolean getObserverNeeded() {
		return this.observerNeeded;
	}
	
	public boolean getFlagsNeeded() {
		return this.flagsNeeded;
	}
	
}
