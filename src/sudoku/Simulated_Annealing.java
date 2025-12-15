package sudoku;

import java.util.*;

/**
 * Simulated Annealing for Sudoku.
 * Similar to hill climbing but sometimes accepts worse solutions.
 */
public class Simulated_Annealing implements Solver
{
    private Random rd = new Random();
    private int maxIterations = 200000;
    private double startTemp = 5.0;
    private double endTemp = 0.001;

    public Simulated_Annealing() { }

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

            double t = temperature(it);

            // pick random row to modify
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

            // apply swap
            swap(current, r, a, b);

            int newConf = current.conflicts();
            int delta = newConf - currConf;

            if (delta <= 0)
            {
                currConf = newConf;

                if (currConf == 0)
                {
                    return current;
                }
            }
            else
            {
                double prob = Math.exp(-delta / t);

                if (rd.nextDouble() < prob)
                {
                    currConf = newConf;
                }
                else
                {
                    // revert
                    swap(current, r, a, b);
                }
            }
        }

        return (currConf == 0) ? current : null;
    }

    private double temperature(int iter)
    {
        double fraction = (double) iter / (double) maxIterations;
        return startTemp * Math.pow(endTemp / startTemp, fraction);
    }

    private void swap(Sudoku s, int r, int c1, int c2)
    {
        int temp = s.get(r, c1);

        s.set(r, c1, s.get(r, c2));
        s.set(r, c2, temp);
    }
}