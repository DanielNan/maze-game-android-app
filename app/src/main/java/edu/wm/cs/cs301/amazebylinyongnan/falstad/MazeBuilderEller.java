package edu.wm.cs.cs301.amazebylinyongnan.falstad;

/**
 * This class uses Eller's algorithm to generate pathways for the maze.
 *
 * The Eller's method builds the maze one row at a time, using sets to keep
 * track of which columns are ultimately connected.
 * When it finishes, it will produce a perfect maze.
 * This implementation also supports mazes that are not perfect.
 *
 * @author: Linyong Nan
 */


public class MazeBuilderEller extends MazeBuilder implements Runnable{

	private int cellID = 1;

	public MazeBuilderEller() {
		super();
		System.out.println("MazeBuilderEller uses Eller's algorithm to generate maze.");
	}

	public MazeBuilderEller(boolean det) {
		super(det);
		System.out.println("MazeBuilderEller uses Eller's algorithm to generate maze.");
	}

	/**
	 * This method uses Eller's algorithm to generate a maze by deleting walls
	 * to connect cells. A concept of set is introduced, which is a group of connect cell space.
	 * The multidimensional array maze contains unit cell coordinate and and cell id
	 * that can be denoted as maze[row][column][ID].
	 * Initially all cells have 0 as their id value.
	 * This method builds the maze one row at a time.
	 */
	@Override
	protected void generatePathways() {
		int[][][] maze = new int[height][width][1];
		for (int i = 0; i < height; i++) {
			addRow(maze, i);
			connectRow(maze, i);
			if (i != height - 1)
				connectColumn(maze, i);
		}
		connectLastRow(maze);
	}

	/**
	 * Add a new row to the maze, and assign new ID to the unit cells that haven't been setup or connected.
	 * after assign an ID to a cell, the ID increments by 1.
	 * @param maze which contains unit cell coordinate and ID
	 * @param row current row
	 */
	private void addRow(int[][][] maze, int row) {
		for (int col = 0; col < width; col++) {
			if (maze[row][col][0] == 0){
				maze[row][col][0] = cellID;
				cellID++;
			}
		}
	}

	/**
	 * This method randomly connect cells within a row.
	 * Check if the cell on the right of the current cell has a different ID or hasn't
	 * been set up yet, if positive, then randomly decide to delete the wall between
	 * two cells and merge the right cell into the set by changing its ID to the
	 * current cell ID.
	 * @param maze
	 * @param row
	 */
	private void connectRow(int[][][] maze, int row) {
		for (int col = 0; col < width-1; col++) {
			// take down wall and merge the cells
			int connect = random.nextInt();
			if (connect % 2 == 0) {
				Wall curWall = new Wall(col, row, CardinalDirection.East);
				//check if two cells about to be connected have different id, otherwise a loop will be introduced.
				if (cells.canGo(curWall) && maze[row][col][0] != maze[row][col+1][0]) {
					cells.deleteWall(curWall);
					//merge by changing cellID
					maze[row][col+1][0] = maze[row][col][0];
				}
			}
		}
	}

	/**
	 * This method randomly connect cells in current row to their bottom cells to expand the set.
	 * Within each set (cell space), at least one connection should be made to the bottom row.
	 * Each loop in the main while loop stands for going through each set. Until a current position
	 * pointer reaches the end of the row, we use a counter to count the number of cells within each
	 * set for the use of generating random target column to be connected if none of the cells in a
	 * set is connected downward. We use a flag to monitor if at least one connection is made within
	 * each set. Then we go through the last column and to decide whether to connect the cell.
	 * @param maze
	 * @param row
	 */
	private void connectColumn(int[][][] maze, int row) {

		int currentCol = 0;

		while (currentCol != width) {
			//one loop stands for a set of connected cells
			int counter = 1;
			int targetColumn = 0;
			boolean deleteAtLeastOne = false;
			//one loop here stands for a cell within a set of connected cells
			while (currentCol != width-1 && cells.hasNoWall(currentCol, row, CardinalDirection.East)){
				int randomConnect = random.nextInt();
				if (randomConnect % 2 == 0) {
					Wall curWall = new Wall(currentCol, row, CardinalDirection.South);
					cells.deleteWall(curWall);
					maze[row+1][currentCol][0] = maze[row][currentCol][0];
					deleteAtLeastOne = true;
				}
				currentCol++;
				counter++;
			}
			//decide whether to take down the South wall of the last column cell.
			if (currentCol == width - 1) {
				int randomLastCol = random.nextInt();
				if (randomLastCol % 2 == 0) {
					Wall lastColWall = new Wall(currentCol, row, CardinalDirection.South);
					cells.deleteWall(lastColWall);
					maze[row+1][currentCol][0] = maze[row][currentCol][0];
					deleteAtLeastOne = true;
				}
			}

			//make sure at least one column is connected to the next row in each set.
			//target column is decided by generating a random number within an interval
			//of the size of a set. Then the random number is subtracted from the end
			//position of the set.
			if (!deleteAtLeastOne){
				int randomWithinSet = random.nextIntWithinInterval(0, counter-1);
				targetColumn = currentCol - randomWithinSet;

				Wall randomWall = new Wall(targetColumn, row, CardinalDirection.South);
				cells.deleteWall(randomWall);
				maze[row+1][targetColumn][0] = maze[row][targetColumn][0];
			}
			currentCol++;
		}
	}


	/**
	 * This method adds the last row to the maze.
	 * Check if the right cell has the same ID with the current cell, if not,
	 * connect two cells by changing the ID of the right cell to the current
	 * cell ID.
	 * @param maze
	 */
	private void connectLastRow(int[][][] maze) {

		for (int col = 0; col < width; col++) {
			Wall lastRowWall = new Wall(col, height-1, CardinalDirection.East);
			//if two cells about to connect has same ID, a loop might be generated.
			if (cells.canGo(lastRowWall) && maze[height-1][col][0] != maze[height-1][col+1][0]) {
				cells.deleteWall(lastRowWall);
				maze[height-1][col+1][0] = maze[height-1][col][0];
			}
		}
	}
}
