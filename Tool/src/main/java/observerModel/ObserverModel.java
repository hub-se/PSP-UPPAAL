package observerModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/** This class stores information about which properties are requested for which pattern
 * 
 * @author Marc Carwehl
 *
 */

public class ObserverModel {
	
	private Scope _scope;
	
	private static HashMap<String, List<String>> patterns;
	public ObserverModel() {
		_scope = new Scope();
		
		patterns = new HashMap<String, List<String>>();
		List<String> parameters = new ArrayList<>();
		patterns.put("Absence", new ArrayList<>(parameters));
		patterns.put("Universality", new ArrayList<>(parameters));
		patterns.put("Existence", new ArrayList<>(parameters));
		patterns.put("Recurrence", new ArrayList<>(parameters));
		patterns.put("MinimumDuration", new ArrayList<>(parameters));
		patterns.put("MaximumDuration", new ArrayList<>(parameters));
		parameters.add("S");
		patterns.put("Precedence", new ArrayList<>(parameters));
		patterns.put("Response", new ArrayList<>(parameters));
		patterns.put("Until", new ArrayList<>(parameters));
		patterns.put("ResponseInvariance", new ArrayList<>(parameters));
		parameters.add("ZS");
		patterns.put("ResponseConstrained", new ArrayList<>(parameters));
		parameters.clear();
		parameters.add("S");
		parameters.add("T");
		patterns.put("ResponseChain", new ArrayList<>(parameters));
		patterns.put("PrecedenceChainN1", new ArrayList<>(parameters));	
		parameters.clear();
		parameters.add("S");
		parameters.add("T0");
		parameters.add("T1");
		parameters.add("T2");
		patterns.put("ResponseChainN1", new ArrayList<>(parameters));
		parameters.add("Z0");
		parameters.add("Z1");
		parameters.add("Z2");
		parameters.add("Z3");
		patterns.put("ResponseChainConstrainedN1", new ArrayList<>(parameters));
		parameters.add("T3");
		patterns.put("ResponseChainConstrained1N", new ArrayList<>(parameters));
		parameters.clear();
		parameters.add("n_");
		patterns.put("BoundedExistence", new ArrayList<>(parameters));	
		parameters.clear();
		parameters.add("S");
		parameters.add("T1");
		parameters.add("T2");
		parameters.add("T3");
		patterns.put("PrecedenceChain1N", new ArrayList<>(parameters));	
		parameters.add("Z0");
		parameters.add("Z1");
		parameters.add("Z2");
		parameters.add("Z3");
		patterns.put("PrecedenceChainConstrained1N", new ArrayList<>(parameters));	
		patterns.put("PrecedenceChainConstrainedN1", new ArrayList<>(parameters));	



	}
	public List<String> getParameters(String pattern, String scope) {
		List<String> liste = new ArrayList<String>(patterns.get(pattern));
		System.out.println(pattern);
		liste.addAll(this._scope.getParameters(scope));
		return liste;
	}
	
	
	
	
}
