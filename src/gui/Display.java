package gui;

import util.FileParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;


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
    private JButton displaySolution;
    private JPanel board;
    private JPanel result;
    private FileDialog openDia;


    ArrayList<int[][]> solutions = new ArrayList<>();
    ArrayList<char[][]> solBd = new ArrayList<>();
    int[][] tempSol1 = {{-1,1,-1},{1,1,2},{-1,3,-1}};
    char[][] tempBd1 = {{'N','X','N'}, {'X','O','X'}, {'N','O','N'}};


    int[][] tempSol2 = {{-1,1,-1,3},{-1,1,1,2},{2 ,-1,3,-1}};
    char[][] tempBd2 = {{'N','X','N', 'O'}, {'N','X','O','X'}, {'N','O','N','X'}};
    int clickCnt = -1;

    public Display() {


        // 主窗口
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Display");
                frame.add(mainPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setSize(930, 570);
                frame.setResizable(false); // irresizable
                frame.setLocationRelativeTo(null); // center to screen
                frame.setVisible(true);
            }
        });

        // 从文件中读取tiles
        read.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser("./testcases");
                fc.showOpenDialog(null);
                File f = fc.getSelectedFile();
                String filePath = f.getPath();
                FileParser fp = new FileParser(filePath);
                fp.test(filePath);// todo：FileParser添加两个方法，分别返回solution和board的解；
                                  //  ArrayList<int[][]> solutions, ArrayList<char[][]> board
                //                 solutions = fp.getSol();
                //                 solBd = fp.getSolBd();
                clickCnt = 0;
                displaySolution.setEnabled(true);

                solutions.add(tempSol1);
                solutions.add(tempSol2);
                solBd.add(tempBd1);
                solBd.add(tempBd2);
                result.removeAll();
                JLabel numSol = new JLabel("Find " + solutions.size() + " solutions.");
                result.add(numSol);

            }
        });

        // 展示board
        displaySolution.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clickCnt < solutions.size() && clickCnt >= 0) {
                    JPanel bd = new Board(solutions.get(clickCnt), solBd.get(clickCnt));
                    board.add(bd);
                    board.revalidate();
                } else if (clickCnt == -1) {
                    new Prompt().readFile();
                    clickCnt--;
                } else if (clickCnt >= solutions.size()){
                    displaySolution.setEnabled(false);
                    new Prompt().noSolution();
                }
            }
        });

        // 展示解法
        displaySolution.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int c = e.getButton();// 得到按下的鼠标键
                if (c == MouseEvent.BUTTON1) {
                    clickCnt++;
                    System.out.println(clickCnt);
                }
            }
        });
    }


    public static void main(String[] args) {
        new Display();
    }
}
