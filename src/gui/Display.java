package gui;

import solver.DLX;
import util.FileParser;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static util.FileParser.*;


public class Display {

    private JPanel mainPanel;
    private JPanel menuPanel;
    private JMenuItem exit;
    private JMenuItem read;
    private JPanel functionPanel;
    private JPanel control;
    private JCheckBox rotation;
    private JCheckBox reflection;
    private JMenuBar mainMenu;
    private JMenu File;
    private JMenu Help;
    private JButton displaySolution;
    private JPanel board;
    private JPanel result;
    private JButton getAllSol;
    private JPanel showSolSum;
    private FileDialog openDia;


    List<int[][]> solutions = new ArrayList<>();
    char[][] solBd;
    String path;
    DLX dlx;
    boolean setRotate = false;
    boolean setReflect = false;
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
                frame.setSize(930, 680);
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
                path = f.getPath();
//                FileParser fp = new FileParser(path);
//                fp.test(path);
                clickCnt = 0;
                displaySolution.setEnabled(true);
                getAllSol.setEnabled(true);
                restart();
//                JLabel numSol = new JLabel("Find " + solutions.size() + " solutions.");
//                result.add(numSol);

            }
        });

        // 获取所有
        getAllSol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clickCnt == -1) {
                    new Prompt().readFile();
                    clickCnt--;
                }
                dlx = loadFile(path, setRotate, setReflect);
                solBd = dlx.getBoardDisplay();
                solutions = dlx.getSolutions();
                // 在result模块展示solution个数
                if(solutions.size() == 0) {
                    result.removeAll();
                    JLabel numSol1 = new JLabel("Find no solution.");
                    result.add(numSol1, BorderLayout.PAGE_START);
                    result.revalidate();
                } else {
                    result.removeAll();
                    JLabel numSol2 = new JLabel("Find " + solutions.size() + " solutions.");
                    result.add(numSol2, BorderLayout.PAGE_START);
                    result.revalidate();
                }
                getAllSol.setEnabled(false);
            }
        });

        // 展示board
        displaySolution.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 在board展示结果
                if(clickCnt < solutions.size() && clickCnt >= 0) {
                    JPanel bd = new Board(solutions.get(clickCnt), solBd);
                    board.add(bd);
                    JLabel cntSol = new JLabel("Solution No. " + (clickCnt+1));
//                    cntSol.setLocation(50,300);
                    result.removeAll();
                    result.add(cntSol, BorderLayout.PAGE_END);
                    result.revalidate();
                    board.revalidate();
                } else if (clickCnt == -1) {
                    new Prompt().readFile();
                    clickCnt--;
                } else if (clickCnt == solutions.size()){
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

        // 设定可以旋转
        rotation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(rotation.isSelected()) {
                    setRotate = true;
                    getAllSol.setEnabled(true);
                    displaySolution.setEnabled(true);
                    // 旋转后更新solution
                    dlx = loadFile(path, setRotate, setReflect);
                    solBd = dlx.getBoardDisplay();
                    solutions = dlx.getSolutions();
                    new Prompt().updateSol();
                    clickCnt = 0;
                    restart();
                } else {
                    setRotate = false;
                    getAllSol.setEnabled(true);
                    displaySolution.setEnabled(true);
                    // 旋转后更新solution
                    dlx = loadFile(path, setRotate, setReflect);
                    solBd = dlx.getBoardDisplay();
                    solutions = dlx.getSolutions();
                    new Prompt().updateSol();
                    clickCnt = 0;
                    restart();
                }
            }
        });

        // 设定可以翻转
        reflection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(reflection.isSelected()) {
                    setReflect = true;
                    getAllSol.setEnabled(true);
                    displaySolution.setEnabled(true);
//                    dlx = loadFile(path, setRotate, setReflect);
//                    solBd = dlx.getBoardDisplay();
//                    solutions = dlx.getSolutions();
//                    new Prompt().updateSol();
                    restart();
                    clickCnt = 0;
                } else {
                    setReflect = false;
                    getAllSol.setEnabled(true);
                    displaySolution.setEnabled(true);
//                    dlx = loadFile(path, setRotate, setReflect);
//                    solBd = dlx.getBoardDisplay();
//                    solutions = dlx.getSolutions();
//                    new Prompt().updateSol();
                    restart();
                    clickCnt = 0;
                }
            }
        });

    }

    public void restart() {
        result.removeAll();
        result.revalidate();
        board.removeAll();
        board.revalidate();
    }

    public static void main(String[] args) {
        new Display();
    }
}
