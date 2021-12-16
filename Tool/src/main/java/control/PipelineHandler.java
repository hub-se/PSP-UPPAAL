package control;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import pipeline.Formula;
import pipeline.Observer;
import pipeline.SystemModelModifier;
import pipeline.XMLModifier;
import uppaal.NTA;

/**
 * This class is used to call the pipeline 
 * @author Marc Carwehl
 *
 */
public class PipelineHandler {
	String TEMPLATE_LOCATION = "./observer_templates/";
	String systemFile;
	private String pattern;
	private String scope;
	private boolean timed;
	
	//<defaultValue, replacedBy>
	private Map<String, String> properties;	
	
	
	public PipelineHandler(String systemFile) {
		this.systemFile = systemFile;
		this.timed = false;
		this.scope = "Globally";
		this.pattern = "Universality";
		this.properties = new HashMap<String, String>();
	}
	
	public PipelineHandler(String systemFile, String pattern) {
		this.systemFile = systemFile;
		this.timed = false;
		this.scope = "Globally";
		this.setPattern(pattern);
		this.properties = new HashMap<String, String>();
	}
	
	public void setTemplateLocation(String location) {
		this.TEMPLATE_LOCATION = location;
	}
	
	public void setPattern(String pattern) {
		switch(pattern) {
			case "Absence":
			case "Universality":
			case "Existence":
			case "BoundedExistence":
			case "Response":
				this.pattern = pattern;
				return;
			default:
				System.out.println("Pattern "+pattern+" not supported!");
				System.exit(-1);
		}
	}
	public String getPattern() {
		return pattern;
	}
	/**
	 * 
	 * @param scope
	 * @param properties - specify the properties (that will replace the placeholders) that are set by the scope, i.e. P, Q, R
	 */
	public void setScope(String scope, String[] properties) {
		int expectedNumberOfProperties = 0;
		//check if number of parameters is as expected
		switch(scope) {
			case "Between":
			case "Until":
				expectedNumberOfProperties++;
			case "After":
			case "Before":
				expectedNumberOfProperties++;
			case "Globally":
				expectedNumberOfProperties++;
				this.scope = scope;
				break;
			default:
				System.out.println("Pattern "+pattern+" not supported!");
				return;
		}
		if(expectedNumberOfProperties != properties.length) {
			System.out.println("Number of properties is unexpected.");
			System.out.println("Expected: " + expectedNumberOfProperties);
			System.out.println("Actual: " + properties.length);
			return;
		}
		//set the parameters
		switch(scope) {
			case "Between":
			case "Until":
				this.properties.put("Q", properties[1]);
				this.properties.put("R", properties[2]);
			case "After":
				this.properties.put("P", properties[0]);
				this.properties.put("Q", properties[1]);
				break;
			case "Before":
				this.properties.put("R", properties[1]);
			case "Globally":
				this.properties.put("Q", properties[0]);
		}
	}
	
	/** This method is similar to setScope(String, String[]). But while setScope(String, String[])
	 * checks whether the number of parameters appears to be okay, this method **does not*+ check any of that. 
	 * Make sure to check that at some other point when using this method!!!
	 * 
	 * @param scope
	 * @param properties
	 */
	public void setScope(String scope, HashMap<String, String> properties) {
		this.properties = properties;
		this.scope = scope;
	}
	
	/**
	 * 
	 * @param timed - true iff timed, false for untimed
	 * @param lowerBound
	 * @param upperBound
	 */
	public void setTimed(boolean timed, int lowerBound, int upperBound) {
		this.timed = timed;
		this.properties.put("t1", Integer.toString(lowerBound));
		this.properties.put("t2", Integer.toString(upperBound));
	}
	
	public boolean getTimed() {
		return this.timed;
	}
	/** no sanity checks in this method!
	 * 
	 * @param defaultValue
	 * @param replacedBy
	 * @return true 
	 */
	public boolean addProperty(String defaultValue, String replacedBy) {
		if(this.properties.containsKey(defaultValue)) {
			System.out.println("The value "+defaultValue+" was set already!");
			System.out.println("Overwriting old value "+this.properties.get(defaultValue)+ "with new value "+replacedBy);
		}
		this.properties.put(defaultValue, replacedBy);
		return true;
	}
	/** This method should be called when all properties are set and everything else is done, 
	 * as this will talk to our model and do the actual **work**
	 * 
	 * @param outputName - path and name where the new model shall be saved
	 */
	public void callPipeline(String outputName) {
		NTA system = new NTA(this.systemFile);
		//__first__ step
		Formula formula = new Formula(this.scope, this.pattern, this.TEMPLATE_LOCATION, this.timed, 
				Integer.parseInt(this.properties.getOrDefault("t1", "-1")),
				Integer.parseInt(this.properties.getOrDefault("t2", "-1")));

		formula.grabFormulas();
		
		
		if(formula.getObserverNeeded()) {
			//__second__ step
			SystemModelModifier modifier;
			if(this.timed) {
				String t1 = this.properties.get("t1");
				String t2 = this.properties.get("t2");
				this.properties.remove("t1");
				this.properties.remove("t2");
				modifier = new SystemModelModifier(system,  this.properties.values().toArray(new String[this.properties.size()]));
				
				this.properties.put("t1", t1);
				this.properties.put("t2", t2);
			}
			else {
				modifier = new SystemModelModifier(system,  this.properties.values().toArray(new String[this.properties.size()]));
			}
			modifier.modify();
			// now the observer has been modified, i.e. "P" has a pre and a post state with all the necessary channels and booleans being set
			
		} else{
			if (formula.getFlagsNeeded()) {
				SystemModelModifier modifier = new SystemModelModifier(system,  this.properties.values().toArray(new String[this.properties.size()]));
				modifier.modifyFlagOnly();
			}
			formula.replaceFormula(this.properties, system);
			formula.addFormulasToNTA(system);
		}

		//__third__ step
		//only needed for the observer method
		if(formula.getObserver() != null) {
			//actually change P to some value
			XMLModifier xmlModifier = new XMLModifier();

			xmlModifier.setProperties(this.properties);
			NTA observerNTA = formula.getObserver();
			// this got us the observer which we can now modify; 
			xmlModifier.fillObserverTemplate(observerNTA, formula);
			//__fourth__ step
			Observer observer = new Observer(observerNTA);
			// now we can add the observer to our system model
			observer.addObserverTemplate(system);
		}
		

		if(formula.globalClockNecessary()) {
			formula.addGlobalClock(system);
		}
		
		SystemModelModifier.printNumberOfChanges();
		
		system.writeModelToFile(outputName + ".xml");
	}
}
