package solver;

import util.Piece;

import java.util.*;

import static util.Common.*;

public class DLX {

    private DLXHeader headerDummy;
    private List<DLXNode> rowDummies;
    private DLXHeader DLXSpaceHeaderEnd;  // the end of space headers, start of piece headers.
    private char[][] boardDisplay;
    private HashMap<Integer, DLXHeader> idx2spaceHeader;
    private HashMap<DLXHeader, Integer> spaceHeader2idx;
    private HashMap<String, DLXHeader> id2PieceHeader;
    private HashMap<DLXHeader, DLXNode> header2lowestNode;
    private Map<String, DLXLevel> HeaderName2Level;
    private boolean allowRotation;
    private boolean allowReflection;
    private List<int[][]> solutions;

    private Stack<DLXNode> currSolutionStack;

    public DLX(Piece board, List<Piece> pieces, boolean allowRotation, boolean allowReflection) {
        headerDummy = new DLXHeader();
        headerDummy.setS(Integer.MAX_VALUE);
        rowDummies = new ArrayList<>();
        boardDisplay = board.getDisplay();
        idx2spaceHeader = new HashMap<>();
        spaceHeader2idx = new HashMap<>();
        id2PieceHeader = new HashMap<>();
        header2lowestNode = new HashMap<>();
        HeaderName2Level = new HashMap<>();
        solutions = new ArrayList<>();


        currSolutionStack = new Stack<>();

        this.allowRotation = allowRotation;
        this.allowReflection = allowReflection;
        addHeaders(pieces);
        addRows(pieces);
//        initHeaderName2Level();
//        printNodes();
    }

    public void setAllowRotation(boolean allowRotation) {
        this.allowRotation = allowRotation;
    }

    public void setAllowReflection(boolean allowReflection) {
        this.allowReflection = allowReflection;
    }

    public int getRowSize() {
        return boardDisplay.length;
    }

    public int getColSize() {
        if (boardDisplay.length == 0) {
            return 0;
        }
        return boardDisplay[0].length;
    }

    private int getHeaderSize() {
        return header2lowestNode.size();
    }

    public List<int[][]> getSolution() {
        return solutions;
    }

    public int getSolutionCount() {
        return solutions.size();
    }

    private void addHeaders(List<Piece> pieces) {
        addSpaceHeaders();
        addPieceHeaders(pieces);
    }

