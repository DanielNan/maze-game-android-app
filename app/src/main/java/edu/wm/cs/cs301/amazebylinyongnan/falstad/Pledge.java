package edu.wm.cs.cs301.amazebylinyongnan.falstad;

import java.util.Random;

import edu.wm.cs.cs301.amazebylinyongnan.falstad.Robot.Direction;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Robot.Turn;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.CardinalDirection;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Distance;


/**
 * Pledge class is a driver class that implements the robotDriver interface.
 * This is a refined wall follower that is able to run around and leave an 
 * obstacle. It picks a random directions as its main direction and applies wall
 * following when it hists an obstacle. It counts left (-1) and right (+1)
 * turns and when the total becomes zero, it is able to leave an
 * obstacle following its main direction again.
 * 
 * @author Linyong Nan
 *
 */
public class Pledge implements RobotDriver, Runnable{
	//fields
	private static final int INITIAL_BATTERY_LEVEL = 2500;
	private BasicRobot robot;
	private int width;
	private int height;
	private Distance distance;
	private int pathLength;
	private boolean stopped;
	private CardinalDirection mainDirection;

    private Thread driveThread;
    private boolean pause = false;
	
	//constructor
	public Pledge () {
		robot = new BasicRobot();
		pathLength = 0;
		stopped = robot.hasStopped();
	}
	
	@Override
	public void setRobot(Robot r) {
		assert r != null : "Robot should not be null.";
		robot = (BasicRobot) r;		
	}

	@Override
	public void setDimensions(int width, int height) {
		if (width >= 0 && height >= 0) {
			this.width = width;
			this.height = height;
		}		
	}

	@Override
	public void setDistance(Distance distance) {
		if (null != distance)
			this.distance = distance;
		assert distance.getMaxDistance() <= (width * height): "Max distance should be less than or equal to the number of cells in the maze.";		
	}

	@Override
	public boolean drive2Exit() throws Exception {
		//main direction is selected in driverSpecificMethod()
        //selectMainDirection();

        driveThread = new Thread(this);
        driveThread.start();

		
		return true;
	}

    public void run(){
        try{
            //turn left means turn right, turn right means turn left
            //but direction is correct
            boolean obstacle = false;
            int steerLevel = setInitialSteerLevel();
            while(!robot.isAtExit() && !stopped && !pause) {
                if (steerLevel != 0)
                    obstacle = true;
                //when the robot has 0 steer level and hasn't encounter any obstacle, it goes straightly until hit a wall
                //when it hits a wall, we rotate the robot to right and add the steer level thus goes into obstacle mode.
                if(!obstacle) {
                    int distance = robot.sensingDistance(Direction.FORWARD);
                    robot.move(distance, false);
                    pathLength = pathLength + distance;
                    robot.getController().updatePathLength(pathLength);
                    assert (robot.distanceToObstacle(Direction.FORWARD) == 0): "The robot has not encountered a wall yet.";
                    robot.rotate(Turn.RIGHT);
                    steerLevel++;
                    obstacle = true;
                }else{
                    //under this situation the driver acts as a wallFollower until the steerLevel is back to 0.
                    if(robot.distanceToObstacle(Direction.LEFT) != 0){
                        findWallToFollow();
                    }
                    assert(robot.distanceToObstacle(Direction.LEFT) == 0): "The robot is not following a wall.";
                    while(obstacle && !stopped && !robot.isAtExit() && !pause) {
                        //we have 90 turn or 180 turn
                        if(robot.distanceToObstacle(Direction.LEFT) != 0) {
                            robot.rotate(Turn.LEFT);
                            steerLevel--;
                            robot.move(1,false);
                            pathLength++;
                            robot.getController().updatePathLength(pathLength);
                        }else{
                            if(robot.distanceToObstacle(Direction.FORWARD) == 0) {
                                robot.rotate(Turn.RIGHT);
                                steerLevel++;
                            }else{
                                robot.move(1, false);
                                pathLength++;
                                robot.getController().updatePathLength(pathLength);
                            }

                            if(steerLevel == 0){
                                obstacle = false;
                            }
                        }
                        //whenever we change the steerLevel, we check if the level is back to 0.
                        if(steerLevel == 0){
                            obstacle = false;
                        }
                        stopped = robot.hasStopped();

                        Thread.sleep(20);

                        if(pause){
                            Thread.interrupted();
                        }

                    }
                }
                stopped = robot.hasStopped();

                Thread.sleep(20);

                if(pause){
                    Thread.interrupted();
                }


            }

            if (robot.getBatteryLevel() != 0){
                assert robot.getController().getMazeConfiguration().getMazecells().isExitPosition
                        (robot.getCurrentPosition()[0], robot.getCurrentPosition()[1]): "Robot is not at exit yet.";
            }

            if (!stopped){
                robot.getController().setPathLength(this.getPathLength()+1);
                robot.getController().setEnergyConsumption(this.getEnergyConsumption());
                escapeFromMaze();
                pathLength++;
                robot.getController().updatePathLength(pathLength);
            }
        }catch(Exception e){}
    }
	
