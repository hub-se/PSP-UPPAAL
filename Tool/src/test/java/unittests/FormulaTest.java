package unittests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pipeline.Formula;
import uppaal.Automaton;
import uppaal.NTA;

/**
 * 
 * @author Marc Carwehl
 *
 */

class FormulaTest {

	NTA aNTA;
	final String NTA_FILE = "./models/2doors.xml";
	final String OBSERVER_FILE = "./observer_templates/Universality_State_Before_Untimed.xml";
	final String AUTOMATON_NAME = "Door";
	final String TEMPLATE_LOCATION = "./observer_templates/";
	NTA observer;
	
	
	@BeforeEach
	void setUp() {
		this.aNTA = new NTA(this.NTA_FILE);
		//make sure that the NTA is actually as expected 
		assertEquals(aNTA.getAutomaton(AUTOMATON_NAME).getLocations().size(), 6);
		assertEquals(aNTA.getAutomaton(AUTOMATON_NAME).getTransitions().size(), 9);
		
		this.observer = new NTA(this.OBSERVER_FILE);
	}

	
	
	@Test
	void test() {
		Formula formulaUnderTest = new Formula("Before", "Universality", TEMPLATE_LOCATION, false, -1, -1);
		assertEquals(null, formulaUnderTest.getFormulas());
		assertEquals(null, formulaUnderTest.getObserver());
		
		formulaUnderTest.grabFormulas();
		assertEquals(observer.getXMLElementName(), formulaUnderTest.getObserver().getXMLElementName());
		formulaUnderTest.addFormulasToNTA(aNTA);

		assertEquals("A[] not Observer.ERROR", formulaUnderTest.getFormulas()[0]);
		//untimed, hence no gc should be necessary
		assertEquals(false, formulaUnderTest.globalClockNecessary());
		
		//when we add a global clock anyways, there should be a declaration afterwards
		formulaUnderTest.addGlobalClock(aNTA);
		assertEquals(true, gc());
		
		//TODO test grab formulas
	}
	
	/**
	 * method to test whether a global clock gc is defined in the system model
	 * @return
	 */
	private boolean gc() {
		for(String declaration : this.aNTA.getDeclarations().getStrings()) {
			if (declaration.contains("gc"))
				return true;
		}
		
		for(String declaration : this.aNTA.getSystemDeclaration().getDeclarations()) {
			if (declaration.contains("gc"))
				return true;
		}
		
		for(Automaton automaton : this.aNTA.getAutomata()) {
			for (String declaration : automaton.getDeclaration().getStrings()) {
				if (declaration.contains("gc"))
					return true;
			}
		}
		return false;
	}

}
