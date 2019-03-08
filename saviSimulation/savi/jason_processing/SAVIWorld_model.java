package savi.jason_processing;
import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*;
import savi.jason_processing.SAVIWorld_model.Button;
import savi.StateSynchronization.*;

import java.util.*;
import java.util.logging.Logger;
import java.io.*;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.infra.centralised.BaseCentralisedMAS;

public class SAVIWorld_model extends PApplet {

	/********** CONSTANTS TO BE LOADED FROM CONFIG FILE**********/
	int	NUMBER_TREES;
	int NUMBER_HOUSES;
	int NUMBER_THREATS; 
	int FRAME_RATE;
	int MAX_IN_X_VEL_THREAT;
	int MAX_IN_Y_VEL_THREAT;
	double MAX_SPEED;
	int PERCEPTION_DISTANCE;
	int WIFI_PERCEPTION_DISTANCE;
	int NUMBER_UAV;
	int NUMBER_UGV;
	int RANDOM_SEED;
	double REASONING_CYCLE_PERIOD;
	int TREE_SIZE;
	int HOUSE_SIZE;
	int THREAT_SIZE;
	int UAV_SIZE;
	int UGV_SIZE;
	double SENSORS_ERROR_PROB;
	double SENSORS_ERROR_STD_DEV;
	/********** CONSTANTS THAT CANNOT BE LOADED FROM THE CONF FILE **********/
	int X_PIXELS = 900;
	int Y_PIXELS = 700;
	int Z_PIXELS = 500;
	
	// TimeStamp file names
	long lastCycleTimeStamp;
	String timeStampFileName;
	
	private static Logger logger = Logger.getLogger(SAVIWorld_model.class.getName());
	
	//Load Parameters
	FileInputStream in = null;	
	Properties modelProps = new Properties();
		
		/************* Global Variables *******************/
	double simTime;      // stores simulation time (in milliseconds) 
	double simTimeDelta; // discrete-time step (in milliseconds)
	boolean simPaused;// simulation paused or not

	List<WorldObject> objects = new ArrayList<WorldObject>();//List of world objects

	JasonMAS jasonAgents; // the BDI agents

	Button playButton,stopButton,pauseButton;
	PShape uasImage,treeImage,houseImage,threatImage,play,pause,restart;

	public void settings() { size(X_PIXELS,Y_PIXELS, P3D);  smooth(8); } // 3D environment

	public static void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "savi.jason_processing.SAVIWorld_model" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}

