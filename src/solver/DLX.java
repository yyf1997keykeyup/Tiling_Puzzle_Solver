package solver;

import util.Piece;

import java.util.*;

import static util.Common.*;

public class DLX {

    private DLXHeader headerDummy;
    private char[][] boardDisplay;
    private boolean allowRotation;
    private boolean allowReflection;
    private Stack<DLXNode> currSolutionStack;
    private List<int[][]> solutions;
    private int uncoveredBoardCells;
    private int rowCount;

    private DLXHeader DLXSpaceHeaderEnd;  // the end of space headers, start of piece headers.
    private HashMap<Integer, DLXHeader> idx2spaceHeader;
    private HashMap<DLXHeader, Integer> spaceHeader2idx;
    private HashMap<String, DLXHeader> id2PieceHeader;
    private HashMap<DLXHeader, DLXNode> header2lowestNode;


    public DLX(Piece board, List<Piece> pieces, boolean allowRotation, boolean allowReflection) {
        headerDummy = new DLXHeader();
        headerDummy.setS(Integer.MAX_VALUE);
        boardDisplay = board.getDisplay();
        this.allowRotation = allowRotation;
        this.allowReflection = allowReflection;
        currSolutionStack = new Stack<>();
        solutions = new ArrayList<>();
        uncoveredBoardCells = 0;
        rowCount = 0;

        idx2spaceHeader = new HashMap<>();
        spaceHeader2idx = new HashMap<>();
        id2PieceHeader = new HashMap<>();
        header2lowestNode = new HashMap<>();

        addHeaders(pieces);
        addRows(pieces);
    }

    // todo(Shilin): display the color of board
    public char[][] getBoardDisplay() {
        return boardDisplay;
    }
    public void setAllowRotation(boolean allowRotation) {
        this.allowRotation = allowRotation;
    }

    public void setAllowReflection(boolean allowReflection) {
        this.allowReflection = allowReflection;
    }
    // todo(Shilin): 获取所有解决方案
    public List<int[][]> getSolutions() {
        return solutions;
    }
    // todo(Shilin): 获取方案总数
    public int getSolutionCount() {
        return solutions.size();
    }

    private int getRowSize() {
        return boardDisplay.length;
    }

    private int getColSize() {
        if (boardDisplay.length == 0) {
            return 0;
        }
        return boardDisplay[0].length;
    }

    private void addHeaders(List<Piece> pieces) {
        addSpaceHeaders();
        addPieceHeaders(pieces);
    }

    private void addSpaceHeaders() {
        // 1. add space headers to dummy
        // 2. add space headers to the maps (e.g., idx2spaceHeader)
        DLXHeader curr = headerDummy;
        for (int i = 0; i < getRowSize(); i++) {
            for (int j = 0; j < getColSize(); j++) {
                if (boardDisplay[i][j] != '\u0000') {
                    DLXHeader newSpaceHeader = new DLXHeader();
                    newSpaceHeader.setC(newSpaceHeader);
                    int id = j + i * getColSize();
                    newSpaceHeader.setN("#" + id);
                    // add space headers to the maps
                    idx2spaceHeader.put(id, newSpaceHeader);
                    spaceHeader2idx.put(newSpaceHeader, id);
                    header2lowestNode.put(newSpaceHeader, newSpaceHeader);
                    newSpaceHeader.setL(curr);
                    uncoveredBoardCells++;
                    curr.setR(newSpaceHeader);
                    curr = newSpaceHeader;
                }
            }
        }
        DLXSpaceHeaderEnd = curr;
    }

    private void addPieceHeaders(List<Piece> pieces) {
        DLXHeader PieceHeaderPoint = DLXSpaceHeaderEnd;
        DLXHeader newPieceHeader;
        for (Piece piece : pieces) {
            newPieceHeader = new DLXHeader();
            newPieceHeader.setC(newPieceHeader);

            header2lowestNode.put(newPieceHeader, newPieceHeader);
            String idString = String.valueOf(piece.getId());
            id2PieceHeader.put(idString, newPieceHeader);
            newPieceHeader.setN(idString);

            newPieceHeader.setL(PieceHeaderPoint);
            PieceHeaderPoint.setR(newPieceHeader);
            PieceHeaderPoint = newPieceHeader;
        }
        PieceHeaderPoint.setR(headerDummy);
        headerDummy.setL(PieceHeaderPoint);
    }

