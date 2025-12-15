package sudoku;

import java.util.*;

/**
 * Simple Genetic Algorithm for Sudoku.
 * Representation: each individual is a Sudoku that respects fixed cells.
 * Fitness: number of conflicts (lower better). Goal: reach 0.
 */
public class Genetic implements Solver
{
    private int populationSize = 200;
    private int maxGenerations = 2000;
    private double mutationRate = 0.06;
    private Random rd = new Random();

    public Genetic() { }

    public Genetic(int popSize, int maxGen, double mutRate)
    {
        this.populationSize = popSize;
        this.maxGenerations = maxGen;
        this.mutationRate = mutRate;
    }

    @Override
    public Sudoku solve(Sudoku puzzle /*, long timeLimitMillis*/)
    {
//        long endTime = System.currentTimeMillis() + timeLimitMillis;

        // initialize population
        List<Sudoku> pop = new ArrayList<>();

        for (int i = 0; i < populationSize; i++)
        {
            Sudoku s = puzzle.clone();
            s.fillRandomRows(rd);
            pop.add(s);
        }

        Sudoku best = null;
        int bestFit = Integer.MAX_VALUE;

        for (int gen = 0; gen < maxGenerations; gen++)
        {
//            if (System.currentTimeMillis() > endTime)
//            {
//                break;
//            }

            // evaluate
            Collections.sort(pop, Comparator.comparingInt(Sudoku::conflicts));

            if (best == null || pop.get(0).conflicts() < bestFit)
            {
                best = pop.get(0).clone();
                bestFit = best.conflicts();

                if (bestFit == 0)
                {
                    return best; // perfect solution found
                }
            }

            // selection: keep elite
            int elite = populationSize / 8;
            List<Sudoku> newPop = new ArrayList<>();

            for (int i = 0; i < elite; i++)
            {
                newPop.add(pop.get(i).clone());
            }

            // create remaining individuals
            while (newPop.size() < populationSize)
            {
                Sudoku p1 = tournament(pop, 5);
                Sudoku p2 = tournament(pop, 5);

                Sudoku child = crossover(p1, p2);
                mutate(child);

                newPop.add(child);
            }

            pop = newPop;
        }

        return best;
    }

    private Sudoku tournament(List<Sudoku> pop, int k)
    {
        Sudoku best = null;

        for (int i = 0; i < k; i++)
        {
            Sudoku cand = pop.get(rd.nextInt(pop.size()));

            if (best == null || cand.conflicts() < best.conflicts())
            {
                best = cand;
            }
        }

        return best;
    }

    // Crossover: each row chooses from parent B or keeps parent A
    private Sudoku crossover(Sudoku a, Sudoku b)
    {
        Sudoku child = a.clone();

        for (int r = 0; r < Sudoku.SIZE; r++)
        {
            if (rd.nextBoolean())
            {
                for (int c = 0; c < Sudoku.SIZE; c++)
                {
                    if (!child.isFixed(r, c))
                    {
                        child.set(r, c, b.get(r, c));
                    }
                }
            }
        }

        return child;
    }

    // Mutation: swap two non-fixed cells in a row
    private void mutate(Sudoku s)
    {
        for (int r = 0; r < Sudoku.SIZE; r++)
        {
            if (rd.nextDouble() < mutationRate)
            {
                List<Integer> idx = new ArrayList<>();

                for (int c = 0; c < Sudoku.SIZE; c++)
                {
                    if (!s.isFixed(r, c))
                    {
                        idx.add(c);
                    }
                }

                if (idx.size() >= 2)
                {
                    int i = idx.get(rd.nextInt(idx.size()));
                    int j = idx.get(rd.nextInt(idx.size()));

                    int temp = s.get(r, i);

                    s.set(r, i, s.get(r, j));
                    s.set(r, j, temp);
                }
            }
        }
    }
}