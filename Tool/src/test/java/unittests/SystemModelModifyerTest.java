package unittests;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pipeline.SystemModelModifier;
import uppaal.Location;
import uppaal.NTA;

/**
 * 
 * @author Marc Carwehl
 *
 */
class SystemModelModifyerTest {

	NTA aNTA;
	final String NTA_FILE = "models/2doors.xml";
	final String AUTOMATON_NAME = "Door";
	@BeforeEach
	void setUp() {
		this.aNTA = new NTA(this.NTA_FILE);
		//make sure that the NTA is actually as expected 
		assertEquals(aNTA.getAutomaton(AUTOMATON_NAME).getLocations().size(), 6);
		assertEquals(aNTA.getAutomaton(AUTOMATON_NAME).getTransitions().size(), 9);


	}

	
	@Test
	void simpleTest() {
		SystemModelModifier underTest = new SystemModelModifier(aNTA, "open");
		underTest.modify();
		assertEquals(8, aNTA.getAutomaton(AUTOMATON_NAME).getLocations().size());
		assertEquals(11, aNTA.getAutomaton(AUTOMATON_NAME).getTransitions().size());
		
		Location preOpen = aNTA.getAutomaton("Door").getLocation("open").getIncommingTransitions().get(0).getSource();
		//TODO test whether the new incoming state has the properties we desire
		
	}
	
	@Test
	void complicatedTest() {
		SystemModelModifier underTest = new SystemModelModifier(aNTA, "wait");
		underTest.modify();
		assertEquals(8, aNTA.getAutomaton(AUTOMATON_NAME).getLocations().size());
		assertEquals(11, aNTA.getAutomaton(AUTOMATON_NAME).getTransitions().size());
	}

	
	
}
