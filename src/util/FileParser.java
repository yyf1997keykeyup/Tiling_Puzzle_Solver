package util;

import solver.DLX;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileParser {
    private static char BLANK = ' ';
    private String filePath;

    public FileParser(String filePath) {
        this.filePath = filePath;
    }

    private List<List<Character>> read() {
        // todo: most of the code is copied directly from others. more modifications are needed.
        File f = new File(filePath);
        FileReader fr;
        try {
            fr = new FileReader(f);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            return new ArrayList<>();
        }
        BufferedReader br = new BufferedReader(fr);
        List<String> lines = new ArrayList<>();
        try {
            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return linesToCells(lines.toArray(new String[0]));
    }

    private List<List<Character>> linesToCells(String[] lines) {
        List<List<Character>> cells = new ArrayList<>(lines.length);
        for (String line : lines) {
            List<Character> cellLine = new ArrayList<>();
            for (char cell : line.toCharArray()) {
                cellLine.add(cell);
            }
            cells.add(cellLine);
        }
        return cells;
    }

    private List<Piece> CellsToPieces(List<List<Character>> cells) {
        List<Piece> pieces = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            for (int j = 0; j < cells.get(i).size(); j++) {
                char cellColor = cells.get(i).get(j);
                if (cellColor != BLANK) {
                    List<Cell> neighbors = new ArrayList<>();
                    searchNeighbor(neighbors, cells, i, j);
                    pieces.add(new Piece(neighbors));
                }
            }
        }
        return pieces;
    }

    private void searchNeighbor(List<Cell> neighbors, List<List<Character>> cells, int i, int j) {
        // DFS of the cells, modify the value in 'cells', store the result in 'neighbors'
        char cellColor = cells.get(i).get(j);
        if (cellColor != BLANK) {
            Cell cell = new Cell(j, i, cellColor);
            neighbors.add(cell);
            cells.get(i).set(j, BLANK);
            if (i - 1 >= 0 && j < cells.get(i - 1).size()) {
                searchNeighbor(neighbors, cells, i - 1, j);
            }
            if (i + 1 < cells.size() && j < cells.get(i + 1).size()) {
                searchNeighbor(neighbors, cells, i + 1, j);
            }
            if (j - 1 >= 0) {
                searchNeighbor(neighbors, cells, i, j - 1);
            }
            if (j + 1 < cells.get(i).size()) {
                searchNeighbor(neighbors, cells, i, j + 1);
            }
        }
    }

    private Piece extractBoard(List<Piece> pieces) {
        /*
         * search and remove board from pieces set (board is the piece with maximum size)
         */
        // search it
        Piece board = null;
        for (Piece piece : pieces) {
            if (board == null) {
                board = piece;
            } else {
                board = board.size() < piece.size() ? piece : board;
            }
        }
        // remove it
        for (Piece piece : pieces) {
            if (piece.getId() == board.getId()) {
                pieces.remove(piece);
                break;
            }
        }
        return board;
    }

    // todo: delete it later. For debugging right now.
    public static void test(String path) {
        long startTime = System.currentTimeMillis();

        FileParser fp = new FileParser(path);
        List<List<Character>> cellsDisplay = fp.read();
        List<Piece> pieces = fp.CellsToPieces(cellsDisplay);
        Piece board = fp.extractBoard(pieces);

//        System.out.println("pieces:");
//        for (Piece piece : pieces) {
//            System.out.println("pieces " + piece.getId() + ":");
//            for (char[] line : piece.getDisplay()) {
//                System.out.println(line);
//            }
//        }
//
//        System.out.println("board:");
//        for (char[] line : board.getDisplay()) {
//            System.out.println(line);
//        }
        boolean rotation = true;
        boolean reflection = true;
        DLX dlx = new DLX(board, pieces, rotation, reflection);
        dlx.search(0);
        for(int[][] solution: dlx.getSolutions()) {
            for (int[] row : solution) {
                for (int cell : row) {
                    System.out.print(cell + "\t");
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println("count: " + dlx.getSolutionCount());
        System.out.println("time: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public static void main(String[] args) {
        Map<String, String> testcases = new HashMap<>();
//        testcases.put("basecase", "testcases/basecase.txt");
//        testcases.put("4*15", "testcases/puzzles/pentominoes4x15.txt");
        testcases.put("partial_cross", "testcases/puzzles/partial_cross.txt");

//        testcases.put("simple_cross", "testcases/simple_cross.txt");

//        testcases.put("redundant_pieces", "testcases/redundant_pieces.txt");



        for (Map.Entry<String, String> entry : testcases.entrySet()) {
            System.out.println(entry.getKey());
            test(entry.getValue());
        }
    }
}
