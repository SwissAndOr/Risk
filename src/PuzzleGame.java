import java.util.Scanner;

public class PuzzleGame {
	private static int[][] grid = new int[4][4];
	private static int emptyX = 3, emptyY = 3;

	private static enum Direction {
		UP(0, -1, 'W'), DOWN(0, 1, 'S'), LEFT(-1, 0, 'A'), RIGHT(1, 0, 'D');
		public final int x, y;
		public final char key;

		private Direction(final int x, final int y, final char key) {
			this.x = x;
			this.y = y;
			this.key = key;
		}
	};

	public static void main(final String[] args) {
		for (int y = 0; y <= 3; y++)
			for (int x = 0; x <= 3; x++)
				grid[x][y] = y * 4 + x + 1;
		grid[emptyX][emptyY] = 0;
		randomize(1000);
		printGrid();
		final Scanner scanner = new Scanner(System.in);
		while (!checkWin()) {
			final char input = scanner.next().toUpperCase().charAt(0);
			for (Direction direction : Direction.values())
				if (input == direction.key)
					slide(direction);
			printGrid();
		}
		scanner.close();
		System.out.println("You won!");
	}

	private static boolean slide(final Direction direction) {
		if (emptyX - direction.x < 0 || emptyX - direction.x > 3 || emptyY - direction.y < 0 || emptyY - direction.y > 3)
			return false;
		grid[emptyX][emptyY] = grid[emptyX - direction.x][emptyY - direction.y];
		emptyX = emptyX - direction.x;
		emptyY = emptyY - direction.y;
		grid[emptyX][emptyY] = 0;
		return true;
	}

	private static void randomize(final int moves) {
		for (int i = 0; i < moves; i++)
			while (!slide(Direction.values()[(int) (Math.random() * 4)]));
	}

	private static boolean checkWin() {
		for (int y = 0; y <= 3; y++)
			for (int x = 0; x <= 3; x++)
				if (y * 4 + x + 1 != 16 && grid[x][y] != y * 4 + x + 1)
					return false;
		return true;
	}

	private static void printGrid() {
		for (int y = 0; y <= 3; y++) {
			for (int x = 0; x <= 3; x++)
				System.out.print((grid[x][y] < 10 ? " " : "") + grid[x][y] + " ");
			System.out.println();
		}
	}
}