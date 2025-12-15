package sudoku;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.*;

public class GUI extends JFrame
{
    private JTextField[][] cells = new JTextField[9][9];
    private Sudoku origin;
    private Sudoku puzzle;
    private Solver gene = new Genetic();
    private Solver hill = new Hill_Climbing();
    private Solver sia = new Simulated_Annealing();
    Random rd = new Random();

    public GUI()
    {
        setTitle("Game - Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(9, 9));
        Font cellFont = new Font(Font.SANS_SERIF, Font.BOLD, 18);

        for (int r = 0; r < 9; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(cellFont);

                tf.addKeyListener(new KeyAdapter()
                {
                    @Override
                    public void keyTyped(KeyEvent e)
                    {
                        char ch = e.getKeyChar();

                        if (!Character.isDigit(ch) || ch == '0')
                        {
                            e.consume();
                        }

                        if (tf.getText().length() >= 1)
                        {
                            e.consume();
                        }
                    }
                });

                int rr = r;
                int cc = c;

                tf.addKeyListener(new KeyAdapter()
                {
                    @Override
                    public void keyReleased(KeyEvent e)
                    {
                        String t = tf.getText();

                        if (t.length() == 0)
                        {
                            puzzle.set(rr, cc, 0);
                        }
                        else
                        {
                            char ch = t.charAt(0);

                            if (ch >= '1' && ch <= '9')
                            {
                                int v = ch - '0';

                                if (!puzzle.isFixed(rr, cc))
                                {
                                    puzzle.set(rr, cc, v);
                                }
                            }
                            else
                            {
                                tf.setText("");
                                puzzle.set(rr, cc, 0);
                            }
                        }
                    }
                });

                tf.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        if (SwingUtilities.isRightMouseButton(e))
                        {
                            boolean f = !puzzle.isFixed(rr, cc);

                            String txt = tf.getText();
                            int val = 0;

                            if (txt.length() > 0 && Character.isDigit(txt.charAt(0)))
                            {
                                val = txt.charAt(0) - '0';
                            }

                            puzzle.setFixed(rr, cc, f);
                            updateCellAppearance(rr, cc);
                        }
                    }
                });

                cells[r][c] = tf;

                JPanel cellWrapper = new JPanel(new BorderLayout());
                cellWrapper.add(tf, BorderLayout.CENTER);

                int top    = (r % 3 == 0) ? 3 : 1;
                int left   = (c % 3 == 0) ? 3 : 1;
                int bottom = (r == 8)     ? 3 : 1;
                int right  = (c == 8)     ? 3 : 1;

                cellWrapper.setBorder(
                        BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK)
                );

                gridPanel.add(cellWrapper);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();

        // Lấy origin sau khi load sample
        loadSamplePuzzle();
        origin = puzzle.clone();

// --- Complete button ---
        JButton autoBtn = new JButton("Complete");
        autoBtn.addActionListener(e -> {
//            Solver[] solvers = { gene, hill, sia };
//            Solver sol = solvers[(int)(Math.random() * solvers.length)];
        	
        	Solver sol = gene;

            autoBtn.setEnabled(false);

            Sudoku solution = sol.solve(puzzle.clone());

            if (solution != null) 
            {
                puzzle = solution;  // cập nhật puzzle
                refreshUIFromModel();
                JOptionPane.showMessageDialog(
                        this,
                        "Solved using: " + sol.getClass().getSimpleName()
                );
            } 
            else 
            {
                JOptionPane.showMessageDialog(
                        this,
                        "Solver failed: " + sol.getClass().getSimpleName()
                );
            }

            autoBtn.setEnabled(true);
        });
        bottom.add(autoBtn);

// --- Reset button ---
        JButton clearBtn = new JButton("Reset");
        clearBtn.addActionListener(e -> {
            puzzle = origin.clone();  // reset về bản gốc
            refreshUIFromModel();
        });
        bottom.add(clearBtn);

// --- Load sample puzzle ---
        JButton sampleBtn = new JButton("Load sample puzzle");
        sampleBtn.addActionListener(e -> {
            loadSamplePuzzle();
            origin = puzzle.clone();  // tạo bảng mới
            refreshUIFromModel();
        });
        bottom.add(sampleBtn);

// --- All is empty to create new puzzle
        JButton empty = new JButton("All is empty");
        empty.addActionListener(e -> {
            loadSamplePuzzle();
            puzzle = new Sudoku();
            refreshUIFromModel();
        });
        bottom.add(empty);

        add(bottom, BorderLayout.SOUTH);

        loadSamplePuzzle();
        refreshUIFromModel();

        setSize(640, 720);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void applySolutionToUI(Sudoku sol)
    {
        for (int r = 0; r < 9; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                int v = sol.get(r, c);
                cells[r][c].setText(v == 0 ? "" : String.valueOf(v));
                cells[r][c].setEditable(!puzzle.isFixed(r, c));
            }
        }
    }

    private void refreshUIFromModel()
    {
        for (int r = 0; r < 9; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                int v = puzzle.get(r, c);
                JTextField tf = cells[r][c];

                tf.setText(v == 0 ? "" : String.valueOf(v));
                updateCellAppearance(r, c);
            }
        }
    }

    private void updateCellAppearance(int r, int c)
    {
        JTextField tf = cells[r][c];

        if (puzzle.isFixed(r, c))
        {
            tf.setBackground(new Color(220, 220, 220));
            tf.setEditable(false);
        }
        else
        {
            tf.setBackground(Color.WHITE);
            tf.setEditable(true);
        }
    }

    private void loadSamplePuzzle() {
        int[][] puzzleData = new int[9][9];

        int filledCells = rd.nextInt(25, 40);

        for (int i = 0; i < filledCells; i++) 
        {
            int r, c, val;

            while (true) 
            {
                // chọn vị trí trống
                do {
                    r = rd.nextInt(9);
                    c = rd.nextInt(9);
//		tạo map chua cac vi tri da check/ them so vao
 
/*
Map<Integer, Integer> contain = new HashMap<Integer, Integer>();
contain.put(r, c);
while(contain(r).containsValue(c))
{
break;
}

*/             	
                } while (puzzleData[r][c] != 0 /*&& */);

                val = rd.nextInt(1, 10);

                // kiểm tra số đó có hợp lệ không
                if (checkIsSafe(puzzleData, r, c, val)) {
                    puzzleData[r][c] = val;
                    break;  // THÊM THÀNH CÔNG → thoát vòng lặp
                }
                // nếu không hợp lệ → thử lại vị trí/số khác
            }
        }

        puzzle = new Sudoku(puzzleData);
        origin = puzzle.clone();
    }

    
//  kiểm tra số hợp lệ không để thêm vào
    private boolean checkIsSafe(int[][] sudoku, int row, int col, int num)
    {
        // Kiểm tra hàng
        for (int c = 0; c < 9; c++) {
            if (sudoku[row][c] == num) {
                return false;
            }
        }

        // Kiểm tra cột
        for (int r = 0; r < 9; r++) {
            if (sudoku[r][col] == num) {
                return false;
            }
        }

        // Kiểm tra vùng 3×3
        int startRow = row - row % 3;
        int startCol = col - col % 3;

        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (sudoku[r][c] == num) {
                    return false;
                }
            }
        }

        return true; // hợp lệ
    }



    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new GUI());
    }
}