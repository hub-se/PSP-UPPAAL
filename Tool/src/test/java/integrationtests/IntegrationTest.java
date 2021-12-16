package integrationtests;


import org.junit.jupiter.api.Test;

import pipeline.Formula;
import pipeline.Observer;
import pipeline.SystemModelModifier;
import pipeline.XMLModifier;
import uppaal.NTA;

/**
 * This class provides a simple example tweaking the 2Doors Uppaal demo.
 * We use the Absence, Before template and scope, untimed. 
 * @author Marc Carwehl
 *
 */
public class IntegrationTest {

	final String TEMPLATE_LOCATION = "./observer_templates/";
	final String SYSTEM_FILE = "./models/2doors.xml";
	
	final String PATTERN_1 = "Absence";
	final String SCOPE_1 = "Before";
	final boolean TIMED_1 = false;
	final int LOWER_TIME_BOUND_1 = -1;
	final int UPPER_TIME_BOUND_1 = -1;
	final String outputPath = "";
	

	
	@Test
	void test() {
		NTA system = new NTA(this.SYSTEM_FILE);
		//__first__ step
		Formula formula = new Formula(this.SCOPE_1, this.PATTERN_1, this.TEMPLATE_LOCATION, this.TIMED_1, this.LOWER_TIME_BOUND_1, this.UPPER_TIME_BOUND_1);
		
		formula.grabFormulas();
		formula.addFormulasToNTA(system);
		
		
		NTA observerNTA = formula.getObserver();
		// this got us the observer which we can now modify; if no observer is necessary, we should skip the next steps
		//TODO: how do we get the properties?
		String[] properties = {"idle", "open", "closed"};
		//__second__ step
		SystemModelModifier modifier = new SystemModelModifier(system, properties);
		modifier.modify();
		// now the observer has been modified, i.e. "P" has a pre and a post state with all the necessary channels and booleans being set
		
		//__third__ step
		//actually change P to door
		XMLModifier xmlModifier = new XMLModifier();
		//Between Open and Closed, the system shall never be Idle
		xmlModifier.addProperty("P", "idle");
		xmlModifier.addProperty("Q", "open");
		xmlModifier.addProperty("R", "closed");
	
		xmlModifier.fillObserverTemplate(observerNTA, formula);
		
		
		
		//__fourth__ step
		Observer observer = new Observer(observerNTA);
		if(formula.globalClockNecessary()) {
			formula.addGlobalClock(system);
		}
		// now we can add the observer to our system model
		observer.addObserverTemplate(system);
		
		
		system.writeModelToFile(this.outputPath + "2Doors_tweak.xml");
	}

}
