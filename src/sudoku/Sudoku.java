package sudoku;

import java.util.*;

public class Sudoku implements Cloneable
{
	public static final int SIZE = 9;
	private int[][] grid;      // 0 means empty
	private boolean[][] fixed; // true if original clue

	public Sudoku()
	{
		grid = new int[SIZE][SIZE];
		fixed = new boolean[SIZE][SIZE];
	}

	public Sudoku(int[][] start)
	{
		this();

		for (int r = 0; r < SIZE; r++)
		{
			for (int c = 0; c < SIZE; c++)
			{
				grid[r][c] = start[r][c];
				fixed[r][c] = (start[r][c] != 0);
			}
		}
	}

	public int get(int r, int c) { return grid[r][c]; }
	public void set(int r, int c, int v) { grid[r][c] = v; }
	public boolean isFixed(int r, int c) { return fixed[r][c]; }
	public void setFixed(int r, int c, boolean f) {	fixed[r][c] = f; }

	public int[][] getGridCopy()
	{
		int[][] copy = new int[SIZE][SIZE];

		for (int r = 0; r < SIZE; r++)
		{
			System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
		}

		return copy;
	}

//	tạo bảng mới
	@Override
	public Sudoku clone()
	{
		Sudoku s = new Sudoku();

		for (int r = 0; r < SIZE; r++)
		{
			for (int c = 0; c < SIZE; c++)
			{
				s.grid[r][c] = this.grid[r][c];
				s.fixed[r][c] = this.fixed[r][c];
			}
		}
		return s;
	}

	// đếm số xung đột
	public int conflicts()
	{
		int conflicts = 0;

		// rows
		for (int r = 0; r < SIZE; r++)
		{
			int[] cnt = new int[SIZE + 1];

			for (int c = 0; c < SIZE; c++)
			{
				int v = grid[r][c];

				if (v >= 1 && v <= 9)  cnt[v]++;
			}

			for (int v = 1; v <= 9; v++)
			{
				if (cnt[v] > 1)
				{
					conflicts += cnt[v] - 1;
				}
			}
		}

		// columns
		for (int c = 0; c < SIZE; c++)
		{
			int[] cnt = new int[SIZE + 1];

			for (int r = 0; r < SIZE; r++)
			{
				int v = grid[r][c];

				if (v >= 1 && v <= 9) cnt[v]++;
			}

			for (int v = 1; v <= 9; v++)
			{
				if (cnt[v] > 1)
				{
					conflicts += cnt[v] - 1;
				}
			}
		}

		// 3x3 blocks
		for (int br = 0; br < 3; br++)
		{
			for (int bc = 0; bc < 3; bc++)
			{
				int[] cnt = new int[SIZE + 1];

				for (int r = br * 3; r < br * 3 + 3; r++)
				{
					for (int c = bc * 3; c < bc * 3 + 3; c++)
					{
						int v = grid[r][c];

						if (v >= 1 && v <= 9) cnt[v]++;
					}
				}

				for (int v = 1; v <= 9; v++)
				{
					if (cnt[v] > 1)
					{
						conflicts += cnt[v] - 1;
					}
				}
			}
		}
		return conflicts;
	}

	// điền số ngẫu nhiên vào các hàng
	public void fillRandomRows(Random rnd)
	{
		for (int r = 0; r < SIZE; r++)
		{
			boolean[] present = new boolean[SIZE + 1];

			// đánh dấu số đã có
			for (int c = 0; c < SIZE; c++)
			{
				int v = grid[r][c];

				if (v >= 1 && v <= 9)
				{
					present[v] = true;
				}
			}

			// gom các số còn thiếu
			List<Integer> missing = new ArrayList<>();

			for (int v = 1; v <= 9; v++)
			{
				if (!present[v])
				{
					missing.add(v);
				}
			}

			// trộn ngẫu nhiên
			for (int i = missing.size() - 1; i > 0; i--)
			{
				int j = rnd.nextInt(i + 1);

				int temp = missing.get(i);
				missing.set(i, missing.get(j));
				missing.set(j, temp);
			}

			// điền vào ô trống
			int idx = 0;

			for (int c = 0; c < SIZE; c++)
			{
				if (grid[r][c] == 0)
				{
					grid[r][c] = missing.get(idx++);
				}
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		for (int r = 0; r < SIZE; r++)
		{
			for (int c = 0; c < SIZE; c++)
			{
				sb.append(grid[r][c]);

				if (c < 8)
				{
					sb.append(' ');
				}
			}

			sb.append('\n');
		}

		return sb.toString();
	}
}