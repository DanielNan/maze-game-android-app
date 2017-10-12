package edu.wm.cs.cs301.amazebylinyongnan.falstad;


import android.os.Handler;

import edu.wm.cs.cs301.amazebylinyongnan.falstad.Constants.StateGUI;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.CardinalDirection;

/**
 * 
 * This class implements the interface class Robot.
 * It establishes the Robot platform and works on the maze controller and 
 * the maze graphics.
 * This class uses mazeController.keyDown method to perform rotate and move 
 * operations and the mazeController class is aware of the graphics
 * and sends notifications if move or rotate operations require an update
 * to the graphics.
 * The robot has distance sensors in all 4 directions and a room sensor.
 * The robot can move forward and can turn, and it also supports move backward
 * by combining series of movements.
 * There is also some energy consumption being involved in any movements
 * and sensor operation and the robot draws energy from its battery on its journey.
 * The final screen will report on the length of the chosen path and the total
 * energy consumed.
 * 
 * @author Linyong Nan
 *
 */
public class BasicRobot implements Robot {
	//Constants
	private static final int INITIAL_BATTERY_LEVEL = 2500;
	private static final int ENERGY_CONSUMED_DETECTING = 1;
	private static final int ENERGY_CONSUMED_ROTATE_90DEGREES = 3;
	private static final int ENERGY_CONSUMED_MOVE_1STEP = 5;
	
	
	//private fields
	private MazeController controller;	
	private float batteryLevel = INITIAL_BATTERY_LEVEL;
	private boolean stopped = false;	
	private int pathLength;
	
	private boolean hasWall;
	
	//constructor
	public BasicRobot() {
		controller = new MazeController();
	}
	
	
	@Override
	public void rotate(Turn turn) {
		if (!stopped && batteryLevel > 0) {
			switch(turn) {
			case LEFT:
				//rotate left by pressing 'h'.
				assert controller.getState() == StateGUI.STATE_PLAY : "Controller is not under STATE_PLAY." + controller.getState();
				System.out.println("Direction before the rotation: " + controller.getCurrentDirection());
				controller.keyDown('h');
				System.out.println("Direction after the rotation: " + controller.getCurrentDirection());
				break;
			case RIGHT:
				assert controller.getState() == StateGUI.STATE_PLAY : "Controller is not under STATE_PLAY." + controller.getState();
				System.out.println("Direction before the rotation: " + controller.getCurrentDirection());
				controller.keyDown('l');
				System.out.println("Direction after the rotation: " + controller.getCurrentDirection());
				break;
			case AROUND:
				//rotate around by pressing 'l' twice.
				assert controller.getState() == StateGUI.STATE_PLAY : "Controller is not under STATE_PLAY." + controller.getState();
				System.out.println("Direction before the rotation: " + controller.getCurrentDirection());
				controller.keyDown('l');
				controller.keyDown('l');
				System.out.println("Direction after the rotation: " + controller.getCurrentDirection());
				break;
			}
			if (turn.name() == "AROUND")
				batteryLevel = batteryLevel - 2 * ENERGY_CONSUMED_ROTATE_90DEGREES;
			else {
				batteryLevel = batteryLevel - ENERGY_CONSUMED_ROTATE_90DEGREES;
			}

            controller.updateBatteryLevel(batteryLevel);

		}
		//if after rotate the battery level is negative, then set it to zero and stop the robot.
		if (batteryLevel <= 0) {
			stopped = true;
			batteryLevel = 0;
			//if didn't end the game normally, set energyConsumption to -1.
			controller.setEnergyConsumption(-1);
			controller.switchToFinishScreen();

            controller.updateBatteryLevel(batteryLevel);

        }
		
	}
	