    private void printNodes() {
        String[][] nodeDisplay = new String[rowDummies.size() + 1][header2lowestNode.size()];
        for (String[] row : nodeDisplay) {
            Arrays.fill(row, " ");
        }
        Map<String, Integer> headerName2col = new HashMap<>();
        DLXHeader headerPoint = headerDummy.getR();
        int col = 0;
        while (headerPoint != null) {
            nodeDisplay[0][col] = headerPoint.getN();
            headerName2col.put(headerPoint.getN(), col++);
            headerPoint = headerPoint.getR();
        }
        for (int i = 0; i < rowDummies.size(); i++) {
            DLXNode rowNode = rowDummies.get(i).getR();
            while (rowNode != null) {
                String headerName = rowNode.getC().getN();
                nodeDisplay[i + 1][headerName2col.get(headerName)] = rowNode.getC().getN();
                rowNode = rowNode.getR();
            }
        }
        for (String[] row : nodeDisplay) {
            for (String cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
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
            // 是否允许翻转
            if (allowRotation) {
                setRotatedMatrixList(displayList);
            }
            // 是否允许镜像
            if (allowReflection) {
                setReflectedMatrixList(displayList);
            }
            for (char[][] singleDisplay : displayList) {
                int numRow = singleDisplay.length;
                int numCol = singleDisplay[0].length;
                for (int i = 0; i + numRow <= getRowSize(); i++) {
                    for (int j = 0; j + numCol <= getColSize(); j++) {
                        if (isDisplayMatch(singleDisplay, i, j)) {
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
        /**
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
        /**
         * 某个单元格能否覆盖到面板，并且颜色匹配
         */
        if (!idx2spaceHeader.containsKey(idx)) {
            return false;
        } else {
            DLXHeader header = idx2spaceHeader.get(idx);
            return isColorMatchHeader(header, color);
        }
    }

    private boolean isColorMatchHeader(DLXHeader header, char color) {
        /**
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
        rowDummies.add(rowDummy);
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

        newNode.setU(upperNode);  // set UP node
        upperNode.setD(newNode);  // set DOWN node
        header2lowestNode.put(header, newNode);

        newNode.setC(header);
        return newNode;
    }

    private void initHeaderName2Level() {
        for (DLXHeader header = headerDummy.getR(); header.getR() != headerDummy; header = header.getR()) {
            HeaderName2Level.put(header.getN(), createLevel(header));
        }
    }

    private DLXLevel createLevel(DLXHeader header) {
        DLXLevel level = new DLXLevel(header);
        for (DLXNode node = header.getD(); node != header; node = node.getD()) {
            level.getNodeStack().push(node);
        }
        return level;
    }

    private void initLevel(DLXLevel level, Map<String, String> coveredMap) {
        Stack<DLXNode> nodeStack = level.getNodeStack();  // 全栈
        Stack<DLXNode> usedStack = level.getUsedStack();
        Stack<DLXNode> candidateStack = level.getCandidateStack();
        // delete all the related rows
        while (!nodeStack.isEmpty()) {
            DLXNode node = nodeStack.pop();
            boolean candidateFlag = true;
            // delete the whole row
            DLXNode rightNode = node.getR();
            while (rightNode != null) {
                deleteSpaceNode(rightNode);
                // overlapping means this row is should not be a candidate
                if (coveredMap.containsKey(rightNode.getC().getN())) {
                    candidateFlag = false;
                }
                rightNode = rightNode.getR();
            }
            // if overlapped, push it to the usedStack, otherwise to the candidateStack
            if (candidateFlag) {
                candidateStack.push(node);
            } else {
                usedStack.push(node);
            }
        }
    }

    private boolean isSolutionDuplicated(int[][] target) {
        for (int[][] solution : solutions) {
            if (isResultAsymmetric(target, solution)) {
                return true;
            }
        }
        return false;
    }

    private boolean isResultAsymmetric(int[][] target, int[][] source) {
        int[][] targetRotated90 = rotateMatrixBy90Degree(source);
        int[][] targetRotated180 = rotateMatrixBy90Degree(targetRotated90);
        // When width == height. At most 8 symmetric patterns.
        if (target.length == target[0].length) {
            int[][] targetRotated270 = rotateMatrixBy90Degree(targetRotated180);
            return isMatrixDuplicated(target, source) ||
                    isMatrixDuplicated(target, targetRotated90) ||
                    isMatrixDuplicated(target, targetRotated180) ||
                    isMatrixDuplicated(target, targetRotated270) ||
                    isMatrixDuplicated(target, reflectMatrix(source)) ||
                    isMatrixDuplicated(target, reflectMatrix(targetRotated90)) ||
                    isMatrixDuplicated(target, reflectMatrix(targetRotated180)) ||
                    isMatrixDuplicated(target, reflectMatrix(targetRotated270));
        }

        // When width != height. At most 4 symmetric patterns.
        else {
            return isMatrixDuplicated(target, source) ||
                    isMatrixDuplicated(target, targetRotated180) ||
                    isMatrixDuplicated(target, reflectMatrix(source)) ||
                    isMatrixDuplicated(target, reflectMatrix(targetRotated180));
        }
    }


    // backtracking function, coming from the paper "Dancing Links".
    public void search(int k) {
        if (headerDummy.getR() == headerDummy) {
            // print the current solution and return
            int[][] display = SolutionTo2DArray();
            if (!isSolutionDuplicated(display)) {
                System.out.println("Solution #" + (getSolutionCount() + 1) + ": ");
                // todo: for debugging
                for (int[] row : display) {
                    for (int cell : row) {
                        System.out.print(cell + "\t");
                    }
                    System.out.println();
                }
                System.out.println();
                solutions.add(display);
            }
        } else {
            DLXHeader column = chooseColumn();
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

    private DLXHeader chooseColumn() {
        int smallest = Integer.MAX_VALUE;
        DLXHeader resColumn = headerDummy.getR();
        for (DLXHeader header = headerDummy.getR(); header != headerDummy; header = header.getR()) {
            if (header.getS() < smallest) {
                smallest = header.getS();
                resColumn = header;
            }
        }
        return resColumn;
    }

    private void coverColumn(DLXHeader column) {
        column.getR().setL(column.getL());
        column.getL().setR(column.getR());
        for (DLXNode firstNode = column.getD(); firstNode != column; firstNode = firstNode.getD()) {
            for (DLXNode node = firstNode.getR(); node != firstNode; node = node.getR()) {
                if (node == null)
                    System.out.println();
                node.getD().setU(node.getU());
                node.getU().setD(node.getD());
                node.getC().decrementS();
            }
        }
    }

    private void uncoverColumn(DLXHeader column) {
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
        int[][] display = new int[getRowSize()][getColSize()];
        for (int i = 0; i < currSolutionStack.size(); i++) {
            DLXNode node = currSolutionStack.get(i);
            int pieceId = -1;
            DLXNode nextNode = node;
            do {
                String colName = nextNode.getC().getN();
                if (colName.charAt(0) != '#') {
                    pieceId = Integer.parseInt(colName);
                    break;
                }
                nextNode = nextNode.getR();
            } while (nextNode != node);

            nextNode = node;
            do {
                String colName = nextNode.getC().getN();
                if (colName.charAt(0) == '#') {
                    int idx = Integer.parseInt(colName.substring(1));
                    int row = idx / getRowSize();
                    int col = idx % getRowSize();
                    display[row][col] = pieceId;
                }
                nextNode = nextNode.getR();
            } while (nextNode != node);
        }
        return display;
    }


    // old version, not used now
    public void run() {
        Map<String, String> coveredMap = new HashMap<>();
        Stack<DLXLevel> levelStack = new Stack<>();
        DLXHeader headerPointer = headerDummy.getR();
        DLXLevel firstLevel = HeaderName2Level.get(headerPointer.getN());
        initLevel(firstLevel, coveredMap);
        levelStack.push(firstLevel);
        deleteHeaderNode(headerPointer);
        do {
            DLXLevel level = levelStack.peek();
            Stack<DLXNode> nodeStack = level.getNodeStack();  // 全栈
            Stack<DLXNode> usedStack = level.getUsedStack();  // 回收栈
            Stack<DLXNode> candidateStack = level.getCandidateStack();  // 候选行栈
            /**
             * 从候选行中选择下一个行，作为当前尝试的路径
             * 1. 如果这不是第一次遍历该Level的候选行时（当coveredSet中有该候选行的数据时），
             *    转移当前候选行到回收栈，并删除在coveredSet中的数据
             * 2. 然后选用下一个候选行。
             */
            // 如果有候选行，且不是第一次遍历该Level的候选行时。
            if (!candidateStack.isEmpty() && coveredMap.containsKey(candidateStack.peek().getC().getN())) {
                DLXNode candidate = candidateStack.pop();
                // 删除该候选行在coveredSet中插入的数据
                DLXNode nextNode = candidate;
                while (nextNode != null) {
                    coveredMap.remove(nextNode.getC().getN());
                    nextNode = nextNode.getR();
                }
                // 转移当前候选行到回收栈
                usedStack.add(candidate);
            }
            // 当有下一候选行时，选用下一个候选行。
            if (!candidateStack.isEmpty()) {
                DLXNode coveredNode = candidateStack.peek();
                // 寻找Piece ID
                while (coveredNode.getR() != null) {
                    coveredNode = coveredNode.getR();
                }
                String PieceId = coveredNode.getC().getN();

                // 加入Set中，标记所覆盖的区域
                coveredNode = candidateStack.peek();
                while (coveredNode != null) {
                    coveredMap.put(coveredNode.getC().getN(), PieceId);
                    coveredNode = coveredNode.getR();
                }
                if (coveredMap.size() == getHeaderSize()) {
                    int[][] solution = getResult(coveredMap);
                    if (!isSolutionDuplicated(solution)) {
                        solutions.add(solution);
                        System.out.println("[INFO]Get one solution!!!!!!!!!");
                        // todo: delete it, just for debugging
                        for (int[] row : solution) {
                            for (int cell : row) {
                                System.out.print(cell + "\t");
                            }
                            System.out.println();
                        }
                        System.out.println();
                        // todo: delete end here
                    }
                }
                // 寻找下一个还没有被cover到的header，若找到，即为下一个level
                while (headerPointer.getN() != null && coveredMap.containsKey(headerPointer.getN())) {
                    headerPointer = headerPointer.getR();
                }
                // 如果还有下一个level，则创建一个新的level对象，并压入栈（下一个循环则会对这个level进行操作）
                if (headerPointer.getN() != null && headerPointer.getN().charAt(0) == '#') {
                    DLXLevel nextLevel = HeaderName2Level.get(headerPointer.getN());
                    initLevel(nextLevel, coveredMap);
                    levelStack.push(nextLevel);
                    deleteHeaderNode(headerPointer);
                }
            }
            // 如果没有候选行可选（候选栈为空），或没有下一个level了（headerPointer == null），
            // 则恢复这一层level的所有Node，在Level栈中删除这一个level（下一个循环则会对上一个level进行操作）
            if (candidateStack.isEmpty() || headerPointer.getN() == null) {
                // 已经没有下一个level了（遍历到最后一个candidate 或 找到了一个solution）
                if (candidateStack.size() >= 1) {
                    while (!candidateStack.isEmpty()) {
                        DLXNode nodeP = candidateStack.pop();
                        nodeStack.push(nodeP);
                        while (nodeP != null) {
                            coveredMap.remove(nodeP.getC().getN());
                            recoverSpaceNode(nodeP);
                            nodeP = nodeP.getR();
                        }
                    }
                }
                // 恢复这一level的所有SpaceNode（usedStack中的所有行）
                while (!usedStack.isEmpty()) {
                    DLXNode usedNode = usedStack.pop();
                    nodeStack.push(usedNode);
                    do {
                        recoverSpaceNode(usedNode);
                        usedNode = usedNode.getR();
                    } while (usedNode != null);
                }
                // 恢复这一level的HeaderNode
                if (headerPointer != null) {
                    recoverHeaderNode(headerPointer);
                }
                // 从栈中弹出这一Level
                levelStack.pop();
                // 若levelStack中仍有Level留存，则重置headerPointer为栈头的level的header。
                if (!levelStack.isEmpty()) {
                    headerPointer = levelStack.peek().getHeader();
                }
            }
        } while (!levelStack.isEmpty());
    }

//    private DLXLevel createLevel(DLXHeader headerPointer, Map<String, String> coveredMap) {
//        deleteHeaderNode(headerPointer);
//        DLXLevel nextLevel = new DLXLevel(headerPointer);
//        initLevel(headerPointer, coveredMap, nextLevel);
//        return nextLevel;
//    }
//
//    private void initLevel(DLXNode headerPointer, Map<String, String> coveredMap, DLXLevel level) {
//        Stack<DLXNode> usedStack = level.getUsedStack();
//        Stack<DLXNode> candidateStack = level.getCandidateStack();
//        // delete all the related rows
//        DLXNode nodePointer = headerPointer;
//        while (nodePointer.getD() != null) {
//            DLXNode rowfirstNode = nodePointer.getD();
//
//            boolean candidateFlag = true;
//            // delete the whole row
//            DLXNode rightNode = rowfirstNode.getR();
//            while (rightNode != null) {
//                deleteSpaceNode(rightNode);
//                // overlapping means this row is should not be a candidate
//                if (coveredMap.containsKey(rightNode.getC().getN())) {
//                    candidateFlag = false;
//                }
//                rightNode = rightNode.getR();
//            }
//            // if overlapped, push it to the usedStack, otherwise to the candidateStack
//            if (candidateFlag) {
//                candidateStack.push(rowfirstNode);
//            } else {
//                usedStack.push(rowfirstNode);
//            }
//            nodePointer = rowfirstNode;
//        }
//    }

    private void deleteHeaderNode(DLXHeader headerNode) {
        DLXNode leftNode = headerNode.getL();
        DLXNode rightNode = headerNode.getR();
        if (leftNode != null) {
            leftNode.setR(rightNode);
        }
        if (rightNode != null) {
            rightNode.setL(leftNode);
        }
    }

    private void recoverHeaderNode(DLXHeader headerNode) {
        DLXNode leftNode = headerNode.getL();
        DLXNode rightNode = headerNode.getR();
        if (leftNode != null) {
            leftNode.setR(headerNode);
        }
        if (rightNode != null) {
            rightNode.setL(headerNode);
        }
    }

    private void deleteSpaceNode(DLXNode node) {
        DLXNode upNode = node.getU();
        DLXNode downNode = node.getD();
        if (upNode != null) {
            upNode.setD(downNode);
        }
        if (downNode != null) {
            downNode.setU(upNode);
        }
        if (node.getC() != null) {
            node.getC().decrementS();
        }
    }

    private void recoverSpaceNode(DLXNode node) {
        DLXNode upNode = node.getU();
        DLXNode downNode = node.getD();
        if (upNode != null) {
            upNode.setD(node);
        }
        if (downNode != null) {
            downNode.setU(node);
        }
        if (node.getC() != null) {
            node.getC().incrementS();
        }
    }

    private int[][] getResult(Map<String, String> coveredMap) {
        int[][] boardDisplay = new int[getRowSize()][getColSize()];
        for (Map.Entry<String, String> entry : coveredMap.entrySet()) {
            String idx = entry.getKey();
            int pieceId = Integer.parseInt(entry.getValue());
            if (idx.charAt(0) == '#') {
                int idxNum = Integer.parseInt(idx.substring(1));
                int row = idxNum / getColSize();
                int col = idxNum % getColSize();
                boardDisplay[row][col] = pieceId;
            }
        }
        return boardDisplay;
    }
}
