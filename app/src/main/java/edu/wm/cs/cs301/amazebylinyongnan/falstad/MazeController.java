package edu.wm.cs.cs301.amazebylinyongnan.falstad;

import edu.wm.cs.cs301.amazebylinyongnan.falstad.CardinalDirection;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Cells;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Factory;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.MazeConfiguration;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.MazeContainer;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.MazeFactory;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Order;


import java.util.ArrayList;
import java.util.Iterator;

import java.io.Serializable;
import android.app.Application;
import android.os.Handler;

import edu.wm.cs.cs301.amazebylinyongnan.ui.GeneratingActivity;
import edu.wm.cs.cs301.amazebylinyongnan.ui.ManualPlayActivity;
import edu.wm.cs.cs301.amazebylinyongnan.ui.AutoPlayActivity;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Robot.Turn;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Constants.StateGUI;





/**
 * Class handles the user interaction. 
 * It implements a state-dependent behavior that controls the display and reacts to key board input from a user. 
 * At this point user keyboard input is first dealt with a key listener (SimpleKeyListener)
 * and then handed over to a MazeController object by way of the keyDown method.
 *
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class MazeController implements Order {
	// Follows a variant of the Model View Controller pattern (MVC).
	// This class acts as the controller that gets user input and operates on the model.
	// A MazeConfiguration acts as the model and this class has a reference to it.
	private MazeConfiguration mazeConfig ; 
	// Deviating from the MVC pattern, the controller has a list of viewers and 
	// notifies them if user input requires updates on the UI.
	// This is normally the task of the model in the MVC pattern.
	
	// views is the list of registered viewers that get notified
	final private ArrayList<Viewer> views = new ArrayList<Viewer>() ; 
	// all viewers share access to the same graphics object, the panel, to draw on
	private MazePanel panel ; 
		

	// state keeps track of the current GUI state, one of STATE_TITLE,...,STATE_FINISH, mainly used in redraw()
	private StateGUI state;
	// possible values are defined in Constants
	// user can navigate 
	// title -> generating -(escape) -> title
	// title -> generation -> play -(escape)-> title
	// title -> generation -> play -> finish -> title
	// STATE_PLAY is the main state where the user can navigate through the maze in a first person view

	private int percentdone = 0; 		// describes progress during generation phase
	private boolean showMaze;		 	// toggle switch to show overall maze on screen
	private boolean showSolution;		// toggle switch to show solution in overall maze on screen
	private boolean mapMode; // true: display map of maze, false: do not display map of maze
	// map_mode is toggled by user keyboard input, causes a call to draw_map during play mode

	// current position and direction with regard to MazeConfiguration
	private int px, py ; // current position on maze grid (x,y)
	private int dx, dy;  // current direction

	// current position and direction with regard to graphics view
	// graphics has intermediate views for a smoother experience of turns
	private int viewx, viewy; // current position
	private int viewdx, viewdy; // current view direction, more fine grained than (dx,dy)
	private int angle; // current viewing angle, east == 0 degrees
	//static final int viewz = 50;    
	private int walkStep; // counter for intermediate steps within a single step forward or backward
	private Cells seencells; // a matrix with cells to memorize which cells are visible from the current point of view
	// the FirstPersonDrawer obtains this information and the MapDrawer uses it for highlighting currently visible walls on the map

	// about the maze and its generation
	private int skill; // user selected skill level, i.e. size of maze
	private Builder builder; // selected maze generation algorithm
	private boolean perfect; // selected type of maze, i.e. 
	// perfect == true: no loops, i.e. no rooms
	// perfect == false: maze can support rooms
	
	// The factory is used to calculate a new maze configuration
	// The maze is computed in a separate thread which makes 
	// communication with the factory slightly more complicated.
	// Check the factory interface for details.
	private Factory factory;
	
	// Filename if maze is loaded from file
	private String filename;
	
	//private int zscale = Constants.VIEW_HEIGHT/2;
	private RangeSet rset;
	
	// debug stuff
	private boolean deepdebug = false;
	private boolean allVisible = false;
	private boolean newGame = false;
	
//---------------------new fields-------------------------------------------------------------
	private int pathLength;
	private float energyConsumption;
	private ManualDriver manualDriver;
	private RobotDriver autoDriver;
	private boolean manual = false;
	private BasicRobot robot;
//---------------------new GUI changes---------------------------------------------------------
	//private MazePanel choicePanel;
	//private int level;

    //---------------------New Fields for Android App-------------------------------------
    private GeneratingActivity generatingActivity;
	private ManualPlayActivity manualPlayActivity;
    private AutoPlayActivity autoPlayActivity;
    private int path;
    private int battery = 2500;
    //------------------------------------------------------------------------------------
	/**
	 * Constructor
	 * Default setting for maze generating algorithm is DFS.
	 */
	public MazeController() {
		super() ;
		//setBuilder(Order.Builder.DFS); 
		panel = new MazePanel() ;
		mazeConfig = new MazeContainer();
		factory = new MazeFactory() ;
		filename = null;
	}
	/**
	 * Constructor that also selects a particular generation method
	 */
	public MazeController(Order.Builder builder)
	{
		super() ;
		setBuilder(builder) ;
		panel = new MazePanel() ;
		mazeConfig = new MazeContainer();
		factory = new MazeFactory() ;
		filename = null;
	}
	/**
	 * Constructor to read maze from file
	 * @param filename
	 */
	public MazeController(String filename) {
		super();
		setBuilder(Order.Builder.DFS); 
		panel = new MazePanel() ;
		mazeConfig = new MazeContainer() ;
		factory = new MazeFactory(); // no factory needed but to allow user to play another round 
		this.filename = filename;
	}
	/**
	 * Loads maze from file and returns a corresponding maze configuration.
	 * @param filename
	 */
	private MazeConfiguration loadMazeConfigurationFromFile(String filename) {
		// load maze from file
		MazeFileReader mfr = new MazeFileReader(filename) ;
		// obtain MazeConfiguration
		return mfr.getMazeConfiguration();
	}

	@Override
    public void storeMaze(int width, int height, int rooms, int expected_partiters, BSPNode root, Cells cells, int[][] dists, int startX, int startY){
        MazeFileWriter mfw = new MazeFileWriter();

        mfw.store(getGeneratingActivity().getFilesDir()+"Maze"+Integer.toString(skill)+".xml", width, height, rooms, expected_partiters, root, cells, dists, startX, startY);
    }


	/**
	 * Method to initialize internal attributes. Called separately from the constructor. 
	 */
	public void init() {
		// special case: load maze from file
		if (null != filename) {
			setState(StateGUI.STATE_GENERATING);
			rset = new RangeSet(panel);
			//panel.initBufferImage() ;
			//addView(new MazeView(this)) ;
			// push results into controller, imitating maze factory delivery
			deliver(loadMazeConfigurationFromFile(filename));
			
			//new GUI change
			
			
			
			// reset filename, next round will be generated again
			filename = null;
			return;
		}
		// common case: generate maze with some algorithm
		assert null != factory : "MazeController.init: factory must be present";
		state = StateGUI.STATE_TITLE;
		rset = new RangeSet(panel);
		//panel.initBufferImage() ;
		//addView(new MazeView(this)) ;
		//notifyViewerRedraw() ;

        //random input
        switchToGeneratingScreen(0);
		
	}
	
	public MazeConfiguration getMazeConfiguration() {
		return mazeConfig ;
	}
	protected StateGUI getState(){
		return state;
	}
	
	///////////// methods for state transitions in UI automaton /////////////////////////////////////////
	// user can navigate 
	// title -> generating -(escape) -> title
	// title -> generation -> play -(escape)-> title
	// title -> generation -> play -> finish -> title
	// STATE_PLAY is the main state where the user can navigate through the maze in a first person view

	/**
	 * Switches to generating screen. 
	 * Uses the factory to start the generation of a maze with a background thread.
	 * This transition is only possible from the title screen. 
	 * @param key is user input, gives skill level to determine the width, height and number of rooms for the new maze
	 */
	private void switchToGeneratingScreen(int key) {
		//assert state == StateGUI.STATE_TITLE : "MazeController.switchToGeneratingScreen: unexpected current state " + state ;
		// switch state and update screen
		setState(StateGUI.STATE_GENERATING);
		percentdone = 0;
		//notifyViewerRedraw() ;
		
		//new GUI changes
		//key = level;
		
		// translate key into skill level if possible
        /*
		int skill = 0 ; // legal default value
		if (level >= '0' && level <= '9') {
			skill = level - '0';
		}
		if (level >= 'a' && level <= 'f') {
			skill = level - 'a' + 10;
		}
		*/
		// set fields to specify order
		//setSkillLevel(skill) ;
		// generation method already set in constructor method
		setPerfect(false); // allow for rooms
		// make maze factory produce a maze 
		// operates with background thread
		// method returns immediately, 
		// maze will be delivered later by calling this.deliver method
        factory.order(this) ;
		
		//new GUI change
		//remove boxes and button when the game start
		//and add back when switch to title screen.
		//removeBoxesNButton();
		
		
		
	}
	
	/**
	 * Switches to playing state, registers appropriate views, updates screen.
	 * This transition is only possible from the generating screen. 
	 */
	private void switchToPlayingScreen() {
		//assert state == StateGUI.STATE_GENERATING : "MazeController.switchToPlayingScreen: unexpected current state " + state ;
		// set the current state for the state-dependent behavior
		setState(StateGUI.STATE_PLAY);
		cleanViews() ;
		// register views for the new maze
		// reset map_scale in mapdrawer to a value of 10
		addView(new FirstPersonDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT, Constants.MAP_UNIT,
				Constants.STEP_SIZE, seencells, mazeConfig.getRootnode())) ;
		
		// order of registration matters, code executed in order of appearance!
		addView(new MapDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,Constants.MAP_UNIT,
				Constants.STEP_SIZE, seencells, 10, this)) ;

		//notifyViewerRedraw() ;
		
		
	}
	/**
	 * Switches to title screen, possibly canceling maze generation.
	 * This transition is possible from several screens.
	 */
	private void switchToTitleScreen(boolean cancelOrder) {
		/*
        System.out.println("switchToTitleScreen: param == " + cancelOrder) ;
		if (cancelOrder) {
			factory.cancel();
		}
		setState(StateGUI.STATE_TITLE);
		notifyViewerRedraw() ;
		
		//new GUI change
		//when the game restart
		choicePanel.addChoicePanel();
		*/
	}
	/**
	 * Switches to title screen, possibly canceling maze generation.
	 * This transition is only possible from the playing screen
	 * by making a forward or backward move through the exit outside
	 * of the maze.
	 */
	protected void switchToFinishScreen() {
		//assert state == StateGUI.STATE_PLAY : "MazeController.switchToFinishScreen: unexpected current state " + state ;
		setState(StateGUI.STATE_FINISH);
        if(manual){
            manualPlayActivity.navigateToWinActivity();
        }else{
            autoPlayActivity.navigateToWinActivity();
        }
		//notifyViewerRedraw() ;
        resetDriver();

	}
	/////////////////////////////// Methods for the Model-View-Controller Pattern /////////////////////////////
	/**
	 * Register a view
	 */
	public void addView(Viewer view) {
		views.add(view) ;
	}
	/**
	 * Unregister a view
	 */
	public void removeView(Viewer view) {
		views.remove(view) ;
	}
	/**
	 * Remove obsolete FirstPersonDrawer and MapDrawer
	 */
	private void cleanViews() {
		// go through views and remove viewers as needed
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			if ((v instanceof FirstPersonDrawer)||(v instanceof MapDrawer))
			{
				it.remove() ;
			}
		}

	}
	/**
	 * Notify all registered viewers to redraw their graphics
	 */
	protected void notifyViewerRedraw() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			//Graphics g = panel.getBufferGraphics() ;
			// viewers draw on the buffer graphics

            v.redraw(panel, state, px, py, viewdx, viewdy, walkStep, Constants.VIEW_OFFSET, rset, angle);

            /*
			if (null == panel.getBufferGraphics()) {
				System.out.println("Maze.notifierViewerRedraw: can't get graphics object to draw on, skipping redraw operation") ;
			}
			*/
		}
		// update the screen with the buffer graphics
		panel.update() ;
	}
	/** 
	 * Notify all registered viewers to increment the map scale
	 */
	private void notifyViewerIncrementMapScale() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			v.incrementMapScale() ;
		}
		// update the screen with the buffer graphics
		panel.update() ;
	}
	/** 
	 * Notify all registered viewers to decrement the map scale
	 */
	private void notifyViewerDecrementMapScale() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			v.decrementMapScale() ;
		}
		// update the screen with the buffer graphics
		panel.update() ;
	}
	////////////////////////////// get methods ///////////////////////////////////////////////////////////////
	boolean isInMapMode() { 
		return mapMode ; 
	} 
	boolean isInShowMazeMode() { 
		return showMaze ; 
	} 
	boolean isInShowSolutionMode() { 
		return showSolution ; 
	} 
	public String getPercentDone(){
		return String.valueOf(percentdone) ;
	}
	public MazePanel getPanel() {
		return panel ;
	}
	////////////////////////////// set methods ///////////////////////////////////////////////////////////////
	////////////////////////////// Actions that can be performed on the maze model ///////////////////////////
	protected void setCurrentPosition(int x, int y)
	{
		px = x ;
		py = y ;
	}
	private void setCurrentDirection(int x, int y)
	{
		dx = x ;
		dy = y ;
	}
	protected int[] getCurrentPosition() {
		int[] result = new int[2];
		result[0] = px;
		result[1] = py;
		return result;
	}
	protected CardinalDirection getCurrentDirection() {
		return CardinalDirection.getDirection(dx, dy);
	}

	/////////////////////// Methods for debugging ////////////////////////////////
	private void dbg(String str) {
		//System.out.println(str);
	}

	private void logPosition() {
		if (!deepdebug)
			return;
		dbg("x="+viewx/Constants.MAP_UNIT+" ("+
				viewx+") y="+viewy/Constants.MAP_UNIT+" ("+viewy+") ang="+
				angle+" dx="+dx+" dy="+dy+" "+viewdx+" "+viewdy);
	}
	
	//////////////////////// Methods for move and rotate operations ///////////////
	final double radify(int x) {
		return x*Math.PI/180;
	}
	/**
	 * Helper method for walk()
	 * @param dir
	 * @return true if there is no wall in this direction
	 */
	protected boolean checkMove(int dir) {
		CardinalDirection cd = null;
		switch (dir) {
		case 1: // forward
			cd = getCurrentDirection();
			break;
		case -1: // backward
			cd = getCurrentDirection().oppositeDirection();
			break;
		default:
			throw new RuntimeException("Unexpexted direction value: " + dir);
		}
		//return mazeConfig.getMazecells().hasNoWall(px, py, cd);
		return !mazeConfig.hasWall(px, py, cd);
	}
	/**
	 * Redraw and wait, used to obtain a smooth appearance for rotate and move operations
	 */
	private void slowedDownRedraw() {
        notifyViewerRedraw() ;
		try {
			Thread.currentThread().sleep(25);
		} catch (Exception e) { }
	}
	/**
	 * Intermediate step during rotation, updates the screen
	 */
	private void rotateStep() {
		angle = (angle+1800) % 360;
		viewdx = (int) (Math.cos(radify(angle))*(1<<16));
		viewdy = (int) (Math.sin(radify(angle))*(1<<16));
		slowedDownRedraw();
	}
	/**
	 * Performs a rotation with 4 intermediate views, 
	 * updates the screen and the internal direction
	 * @param dir for current direction
	 */
	synchronized private void rotate(int dir) {
		final int originalAngle = angle;
		final int steps = 4;

		for (int i = 0; i != steps; i++) {
			// add 1/4 of 90 degrees per step 
			// if dir is -1 then subtract instead of addition
			angle = originalAngle + dir*(90*(i+1))/steps; 
			rotateStep();
		}
		setCurrentDirection((int) Math.cos(radify(angle)), (int) Math.sin(radify(angle))) ;
		logPosition();
	}
	/**
	 * Moves in the given direction with 4 intermediate steps,
	 * updates the screen and the internal position
	 * @param dir, only possible values are 1 (forward) and -1 (backward)
	 */
	synchronized private void walk(int dir) {
		if (!checkMove(dir))
			return;
		// walkStep is a parameter of the redraw method in FirstPersonDrawer
		// it is used there for scaling steps
		// so walkStep is implicitly used in slowedDownRedraw which triggers the redraw
		// operation on all listed viewers

        for (int step = 0; step != 4; step++) {
			walkStep += dir;
			slowedDownRedraw();
		}
		setCurrentPosition(px + dir*dx, py + dir*dy) ;
		walkStep = 0;
		logPosition();
	}

	/**
	 * checks if the given position is outside the maze
	 * @param x
	 * @param y
	 * @return true if position is outside, false otherwise
	 */
	private boolean isOutside(int x, int y) {
		return !mazeConfig.isValidPosition(x, y) ;
	}

	/**
	 * Method incorporates all reactions to keyboard input in original code, 
	 * The simple key listener calls this method to communicate input.
	 */
	public boolean keyDown(int key) {
		String s1 = "before: px,py:" + px + "," + py + " dir:" + dx + "," + dy;
		// possible inputs for key: unicode char value, 0-9, A-Z, Escape, 'k','j','h','l'
		// depending on the current state of the GUI, inputs have different effects
		// implemented as a little automaton that switches state and performs necessary actions
		switch (state) {
		// if screen shows title page, keys describe level of expertise
		// create a maze according to the user's selected level
		// user types wrong key, just use 0 as a possible default value
		case STATE_TITLE:
			//switchToGeneratingScreen(key);
			break;
			// if we are currently generating a maze, recognize interrupt signal (ESCAPE key)
			// to stop generation of current maze
		case STATE_GENERATING:
            /*
			if (key == Constants.ESCAPE) {
				switchToTitleScreen(true);
			}
			*/
			break;

			// if user explores maze, 
			// react to input for directions and interrupt signal (ESCAPE key)	
			// react to input for displaying a map of the current path or of the overall maze (on/off toggle switch)
			// react to input to display solution (on/off toggle switch)
			// react to input to increase/reduce map scale
			
		case STATE_PLAY:

			switch (key) {
			//since duplicate key had been reduced in the simpleKeyListener
			case 'k': case '8':
				// move forward
				walk(1);
                battery = battery - 5;
				if (isOutside(px,py)) {
					switchToFinishScreen();
				}
				break;
			case 'h': case '4':
				// turn left
				rotate(1);
                battery = battery - 3;
                break;
			case 'l': case '6':
				// turn right
				rotate(-1);
                battery = battery - 3;
				break;
			case 'j': case '2':
				// move backward
				walk(-1);
                battery = battery - 5;
				if (isOutside(px,py)) {
					switchToFinishScreen();
				}
				break;
			case Constants.ESCAPE: case 65385:
				// escape to title screen
				switchToTitleScreen(false);
				break;
			case ('w' & 0x1f): 
				// Ctrl-w makes a step forward even through a wall
				// go to position if within maze
				if (mazeConfig.isValidPosition(px + dx, py + dy)) {
					setCurrentPosition(px + dx, py + dy) ;
					notifyViewerRedraw() ;
				}
				break;
			case '\t': case 'm':
				// show local information: current position and visible walls
				// precondition for showMaze and showSolution to be effective
				// acts as a toggle switch
				mapMode = !mapMode; 		
				notifyViewerRedraw() ; 
				break;
			case 'z':
				// show the whole maze
				// acts as a toggle switch
				showMaze = !showMaze; 		
				notifyViewerRedraw() ; 
				break;
			case 's':
				// show the solution as a yellow line towards the exit
				// acts as a toggle switch
				showSolution = !showSolution; 		
				notifyViewerRedraw() ;
				break;
			case '+': case '=':
				// zoom into map
				notifyViewerIncrementMapScale() ;
				notifyViewerRedraw() ; // seems useless but it is necessary to make the screen update
				break ;
			case '-':
				// zoom out of map
				notifyViewerDecrementMapScale() ;
				notifyViewerRedraw() ; // seems useless but it is necessary to make the screen update
				break ;
			} // end of internal switch statement for playing state
			// debug
			/*
			if (this.mazeConfig.isValidPosition(px, py)) {
				String s2 = "after: px,py:" + px + "," + py + " dir:" + dx + "," + dy;
				System.out.println("mc.keydown:" + s1 + "  " + s2);
				String s3 = "Walls at: px,py:" + px + "," + py;
				s3 += " ESWN: " + mazeConfig.hasWall(px, py, CardinalDirection.East) +
						"," + mazeConfig.hasWall(px, py, CardinalDirection.South) +
						"," + mazeConfig.hasWall(px, py, CardinalDirection.West) +
						"," + mazeConfig.hasWall(px, py, CardinalDirection.North);
				System.out.println("mc.keydown:" + s3);
			}
			*/
			//
			break ;
		// if we are finished, return to initial state with title screen	
		case STATE_FINISH:
			//switchToTitleScreen(false);
			break;
		} 
		return true;
	}


	////////// set methods for fields ////////////////////////////////
	private void setSkillLevel(int skill) {
		this.skill = skill ;
	}

	private void setBuilder(Builder builder) {
		this.builder = builder ;
	}

	private void setPerfect(boolean perfect) {
		this.perfect = perfect ;
	}
	/**
	 * Sets the internal state of the game state.
	 * Method checks if state transition is as expected.
	 * @param newState the state to set
	 */
	protected void setState(StateGUI newState) {
		// check if transition is as expected
		// null -> STATE_TITLE: game starts, initialization
		// TITLE->GENERATING: game switches to generating screen
		// GENERATING -> TITLE: escape button, reset game
		// GENERATING -> PLAYING: maze generation finished, start game
		// PLAYING -> TITLE: escape button, reset game
		// PLAYING -> FINISH: game over
		// FINISH -> TITLE: reset game
		
		// special case: game starts
        /*
		if (null == state) {
			// newState should be initial state in actual game
			// may be different for testing purposes
			if (newState != StateGUI.STATE_TITLE) {
				System.out.println("Warning: MazeController.StateGUI: automaton starts in state: " + newState);
			}
			state = newState; // update operation
			return;
		}
        */
		// state != null, safe to use switch statement
        /*
		String msg = "MazeController.setState: illegal state transition: " + state + " to " + newState;
		switch (state) {
		case STATE_TITLE:
			assert (newState == StateGUI.STATE_GENERATING) : msg ;	
			break;
		case STATE_GENERATING:
			assert (newState == StateGUI.STATE_TITLE || newState == StateGUI.STATE_PLAY) : msg ;
			break;
		case STATE_PLAY:
			assert (newState == StateGUI.STATE_TITLE || newState == StateGUI.STATE_FINISH) : msg ;
			break;
		case STATE_FINISH:
			assert (newState == StateGUI.STATE_TITLE) : msg ;	
			break;
		default: 
			throw new RuntimeException("Inconsistent enum type") ;
		}
		*/
		// update operation
		state = newState;
	}




	///////////////// methods to implement Order interface //////////////
	@Override
	public int getSkillLevel() {
		return skill;
	}
	@Override
	public Builder getBuilder() {
		return builder ;
	}
	@Override
	public boolean isPerfect() {
		return perfect;
	}
	@Override
	public void deliver(MazeConfiguration mazeConfig) {
        System.out.println("Deliver method is called.");
		this.mazeConfig = mazeConfig ;
		
		// WARNING: DO NOT REMOVE, USED FOR GRADING PROJECT ASSIGNMENT
		if (Cells.deepdebugWall)
		{   // for debugging: dump the sequence of all deleted walls to a log file
			// This reveals how the maze was generated
			mazeConfig.getMazecells().saveLogFile(Cells.deepedebugWallFileName);
		}
		////////
		
		// adjust internal state of maze model
		// visibility settings
		showMaze = false ;
		showSolution = false ;
		mapMode = false;
		// init data structure for visible walls
		seencells = new Cells(mazeConfig.getWidth()+1,mazeConfig.getHeight()+1) ;
		// obtain starting position
		int[] start = mazeConfig.getStartingPosition() ;
		setCurrentPosition(start[0],start[1]) ;
		// set current view direction and angle
		setCurrentDirection(1, 0) ; // east direction
		viewdx = dx<<16; 
		viewdy = dy<<16;
		angle = 0; // angle matches with east direction, hidden consistency constraint!
		walkStep = 0; // counts incremental steps during move/rotate operation
		
		// update screens for playing state
		switchToPlayingScreen();
	}
	/**
	 * Allows external increase to percentage in generating mode.
	 * Internal value is only update if it exceeds the last value and is less or equal 100
	 * @param percentage gives the new percentage on a range [0,100]
	 * @return true if percentage was updated, false otherwise
	 */
	@Override
	public void updateProgress(int percentage) {
		if (percentdone < percentage && percentage <= 100) {
			percentdone = percentage;
			if (state == StateGUI.STATE_GENERATING)
			{
				//send the percentage info to generating activity
                generatingActivity.updatePercentage(percentdone);
                //notifyViewerRedraw() ;
			}
			else
				dbg("Warning: Receiving update request for increasePercentage while not in generating state, skip redraw.") ;
		}
	}	
	
	
