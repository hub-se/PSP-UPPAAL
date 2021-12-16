package unittests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pipeline.Observer;
import uppaal.NTA;

/**
 * 
 * @author Marc Carwehl
 *
 */
class ObserverTest {
	NTA aNTA;
	final String NTA_FILE = "models/2doors.xml";
	final String AUTOMATON_NAME = "Door";
	Observer observer;
	NTA observerNTA;
	//TODO update path
	final String OBSERVER_PATH = "observer_templates/Absence_State_Between_Untimed.xml";
	
	@BeforeEach
	void setUp() {
		this.aNTA = new NTA(this.NTA_FILE);
		//make sure that the NTA is actually as expected 
		assertEquals(aNTA.getAutomaton(AUTOMATON_NAME).getLocations().size(), 6);
		assertEquals(aNTA.getAutomaton(AUTOMATON_NAME).getTransitions().size(), 9);
		
		this.observerNTA = new NTA(OBSERVER_PATH);
		
		this.observer = new Observer(observerNTA);


	}

	
	@Test
	void test() {
		int templateCount = aNTA.getAutomata().size();
		this.observer.addObserverTemplate(aNTA);
		//add a template -> count should increase and the new template should be the last in the list
		assertEquals(templateCount+1, aNTA.getAutomata().size());
		assertEquals(observerNTA.getAutomata().get(0), aNTA.getAutomata().get(templateCount));		
		

	}
	

}
