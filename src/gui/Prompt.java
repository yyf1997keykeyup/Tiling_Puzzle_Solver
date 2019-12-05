package gui;

import javax.swing.*;
import java.awt.*;

class Prompt extends JDialog {
    public void readFile(){
        Container container=getContentPane();//创建一个容器
        container.add(new JLabel("先读文件啦！"));
        setBounds(120,120,200,150);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public void noSolution(){
        Container container=getContentPane();//创建一个容器
        container.add(new JLabel("无解了啦！"));
        setBounds(120,120,200,150);
        setVisible(true);
        setLocationRelativeTo(null);
    }
}
