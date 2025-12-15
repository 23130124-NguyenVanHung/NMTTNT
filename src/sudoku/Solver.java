package sudoku;

//Solver.java
public interface Solver {
 /**
  * Solve the given sudoku puzzle. The solver must respect fixed cells from the puzzle.
  * Returns a solved Sudoku object or null if not found.
  */
 Sudoku solve(Sudoku puzzle);
}