/************* Main setup() ***********************/
// Main system initialization function
// called by Processing once on startup
//************************************************/
public void setup() {
	/***LOAD SIM PARAMETERS ****/
	try {
		String filePath = new File("").getAbsolutePath();
		filePath = filePath + "/config.cfg";
		System.out.println(filePath);
		File inFile = new File(filePath);
		in = new FileInputStream(inFile);
		modelProps.load(in);
		}
	catch (FileNotFoundException  e) {
		System.out.println("File not found");
	}catch (Exception  e) {
		System.out.println("Exception occurred");
	}
	NUMBER_TREES = Integer.parseInt(modelProps.getProperty("NUMBER_TREES"));
	NUMBER_HOUSES = Integer.parseInt(modelProps.getProperty("NUMBER_HOUSES"));
	NUMBER_THREATS = Integer.parseInt(modelProps.getProperty("NUMBER_THREATS"));
	//X_PIXELS = Integer.parseInt(modelProps.getProperty("X_PIXELS"));
	//Y_PIXELS = Integer.parseInt(modelProps.getProperty("Y_PIXELS"));
	FRAME_RATE = Integer.parseInt(modelProps.getProperty("FRAME_RATE"));
	MAX_SPEED = (double) Double.parseDouble(modelProps.getProperty("MAX_SPEED"));
	PERCEPTION_DISTANCE = Integer.parseInt(modelProps.getProperty("PERCEPTION_DISTANCE"));
	WIFI_PERCEPTION_DISTANCE = Integer.parseInt(modelProps.getProperty("WIFI_PERCEPTION_DISTANCE"));
	NUMBER_UGV = Integer.parseInt(modelProps.getProperty("NUMBER_UGV"));
	NUMBER_UAV = Integer.parseInt(modelProps.getProperty("NUMBER_UAV"));
	RANDOM_SEED = Integer.parseInt(modelProps.getProperty("RANDOM_SEED"));
	REASONING_CYCLE_PERIOD = (double) Double.parseDouble(modelProps.getProperty("REASONING_CYCLE_PERIOD"));
	TREE_SIZE = Integer.parseInt(modelProps.getProperty("TREE_SIZE"));
	HOUSE_SIZE = Integer.parseInt(modelProps.getProperty("HOUSE_SIZE"));
	THREAT_SIZE = Integer.parseInt(modelProps.getProperty("THREAT_SIZE"));
	UGV_SIZE = Integer.parseInt(modelProps.getProperty("UGV_SIZE"));
	UAV_SIZE = Integer.parseInt(modelProps.getProperty("UAV_SIZE"));
	SENSORS_ERROR_PROB = (double) Double.parseDouble(modelProps.getProperty("SENSORS_ERROR_PROB"));
	SENSORS_ERROR_STD_DEV = (double) Double.parseDouble(modelProps.getProperty("SENSORS_ERROR_STD_DEV"));
	// Initialization code goes here
	simTime = 0;      // seconds
	simTimeDelta = 1000/FRAME_RATE; // milliseconds
	
	simPaused = false;// not paused by default  
   
	playButton = new Button("play", width/2-20, 10, 40, 40);
	pauseButton = new Button("pause", width/2-20, 10, 40, 40);
	stopButton = new Button("restart", width/2+20, 10, 40, 40);
	
	play=loadShape("SimImages/play.svg");
	pause=loadShape("SimImages/pause.svg");
	restart=loadShape("SimImages/replay.svg");

	//load images for visualization
	treeImage=loadShape("SimImages/tree.svg");
	houseImage=loadShape("SimImages/home.svg");
	play=loadShape("SimImages/play.svg");
	pause=loadShape("SimImages/pause.svg");
	restart=loadShape("SimImages/replay.svg");
	threatImage=loadShape("SimImages/warning.svg");
	uasImage = loadShape("SimImages/robot.svg");
	
	Random rand = new Random();
	
	for(int i = 0; i < NUMBER_UAV+NUMBER_UGV; i++)  { //Put UAS
		//_PIXELS is the maximum and the 1 is our minimum
		//TODO: right now agents are initialized with strings "0", "1", "2", ... as identifiers and a fixed type "demo" which matches their asl file name. This should be configurable...
		if(RANDOM_SEED != -1) {
			rand = new Random(RANDOM_SEED+i);
		}	
	
		if(i < NUMBER_UGV)  { //Put UaV
			//_PIXELS is the maximum and the 1 is our minimum
			//TODO: right now agents are initialized with strings "0", "1", "2", ... as identifiers and a fixed type "demo" which matches their asl file name. This should be configurable...
			objects.add(new UgV(i, new PVector(rand.nextInt(X_PIXELS) + 1, rand.nextInt(Y_PIXELS) + 1, UGV_SIZE/2), UGV_SIZE,"demo", this, uasImage, REASONING_CYCLE_PERIOD, SENSORS_ERROR_PROB, SENSORS_ERROR_STD_DEV, RANDOM_SEED));
		}else {
			objects.add(new UaV(i, new PVector(rand.nextInt(X_PIXELS) + 1, rand.nextInt(Y_PIXELS) + 1, UAV_SIZE/2), UAV_SIZE,"demo", this, uasImage, REASONING_CYCLE_PERIOD, SENSORS_ERROR_PROB, SENSORS_ERROR_STD_DEV, RANDOM_SEED));
		}
		
	}
	
	for(int i = 0; i < NUMBER_TREES; i++) { //Put trees
		//_PIXELS is the maximum and the 1 is our minimum.
		if(RANDOM_SEED != -1) {
			rand = new Random(2*RANDOM_SEED+i);
		}
		objects.add(new WorldObject(i, new PVector(rand.nextInt(X_PIXELS) + 1, rand.nextInt(Y_PIXELS) + 1, TREE_SIZE/2), TREE_SIZE, "tree", this, treeImage));
	}
	for(int i = 0; i < NUMBER_HOUSES; i++) { //Put houses
		//_PIXELS is the maximum and the 1 is our minimum.
		if(RANDOM_SEED != -1) {
			rand = new Random(3*RANDOM_SEED+i);
		}
		objects.add(new WorldObject(i, new PVector(rand.nextInt(X_PIXELS) + 1, rand.nextInt(Y_PIXELS) + 1, HOUSE_SIZE/2), HOUSE_SIZE, "house", this, houseImage));
	}
	for(int i = 0; i < NUMBER_THREATS; i++) { //Put threats
		//_PIXELS is the maximum and the 1 is our minimum.
		if(RANDOM_SEED != -1) {
			rand = new Random(4*RANDOM_SEED+i);
		}
		objects.add(new Threat(i, rand.nextInt(X_PIXELS) + 1, rand.nextInt(Y_PIXELS) + 1, THREAT_SIZE/2, RANDOM_SEED, MAX_SPEED, THREAT_SIZE, "threat", this, threatImage));

	}          

	// smoother rendering (optional)
	frameRate(FRAME_RATE); // max 60 draw() calls per real second. (Can make it a larger value for the simulation to go faster)
	// simTimeDelta is now 1000/FRAME_RATE, meaning the simulation is in real-time if the processor can manage it.

	//If the processor is not fast enough to maintain the specified rate, the frame rate will not be achieved 

	//======= set up Jason BDI agents ================
	Map<String,AgentModel> agentList = new HashMap<String,AgentModel>();
	
	for(WorldObject wo: objects) {//Create UAS agents
		if(wo instanceof UxV) {
			agentList.put(((UxV)wo).getBehavior().getID(), ((UxV)wo).getBehavior());
		}	
	}
	
	jasonAgents = new JasonMAS(agentList);
	jasonAgents.startAgents();
	//==========================================
	
	// Set up the cycle length logfile
	this.lastCycleTimeStamp = 0;
	this.timeStampFileName = "SimulationTimeStamps.log";
	try {
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.timeStampFileName));
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

