import java.util.Arrays;

// Name:Akanksha Priya
// USC NetID:apriya@usc.edu
// CS 455 PA3
// Fall 2018

/**
 * VisibleField class This is the data that's being displayed at any one point
 * in the game (i.e., visible field, because it's what the user can see about
 * the minefield), Client can call getStatus(row, col) for any square. It
 * actually has data about the whole current state of the game, including the
 * underlying mine field (getMineField()). Other accessors related to game
 * status: numMinesLeft(), isGameOver(). It also has mutators related to moves
 * the player could do (resetGameDisplay(), cycleGuess(), uncover()), and
 * changes the game state accordingly.
 * 
 * It, along with the MineField (accessible in mineField instance variable),
 * forms the Model for the game application, whereas GameBoardPanel is the View
 * and Controller, in the MVC design pattern. It contains the MineField that
 * it's partially displaying. That MineField can be accessed (or modified) from
 * outside this class via the getMineField accessor.
 */
public class VisibleField {
	// ----------------------------------------------------------
	// The following public constants (plus numbers mentioned in comments below) are
	// the possible states of one
	// location (a "square") in the visible field (all are values that can be
	// returned by public method
	// getStatus(row, col)).

	// Covered states (all negative values):
	public static final int COVERED = -1; // initial value of all squares
	public static final int MINE_GUESS = -2;
	public static final int QUESTION = -3;

	// Uncovered states (all non-negative values):

	// values in the range [0,8] corresponds to number of mines adjacent to this
	// square

	public static final int MINE = 9; // this loc is a mine that hasn't been guessed already (end of losing game)
	public static final int INCORRECT_GUESS = 10; // is displayed a specific way at the end of losing game
	public static final int EXPLODED_MINE = 11; // the one you uncovered by mistake (that caused you to lose)
	// ----------------------------------------------------------

	private MineField mineField;
	private int numRows;
	private int numCols;
	private int[][] status = new int[numRows][numCols];
	private int totalMines;

	/**
	 * Create a visible field that has the given underlying mineField. The initial
	 * state will have all the mines covered up, no mines guessed, and the game not
	 * over.
	 * 
	 * @param mineField the minefield to use for for this VisibleField
	 */
	public VisibleField(MineField mineField) {
		this.mineField = mineField;
		this.numRows = mineField.numRows();
		this.numCols = mineField.numCols();
		this.totalMines = this.mineField.numMines();
		this.status = new int[this.numRows][this.numCols];
		for (int i = 0; i < status.length; i++) {
			for (int j = 0; j < status[0].length; j++) {
				status[i][j] = VisibleField.COVERED;
			}
		}

	}

	/**
	 * Reset the object to its initial state (see constructor comments), using the
	 * same underlying MineField.
	 */
	public void resetGameDisplay() {
		for (int i = 0; i < status.length; i++) {
			for (int j = 0; j < status[0].length; j++) {
				status[i][j] = VisibleField.COVERED;
			}
		}
	}

	/**
	 * Returns a reference to the mineField that this VisibleField "covers"
	 * 
	 * @return the minefield
	 */
	public MineField getMineField() {
		return this.mineField;
	}

	/**
	 * get the visible status of the square indicated.
	 * 
	 * @param row row of the square
	 * @param col col of the square
	 * @return the status of the square at location (row, col). See the public
	 *         constants at the beginning of the class for the possible values that
	 *         may be returned, and their meanings. PRE: getMineField().inRange(row,
	 *         col)
	 */
	public int getStatus(int row, int col) {

		return this.status[row][col];
	}

	/**
	 * Return the the number of mines left to guess. This has nothing to do with
	 * whether the mines guessed are correct or not. Just gives the user an
	 * indication of how many more mines the user might want to guess. So the value
	 * can be negative, if they have guessed more than the number of mines in the
	 * minefield.
	 * 
	 * @return the number of mines left to guess.
	 */
	public int numMinesLeft() {
		int discoverdMines = 0;
		for (int i = 0; i < status.length; i++) {
			for (int j = 0; j < status[0].length; j++) {
				if (status[i][j] == VisibleField.MINE_GUESS) {
					discoverdMines++;
				}
			}
		}

		return (this.totalMines - discoverdMines);

	}

	/**
	 * Cycles through covered states for a square, updating number of guesses as
	 * necessary. Call on a COVERED square changes its status to MINE_GUESS; call on
	 * a MINE_GUESS square changes it to QUESTION; call on a QUESTION square changes
	 * it to COVERED again; call on an uncovered square has no effect.
	 * 
	 * @param row row of the square
	 * @param col col of the square PRE: getMineField().inRange(row, col)
	 */
	public void cycleGuess(int row, int col) {
		if (this.status[row][col] == VisibleField.COVERED) {
			this.status[row][col] = VisibleField.MINE_GUESS;
		} else if (this.status[row][col] == VisibleField.MINE_GUESS) {
			this.status[row][col] = VisibleField.QUESTION;
		} else if (this.status[row][col] == VisibleField.QUESTION) {
			this.status[row][col] = VisibleField.COVERED;
		}

	}

