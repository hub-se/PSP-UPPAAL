package unittests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pipeline.Formula;
import pipeline.XMLModifier;
import uppaal.Location;
import uppaal.NTA;
import uppaal.Transition;

/**
 * 
 * @author Marc Carwehl
 *
 */
class XMLModifierTest {
	private XMLModifier modifier;
	private NTA observerTemplateNTA;
	private String observerPath = "observer_templates/Absence_State_Between_Untimed.xml";
	private Formula formula;
	private String templatePath = "./models/2doors.xml";
	private Location P;
	private Location Q;
	
	@BeforeEach
	void setUp() {
		this.modifier = new XMLModifier();
		this.modifier.addProperty("P", "closing");
		this.modifier.addProperty("Q", "open");
		this.observerTemplateNTA = new NTA(this.observerPath);
		this.formula = new Formula("Between", "Absence", this.templatePath, false, 0, 0);
		this.P = null;
		this.Q = null;
		for(Location l : this.observerTemplateNTA.getAutomata().get(0).getLocations()) {
			if(l.getName() != null) {
				if(l.getName().getName() != null) {
					if(l.getName().getName().contains("P"))
						this.P = l;
					if(l.getName().getName().contains("Q"))
						this.Q = l;
				}
			}
			
		}
	}

	@Test
	void test() {
		assertEquals(3, this.numberOfTransitionsContaining("P"));
		assertEquals(3, this.numberOfTransitionsContaining("Q"));

		this.modifier.fillObserverTemplate(this.observerTemplateNTA, this.formula);
		//P -> closing
		assertEquals(0, this.numberOfTransitionsContaining("P"));
		assertEquals(3, this.numberOfTransitionsContaining("closing"));
		//Q -> open
		assertEquals(0, this.numberOfTransitionsContaining("Q"));
		assertEquals(3, this.numberOfTransitionsContaining("open"));
		
		assertEquals(false, this.observerTemplateNTA.getQueries().getQueries().get(1).getFormula().contains("$Q"));
		//System.out.println(this.observerTemplateNTA.getQueries().getQueries().get(1).getFormula());
		assertEquals(true, this.observerTemplateNTA.getQueries().getQueries().get(1).getFormula().contains("open"));
		
	}
	
	int numberOfTransitionsContaining(String P) {
		int number = 0;
		for(Transition t : this.observerTemplateNTA.getAutomata().get(0).getTransitions()) {
			if(t.getGuard() != null) {
				if(t.getGuard().getGuard().contains(P)) {
					number++;
					continue;
				}
			}
			if(t.getSync() != null) {
				if(t.getSync().getChannelName().contains(P)) {
					number++;
				}
			}
		}
		return number;
	}

}
