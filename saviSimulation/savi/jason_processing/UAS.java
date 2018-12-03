package savi.jason_processing;

import java.util.ArrayList;

import java.util.List;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;
import savi.jason_processing.ROBOT_model.House;
import savi.jason_processing.ROBOT_model.Threat;
import savi.jason_processing.ROBOT_model.Tree;

import processing.data.*; 
import processing.event.*; 
import processing.opengl.*;


public class UAS extends AgentModel {
	  private static final double SPEED = 3; //we move by one unit / pixel at each timestep?

	//-----------------------------------------
	  // DATA (or state variables)
	  //-----------------------------------------
	  
	  int ID; 
	  
	  //SyncAgentState agentState; // contains all relevant info = It's in the superclass!
	  
	  PVector initialPosition; // to be able to reset
	 
	  //-----------------------------------------
	  // METHODS (functions that act on the data)
	  //-----------------------------------------
	  // Constructor: called when an object is created using
	  //              the "new" keyword. It's the only method
	  //              that doesn't have a type (not even void).
	  UAS(int id, PVector initialPosition) {
	    // Initialize data values
	    ID = id;
	    
	    agentState = new SyncAgentState();
	    this.initialPosition = initialPosition;
	  
	    PVector position = initialPosition.copy(); //Assume that the initial position is at the center of the display window
	    
	    agentState.setPosition(position); //value type is PVector
	    agentState.setSpeedAngle(0.0); //TODO: calculate velocity angle + magnitude
	    agentState.setSpeedValue(0.0); //TODO
	    agentState.setCompassAngle(-Math.PI/2); //TODO calculate direction we're facing
	    //agentState.setCompassAngle(0);
	    
	    agentState.setCameraInfo(new ArrayList<VisibleItem>()); //TODO: calculate what we can see
	    
	    
	    //maxSpeed = 0.051f; //  km/s  i.e 100 knots
	    //Altitude = 10000 ;//Assume the altitude is in feet
	    
	    //agent architecture
	    //BaseCentralisedMAS.getRunner().setupLogger();
	    
	    //ag = new SimpleJasonAgent(this);
	    
	    
	  }

	public PVector getPosition() {
		  return agentState.getPosition();
	  }
	  
	  public double getCompassAngle() {
		  return agentState.getCompassAngle();
	  }
	  