	/**
	 * Uncovers this square and returns false iff you uncover a mine here. If the
	 * square wasn't a mine or adjacent to a mine it also uncovers all the squares
	 * in the neighboring area that are also not next to any mines, possibly
	 * uncovering a large region. Any mine-adjacent squares you reach will also be
	 * uncovered, and form (possibly along with parts of the edge of the whole
	 * field) the boundary of this region. Does not uncover, or keep searching
	 * through, squares that have the status MINE_GUESS.
	 * 
	 * @param row of the square
	 * @param col of the square
	 * @return false iff you uncover a mine at (row, col) PRE:
	 *         getMineField().inRange(row, col)
	 */
	public boolean uncover(int row, int col) {
		if (getMineField().hasMine(row, col)) {
			updateStatusOfSquares(row, col);
			return false;
		} else {
			uncoverSquare(row, col, this.numRows, this.numCols);
		}
		return true;
	}

	private void updateStatusOfSquares(int row, int col) {
		// display all unguessed mines
		for (int i = 0; i < status.length; i++) {
			for (int j = 0; j < status[0].length; j++) {
				if (getMineField().hasMine(i, j)) {
					if (row == i && col == j) {
						status[i][j] = VisibleField.EXPLODED_MINE;
					} else if (status[i][j] != VisibleField.MINE_GUESS) {
						status[i][j] = VisibleField.MINE;
					} else if (status[i][j] == VisibleField.MINE_GUESS) {
						// do nothing
					}
				} else {
					if (status[i][j] == VisibleField.MINE_GUESS) {
						status[i][j] = VisibleField.INCORRECT_GUESS;
					}
				}
			}
		}
	}

	/**
	 * Returns whether the game is over.
	 * 
	 * @return whether game over
	 */
	public boolean isGameOver() {

		int uncoveredSquares = 0;
		for (int i = 0; i < status.length; i++) {
			for (int j = 0; j < status[0].length; j++) {
				if (status[i][j] == VisibleField.EXPLODED_MINE) {
					return true;
				} else if (status[i][j] == VisibleField.COVERED || status[i][j] == VisibleField.MINE_GUESS
						|| status[i][j] == VisibleField.QUESTION) {
					uncoveredSquares++;
				}
			}
		}
		// uncoveredSquares equals total number of mines then game is over
		if (uncoveredSquares == mineField.numMines()) { 
			return true;
		}
		return false;
	}

	/**
	 * Return whether this square has been uncovered. (i.e., is in any one of the
	 * uncovered states, vs. any one of the covered states).
	 * 
	 * @param row of the square
	 * @param col of the square
	 * @return whether the square is uncovered PRE: getMineField().inRange(row, col)
	 */
	public boolean isUncovered(int row, int col) {
		if (this.status[row][col] != VisibleField.COVERED && this.status[row][col] != VisibleField.MINE_GUESS
				&& this.status[row][col] != VisibleField.QUESTION) {
			return true;
		}
		return false;
	}

	private void uncoverSquare(int row, int col, int totalRows, int totalCols) {
		if (!mineField.inRange(row, col)) {
			return;
		}
		if (status[row][col] == VisibleField.COVERED || status[row][col] == VisibleField.QUESTION) {
			if (this.mineField.hasMine(row, col)) {
				status[row][col] = VisibleField.EXPLODED_MINE;
			}
			int noOfAdjMines = this.mineField.numAdjacentMines(row, col);

			if (noOfAdjMines == 0) {
				status[row][col] = noOfAdjMines;
			} else if (noOfAdjMines >= 1 && noOfAdjMines <= this.totalMines) {
				status[row][col] = noOfAdjMines;
				return;
			}

			uncoverSquare(row - 1, col - 1, totalRows, totalCols);
			uncoverSquare(row - 1, col, totalRows, totalCols);
			uncoverSquare(row - 1, col + 1, totalRows, totalCols);
			uncoverSquare(row, col - 1, totalRows, totalCols);
			uncoverSquare(row, col + 1, totalRows, totalCols);
			uncoverSquare(row + 1, col - 1, totalRows, totalCols);
			uncoverSquare(row + 1, col, totalRows, totalCols);
			uncoverSquare(row + 1, col + 1, totalRows, totalCols);
		}

	}

}
