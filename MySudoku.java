package student_productivity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class MySudoku extends JFrame {
    public static byte[][] sudoku = new byte[729][82];
    public static byte step = 0;

    private   int WindowWidth = 777;
    private   int WindowHeight = 636;

    public MySudoku() {
        Smethods.start(sudoku);
        final byte border = 14;
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("sudoku.png"));
        } catch (IOException e) {
            // Handle the IOException
        }
        setResizable(false);
        setIconImage(image);
        setSize(WindowWidth, WindowHeight);
        setLocation(0, 0);
        setLayout(new BorderLayout());

        add(new SPanel(new Dimension(WindowWidth, border)), BorderLayout.NORTH);
        add(new SPanel(new Dimension(WindowWidth, border)), BorderLayout.SOUTH);
        add(new SPanel(new Dimension(border, WindowHeight)), BorderLayout.EAST);
        add(new SPanel(new Dimension(0, WindowHeight)), BorderLayout.WEST);

        DisplayPanel dp = new DisplayPanel();
        dp.setBackground(Color.WHITE);
        add(dp, BorderLayout.CENTER);

        setVisible(true);
    }

    // You can add other methods or modify existing ones as needed
}
