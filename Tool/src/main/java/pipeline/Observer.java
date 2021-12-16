package pipeline;

import java.util.List;

import uppaal.NTA;
import uppaal.SystemDeclaration;
import uppaal.Location.LocationType;

/**
 * This represents the ***fourth*** step in our process
 * 
 * This class is used to modify the Observer XML, i.e. changing placeholders like P to their respective 
 * names.
 * 
 * @author Marc Carwehl
 *
 */

public class Observer {
	
	private NTA observer;
	
	public Observer(NTA observer){
		this.observer = observer;
	}
	
	
	/***
	 * This method adds the observers template to the system specified as parameter
	 * @param system - NTA model of the system
	 */
	public void addObserverTemplate(NTA system) {
		this.observer.getAutomata().get(0).setName("observer");
		system.addAutomaton(this.observer.getAutomaton("observer"));
		List<String> systemdeclaration = system.getSystemDeclaration().getDeclarations();
		systemdeclaration.add(0, "Observer = observer();");
		
		for(int i = 0; i < systemdeclaration.size(); i++) {
			if(systemdeclaration.get(i).startsWith("system")) {
				String s = systemdeclaration.get(i).replace(";", ", Observer;");
				systemdeclaration.remove(i);
				systemdeclaration.add(s);
				break;
			}
		}
		
		system.setQueries(this.observer.getQueries());
		
		//here we also add our second semaphore flag
		//and we set it to 1 if the observer starts in a committed state
		if(this.observer.getAutomata().get(0).getInit().getType() == LocationType.COMMITTED)
			system.getDeclarations().add("bool nxtCmt = 1;");
		//or 0 otherwise
		else 
			system.getDeclarations().add("bool nxtCmt = 0;");

		/*for(String line : systemdeclaration) {
			System.out.println(line);
			if(line.startsWith("system")) {
				line.replace(";", ", Observer;");
				
			}
		}
		SystemDeclaration sd = new SystemDeclaration();
		sd.addDeclarations(systemdeclaration);
		system.setSystemDeclaration(sd);*/
		
		
		
	}
	
	/**
	 * This method is used to identify whether a global clock is needed by the observer given as parameter
	 * @param observer - the observer
	 * @return - true iff gc is mentioned in observer
	 */
	/*
	 * this method is defined in Formula.class
	public boolean globalClockNecessary(NTA observer) {
		for(String declaration : observer.getDeclarations().getStrings()) {
			if (declaration.contains("gc"))
				return true;
		}
		
		for(String declaration : observer.getSystemDeclaration().getDeclarations()) {
			if (declaration.contains("gc"))
				return true;
		}
		
		for(Automaton automaton : observer.getAutomata()) {
			for (String declaration : automaton.getDeclaration().getStrings()) {
				if (declaration.contains("gc"))
					return true;
			}
		}
		return false;
	}*/
}
