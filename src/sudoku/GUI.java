package sudoku;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.*;

public class GUI extends JFrame
{
    private JTextField[][] cells = new JTextField[9][9];
    private Sudoku origin;
    private Sudoku puzzle;
    private Solver gene = new Genetic();
    private Solver hill = new Hill_Climbing();
    private Solver sia = new Simulated_Annealing();
    private Random rd = new Random();

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
                        if (!Character.isDigit(ch) || ch == '0') e.consume();
                        if (tf.getText().length() >= 1) e.consume();
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
                        if (t.isEmpty()) puzzle.set(rr, cc, 0);
                        else
                        {
                            char ch = t.charAt(0);
                            if (ch >= '1' && ch <= '9' && !puzzle.isFixed(rr, cc))
                                puzzle.set(rr, cc, ch - '0');
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
                            puzzle.setFixed(rr, cc, f);
                            updateCellAppearance(rr, cc);
                        }
                    }
                });

                cells[r][c] = tf;

                JPanel cellWrapper = new JPanel(new BorderLayout());
                cellWrapper.add(tf, BorderLayout.CENTER);

                int top = (r % 3 == 0) ? 3 : 1;
                int left = (c % 3 == 0) ? 3 : 1;
                int bottom = (r == 8) ? 3 : 1;
                int right = (c == 8) ? 3 : 1;

                cellWrapper.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));
                gridPanel.add(cellWrapper);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();

        JButton autoBtn = new JButton("Complete");
        autoBtn.addActionListener(e -> {
            Solver sol = gene; // có thể thay đổi hill hoặc sia
            autoBtn.setEnabled(false);
            Sudoku solution = sol.solve(puzzle.clone());
            if (solution != null)
            {
                puzzle = solution;
                refreshUIFromModel();
                JOptionPane.showMessageDialog(this, "Solved using: " + sol.getClass().getSimpleName());
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Solver failed: " + sol.getClass().getSimpleName());
            }
            autoBtn.setEnabled(true);
        });
        bottom.add(autoBtn);

        JButton clearBtn = new JButton("Reset");
        clearBtn.addActionListener(e -> {
            puzzle = origin.clone();
            refreshUIFromModel();
        });
        bottom.add(clearBtn);

        JButton sampleBtn = new JButton("Load sample puzzle");
        sampleBtn.addActionListener(e -> {
            loadSamplePuzzle();
            origin = puzzle.clone();
            refreshUIFromModel();
        });
        bottom.add(sampleBtn);

        JButton empty = new JButton("All is empty");
        empty.addActionListener(e -> {
            puzzle = new Sudoku();
            origin = puzzle.clone();
            refreshUIFromModel();
        });
        bottom.add(empty);

        add(bottom, BorderLayout.SOUTH);

        loadSamplePuzzle();
        origin = puzzle.clone();
        refreshUIFromModel();

        setSize(640, 720);
        setLocationRelativeTo(null);
        setVisible(true);
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

    // --- Hàm mới: sinh puzzle luôn có lời giải ---
   private void loadSamplePuzzle()
{
    // 1. tạo solution hoàn chỉnh bằng Genetic
    Sudoku full = new Sudoku();
    full.fillRandomRows(rd);
    Sudoku solved = gene.solve(full);
    if (solved == null) solved = full; // fallback

    // 2. tạo puzzle từ solution: xóa ngẫu nhiên
    int[][] puzzleData = solved.getGridCopy();
    ArrayList<int[]> positions = new ArrayList<>();
    for (int r = 0; r < 9; r++)
        for (int c = 0; c < 9; c++)
            positions.add(new int[]{r, c});
    Collections.shuffle(positions, rd);

    int clues = rd.nextInt(25, 40); // số ô giữ lại
    for (int i = clues; i < 81; i++)
    {
        int r = positions.get(i)[0];
        int c = positions.get(i)[1];
        puzzleData[r][c] = 0;
    }

    puzzle = new Sudoku(puzzleData);
    origin = puzzle.clone(); // origin = puzzle lúc mới tạo
}


    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new GUI());
    }
}