//------------------------------new methods------------------------------------
	
	//these are new setter and getter methods for pathLength and EnergyConsumption.
	
	/**
	 * This method helps to set the pathLength. This method is called when the game is over.
	 * @param pathLength
	 */
	public void setPathLength(int pathLength) {
		this.pathLength = pathLength;
	}
	
	/**
	 * This method returns the pathLength.
	 * @return
	 */
	public int getPathLength() {
		return pathLength;
	}
	
	/**
	 * This method is called when either the robot gets out of maze or when the robot runs out of battery.
	 * If the robots gets our of maze, a total energy consumption is set through this method, otherwise, a negative 
	 * number is set so the MazeView can know what message to display.
	 * @param energyConsumption
	 */
	public void setEnergyConsumption(float energyConsumption) {
		this.energyConsumption = energyConsumption;
	}
	
	/**
	 * This method helps the mazeView to display the total energy consumption.
	 * @return
	 */
	public float getEnergyConsumption() {
		return energyConsumption;
	}
	
	/**
	 * This method is called when the start button is clicked and we extract from the user input, set that input
	 * to the field inside the mazeController class.
	 * @param driverType
	 */
	public void setDriver(String driverType){
		if (driverType.equals("Manual")) {
			System.out.println("You choose manual mode.");
			manualDriver = new ManualDriver();
			
			//assign a robot to the driver
			robot = new BasicRobot();
			robot.setMaze(this);
			manualDriver.setRobot(robot);
			
			
			//this is for is ManualDriver(), which is used by simplekeyListener class.
			manual = true;
            panel.setManual(true);
		}
		else if (driverType.equals("Wizard")) {
			System.out.println("You choose Wizard as your driver.");
			System.out.println("Press m,z,s to display map and solution, then press any key to start auto driving.");
			System.out.println("No key stroke is accepted during auto driving.");
			autoDriver = (Wizard) new Wizard();
			
			robot = new BasicRobot();
			robot.setMaze(this);
			autoDriver.setRobot(robot);
            panel.setManual(false);

            //let wizard set pathLength
            autoDriver.callDriverSpecificMethod();
			
		}
		else if (driverType.equals("WallFollower")) {
			System.out.println("You choose WallFollower as your driver.");
			autoDriver = (WallFollower) new WallFollower();
			
			robot = new BasicRobot();
			robot.setMaze(this);
			autoDriver.setRobot(robot);
            panel.setManual(false);

        }else{
			assert driverType.equals("Pledge"): "Unexpected driver type";
			System.out.println("You choose Pledge as your driver.");
			autoDriver = (Pledge) new Pledge();
			
			robot = new BasicRobot();
			robot.setMaze(this);
			autoDriver.setRobot(robot);
            panel.setManual(false);

            //let pledge select main direction.
            autoDriver.callDriverSpecificMethod();


        }
	}

    /**
     * This method return the manual driver.
     * @return
     */
    public ManualDriver getManualDriver(){
        return manualDriver;
    }

	
	/**
	 * This method return the auto driver.
	 * @return
	 */
	public RobotDriver getAutoDriver(){
		return autoDriver;
	}
	
	/**
	 * This method helps the SimpleKeyListener to determine whether it is dealing with a manual driver or
	 * an auto driver.
	 * @return
	 */
	public boolean isManualDriver() {
		return manual;
	}
	
	/**
	 * This method is called when the start button is clicked, and we extract from the user input and set that
	 * to the field inside the mazeController.
	 * @param builderType
	 */
	public void setBuilder(String builderType) {
		if(builderType.equals("DFS")) {
			System.out.println("You choose DFS algorithm to build maze.");
			setBuilder(Builder.DFS);
		}
		else if (builderType.equals("Prim")) {
			System.out.println("You choose Prim ");
			setBuilder(Builder.Prim);
		}else{
			assert builderType.equals("Eller"): "Unexpected builder type.";
			System.out.println("You choose Eller");
			setBuilder(Builder.Eller);
		}
	}
	
	/**
	 * This method is called when the start button is clicked and we set the user input to the field inside the 
	 * mazeController.
	 * @param level
	 */
    /*
	public void setSkillLevel(int level) {
		//System.out.println("You choose skill level: " + level);
		//this.level = level.charAt(0);
	}
	*/

	
	/**
	 * This method helps to reset some parameters such as pathLength and energy level, but it firsts determines whether
	 * the input is manual driver or not.
	 */
	public void resetDriver() {
		if (manual) {
			manualDriver.reset();
		}else{
			autoDriver.reset();
		}
		manual = false;
	}

	//---------------------------New Methods for Android App----------------------------
    //constructor for play activity
    public MazeController(ManualPlayActivity activity){
        manualPlayActivity = activity;
        panel = new MazePanel();
    }

    public MazeController(AutoPlayActivity activity){
        autoPlayActivity = activity;
        panel = new MazePanel();
    }

    public void beginGraphics(){
        notifyViewerRedraw();
    }

    /*
    public MazePanel getAndroidPanel(){
        return androidPanel;
    }
    */


    public void setUpInputsInController(int level, String builder, String driver){
        setSkillLevel(level);
        setBuilder(builder);
        setDriver(driver);
    }

    public GeneratingActivity getGeneratingActivity(){
        return generatingActivity;
    }

    public void setGeneratingActivity(GeneratingActivity generatingActivity){
        this.generatingActivity = generatingActivity;
    }

    public void setManualPlayActivity(ManualPlayActivity manualPlayActivity){
        this.manualPlayActivity = manualPlayActivity;
    }

    public void setAutoPlayActivity(AutoPlayActivity autoPlayActivity){
        this.autoPlayActivity = autoPlayActivity;
    }

    //call intent to manualplay and

    @Override
    public void notifyController(){
        generatingActivity.startPlayActivity();
        switchToPlayingScreen();
    }

    public void updateBatteryLevel(float batteryLevel){
        if (manual){
            manualPlayActivity.updateBatteryLevel(batteryLevel);
        }else{
            autoPlayActivity.updateBatteryLevel(batteryLevel);
        }
    }

    public void updatePathLength(int pathLength){
        if(manual){
            manualPlayActivity.updatePathLength(pathLength);
        }else{
            autoPlayActivity.updatePathLength(pathLength);
        }
    }

    public void updatePath(int pathLength){
        path = pathLength;
    }

    public int getBattery(){
        return battery;
    }

    public int getPath(){
        return path;
    }


	//----------------------------------------------------------------------------------

	
	
}
