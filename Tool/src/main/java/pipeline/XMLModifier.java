package pipeline;

import java.util.HashMap;
import java.util.Map;

import uppaal.Automaton;
import uppaal.NTA;
import uppaal.Transition;
import uppaal.labels.Synchronization.SyncType;

/**
 * This represents the ***third*** step in our process
 * 
 * This class is used to modify the system XML, i.e. adding states that precede / succeed the states we want to observe.
 * 
 * @author Marc Carwehl
 *
 */


public class XMLModifier {
	//<defaultValue, replacedBy>
	private Map<String, String> properties;
	
	// placeholder name of the lower time bound
	private final static String LOWER_TIME_BOUND_NAME = "t1";
	// placeholder name of the upper time bound
	private final static String UPPER_TIME_BOUND_NAME = "t2";
	// placeholder name of a quantity
	//private final static String QUANTITY_PARAMETER = "n_";
	private static final int UPPAAL_INT_MAX_VALUE = 32768;

	public XMLModifier(){
		this.properties = new HashMap<String, String>();
	}
	/**
	 * This method adds a tuple of Strings to the properties. We specify the placeholder by 
	 * defaultValue and the replacement as replacedBy
	 * 
	 * Only placeholders that we use in the observers are supported. Otherwise this method 
	 * will return false, which indicates a problem.
	 * False will also be returned if the defaultValue is already specified with a replacement.
	 *  Consider using changeProperty in this case
	 *  
	 * @param defaultValue - the value we use in the templates, e.g. P, Q, R..
	 * @param replacedBy - the replacement, i.e. the value in the system automaton
	 * @return
	 */
	public boolean addProperty(String defaultValue, String replacedBy) {
		if(!checkDefault(defaultValue)) {
			return false;
		}
		if(!this.properties.containsKey(defaultValue)) {
			this.properties.put(defaultValue, replacedBy);
			return true;
		}
		System.out.println("This default value is already set: " + defaultValue);
		return false;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	/**
	 * This method changes a tuple of Strings in the properties that is already in the mapping!
	 * We specify the placeholder by 
	 * defaultValue and the replacement as replacedBy
	 * 
	 * Only placeholders that we use in the observers are supported. 
	 *  
	 * @param defaultValue - the value we use in the templates, e.g. P, Q, R..
	 * @param replacedBy - the replacement, i.e. the value in the system automaton
	 */
	public void changeProperty(String defaultValue, String replacedBy) {
		if(!checkDefault(defaultValue)) {
			return;
		}
		this.properties.replace(defaultValue, replacedBy);
	}
	
	private boolean checkDefault(String value) {
		switch(value) {
		case "P":
		case "Q":
		case "R":
		case "S":
		case "T":
		case "t1":
		case "t2":
		case "t3":
			break;
		default:
			System.out.println("This default value is not supported: "+value);
			return false;
		}
		return true;
	}
	
	/**
	 * function to change default values from observer file
	 * default values and their replacement are specified in this.properties
	 * make sure to fill this first, as otherwise this function won't do anything
	 * @param observerNTATemplate - observer template that will be enriched 
	 * @param formula - the formula containing queries that will be enriched
	 */
	public void fillObserverTemplate(NTA observerNTATemplate, Formula formula) {
		// the observer template has exactly one automaton
		assert observerNTATemplate.getAutomata().size() == 1;

		Automaton observer = observerNTATemplate.getAutomata().get(0);

		for (String defaultValue : this.properties.keySet()) {
			String replacedBy = this.properties.get(defaultValue);
		
			String parameterName = defaultValue;
			//assert parameter.getState() != null;
			String parameterValue = replacedBy;


			// to be safe, include the underscore to find and replace substrings
			parameterName = parameterName + "_";
			parameterValue = parameterValue + "_";

			for (Transition t : observer.getTransitions()) {
				// change channel if existent
				if (t.getSync() != null) {
					String channelName = t.getSync().getChannelName();
					if (channelName.contains(parameterName)) {
						String oldChannelName = channelName;
						channelName = channelName.replaceAll(parameterName, parameterValue);
						t.getSync().setChannelName(channelName);

						String sndRev = "";
						if (t.getSync().getSyncType() == SyncType.INITIATOR) {
							sndRev = "!";
						} else {
							sndRev = "?";
						}
					}
				}

				// change guard if existent
				if (t.getGuard() != null) {
					String guard = t.getGuard().getGuard();
					if (guard.contains(parameterName)) {
						String oldGuard = guard;
						guard = guard.replaceAll(parameterName, parameterValue);
						t.getGuard().setGuard(guard);
					}
				}
			}

			// change locations
			for (uppaal.Location l : observer.getLocations()) {
				if (l.getName() != null) {
					uppaal.Name name = l.getName();
					String nameString = name.getName();
					if (nameString.contains(parameterName)) {
						String oldLocName = nameString;
						nameString = nameString.replaceAll(parameterName, parameterValue);
						name.setName(nameString);
					}
				}
			}
			// change queries
			// parameter names now start with $ and are not suffixed with an underscore
			parameterName = "$" + defaultValue;
			String observerParmeterName = "Observer." + parameterName;
			parameterValue = replacedBy;
			for (uppaal.Query q : observerNTATemplate.getQueries().getQueries()) {
				String queryString = q.getFormula();
				if (queryString.contains(observerParmeterName)) {
					String oldQueryString = queryString;
					queryString = queryString.replace(parameterName, parameterValue);
					q.setFormula(queryString);
				}
				//should not be needed as $P should never be without Observer.$P
				//edit: is indeed needed as $P can mean the state of the observed system! 
				if (queryString.contains(parameterName)) {
					// component of the location is needed if the parameter is not prefixed by
					// "Observer." such as in "Observer.$P_happens".
					//edit: in our case, then there is no component to prefix
					//String componentName = this.findComponentOfLocation(nta, parameterValue);
					//String replacingName = componentName + "." + parameterValue;
					String replacingName = parameterValue;
					String oldQueryString = queryString;
					queryString = queryString.replace(parameterName, replacingName);
					q.setFormula(queryString);
				}
			}
		}

		// time bound
		//TimeBound timeBound = formula.getTimebound();
		int lTimeBound = formula.getLowerTimebound();
		int uTimeBound = formula.getUpperTimebound();
		
		
		if (lTimeBound != -1 || uTimeBound != -1) {
			for (uppaal.Transition t : observer.getTransitions()) {
				// change guard if existent
				if (t.getGuard() != null) {
					String guard = t.getGuard().getGuard();
					if (guard.contains(LOWER_TIME_BOUND_NAME)) {
						String oldGuard = guard;
						guard = guard.replaceAll(LOWER_TIME_BOUND_NAME, "" + lTimeBound);
						t.getGuard().setGuard(guard);
					}
					if (guard.contains(UPPER_TIME_BOUND_NAME)) {
						int uBound = uTimeBound;
						if (uBound == 0) {
							uBound = UPPAAL_INT_MAX_VALUE;
						}
						String oldGuard = guard;
						guard = guard.replaceAll(UPPER_TIME_BOUND_NAME, "" + uBound);
						t.getGuard().setGuard(guard);
					}
				}
			}

			// change queries if existent
			for (uppaal.Query q : observerNTATemplate.getQueries().getQueries()) {
				String queryString = q.getFormula();
				if (queryString.contains("$" + LOWER_TIME_BOUND_NAME)) {
					String oldQueryString = queryString;
					queryString = queryString.replaceAll("$" + LOWER_TIME_BOUND_NAME, "" + lTimeBound);
					q.setFormula(queryString);
				}
				if (queryString.contains("$" + UPPER_TIME_BOUND_NAME)) {
					int uBound = uTimeBound;
					if (uBound == 0) {
						uBound = UPPAAL_INT_MAX_VALUE;
					}
					String oldQueryString = queryString;
					queryString = queryString.replaceAll("$" + UPPER_TIME_BOUND_NAME, "" + uBound);
					q.setFormula(queryString);
				}
			}
		}

		/* 
		 * We focus on patterns without quantity for now
		// quantity in guards
		Quantity quantity = formula.getPattern().getQuantity();
		if (quantity != null) {
			for (uppaal.Transition t : observer.getTransitions()) {
				// change guard if existent
				if (t.getGuard() != null) {
					String guard = t.getGuard().getGuard();
					if (guard.contains(QUANTITY_PARAMETER)) {
						String oldGuard = guard;
						guard = guard.replaceAll(QUANTITY_PARAMETER, "" + quantity.getParameter());
						t.getGuard().setGuard(guard);
					}
				}

			}
		}
		*/
	}
	
}