    private void addRows(List<Piece> pieces) {
        for (Piece piece : pieces) {
            List<char[][]> displayList = new ArrayList<>();
            displayList.add(piece.getDisplay());
            if (allowRotation) {
                setRotatedMatrixList(displayList);
            }
            if (allowReflection) {
                setReflectedMatrixList(displayList);
            }
            for (char[][] singleDisplay : displayList) {
                int numRow = singleDisplay.length;
                int numCol = singleDisplay[0].length;
                for (int i = 0; i + numRow <= getRowSize(); i++) {
                    for (int j = 0; j + numCol <= getColSize(); j++) {
                        if (isDisplayMatch(singleDisplay, i, j)) {
                            rowCount++;
                            addRow(singleDisplay, piece.getId(), i, j);
                        }
                    }
                }
            }
        }
        for (Map.Entry<DLXHeader, DLXNode> entry : header2lowestNode.entrySet()) {
            DLXHeader header = entry.getKey();
            DLXNode node = entry.getValue();
            header.setU(node);
            node.setD(header);
        }
    }

    private boolean isDisplayMatch(char[][] display, int offsetRow, int offsetCol) {
        /*
         * piece的所有单元格能否覆盖到面板，并且颜色匹配
         */
        int matchSum = 0;
        for (int i = 0; i < display.length; i++) {
            for (int j = 0; j < display[0].length; j++) {
                char color = display[i][j];
                int idx = (i + offsetRow) * getColSize() + j + offsetCol;
                if (!pieceCellMatch(idx, color)) {
                    return false;
                } else {
                    matchSum++;
                }
            }
        }
        return matchSum == display.length * display[0].length;
    }

    private boolean pieceCellMatch(int idx, char color) {
        /*
         * 某个单元格能否覆盖到面板，并且颜色匹配
         */
        if (color != '\u0000' && !idx2spaceHeader.containsKey(idx)) {
            return false;
        } else {
            DLXHeader header = idx2spaceHeader.get(idx);
            return isColorMatchHeader(header, color);
        }
    }

    private boolean isColorMatchHeader(DLXHeader header, char color) {
        /*
         * piece的某个单元的颜色是否能够能与面板的颜色匹配
         */
        if (color == '\u0000') {
            return true;
        }
        int idx = spaceHeader2idx.get(header);
        int y = idx / getColSize();
        int x = idx % getColSize();
        if (y >= getRowSize() || x >= getColSize()) {
            return false;
        }
        return color == boardDisplay[y][x];
    }

    private void addRow(char[][] display, int id, int offsetRow, int offsetCol) {
        DLXNode rowDummy = new DLXNode();
        DLXNode rowHead = rowDummy;
        for (int i = 0; i < display.length; i++) {
            for (int j = 0; j < display[0].length; j++) {
                if (display[i][j] != '\u0000') {
                    int idx = (i + offsetRow) * getColSize() + j + offsetCol;
                    DLXHeader header = idx2spaceHeader.get(idx);
                    rowHead = addNode(header, rowHead);
                }
            }
        }
        DLXHeader header = id2PieceHeader.get(String.valueOf(id));
        rowHead = addNode(header, rowHead);
        rowHead.setR(rowDummy.getR());
        rowDummy.getR().setL(rowHead);
    }

    private DLXNode addNode(DLXHeader header, DLXNode rowHead) {
        header.incrementS();
        DLXNode newNode = new DLXNode();

        newNode.setL(rowHead);
        rowHead.setR(newNode);
        DLXNode upperNode = header2lowestNode.get(header);

        newNode.setU(upperNode);
        upperNode.setD(newNode);
        header2lowestNode.put(header, newNode);

        newNode.setC(header);
        return newNode;
    }


    private boolean isSolutionDuplicated(int[][] target) {
        for (int[][] solution : solutions) {
            if (isResultAsymmetric(target, solution)) {
                return true;
            }
        }
        return false;
    }

    public boolean isResultAsymmetric(int[][] target, int[][] source) {
        int[][] targetRotated90 = rotateMatrixBy90Degree(source);
        int[][] targetRotated180 = rotateMatrixBy90Degree(targetRotated90);
        int[][] targetRotated270 = rotateMatrixBy90Degree(targetRotated180);

        // If width == height. At most 8.
        if (target.length == target[0].length) {
            return isMatrixDuplicated(target, source) ||
                    isMatrixDuplicated(target, targetRotated90) ||
                    isMatrixDuplicated(target, targetRotated180) ||
                    isMatrixDuplicated(target, targetRotated270) ||
                    isMatrixDuplicated(target, reflectMatrix(source)) ||
                    isMatrixDuplicated(target, reflectMatrix(targetRotated90)) ||
                    isMatrixDuplicated(target, reflectMatrix(targetRotated180)) ||
                    isMatrixDuplicated(target, reflectMatrix(targetRotated270));
        }
        // If width != height. At most 4.
        else {
            return isMatrixDuplicated(target, source) ||
                    isMatrixDuplicated(target, targetRotated180) ||
                    isMatrixDuplicated(target, reflectMatrix(source)) ||
                    isMatrixDuplicated(target, reflectMatrix(targetRotated180));
        }
    }

