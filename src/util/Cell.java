package util;

class Cell {
    private int x;
    private int y;
    private char color;       // 颜色
    private int BelongColor;  // 板块色号

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public char getColor() {
        return color;
    }

    public int getBelongColor() {
        return BelongColor;
    }

    public Cell(int x, int y, char color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }


}