/************* Main draw() ***********************/
// Main state update and visualization function
// called by Processing in an infinite loop
//************************************************/
	public void draw(){
		if(simPaused){
			background(240); // white background

			for(WorldObject wo: objects){ //Makes all objects on screen.
					wo.draw();
			}	
			      
			playButton.label="play";

			playButton.drawButton();
			stopButton.drawButton();

			return; // don't change anything if sim is paused
		}
	
	
		// 1. TIME UPDATE
	simTime += simTimeDelta; // simple discrete-time advance
	
	long currentSystemTime = System.currentTimeMillis();
	long simulationCycleTime = currentSystemTime - this.lastCycleTimeStamp;
	this.lastCycleTimeStamp = currentSystemTime;
	
	// Write the timestamp to the timestamp logfile
	try {
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.timeStampFileName, true));
		writer.append((new Long(simulationCycleTime)).toString());
		writer.newLine();
		writer.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	logger.info("== SAVIWorld_Model draw() == at:"+simTime);
	// 2. STATE UPDATE (SIMULATION)
	for(WorldObject wo: objects){ //Update threats
			wo.update(simTime, simTimeDelta, PERCEPTION_DISTANCE, WIFI_PERCEPTION_DISTANCE, objects);
	}  
	// 3. VISUALIZATION
	//------------------
	background(240); // white background
    
	for(WorldObject wo: objects){ //Makes all objects on screen.
			wo.draw();
	}
	
	playButton.label="pause";
	
	playButton.drawButton();
	stopButton.drawButton();
}


public void mousePressed(){

	if(playButton.MouseIsOver()){
		pauseSimulation();
	}

	if(stopButton.MouseIsOver()){
		resetSimulation();
	}
}

//************ UTILITY FUNCTIONS *****************/
// These are general helper functions that don't
// belong to any particular class.
//************************************************/
// Reset the simulation
public void resetSimulation(){
	// Set simulation time to zero
	simTime = 0;

	objects.removeAll(objects);
	
	setup();
	// Unpause the simulation =" use other method to ensure that agents are also unpaused
	pauseSimulation();
}
// Pause the simulation
public void pauseSimulation(){
	if(!simPaused) { //the sim is NOT paused and we want to pause it
		
		System.out.println("pausing simulation!-------===================================================");
		for(WorldObject wo: objects){ //unpause all agents
			if(wo instanceof UaV) {
				((UaV)wo).getBehavior().pauseAgent();
			}	
		}
	} else { //the sim was paused, unpause it
		System.out.println("resuming simulation!-------");
		for(WorldObject wo: objects){ //pause all agents
			if(wo instanceof UaV)
				((UaV)wo).getBehavior().unPauseAgent();			
		}	
	}
	simPaused = !simPaused;
}


//************** USER INPUT *****************************/
// These functions handle user input events
// See "Input" subsection in processing.org/reference
//************r*******************************************/
public void keyPressed(){  // handle keyboard input
		switch(key) {
		case 'r': case 'R': resetSimulation(); break; // reset the simulation
		case ' ':           pauseSimulation(); break; // pause the simulation   
		default: break; // ignore any other key presses
		}
	}


//the Button class
public class Button{
	String label; // button label
	float x;      // top left corner x position
	float y;      // top left corner y position
	float w;      // width of button
	float h;      // height of button

	// constructor
	Button(String labelB, float xpos, float ypos, float widthB, float heightB){
		label = labelB;
		x = xpos;
		y = ypos;
		w = widthB;
		h = heightB;
	}

	void drawButton(){
		shapeMode(CORNER);

		if(label.contentEquals("play")){
			//s=loadShape("play.svg");
			shape(play, x, y, w, h);
		}
		else if(label.contentEquals("restart")){
			//s=loadShape("replay.svg");
			shape(restart, x, y, w, h);
		}
		else {
			//s=loadShape("pause.svg");
			shape(pause, x, y, w, h);
		}
	}

	boolean MouseIsOver(){
		if (mouseX > x && mouseX < (x + w) && mouseY > y && mouseY < (y + h)){
			return true;
		}
		return false;
	}

}


}