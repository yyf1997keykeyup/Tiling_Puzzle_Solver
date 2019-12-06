package gui;

import javax.swing.*;
import java.awt.*;

class Prompt extends JDialog {
    public void readFile(){
        Container container=getContentPane();//创建一个容器
        container.add(new JLabel("Please choose a file to solve!"));
        setBounds(120,120,200,150);
        setVisible(true);
        setLocationRelativeTo(null);
    }


    public void updateSol(){
        Container container=getContentPane();//创建一个容器
        container.add(new JLabel("Solutions are updated!"));
        setBounds(120,120,200,150);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public void noSolution(){
        Container container=getContentPane();//创建一个容器
        container.add(new JLabel("No solutions any more!"));
        setBounds(120,120,200,150);
        setVisible(true);
        setLocationRelativeTo(null);
    }
}
