package gui;

import java.awt.*;
import java.util.ArrayList;

public class PaintGraphics {

    public static ArrayList<Tile> getCells() {
        //todo: 读取文件，并返回ArrayList，List中的元素为每个Tile，Tile的结构以二维数组存储，同时存储颜色
        ArrayList<Tile> Tiles = new ArrayList<>();
        Tile tile1 = new Tile();
        Tile tile2 = new Tile();
        int[][] cell1 = {{1,2,1,2},{0,0,0,1},{0,0,0,1}};
        int[][] cell2 = {{1,2,1},{0,1,0}};
        tile1.cells = cell1;
        tile2.cells = cell2;
        tile1.color = new Color(187,255,255);
        tile2.color = new Color(187,255,0);
        Tiles.add(tile1);
        Tiles.add(tile2);
        return Tiles;
    }


    public void paint(Graphics g) {
        ArrayList<Tile> tiles = new ArrayList<>();
        int x = 100;
        int y = 100;
        int width = 50;
        int height = 50;
        int x_initial;
        int y_initial;
        for (Tile tile : tiles = getCells()) {
            x_initial = x;
            y_initial = y;
            for (int i = 0; i < tile.cells.length; i++) {
                for (int j = 0; j < tile.cells[0].length; j++) {
                    if (tile.cells[i][j] != 0) {
                        g.setColor(Color.black);
                        g.drawRect(x, y, width, height);
                        g.setColor(tile.color);
                        g.fillRect(x + 1, y + 1, width - 1, height - 1);
                        x += width;
                    } else {
                        x += width;
                    }
                }
                if (i != tile.cells.length - 1) {
                    x = x_initial;
                }
                y += height;
            }
            x += 20;
            y = y_initial;
        }
    }
}