    // backtracking function, coming from the paper "Dancing Links".
    public void search(int k) {
        if (uncoveredBoardCells == 0) {
//        if (headerDummy.getR() == headerDummy) {
            // print the current solution and return
            int[][] display = SolutionTo2DArray();
            if (!isSolutionDuplicated(display)) {
                System.out.println(solutions.size());
                solutions.add(display);
            }
        } else {
            DLXHeader column = chooseColumn(true);
            coverColumn(column);
            for (DLXNode firstNode = column.getD(); firstNode != column; firstNode = firstNode.getD()) {
                currSolutionStack.push(firstNode);
                for (DLXNode node = firstNode.getR(); node != firstNode; node = node.getR()) {
                    coverColumn(node.getC());
                }
                search(k + 1);
                // set rowHeadNode <- O_k
                // set column <- rowHeadNode.getC()
                firstNode = currSolutionStack.pop();
                column = firstNode.getC();
                for (DLXNode node = firstNode.getL(); node != firstNode; node = node.getL()) {
                    uncoverColumn(node.getC());
                }
            }
            uncoverColumn(column);
        }
    }

    private DLXHeader chooseColumn(boolean isOptimal) {
        if (isOptimal) {
            // optimal version: choose the header with minimum size
            int smallest = Integer.MAX_VALUE;
            DLXHeader resColumn = headerDummy.getR();
            for (DLXHeader header = headerDummy.getR(); header != headerDummy; header = header.getR()) {
                if (header.getN().charAt(0) == '#' && header.getS() < smallest) {
                    smallest = header.getS();
                    resColumn = header;
                }
            }
            return resColumn;
        } else {
            // normal version: choose the next header
            return headerDummy.getR();
        }

    }

    private void coverColumn(DLXHeader column) {
        if (column.getN().charAt(0) == '#'){
            uncoveredBoardCells--;
        }
        column.getR().setL(column.getL());
        column.getL().setR(column.getR());
        for (DLXNode firstNode = column.getD(); firstNode != column; firstNode = firstNode.getD()) {
            for (DLXNode node = firstNode.getR(); node != firstNode; node = node.getR()) {
                node.getD().setU(node.getU());
                node.getU().setD(node.getD());
                node.getC().decrementS();
            }
        }
    }

    private void uncoverColumn(DLXHeader column) {
        if (column.getN().charAt(0) == '#'){
            uncoveredBoardCells++;
        }
        for (DLXNode firstNode = column.getU(); firstNode != column; firstNode = firstNode.getU()) {
            for (DLXNode node = firstNode.getL(); node != firstNode; node = node.getL()) {
                node.getC().incrementS();
                node.getD().setU(node);
                node.getU().setD(node);
            }
        }
        column.getR().setL(column);
        column.getL().setR(column);
    }

    private int[][] SolutionTo2DArray() {
        /*
         * convert pieces stored in the currSolutionStack to 2D array.
         */
        int[][] display = new int[getRowSize()][getColSize()];
        for (int[] row : display) {
            Arrays.fill(row, -1);
        }
        for (DLXNode node : currSolutionStack) {
            int pieceId = -1;
            // find the piece id from the headers
            DLXNode nextNode = node;
            do {
                String colName = nextNode.getC().getN();
                if (colName.charAt(0) != '#') {
                    pieceId = Integer.parseInt(colName);
                    break;
                }
                nextNode = nextNode.getR();
            } while (nextNode != node);

            // get all the indexes (position info), and set pieceId to the 2DArray.
            nextNode = node;
            do {
                String colName = nextNode.getC().getN();
                if (colName.charAt(0) == '#') {
                    int idx = Integer.parseInt(colName.substring(1));
                    int row = idx / getColSize();
                    int col = idx % getColSize();
                    display[row][col] = pieceId;
                }
                nextNode = nextNode.getR();
            } while (nextNode != node);
        }
        return display;
    }
}
