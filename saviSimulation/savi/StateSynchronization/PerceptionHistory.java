package savi.StateSynchronization;

import java.util.ArrayList;
import java.util.List;

import jason.asSyntax.Literal;

/**
 * Used for tracking the perceptions sent to the agent.
 * @author patrickgavigan
 *
 */
public class PerceptionHistory {
	
	// The previous perception snapshot
	private PerceptionSnapshot previousPerception;
	
	/**
	 * Default Constructor
	 */
	public PerceptionHistory() {
		this.previousPerception = new PerceptionSnapshot();
	}
	
	
	/**
	 * Updates the perception history and updates the agent. Looks for things that are sufficiently 
	 * different in order to send the update to the agent
	 */
	public List<Literal> updatePerceptionsChangesOnly(PerceptionSnapshot newPerceptions) {
		
		// Make a new snapshot to become the new version of this.previousPerception
		PerceptionSnapshot newPreviousPerception = new PerceptionSnapshot();
		
		// Make a safe copy of the new perceptions, we will be modifying it
		PerceptionSnapshot differentPerceptions = new PerceptionSnapshot(newPerceptions);

		// Make a snapshot for what will actually be sent to the agent
		PerceptionSnapshot outputSnapshot = new PerceptionSnapshot();
		
		// Get the list of previous perceptions
		List<Perception> prevPerceptList = new ArrayList<Perception>(this.previousPerception.getPerceptionList());
		
		// Iterate through the prevPerceptList
		for (int i = 0; i < prevPerceptList.size(); i++) {
			Perception similarPercept = differentPerceptions.pullSimilarPerception(prevPerceptList.get(i));
			
			if (similarPercept == null) {	// No similar Perceptions
				// This perception was seen before but now isn't. Need to add a negative perception to the output list
				Perception negativePercept = prevPerceptList.get(i).clone();
				negativePercept.perceptionLost();
				outputSnapshot.addPerception(negativePercept);

			} else {
				// This had been seen before. Need to log it in the history but don't send this to the agent
				newPreviousPerception.addPerception(similarPercept);
			}
		}
		
		// Deal with any remaining new perceptions that were not in the previous list
		newPreviousPerception.addPerceptionsFromSnapshot(differentPerceptions);
		outputSnapshot.addPerceptionsFromSnapshot(differentPerceptions);
				
		// Update the internal perception history
		this.previousPerception = newPreviousPerception;
		
		// Return the literals for the perception
		return outputSnapshot.getLiterals();
	}
	
	
	public List<Literal> updatePerceptionsPassThrough(PerceptionSnapshot newPerceptions) {
		this.previousPerception = new PerceptionSnapshot(newPerceptions);
		return previousPerception.getLiterals();
	}
	
	
	
	/**
	 * Provides the new list of perceptions based on the history and the update
	 */
	public List<Literal> updatePerceptions(PerceptionSnapshot newPerceptions) {
		//return this.updatePerceptionsChangesOnly(newPerceptions);
		return this.updatePerceptionsPassThrough(newPerceptions);
	}
}