	/**
	 * This method helps the driver to escape from the maze at the exit position.
	 * getMazecells.hasNoWall is used to determine the direction and isValidPosition
	 * is used to determine whether the driver gets out of the maze successfully.
	 * 
	 * @throws Exception
	 */
	private void escapeFromMaze() throws Exception {
		assert robot.distanceToObstacle(Direction.BACKWARD) != Integer.MAX_VALUE : "Unexpected exit environment.";
		
		switch(robot.getCurrentDirection()){
			case East:
				if(robot.getController().getMazeConfiguration().getMazecells().hasNoWall(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1], CardinalDirection.East)
						&& !robot.getController().getMazeConfiguration().isValidPosition(robot.getCurrentPosition()[0]+1, robot.getCurrentPosition()[1])){
					robot.move(1, false);
				}
				else if(robot.getController().getMazeConfiguration().getMazecells().hasNoWall(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1], CardinalDirection.North)){
					robot.rotate(Turn.RIGHT);
					robot.move(1, false);
				}else{
					robot.rotate(Turn.LEFT);
					robot.move(1, false);
				}
				break;
			case West:
				if(robot.getController().getMazeConfiguration().getMazecells().hasNoWall(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1], CardinalDirection.West)
						&& !robot.getController().getMazeConfiguration().isValidPosition(robot.getCurrentPosition()[0]-1, robot.getCurrentPosition()[1])){
					robot.move(1, false);
				}
				else if (robot.getController().getMazeConfiguration().getMazecells().hasNoWall(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1], CardinalDirection.North)){
					robot.rotate(Turn.LEFT);
					robot.move(1, false);
				}else{
					robot.rotate(Turn.RIGHT);
					robot.move(1, false);
				}
				break;
			case North:
				if(robot.getController().getMazeConfiguration().getMazecells().hasNoWall(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1], CardinalDirection.North)
						&& !robot.getController().getMazeConfiguration().isValidPosition(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1]-1)){
					robot.move(1, false);
				}
				else if (robot.getController().getMazeConfiguration().getMazecells().hasNoWall(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1], CardinalDirection.East)){
					robot.rotate(Turn.LEFT);
					robot.move(1, false);
				}else{
					robot.rotate(Turn.RIGHT);
					robot.move(1, false);
				}
				break;
			case South:
				if(robot.getController().getMazeConfiguration().getMazecells().hasNoWall(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1], CardinalDirection.South)
						&& !robot.getController().getMazeConfiguration().isValidPosition(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1]+1)){
					robot.move(1, false);
				}
				else if (robot.getController().getMazeConfiguration().getMazecells().hasNoWall(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1], CardinalDirection.West)){
					robot.rotate(Turn.LEFT);
					robot.move(1, false);
				}else{
					robot.rotate(Turn.RIGHT);
					robot.move(1, false);
				}
				break;
		}
	}
	
	/**
	 * This method helps the driver to find a wall to follow initially.
	 * Since it is highly unlikely that robot will be at the exit position while finding a wall to follow
	 * We will not consider such scenario.
	 */
	private void findWallToFollow() {
		assert (robot.distanceToObstacle(Direction.LEFT) != 0): "The robot is following the wall.";
		int distance = robot.sensingDistance(Direction.FORWARD);
		if (distance == 0) {
			robot.rotate(Turn.RIGHT);
		}else{
			robot.move(distance,false);
			//distanceToObstacle method does not cost energy.
			assert (robot.distanceToObstacle(Direction.FORWARD) == 0) : "There is no wall in the front of the robot.";
			robot.rotate(Turn.RIGHT);
		}
	}
	
	/**
	 * This method helps to determine a random main direction that is in the form CardinalDirection.
	 * @return CardinalDirection
	 */
	private CardinalDirection selectMainDirection() {
		//randomly select a direction here.
		Random random = new Random();
		int randomDirection = random.nextInt(4);
		switch(randomDirection) {
			case 0: 
				mainDirection = CardinalDirection.East;
				break;
			case 1:
				mainDirection = CardinalDirection.West;
				break;
			case 2:
				mainDirection = CardinalDirection.North;
				break;
			case 3:
				mainDirection = CardinalDirection.South;
				break;
		}
		return mainDirection;
	}
	
	/**
	 * This method helps to determine the initial steer level given the main direction from 
	 * SelectMainDirection, then the algorithm determines which direction the driver should rotate to.
	 * @return
	 */
	private int setInitialSteerLevel() {
		//compare current direction with main direction, then calculate the steer level.
		//note that initial direction for robot is CardinalDirection.East
		int initialSteerLevel = 0;
		switch(mainDirection) {
			case East:
				initialSteerLevel = 0;
				break;
			case West:
				initialSteerLevel = -2;
				break;
			case North:
				initialSteerLevel = 1;
				break;
			case South:
				initialSteerLevel = -1;
				break;
		}
		return initialSteerLevel;
	}

	@Override
	public float getEnergyConsumption() {
		assert INITIAL_BATTERY_LEVEL - robot.getBatteryLevel() > 0 : "Unexpected energy consumption.";
		return INITIAL_BATTERY_LEVEL - robot.getBatteryLevel();
	}

	@Override
	public int getPathLength() {
		return pathLength;
	}
	
	/**
	 * This methods returns the robot.
	 * 
	 * @return
	 */
	public BasicRobot getRobot() {
		return robot;
	}
	
	@Override
	public void reset() {
		pathLength = 0;
		stopped = false;
		robot.reset();
	}
	
	/**
	 * This method returns the dimension of the maze.
	 * @return dimension
	 */
	public int[] getDimension(){
		int[] dimension = new int[2];
		dimension[0] = width;
		dimension[1] = height;
		return dimension;
	}
	
	/**
	 * This method determines whether the distance field is null or not.
	 * @return boolean
	 */
	public boolean hasDistance(){
		if (distance != null){
			return true;
		}else{
			return false;
		}
	}

    @Override
    public void pauseDrive(){
        pause = true;
    }

    @Override
    public void resumeDrive(){
        pause = false;

        try{
            drive2Exit();
        }catch(Exception e){}
    }

    @Override
    public void callDriverSpecificMethod(){
        selectMainDirection();
    }
	
}
