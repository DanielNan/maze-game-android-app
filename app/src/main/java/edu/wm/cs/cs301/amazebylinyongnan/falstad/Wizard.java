package edu.wm.cs.cs301.amazebylinyongnan.falstad;

import edu.wm.cs.cs301.amazebylinyongnan.falstad.Constants.StateGUI;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Robot.Direction;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Robot.Turn;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.CardinalDirection;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Distance;

/**
 * Wizard is a driver class that implements the robotDriver interface.
 * The wizard is a cheater who uses the MazeController.getNeighborCloserToExit()
 * method to find the exit. The wizard is intended to work as a baseline
 * algorithm to see how the most efficient algorithm can perform in terms
 * of energy consumption and path length.
 * 
 * @author Linyong Nan
 *
 */
public class Wizard implements RobotDriver, Runnable{

	//fields
	private static final int INITIAL_BATTERY_LEVEL = 2500;
	private BasicRobot robot;
	private Distance distance;
	private int width;
	private int height;
	private int pathLength;
	private boolean stopped;
    private boolean pause = false;
    private Thread driveThread;
	
	//constructor
	public Wizard() {
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

    public void run() {
        try{
            int[] neighbor = new int[2];
            setDimensions(robot.getController().getMazeConfiguration().getWidth(),robot.getController().getMazeConfiguration().getHeight());
            setDistance(robot.getController().getMazeConfiguration().getMazedists());
            //calculate anticipated pathLength
            int len = distance.getDistance(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1]);
            pathLength = 0;
            while(pathLength < len - 1 && !stopped && !pause) {

                neighbor = robot.getController().getMazeConfiguration().getNeighborCloserToExit(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1]);
                assert (neighbor[0] == robot.getCurrentPosition()[0] || neighbor[1] == robot.getCurrentPosition()[1]) : "Unexpected neighbor position.";

                drive2Neighbor(neighbor[0],neighbor[1]);
                pathLength++;
                stopped = robot.hasStopped();

                Thread.sleep(20);

                if(pause){
                    Thread.interrupted();
                }

            }
            if (robot.getBatteryLevel() != 0){
                //assert robot.getController().getMazeConfiguration().getMazecells().isExitPosition
                //  (robot.getCurrentPosition()[0], robot.getCurrentPosition()[1]): "Robot is not at exit yet.";
            }

            if (!stopped && (robot.getController().getState() == StateGUI.STATE_PLAY)){
                robot.getController().setPathLength(this.getPathLength()+1);
                robot.getController().setEnergyConsumption(this.getEnergyConsumption());
                escapeFromMaze();
                pathLength++;
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
	 * This methods help Wizard to determine the closest path to the exit.
	 * The position of neighbor closer to the exit is inputed and then 
	 * the driver will simply operate the robot towards that position.
	 * 
	 * @param nx
	 * @param ny
	 * @throws Exception
	 */
	private void drive2Neighbor(int nx, int ny) throws Exception {
		//note that since the map is upside down, so all left-right turn is switched here
		switch(robot.getCurrentDirection()) {
		case East:
			if (nx > robot.getCurrentPosition()[0]){
				robot.move(1, false);
			}
			else if (nx == robot.getCurrentPosition()[0]) {
				if (ny > robot.getCurrentPosition()[1]) {
					robot.rotate(Turn.LEFT);
					robot.move(1, false);
				}else{
					robot.rotate(Turn.RIGHT);
					robot.move(1, false);
				}
			}else{
				robot.rotate(Turn.AROUND);
				robot.move(1, false);
			}
			break;
		case West:
			if (nx < robot.getCurrentPosition()[0]){
				robot.move(1, false);
			}
			else if (nx == robot.getCurrentPosition()[0]) {
				if (ny > robot.getCurrentPosition()[1]) {
					robot.rotate(Turn.RIGHT);
					robot.move(1, false);
				}else{
					robot.rotate(Turn.LEFT);
					robot.move(1, false);
				}
			}else{
				robot.rotate(Turn.AROUND);
				robot.move(1, false);
			}
			break;
		case North:
			if (nx > robot.getCurrentPosition()[0]){
				robot.rotate(Turn.LEFT);
				robot.move(1, false);
			}
			else if (nx == robot.getCurrentPosition()[0]) {
				if (ny > robot.getCurrentPosition()[1]) {
					robot.rotate(Turn.AROUND);
					robot.move(1, false);
				}else{
					robot.move(1, false);
				}
			}else{
				robot.rotate(Turn.RIGHT);
				robot.move(1, false);
			}
			break;
		case South:
			if (nx < robot.getCurrentPosition()[0]){
				robot.rotate(Turn.LEFT);
				robot.move(1, false);
			}
			else if (nx == robot.getCurrentPosition()[0]) {
				if (ny > robot.getCurrentPosition()[1]) {
					robot.move(1, false);
				}else{
					robot.rotate(Turn.AROUND);
					robot.move(1, false);
				}
			}else{
				robot.rotate(Turn.RIGHT);
				robot.move(1, false);
			}
			break;
		}
	}
	
	

	@Override
	public float getEnergyConsumption() {
		assert INITIAL_BATTERY_LEVEL - robot.getBatteryLevel() >= 0 : "Unexpected energy consumption.";
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
	//------------------------- android app -------------------------------------
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
        try{
            robot.getController().updatePathLength
                    (robot.getController().getMazeConfiguration().getMazedists()
                            .getDistance(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1]));
        }catch(Exception e){}
    }


}
