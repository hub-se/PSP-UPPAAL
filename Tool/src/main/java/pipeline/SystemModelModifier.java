package pipeline;

import java.util.List;

import uppaal.Automaton;
import uppaal.Declaration;
import uppaal.Location;
import uppaal.NTA;
import uppaal.Name;
import uppaal.Transition;
import uppaal.Location.LocationType;
import uppaal.labels.Synchronization;
import uppaal.labels.Update;
import uppaal.labels.Synchronization.SyncType;

/**
 * This represents the ***second*** step in our process
 * 
 * This class is used to modify the system model, i.e. generate states that precede/succeed states we want to observe and 
 * set specific variables like P_holds
 * 
 * @author Marc Carwehl
 *
 */

public class SystemModelModifier {
	
	private static int statesAdded = 0;
	private static int transitionsAdded = 0;
	private NTA systemModel;
	private String[] toObserve;
		
	public SystemModelModifier(NTA model, String[] properties){
		this.systemModel = model;
		this.toObserve = properties;
	}
	
	public SystemModelModifier(NTA model, String property){
		this.systemModel = model;
		this.toObserve = new String[]{property};
	}
	
	
	/**
	 * This method is used to modify each of the properties set in toObserve.
	 * We go through each template in the given model and add a state before and after the property state holds.
	 */
	public void modify() {
		//before we modify anything, i.e. add states and transitions, we should add the semaphore!!
		addTwoSemaphores();
		for(String state : this.toObserve)
			modify(state);
	}
	/**
	 * method to add semaphore flag to every transition. 
	 * This disables every transition while the system is in our pseudo-states
	 * See Li2010 for information on this, mayFire
	 */
	private void addOneSemaphore() {
		this.systemModel.getDeclarations().add("int mayFire = 0;");
		for(Automaton a : systemModel.getAutomata())
			for(Transition t : a.getTransitions()) {
				//if there is no guard, we should add one
				if(t.getGuard() == null) {
					t.setGuard("mayFire == 0");
				} else {
					//if there is a Guard, we shall just add a conjunction and the semaphore
					t.setGuard(t.getGuard() + "&& mayFire == 0");
				}
			}
	}
	
	private void addTwoSemaphores() {
		this.systemModel.getDeclarations().add("int mayFire = 0;");
		for(Automaton a : systemModel.getAutomata())
			for(Transition t : a.getTransitions()) {
				//if there is no guard, we should add one
				if(t.getGuard() == null) {
					t.setGuard("mayFire == 0 && !nxtCmt");
				} else {
					//if there is a Guard, we shall just add a conjunction and the semaphore
					t.setGuard(t.getGuard() + "&& mayFire == 0 && !nxtCmt");
				}
			}
	}
	
