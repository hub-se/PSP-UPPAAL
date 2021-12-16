package observerModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/** This class stores information about which properties are requested for which scope
 * 
 * @author Marc Carwehl
 *
 */
public class Scope {
	private static HashMap<String, List<String>> scopes;
	public Scope() {
		scopes = new HashMap<String, List<String>>();
		List<String> parameters = new ArrayList<>();
		parameters.add("P");
		scopes.put("Globally", new ArrayList<>(parameters));
		parameters.add("Q");
		scopes.put("After", new ArrayList<>(parameters));
		parameters.add("R");
		scopes.put("Until", new ArrayList<>(parameters));
		scopes.put("Between", new ArrayList<>(parameters));
		parameters.clear();
		parameters.add("P");
		parameters.add("R");
		scopes.put("Before", new ArrayList<>(parameters));
	}
	public List<String> getParameters(String scope) {
		return scopes.get(scope);
	}
}