	  //// State Update: Read actions from queue, execute them
	  // also includes coordinates of threat.
	  public void update(int visionDistance, List<Threat> threats, List<Tree> trees, List<House> houses){


		  PVector position = (PVector) agentState.getPosition();
		  double speedValue = agentState.getSpeedValue();
		  double compassAngle = agentState.getCompassAngle(); //TODO for now speedAngle is always zero 
		  
		  List<String> toexec = agentState.getAllActions();
		  
		  //System.out.print(agentState.getAllActions().size());
		  
		  for (String action : toexec) {
			  System.out.println("UAS doing:"+ action);
			    if (action.equals("turn(left)")) //TODO: make these MOD 2 pi ? 
		        	compassAngle += Math.PI/16.0;
		        else if (action.equals("turn(right)")) 
		        	compassAngle -= Math.PI/16.0;
		        else if (action.equals( "thrust(on)")) 
		        	speedValue = SPEED;
		        else if (action.equals("thrust(off)")) 
		        	speedValue = 0;  
		  }
		  
		  
		  double movingAngle = compassAngle+agentState.getSpeedAngle();	  
		  
		  double cosv = Math.cos(movingAngle);
		  double sinv = Math.sin(movingAngle);
		  
		  
		  //	TODO: calculate new position
		  PVector temp = new PVector(Math.round(cosv*speedValue), Math.round(sinv*speedValue));
		  position.add(temp);
		  
		  //put info back into Agentstate
		  agentState.setPosition(position);
		  
		  //Normalize angle between 0 and 2 Pi
		  compassAngle = compassAngle % 2* Math.PI;
		  
		  agentState.setCompassAngle(compassAngle);
		  agentState.setSpeedValue(speedValue);
		  
		  
		  //TODO: calculate what we can see
		  
		  List<VisibleItem> things = new ArrayList<VisibleItem>();
		  
		  
		  //Calculate threats been seen
		  
		 for(int i=0; i<threats.size(); i++) { 
		  
			 //get relative position of aircraft to UAS:
			 float deltax = threats.get(i).position.x - getPosition().x;
			 float deltay = threats.get(i).position.y - getPosition().y;
		  
			 //convert to polar
			 //calculate distance
			 double dist  = Math.sqrt(deltax*deltax + deltay*deltay);
		  
			 if(dist<visionDistance) {
		  
				 double theta = Math.atan2(deltay, deltax);
				 double angle = (theta - getCompassAngle());// % 2* Math.PI; //(adjust to 0, 2pi) interval
		  
				 // to normalize between 0 to 2 Pi
				 if(angle<0) angle+=2*Math.PI;
				 if(angle>2*Math.PI) angle-=2*Math.PI;
				
				 //double angleAdjusted= angle % (2*Math.PI);
				 
				 if (angle < Math.PI/2. || angle > 3* Math.PI/2.) {
		  			//it's visible 
					 things.add(new VisibleItem("tree", angle, dist)); 	
		  		}
		    }		
		 }
			 	
		  
		 
		  for(int i=0; i<trees.size(); i++) { 
				 
			  //get relative position of aircraft to UAS:
			  float deltax = trees.get(i).X - getPosition().x;
			  float deltay = trees.get(i).Y - getPosition().y;
			  
			  //convert to polar
			  //calculate distance
			  double dist  = Math.sqrt(deltax*deltax + deltay*deltay);
			  
			  if(dist<visionDistance) {
			  
				  System.out.println("EN DISTANCIA"+"**********************************************");  
				  
				  
				  double theta = Math.atan2(deltay, deltax);
				  double angle = (theta - getCompassAngle());// % 2* Math.PI; //(adjust to 0, 2pi) interval
		
				  // to normalize between 0 to 2 Pi
				  if(angle<0) angle+=2*Math.PI;
				  if(angle>2*Math.PI) angle-=2*Math.PI;
				  
				  //angle = (getCompassAngle() - theta );// % 2* Math.PI;
				  
				  //angle = theta;
				  
				  if (angle < Math.PI/2. || angle > 3* Math.PI/2.) {
					  //it's visible 
					  things.add(new VisibleItem("tree", angle, dist));  
				  }
			  }	 
		  } 
			 
		  
		   for(int i=0; i<houses.size(); i++) { 
				  
			   //get relative position of aircraft to UAS:
			   float deltax = houses.get(i).X - getPosition().x;
			   float deltay = houses.get(i).Y - getPosition().y;
				  
			   //convert to polar
			   //calculate distance
			   double dist  = Math.sqrt(deltax*deltax + deltay*deltay);
				  
			   if(dist<visionDistance) {
				  
				   double theta = Math.atan2(deltay, deltax);
				   double angle = (theta - getCompassAngle());// % 2* Math.PI; //(adjust to 0, 2pi) interval
				  
				   // to normalize between 0 to 2 Pi
				   if(angle<0) angle+=2*Math.PI;
				   if(angle>2*Math.PI) angle-=2*Math.PI;
				   
				   if (angle < Math.PI/2. || angle > 3* Math.PI/2.) { 
					   //it's visible
					   things.add(new VisibleItem("house", angle, dist));  
				   }	  
			}  
			  
			  
	   }		 
		 
		  agentState.setCameraInfo(things); 
 }
	  
	  
	  
	  
	  
	  // State reset
	  public void reset(){
	     // Initialize data values
	    PVector position = initialPosition.copy(); //Assume that the initial position is at the center of the display window
	    agentState.setPosition(position);
	    agentState.setSpeedValue(0.0);
	    agentState.setCameraInfo(new ArrayList<VisibleItem>());
	  }
	 
	  
	
	 
}