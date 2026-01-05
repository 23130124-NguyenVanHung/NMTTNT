package sudoku;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
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

    // ===== FONT =====
    private final Font FONT_FIXED  = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private final Font FONT_NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

    // ====== GUI ======
    public GUI()
    {
        setTitle("Game - Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(9, 9));

        // ===== GRID =====
        for (int r = 0; r < 9; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(FONT_NORMAL);

                int rr = r, cc = c;

                // chỉ cho nhập 1-9
                tf.addKeyListener(new KeyAdapter() 
                {
                    @Override
                    public void keyTyped(KeyEvent e) 
                    {
                        char ch = e.getKeyChar();
                        if (!Character.isDigit(ch) || ch == '0' || tf.getText().length() >= 1)
                            e.consume();
                    }
                });

                // cập nhật model
                tf.addKeyListener(new KeyAdapter() 
                {
                    @Override
                    public void keyReleased(KeyEvent e) 
                    {
                        if (puzzle.isFixed(rr, cc)) return;

                        String t = tf.getText();
                        if (t.isEmpty()) puzzle.set(rr, cc, 0);
                        else puzzle.set(rr, cc, t.charAt(0) - '0');
                    }
                });

                // chuột phải → fixed
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

                JPanel wrap = new JPanel(new BorderLayout());
                wrap.add(tf);

                int top    = (r % 3 == 0) ? 3 : 1;
                int left   = (c % 3 == 0) ? 3 : 1;
                int bottom = (r == 8) ? 3 : 1;
                int right  = (c == 8) ? 3 : 1;

                wrap.setBorder(BorderFactory.createMatteBorder(
                        top, left, bottom, right, Color.BLACK));

                gridPanel.add(wrap);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel bottom = new JPanel();

        JButton autoBtn = new JButton("Complete");
        autoBtn.addActionListener(e -> {
            autoBtn.setEnabled(false);

            String err = validatePuzzle(puzzle);
            if (err != null) {
                JOptionPane.showMessageDialog(this, err);
                autoBtn.setEnabled(true);
                return;
            }

            Sudoku solved = gene.solve(puzzle.clone());
            if (solved != null) {
                // GIỮ FIXED – CHỈ ĐIỀN Ô TRỐNG
                for (int r = 0; r < 9; r++)
                    for (int c = 0; c < 9; c++)
                        if (!puzzle.isFixed(r, c))
                            puzzle.set(r, c, solved.get(r, c));

                refreshUIFromModel();
                JOptionPane.showMessageDialog(this, "Solved!");
            } else {
                JOptionPane.showMessageDialog(this, "Solver failed!");
            }
            autoBtn.setEnabled(true);
        });

        bottom.add(autoBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            puzzle = origin.clone();
            refreshUIFromModel();
        });
        bottom.add(resetBtn);

        JButton sampleBtn = new JButton("Load sample");
        sampleBtn.addActionListener(e -> {
            loadSamplePuzzle();
            refreshUIFromModel();
        });
        bottom.add(sampleBtn);

        JButton emptyBtn = new JButton("All empty");
        emptyBtn.addActionListener(e -> {
            puzzle = new Sudoku();
            origin = puzzle.clone();
            refreshUIFromModel();
        });
        bottom.add(emptyBtn);

        add(bottom, BorderLayout.SOUTH);

        // ===== INIT =====
        loadSamplePuzzle();
        refreshUIFromModel();

        setSize(640, 720);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ===== UPDATE UI =====
    private void refreshUIFromModel()
    {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++) {
                JTextField tf = cells[r][c];
                int v = puzzle.get(r, c);
                tf.setText(v == 0 ? "" : String.valueOf(v));
                updateCellAppearance(r, c);
            }
    }

    private void updateCellAppearance(int r, int c)
    {
        JTextField tf = cells[r][c];
        if (puzzle.isFixed(r, c)) {
            tf.setBackground(new Color(220, 220, 220));
            tf.setEditable(false);
            tf.setFont(FONT_FIXED);   // ⭐ ĐỀ ĐẬM
        } else {
            tf.setBackground(Color.WHITE);
            tf.setEditable(true);
            tf.setFont(FONT_NORMAL); // ⭐ solver / nhập tay
        }
    }

    // ===== VALIDATE =====
    private String validatePuzzle(Sudoku s)
    {
        for (int i = 0; i < 9; i++) {
            boolean[] row = new boolean[10];
            boolean[] col = new boolean[10];
            for (int j = 0; j < 9; j++) {
                int r = s.get(i, j);
                int c = s.get(j, i);
                if (r != 0 && row[r]) return "Row " + (i+1) + " duplicate";
                if (c != 0 && col[c]) return "Column " + (i+1) + " duplicate";
                if (r != 0) row[r] = true;
                if (c != 0) col[c] = true;
            }
        }
        return null;
    }

    // ===== LOAD SAMPLE =====
    private void loadSamplePuzzle()
    {
        Sudoku full = new Sudoku();
        full.fillRandomRows(rd);
        Sudoku solved = gene.solve(full);

        int[][] data = solved.getGridCopy();

        ArrayList<int[]> pos = new ArrayList<>();
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                pos.add(new int[]{r, c});

        for (int i = pos.size()-1; i > 0; i--) {
            int j = rd.nextInt(i+1);
            int[] t = pos.get(i);
            pos.set(i, pos.get(j));
            pos.set(j, t);
        }

        int clues = rd.nextInt(25, 40);
        for (int i = clues; i < 81; i++) {
            int[] p = pos.get(i);
            data[p[0]][p[1]] = 0;
        }

        puzzle = new Sudoku(data);

        // ⭐ SET FIXED
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (puzzle.get(r, c) != 0)
                    puzzle.setFixed(r, c, true);

        origin = puzzle.clone();
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(GUI::new);
    }
}