	/**
	 * method to modify a system such that we can observe a certain state whenever it is entered or left
	 * 
	 * @param state - name of the state that we want to observe
	 */
	private void modify(String state) {
		//add boolean values to system declaration for state_holds etc
		Declaration sd = this.systemModel.getDeclarations();
		sd.add("bool " +state+ "_holds = false;");
		sd.add("bool " +state+ "_held_once = false;");
		sd.add("broadcast chan " +state+ "_reached;");
	
		for(Automaton a : this.systemModel.getAutomata()) {
			List<Location> locations = (List<Location>) a.getLocations().clone();
			for(Location l : locations) {
				//if l-state's name == l
				if(l.getName() != null) {
					if(l.getName().getName().equals(state)) {
						modify(l);//we could add a return; here and an error outside the loop, if there is no state that has the correct name, we may want to throw an error
					}
				}
			}
		}
	}
	
	
	/**
	 * method to make a new location that 'precedes' the one we are interested in and sends a sync when it is entered
	 * and to make new locations that 'succeed' the one we are interested in for every outgoing transition
	 * 
	 * @param state - the location which is of interest
	 */
	private void modify(Location state) {
		String name = state.getName().getName();
		
		//***incoming edges:***
		
		//make new location that precedes state
		Location incoming = new Location(state.getAutomaton(), new Name(name+"_ENTER"), LocationType.COMMITTED, 0, 0);
		statesAdded++;

		//deprecated //if state is the initial location, then make the new incoming location the initial one for this automaton 
		incoming.setType(LocationType.COMMITTED);
		if(state.getAutomaton().getInit()==state) {
			//no, if state is the initial location, we will add a new initial location
			Location init = new Location(state.getAutomaton(), new Name(""), LocationType.COMMITTED, 0, 0);
			statesAdded++;
			init.setType(LocationType.COMMITTED);
			state.getAutomaton().setInit(init);
			Transition t = new Transition(state.getAutomaton(), init, incoming);
			transitionsAdded++;		
			t.setUpdate(new Update("mayFire++"));
		}
		
		
		//and that inherits the same invariants
		if(state.getInvariant() != null)
			incoming.setInvariant(state.getInvariant().toString());
		//for all transitions that enter state, redirect them to incoming
		for(Transition iTransition : state.getIncommingTransitions()) {
			//ignore self-loops
			if(!iTransition.getSource().equals(state)) {
				iTransition.setTarget(incoming);
				//we should also update the semaphore coming into incoming
				if(iTransition.getUpdate() == null)
					iTransition.setUpdate(new Update("mayFire++"));
				else 
					//there already is an update
					//the Update class automatically handles the ,\n after every String that we add to it, isn't that neat?!
					iTransition.addUpdate("mayFire++");
				//here we now add the other updates as well, i.e. X_holds = 1 and so on
				iTransition.addUpdate(name + "_holds = 1");
				iTransition.addUpdate(name + "_held_once = 1");
			}
		}

		
		
		//make new transition from incoming to state
		Transition incomingT = new Transition(state.getAutomaton(), incoming, state);
		transitionsAdded++;
		//this added update was moved to, it is now added to one transition earlier 
		//incomingT.setUpdate(new Update(name+"_holds = 1, "+name+"_held_once=1"));
		//also update semaphore
		incomingT.addUpdate("mayFire--");
		//set the sync
		incomingT.setSync(new Synchronization(name+"_reached", SyncType.INITIATOR));
		
		//***outgoing edges:***
		
		//for every outgoing edge
		for(Transition edge : state.getOutgoingTransitions()) {
			//check if self-loop
			if(!edge.getTarget().equals(state)) {
				//make a new location
				Location outgoing = new Location(state.getAutomaton(), new Name(""), LocationType.COMMITTED, 0, 0);
				statesAdded++;
				outgoing.setType(LocationType.COMMITTED);
				//which inherits the same invariants as the succeeding location
				if(edge.getTarget().getInvariant() != null)
					outgoing.setInvariant(edge.getTarget().getInvariant().toString());
				//and is linked by a new transition to state
				Transition outgoingT = new Transition(state.getAutomaton(), outgoing, edge.getTarget());
				transitionsAdded++;
				//here we add the semaphore nxtCmt
				outgoingT.setGuard("!nxtCmt");
				
				//which updates and syncs as expected
				//outgoingT.setUpdate(new Update(name+"_holds = 0"));
				edge.addUpdate(name+"_holds = 0");
				//for this sync, all states need to have names! otherwise there is a null pointer exception and we don't know how to name the sync
				//outgoingT.setSync(new Synchronization(name+"_LEFTTO_"+outgoingT.getTarget().getName().getName(), SyncType.INITIATOR));
				//Declaration sd = this.systemModel.getDeclarations();
				//sd.add("broadcast chan " +name+ "_LEFTTO_"+outgoingT.getTarget().getName().getName() + ";");
				//and make edge go only to the new outgoing location
				edge.setTarget(outgoing);
				//also we have to update the semaphore mayFire here
				if(edge.getUpdate() == null)
					edge.setUpdate(new Update("mayFire++"));
				else 
					edge.addUpdate("mayFire++");
				//and also decrease the semaphore for the newly added transition
				outgoingT.addUpdate("mayFire--");
			}
		}
	}
	
	
	/**
	 * This method is used to modify each of the properties set in toObserve.
	 * We go through each template in the given model and add a state before and after the property state holds.
	 */
	public void modifyFlagOnly() {
		addOneSemaphore();
		for(String s : this.toObserve) {
			//add boolean values to system declaration for state_holds etc
			Declaration sd = this.systemModel.getDeclarations();
			sd.add("bool " +s+ "_held_once = false;");
		
			for(Automaton a : this.systemModel.getAutomata()) {
				List<Location> locations = (List<Location>) a.getLocations().clone();
				for(Location state : locations) {
					//if state name == l
					if(state.getName() != null) {
						if(state.getName().getName().equals(s)) {
							String name = s;
							
							//***incoming edges:***
							
							//make new location that precedes state
							Location incoming = new Location(state.getAutomaton(), new Name(name+"_ENTER"), LocationType.COMMITTED, 0, 0);
							statesAdded++;

							//deprecated //if state is the initial location, then make the new incoming location the initial one for this automaton 
							incoming.setType(LocationType.COMMITTED);
							if(state.getAutomaton().getInit()==state) {
								//no, if state is the initial location, we will add a new initial location
								Location init = new Location(state.getAutomaton(), new Name(""), LocationType.COMMITTED, 0, 0);
								statesAdded++;
								init.setType(LocationType.COMMITTED);
								state.getAutomaton().setInit(init);
								Transition t = new Transition(state.getAutomaton(), init, incoming);
								transitionsAdded++;		
								t.setUpdate(new Update("mayFire++"));
							}
							
							if(state.getAutomaton().getInit()==state)
								state.getAutomaton().setInit(incoming);
							//and that inherits the same invariants
							if(state.getInvariant() != null)
								incoming.setInvariant(state.getInvariant().toString());
							//for all transitions that enter state, redirect them to incoming
							for(Transition iTransition : state.getIncommingTransitions()) {
								//ignore self-loops
								if(!iTransition.getSource().equals(state)) {
									iTransition.setTarget(incoming);
									if(iTransition.getUpdate() == null)
										iTransition.setUpdate(new Update("mayFire++"));
									else 
										iTransition.addUpdate("mayFire++");
								}
							}
							//make new transition from incoming to state
							Transition incomingT = new Transition(state.getAutomaton(), incoming, state);
							transitionsAdded++;
							incomingT.setUpdate(new Update(name+"_holds = 1, "+name+"_held_once=1"));
							incomingT.addUpdate("mayFire--");
						}
					}
				}
			}
		}
	}
	
	public static void printNumberOfChanges() {
		System.out.println(statesAdded + " states were added!");
		System.out.println(transitionsAdded + " transitions were added");
	}
	
	
}
