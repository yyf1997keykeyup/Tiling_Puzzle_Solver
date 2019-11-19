package util;

import java.util.ArrayList;
import java.util.List;

public class Piece {
    /**
     * The color display of piece.
     * '' represents empty.
     * Different characters represent different colors.
     */
    private static int idGenerator = 0;
    private int id;  // Unique identifier
    private List<Cell> Cells;  // cells that it contains
    private char[][] display;  // 2-D color display


    public Piece(List<Cell> Cells) {
        this.id = idGenerator++;
        this.Cells = Cells;
        this.generateDisplay();
    }

    private void generateDisplay() {
        int xMax = -1;
        int xMin = Integer.MAX_VALUE;
        int yMax = -1;
        int yMin = Integer.MAX_VALUE;
        for (Cell cell : Cells) {
            xMax = Math.max(xMax, cell.getX());
            xMin = Math.min(xMin, cell.getX());
            yMax = Math.max(yMax, cell.getY());
            yMin = Math.min(yMin, cell.getY());
        }
        display = new char[yMax - yMin + 1][xMax - xMin + 1];
        int offsetX = xMin;
        int offsetY = yMin;
        for (Cell cell : Cells) {
            display[cell.getY() - offsetY][cell.getX() - offsetX] = cell.getColor();
        }
    }

    public int size() {
        return Cells.size();
    }

    public int getId() {
        return id;
    }

    public char[][] getDisplay() {
        return display;
    }
}