	@Override
	public void move (int distance, boolean manual) {

		//if manual mode
		if (manual) {
			int walkStep = 0;
			while (walkStep < distance && batteryLevel > 0 && !stopped) {
				if (controller.getMazeConfiguration().hasWall(controller.getCurrentPosition()[0], controller.getCurrentPosition()[1], controller.getCurrentDirection())) {
					walkStep++;
					hasWall = true;
				}else{
					//move forward by pressing 'k'.
					assert controller.getState() == StateGUI.STATE_PLAY : "Controller is not under STATE_PLAY.";
					hasWall = false;
					controller.keyDown('k');
					walkStep++;
					pathLength++;
                    controller.updatePath(pathLength);
					batteryLevel = batteryLevel - ENERGY_CONSUMED_MOVE_1STEP;
                    controller.updateBatteryLevel(batteryLevel);
                }
			}
		}else{
			int walkStep = 0;
			while (walkStep < distance && batteryLevel > 0 && !stopped) {
				if (controller.getMazeConfiguration().hasWall(controller.getCurrentPosition()[0], controller.getCurrentPosition()[1], controller.getCurrentDirection())) {
					walkStep++;
					hasWall = true;
				}else{
					//move forward by pressing 'k'.
					assert controller.getState() == StateGUI.STATE_PLAY : "Controller is not under STATE_PLAY.";
					hasWall = false;
					controller.keyDown('k');
					walkStep++;
                    pathLength++;
                    controller.updatePath(pathLength);
                    batteryLevel = batteryLevel - ENERGY_CONSUMED_MOVE_1STEP;
                    controller.updateBatteryLevel(batteryLevel);
                }
			}
		}
		//if after move the battery level is negative, then set it to zero and stop the robot.
		if (batteryLevel <= 0) {
			stopped = true;
			batteryLevel = 0;	
			controller.setEnergyConsumption(-1);
			controller.switchToFinishScreen();
            controller.updateBatteryLevel(batteryLevel);
        }
	}
	
	@Override
	public int[] getCurrentPosition() throws Exception {
		return controller.getCurrentPosition();
	}

	@Override
	public void setMaze(MazeController maze) {
		assert null != maze : "BasicRobot.setMaze: Maze should not be null.";
		if (maze != null) {
			controller = maze;
		}
	}

	@Override
	public boolean isAtExit() {
		return controller.getMazeConfiguration().getMazecells().isExitPosition(controller.getCurrentPosition()[0], controller.getCurrentPosition()[1]);
	}

	@Override
	public boolean canSeeExit(Direction direction) throws UnsupportedOperationException {
		batteryLevel = batteryLevel - ENERGY_CONSUMED_DETECTING;
		return distanceToObstacle(direction) == Integer.MAX_VALUE;
	}

	@Override
	public boolean isInsideRoom() throws UnsupportedOperationException {
		return controller.getMazeConfiguration().getMazecells().isInRoom(controller.getCurrentPosition()[0],controller.getCurrentPosition()[1]);
	}

	@Override
	public boolean hasRoomSensor() {
		return true;
	}

	@Override
	public CardinalDirection getCurrentDirection() {
		assert controller.getCurrentDirection() != null: "Controller hasn't been setup yet.";
		return controller.getCurrentDirection();
	}

	@Override
	public float getBatteryLevel() {
		assert batteryLevel >= 0 : "BasicRobot.getBatteryLevel: Battery level should be a nonnegative value.";
		return batteryLevel;
	}

	@Override
	public void setBatteryLevel(float level) {
		assert level > 0 : "BasicRobot.setBatteryLevel: Battery level <= 0, the robot stops to function.";
		if (level >= 0) {
			batteryLevel = level;
		}
	}

	@Override
	public float getEnergyForFullRotation() {
		return 4 * ENERGY_CONSUMED_ROTATE_90DEGREES;
	}

	@Override
	public float getEnergyForStepForward() {
		return ENERGY_CONSUMED_MOVE_1STEP;
	}

