package util;

public class Cell {
    private int x;
    private int y;
    private char color;       // 颜色

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public char getColor() {
        return color;
    }

    public Cell(int x, int y, char color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }


}
