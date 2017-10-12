package edu.wm.cs.cs301.amazebylinyongnan.falstad;


import edu.wm.cs.cs301.amazebylinyongnan.falstad.Distance;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Order;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Constants.StateGUI;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.Robot.Turn;

/**
 * This class implements the interface RobotDriver class.
 * This class provides a manual driver functionality and interactss
 * with the Robot but not much else.
 * This class also receives keyboard input from SimpleKeyListener and calls
 * move or rotate methods of BasicRobot and through which connects to the 
 * controller and thus the graphics.
 * This class is used by MazeApplication.java.
 * 
 * 
 * @author Linyong Nan
 *
 */


public class ManualDriver implements RobotDriver, Runnable {
	//fields
	private static final int INITIAL_BATTERY_LEVEL = 2500;
	private BasicRobot robot;
	private Distance distance;
	private int width;
	private int height;
	private boolean manual;
	private int pathLength;
	private boolean stopped;
	
	boolean notChecked = true;
    private Thread driveThread;
    private int key;
	
	//constructor
	public ManualDriver() {
		robot = new BasicRobot();
		manual = true;
		pathLength = 0;
		stopped = robot.hasStopped();
	}
	
	
	/**
	 * method incorporates reactions to keyboard input during STATE_PLAY.
	 * The simple key listener calls this method to communicate input.
	 * @param key
	 * @return true
	 */
	public boolean keyDown(int key){

        this.key = key;
        driveThread = new Thread(this);
        driveThread.start();

		return true;
	}

    public void run(){
        try{
            //if robot is at exit, then set the total path length and total energy consumption.
            if (robot.isAtExit() && (key == 'k' || key == '8')) {
                robot.getController().setPathLength(this.getPathLength());
                robot.getController().setEnergyConsumption(this.getEnergyConsumption());
            }

            switch(key) {
                case 'k': case '8':
                    robot.move(1, manual);
                    //if robot run into wall or border, don't add up pathLength.
                    if (!stopped && !robot.hasWall())
                        pathLength++;
                        robot.getController().updatePathLength(pathLength);
                    break;
                case 'h': case '4':
                    robot.rotate(Turn.LEFT);
                    break;
                case 'l': case '6':
                    robot.rotate(Turn.RIGHT);
                    break;
                case 'j': case '2':
                    //support back moon walk by rotate around, move forward and rotate around again
                    //one can see the effect of this by pressing down key when playing the game.
                    robot.rotate(Turn.AROUND);
                    robot.move(1, manual);
                    robot.rotate(Turn.AROUND);
                    if (!stopped && !robot.hasWall())
                        pathLength++;
                        robot.getController().updatePathLength(pathLength);
                    break;
            }
            Thread.sleep(30);

        }catch(Exception e){}
    }




	/**
	 * This method returns the robot object.
	 * @return robot
	 */
	public BasicRobot getRobot() {
		return robot;
	}
	
	
//-----------------------------Override Methods------------------------------------------
	
	@Override
	public void setRobot(Robot r) {
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
		assert distance.getMaxDistance() < (width * height): "Max distance should be less than the number of cells in the maze.";
	}

	@Override
	public boolean drive2Exit() throws Exception {
		return false;
	}

	@Override
	public float getEnergyConsumption() {
		float remainBatteryLevel = robot.getBatteryLevel();
		return INITIAL_BATTERY_LEVEL - remainBatteryLevel;
	}

	@Override
	public int getPathLength() {
		return pathLength;
	}
	
	
	@Override
	public void reset() {
		pathLength = 0;
		stopped = false;
		robot.reset();
	}

    @Override
    public void pauseDrive(){}

    @Override
    public void resumeDrive(){}

    @Override
    public void callDriverSpecificMethod(){}

}
