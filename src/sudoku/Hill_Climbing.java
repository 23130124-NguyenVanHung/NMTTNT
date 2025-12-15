package sudoku;

import java.util.*;

/**
 * Hill Climbing for Sudoku:
 * Start with random fill per row, then try swaps that reduce conflicts.
 */
public class Hill_Climbing implements Solver
{
    private Random rd = new Random();
    private int maxIterations = 200000;

    public Hill_Climbing()
    {
    }

    @Override
    public Sudoku solve(Sudoku puzzle /*, long timeLimitMillis*/)
    {
//        long endTime = System.currentTimeMillis() + timeLimitMillis;

        Sudoku current = puzzle.clone();
        current.fillRandomRows(rd);

        int currConf = current.conflicts();

        if (currConf == 0)
        {
            return current;
        }

        for (int it = 0; it < maxIterations; it++)
        {
//            if (System.currentTimeMillis() > endTime)
//            {
//                break;
//            }

            int r = rd.nextInt(Sudoku.SIZE);

            List<Integer> idx = new ArrayList<>();

            for (int c = 0; c < Sudoku.SIZE; c++)
            {
                if (!current.isFixed(r, c))
                {
                    idx.add(c);
                }
            }

            if (idx.size() < 2)
            {
                continue;
            }

            int a = idx.get(rd.nextInt(idx.size()));
            int b = idx.get(rd.nextInt(idx.size()));

            if (a == b)
            {
                continue;
            }

            // attempt swap
            swap(current, r, a, b);

            int newConf = current.conflicts();

            if (newConf <= currConf)
            {
                currConf = newConf;

                if (currConf == 0)
                {
                    return current;
                }
            }
            else
            {
                // revert swap
                swap(current, r, a, b);
            }
        }

        return (currConf == 0) ? current : null;
    }

    private void swap(Sudoku s, int r, int c1, int c2)
    {
        int temp = s.get(r, c1);

        s.set(r, c1, s.get(r, c2));
        s.set(r, c2, temp);
    }
}