	@Override
	public boolean hasStopped() {
		return stopped;
	}
	
	
	@Override
	public int distanceToObstacle(Direction direction) throws UnsupportedOperationException {
		//searchDirection = superimpose cardinalDirection and relative direction. 
		CardinalDirection searchDirection = findSearchingDirection(direction);
		int px = controller.getCurrentPosition()[0];
		int py = controller.getCurrentPosition()[1];
		int distance = 0;
		boolean noObstacle = !controller.getMazeConfiguration().hasWall(px, py, searchDirection);
		
		//search each direction
		switch (searchDirection) {
		case East:
			//proceed by adding the distance by 1, until it meets any form of obstacle
			//if proceed to an invalid position, then we get out of the maze. 
			while(noObstacle){
				if (px+distance == controller.getMazeConfiguration().getWidth()-1) {
					noObstacle = false;
				}
				else if(!controller.getMazeConfiguration().isValidPosition(px+distance, py)){
					distance = Integer.MAX_VALUE;
					noObstacle = false;
				}else{
					distance++;
					noObstacle = !controller.getMazeConfiguration().hasWall(px+distance, py, searchDirection); 
				}
			}
			break;
		case West:
			while(noObstacle){
				if (px-distance == 0) {
					noObstacle = false;
				}
				else if(!controller.getMazeConfiguration().isValidPosition(px-distance, py)){
					distance = Integer.MAX_VALUE;
					noObstacle = false;
				}else{
					distance++;
					noObstacle = !controller.getMazeConfiguration().hasWall(px-distance, py, searchDirection);
				}
			}
			break;
		case North:
			while(noObstacle){
				if (py-distance == 0) {
					noObstacle = false;
				}
				else if(!controller.getMazeConfiguration().isValidPosition(px, py-distance)){
					distance = Integer.MAX_VALUE;
					noObstacle = false;
				}else{
					distance++;
					noObstacle = !controller.getMazeConfiguration().hasWall(px, py-distance, searchDirection);
				}
			}
			break;
		case South:
			while(noObstacle){
				if (py+distance == controller.getMazeConfiguration().getHeight()-1) {
					noObstacle = false;
				}
				else if(!controller.getMazeConfiguration().isValidPosition(px, py+distance)){
					distance = Integer.MAX_VALUE;
					noObstacle = false;
				}else{
					distance++;
					noObstacle = !controller.getMazeConfiguration().hasWall(px, py+distance, searchDirection);
				}
			}
			break;
		}
		return distance;
	}
	
	
	@Override
	public boolean hasDistanceSensor(Direction direction) {
		return true;
	}
	
//----------------------------------------new methods-----------------------------------------------
	
	/**
	 * This method calculates a searching direction by superimpose CardinalDirection 
	 * East, West, North, South and Direction of Left, Right, Forward, and Backward.
	 * 
	 * @param direction
	 * @return searchDirection
	 */
	//the south and north is upside down, need to change the orientation here.
	private CardinalDirection findSearchingDirection(Direction direction) {
		CardinalDirection searchDirection = controller.getCurrentDirection();
		switch(direction) {
			case LEFT:
				searchDirection = controller.getCurrentDirection().rotateClockwise();
				break;
			case RIGHT:
				searchDirection = controller.getCurrentDirection().oppositeDirection().rotateClockwise();
				break;
			case FORWARD:
				searchDirection = controller.getCurrentDirection();
				break;
			case BACKWARD:
				searchDirection = controller.getCurrentDirection().oppositeDirection();
				break;
		}
		return searchDirection;
	}

	
	/**
	 * This method simulates using distance sensor, 
	 * 1 battery level is consumed every time used.
	 * @param direction
	 * @return distanceToObstacle(direction)
	 */
	public int sensingDistance(Direction direction) {
		assert batteryLevel > 0: "Battery level insufficient.";
		batteryLevel = batteryLevel - ENERGY_CONSUMED_DETECTING;
		return distanceToObstacle(direction);
	}
	
	/**
	 * This method returns the controller.
	 * @return controller
	 */
	public MazeController getController() {
		assert controller != null: "Controller hasn't been set up yet.";
		return controller;
	}
	
	/**
	 * This method reset the battery level and restart the robot
	 * the whenever the game is restarted.
	 */
	public void reset() {
		stopped = false;
		batteryLevel = INITIAL_BATTERY_LEVEL;
	}
	
	/**
	 * This method returns the boolean hasWall to help keep track of the path length.
	 * @return hasWall
	 */
	public boolean hasWall() {
		return hasWall;
	}
	
	
}
