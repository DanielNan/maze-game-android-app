package edu.wm.cs.cs301.amazebylinyongnan.falstad;

import edu.wm.cs.cs301.amazebylinyongnan.falstad.Robot.Direction;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Robot.Turn;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.CardinalDirection;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Distance;
/**
 * WallFollower is a driver that implements the robotDriver interface.
 * The robot needs a distance sensor at the front and at the left to perform
 * It follows the wall on its left hand side. 
 * 
 * @author Linyong Nan
 *
 */
public class WallFollower implements RobotDriver, Runnable{
	//fields
	private static final int INITIAL_BATTERY_LEVEL = 2500;
	private BasicRobot robot;
	private int width;
	private int height;
	private Distance distance;
	private int pathLength;
	private boolean stopped;

    private Thread driveThread;
    private boolean pause = false;
	
	//constructor
	public WallFollower() {
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

        driveThread = new Thread(this);
        driveThread.start();
		
		return true;
	}

    public void run(){
        try{
            if (robot.sensingDistance(Direction.LEFT) != 0) {
                findWallToFollow();
            }

            assert (robot.distanceToObstacle(Direction.LEFT) == 0) : "The robot is not following the wall.";
            while(!robot.isAtExit() && !stopped && !pause) {
                //check if the robot is following the wall should not consume any energy.
                //so we use distanceToObstacle method instead of sensingDistance method, which cost 1 level of battery.

                //use left distance sensor, cost 1 energy.
                if(robot.sensingDistance(Direction.LEFT) != 0) {
                    robot.rotate(Turn.LEFT);
                    robot.move(1, false);
                    pathLength++;
                    robot.getController().updatePathLength(pathLength);
                }else{
                    //use front distance sensor, cost 1 energy.
                    if(robot.sensingDistance(Direction.FORWARD) == 0) {
                        robot.rotate(Turn.RIGHT);
                    }else{
                        robot.move(1, false);
                        pathLength++;
                        robot.getController().updatePathLength(pathLength);
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
			pathLength++;
            robot.getController().updatePathLength(pathLength);
            //distanceToObstacle method does not cost energy.
			assert (robot.distanceToObstacle(Direction.FORWARD) == 0) : "There is no wall in the front of the robot.";
			robot.rotate(Turn.RIGHT);
		}
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
    public void callDriverSpecificMethod(){}
	
}
