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

    // ⭐ ĐỀ GỐC – KHÔNG BAO GIỜ THAY ĐỔI
    private boolean[][] given = new boolean[9][9];

    private Solver gene = new Genetic();
    private Random rd = new Random();

    // ===== FONT =====
    private final Font FONT_FIXED  = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private final Font FONT_NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

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
                        puzzle.set(rr, cc, t.isEmpty() ? 0 : t.charAt(0) - '0');
                    }
                });

                // chuột phải → khóa ô (KHÔNG ÁP DỤNG CHO ĐỀ)
                tf.addMouseListener(new MouseAdapter() 
                {
                    @Override
                    public void mouseClicked(MouseEvent e) 
                    {
                        if (SwingUtilities.isRightMouseButton(e)) 
                        {

                            // ❌ không cho sửa đề gốc
                            if (given[rr][cc]) return;

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
        JButton solveBtn = new JButton("Complete");
        solveBtn.addActionListener(e -> {

            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    if (puzzle.get(r, c) != 0) {
                        puzzle.setFixed(r, c, true);
                        given[r][c] = true;
                    }

            // ⭐ 2. SOLVE
            Sudoku solved = gene.solve(puzzle.clone());
            if (solved == null) {
                JOptionPane.showMessageDialog(this, "Solver failed!");
                return;
            }

            // ⭐ 3. CHỈ ĐIỀN Ô TRỐNG
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    if (!puzzle.isFixed(r, c))
                        puzzle.set(r, c, solved.get(r, c));

            origin = puzzle.clone();
            refreshUIFromModel();
        });

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            puzzle = origin.clone();
            refreshUIFromModel();
        });

        JButton sampleBtn = new JButton("Load sample");
        sampleBtn.addActionListener(e -> {
            loadSamplePuzzle();
            refreshUIFromModel();
        });

        JButton emptyBtn = new JButton("All empty");
        emptyBtn.addActionListener(e -> {
            puzzle = new Sudoku();
            origin = puzzle.clone();

            // ⭐ xóa toàn bộ đề gốc
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    given[r][c] = false;

            refreshUIFromModel();
        });

        bottom.add(solveBtn);
        bottom.add(resetBtn);
        bottom.add(sampleBtn);
        bottom.add(emptyBtn);

        add(bottom, BorderLayout.SOUTH);

        // ===== INIT =====
        loadSamplePuzzle();
        refreshUIFromModel();

        setSize(640, 720);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ===== UI =====
    private void refreshUIFromModel()
    {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++) 
            {
                JTextField tf = cells[r][c];
                int v = puzzle.get(r, c);
                tf.setText(v == 0 ? "" : String.valueOf(v));
                updateCellAppearance(r, c);
            }
    }

    private void updateCellAppearance(int r, int c)
    {
        JTextField tf = cells[r][c];
        if (puzzle.isFixed(r, c)) 
        {
            tf.setBackground(new Color(220, 220, 220));
            tf.setEditable(false);
            tf.setFont(FONT_FIXED);
        } else {
            tf.setBackground(Color.WHITE);
            tf.setEditable(true);
            tf.setFont(FONT_NORMAL);
        }
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

        for (int i = pos.size() - 1; i > 0; i--) 
        {
            int j = rd.nextInt(i + 1);
            int[] t = pos.get(i);
            pos.set(i, pos.get(j));
            pos.set(j, t);
        }

        int clues = rd.nextInt(25, 40);
        for (int i = clues; i < 81; i++) 
        {
            int[] p = pos.get(i);
            data[p[0]][p[1]] = 0;
        }

        puzzle = new Sudoku(data);

        // ⭐ đánh dấu đề gốc
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++) 
            {
                if (puzzle.get(r, c) != 0) 
                {
                    puzzle.setFixed(r, c, true);
                    given[r][c] = true;
                } else {
                    given[r][c] = false;
                }
            }

        origin = puzzle.clone();
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(GUI::new);
    }
}
