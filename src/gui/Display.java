package gui;

import util.FileParser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Display {

    private JPanel mainPanel;
    private JPanel menuPanel;
    private JMenuItem exit;
    private JMenuItem read;
    private JPanel functionPanel;
    private JPanel control;
    private JCheckBox rotation;
    private JCheckBox rotaRefl;
    private JCheckBox extraBlocks;
    private JMenuBar mainMenu;
    private JMenu File;
    private JMenu Help;
    private JCheckBox noSymmetry;
    private JButton getSolution;
    private JButton displaySolution;
    private JButton displayStep;
    private JSlider changeSpeed;
    private JButton pause;
    private JButton stop;
    private JPanel board;
    private JPanel result;
    private JPanel tiles;
    private FileDialog openDia;



    public Display() {
    //todo: display pieces
        read.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser("./testcases");
                fc.showOpenDialog(null);
                File f = fc.getSelectedFile();
                String filePath = f.getPath();
                FileParser fp = new FileParser(filePath);
                fp.test(filePath); // todo：返回int[][]，连接数组和board
                Demo d = new Demo();
//                d.launch();
            }
        });
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Display");
        frame.setContentPane(new Display().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(930, 570);
        frame.setResizable(false); // irresizable
        frame.setLocationRelativeTo(null); // center to screen
        frame.setVisible(true);
    }

    private class Demo extends JFrame {

        public Demo() {
            setSize(1000, 600);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);
            setLocationRelativeTo(null); // center to screen
            setResizable(false);
        }

        public Dimension getPreferredSize() {
            return new Dimension(600, 200);
        }

        public void launch() {
            repaint();
        }

//        public static ArrayList<Tile> getCells() {
//            //todo: 读取文件，并返回ArrayList，List中的元素为每个Tile，Tile的结构以二维数组存储，同时存储颜色
//            ArrayList<Tile> Tiles = new ArrayList<>();
//            Tile tile1 = new Tile();
//            Tile tile2 = new Tile();
//            int[][] cell1 = {{1, 2, 1, 2}, {0, 0, 0, 1}, {0, 0, 0, 1}};
//            int[][] cell2 = {{1, 2, 1}, {0, 1, 0}};
//            tile1.cells = cell1;
//            tile2.cells = cell2;
//            tile1.color = new Color(187, 255, 255);
//            tile2.color = new Color(187, 255, 0);
//            Tiles.add(tile1);
//            Tiles.add(tile2);
//            return Tiles;
//        }

        public void paint(Graphics g) {

            int[][] sol = {{-1, 1, -1}, {1, 1, 2}, {-1, 3, -1}};
            char[][] board = {{'N', 'X', 'N'}, {'X', 'O', 'X'}, {'N', 'O', 'N'}};

            // Assign colors to all tiles
            HashMap<Integer, Color> tileColor = new HashMap<>();
            Random rand = new Random();
            for (int[] i : sol) {
                for (int j : i) {
                    tileColor.put(j, new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
                }
            }

            // Draw board with colors
            int x = 100;
            int y = 100;
            int width = 50;
            int height = 50;
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
            Font drawFont = new Font("Arial", Font.BOLD, 30);
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j] != 'N') {
                        g.setFont(drawFont);
                        g.setColor(Color.black);
                        g.drawString(String.valueOf(board[i][j]), x + 15, y + 35);
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



}
