package gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Random;

public class Board extends JPanel {

    int[][] solutions;
    char[][] board;

    public Board(){}

    public Board(int[][] solutions, char[][] board) {
        this.solutions = solutions;
        this.board = board;
    }

    public void paintComponent(Graphics g) {

        int[][] sol = this.solutions;
        char[][] board = this.board;

        // Assign colors to all tiles
        HashMap<Integer, Color> tileColor = new HashMap<>();
        Random rand = new Random();
        for(int[] i : sol) {
            for(int j : i) {
                tileColor.put(j, new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
            }
        }

        // Draw board with colors
        int x = 100;
        int y = 100;
        int width = 30;
        int height = 30;
        final int initial_x = x;
        final int initial_y = y;
        for (int i = 0; i < sol.length; i++) {
            for (int j = 0; j < sol[0].length; j++) {
                if (sol[i][j] != -1) {
                    g.setColor(Color.black);
                    g.drawRect(x, y, width, height);
                    g.setColor(tileColor.get(sol[i][j]));
                    g.fillRect(x + 1, y + 1, width - 1, height - 1);
                    x += width;
                } else {
                    x += width;
                }
            }
            x = initial_x;
            y += height;
        }

        x = initial_x;
        y = initial_y;
        Font drawFont = new Font("Arial", Font.BOLD,20);
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(board[i][j] != '\u0000'){
                    g.setFont(drawFont);
                    g.setColor(Color.black);
                    g.drawString(String.valueOf(board[i][j]), x+8, y+25);
                    x += width;
                } else {
                    x += width;
                }
            }
            x = initial_x;
            y += height;
        }
    }
}
