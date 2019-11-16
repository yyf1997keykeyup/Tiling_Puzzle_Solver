package solver;

import util.Piece;

import java.util.*;

public class DLX {

    private DLXHeader headerDummy;
    private List<DLXNode> rowDummies;
    private DLXHeader DLXSpaceHeaderEnd;  // the end of space headers, start of piece headers.
    private char[][] boardDisplay;
    private HashMap<Integer, DLXHeader> idx2spaceHeader;
    private HashMap<DLXHeader, Integer> spaceHeader2idx;
    private HashMap<String, DLXHeader> id2PieceHeader;
    private HashMap<DLXHeader, DLXNode> header2lowestNode;

    public DLX(Piece board, List<Piece> pieces) {
        headerDummy = new DLXHeader();
        rowDummies = new ArrayList<>();
        boardDisplay = board.getDisplay();
        idx2spaceHeader = new HashMap<>();
        spaceHeader2idx = new HashMap<>();
        id2PieceHeader = new HashMap<>();
        header2lowestNode = new HashMap<>();
        addHeaders(pieces);
        addRows(pieces);
//        printNodes();
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

    private void addHeaders(List<Piece> pieces) {
        addSpaceHeaders();
        addPieceHeaders(pieces);
    }

    private void printNodes() {
        String[][] nodeDisplay = new String[rowDummies.size() + 1][header2lowestNode.size()];
        Map<String, Integer> headerName2col = new HashMap<>();
        DLXHeader headerPoint = headerDummy.getR();
        int col = 0;
        while (headerPoint != null) {
            nodeDisplay[0][col] = headerPoint.getN();
            headerName2col.put(headerPoint.getN(), col++);
            headerPoint = headerPoint.getR();
        }
        for (int i=0; i<rowDummies.size(); i++) {
            DLXNode rowNode = rowDummies.get(i).getR();
            while(rowNode != null) {
                String headerName = rowNode.getC().getN();
                nodeDisplay[i+1][headerName2col.get(headerName)] = rowNode.toString();
                rowNode = rowNode.getR();
            }
        }
        for(String[] row : nodeDisplay) {
            for(String cell : row) {
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
                    newSpaceHeader.setN("#" + (j + i * getRowSize()));
                    // add space headers to the maps
                    idx2spaceHeader.put(j + i * getRowSize(), newSpaceHeader);
                    spaceHeader2idx.put(newSpaceHeader, j + i * getRowSize());
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
        for (Piece piece : pieces) {
            DLXHeader newPieceHeader = new DLXHeader();
            header2lowestNode.put(newPieceHeader, newPieceHeader);
            String idString = String.valueOf(piece.getId());
            id2PieceHeader.put(idString, newPieceHeader);
            newPieceHeader.setN(idString);

            newPieceHeader.setL(PieceHeaderPoint);
            PieceHeaderPoint.setR(newPieceHeader);
            PieceHeaderPoint = newPieceHeader;
        }
    }

    private void addRows(List<Piece> pieces) {
        for (Piece piece : pieces) {
            char[][] display = piece.getDisplay();
            int numRow = display.length;
            int numCol = display[0].length;
            for (int i = 0; i + numRow <= getRowSize(); i++) {
                for (int j = 0; j + numCol <= getColSize(); j++) {
                    if (isPieceMatch(piece, i, j)) {
                        addRow(piece, i, j);
                    }
                }
            }
        }
    }

    private void addRow(Piece piece, int offsetRow, int offsetCol) {
        DLXNode rowDummy = new DLXNode();
        DLXNode rowHead = rowDummy;
        char[][] display = piece.getDisplay();
        for (int i = 0; i < display.length; i++) {
            for (int j = 0; j < display[0].length; j++) {
                if (display[i][j] != '\u0000') {
                    int idx = (i + offsetRow) * getRowSize() + j + offsetCol;
                    DLXHeader header = idx2spaceHeader.get(idx);
                    rowHead = addNode(header, rowHead);
                }
            }
        }
        String idString = String.valueOf(piece.getId());
        DLXHeader header = id2PieceHeader.get(idString);
        rowDummies.add(rowDummy);
        addNode(header, rowHead);
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

    private boolean isPieceMatch(Piece piece, int offsetRow, int offsetCol) {
        char[][] display = piece.getDisplay();
        int matchSum = 0;
        for (int i = 0; i < display.length; i++) {
            for (int j = 0; j < display[0].length; j++) {
                char color = display[i][j];
                int idx = (i + offsetRow) * getRowSize() + j + offsetCol;
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
        if (!idx2spaceHeader.containsKey(idx)) {
            return false;
        } else {
            DLXHeader header = idx2spaceHeader.get(idx);
            return isColorMatchHeader(header, color);
        }
    }

    private boolean isColorMatchHeader(DLXHeader header, char color) {
        if (color == '\u0000') {
            return true;
        }
        int idx = spaceHeader2idx.get(header);
        int y = idx / getRowSize();
        int x = idx % getRowSize();
        if (y >= getRowSize() || x >= getColSize()) {
            return false;
        }
        return color == boardDisplay[y][x];
    }

    public void run() {
        Map<String, String> coveredMap = new HashMap<>();
        Stack<DLXLevel> levelStack = new Stack<>();
        DLXHeader headerPointer = headerDummy.getR();
        DLXLevel firstLevel = createLevel(headerPointer, coveredMap);
        levelStack.push(firstLevel);
        do {
            DLXLevel level = levelStack.peek();
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
                while(coveredNode.getR() != null) {
                    coveredNode = coveredNode.getR();
                }
                String PieceId = coveredNode.getC().getN();

                // 加入Set中，标记所覆盖的区域
                coveredNode = candidateStack.peek();
                while (coveredNode != null) {
                    coveredMap.put(coveredNode.getC().getN(), PieceId);
                    coveredNode = coveredNode.getR();
                }
                // todo: Get one solution!
                if (coveredMap.size() == getHeaderSize()) {
                    System.out.println("[INFO]Get one solution!!!!!!!!!");
//                    extractResult(levelStack);
                    printResult(coveredMap);
                }
                // 寻找下一个还没有被cover到的header，若找到，即为下一个level
                while (headerPointer != null && coveredMap.containsKey(headerPointer.getN())) {
                    headerPointer = headerPointer.getR();
                }
                // 如果还有下一个level，则创建一个新的level对象，并压入栈（下一个循环则会对这个level进行操作）
                if (headerPointer != null) {
                    DLXLevel newLevel = createLevel(headerPointer, coveredMap);
                    levelStack.push(newLevel);
                }
            }
            // 如果没有候选行可选（候选栈为空），或没有下一个level了（headerPointer == null），
            // 则恢复这一层level的所有Node，在Level栈中删除这一个level（下一个循环则会对上一个level进行操作）
            if (candidateStack.isEmpty() || headerPointer == null) {
                // 如果这是该level最后一个候选行，则恢复该行，并从coveredMap中删除
                if (candidateStack.size() == 1) {
                    DLXNode nodeP = candidateStack.pop();
                    while (nodeP != null) {
                        coveredMap.remove(nodeP.getC().getN());
                        recoverSpaceNode(nodeP);
                        nodeP = nodeP.getR();
                    }
                }
                // 恢复这一level的所有SpaceNode（usedStack中的所有行）
                while (!usedStack.isEmpty()) {
                    DLXNode usedNode = usedStack.pop();
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
                // 若levelStack中仍有Level留存，则重置headerPointer。
                if (!levelStack.isEmpty()) {
                    headerPointer = levelStack.peek().getHeader();
                }
            }
//            extractResult(levelStack);
        } while (!levelStack.isEmpty());
    }

    private DLXLevel createLevel(DLXHeader headerPointer, Map<String, String> coveredMap) {
        deleteHeaderNode(headerPointer);
        DLXLevel nextLevel = new DLXLevel(headerPointer);
        initLevel(headerPointer, coveredMap, nextLevel);
        return nextLevel;
    }

    private void initLevel(DLXNode headerPointer, Map<String, String> coveredMap, DLXLevel level) {
        Stack<DLXNode> usedStack = level.getUsedStack();
        Stack<DLXNode> candidateStack = level.getCandidateStack();
        // delete all the related rows
        DLXNode nodePointer = headerPointer;
        while (nodePointer.getD() != null) {
            DLXNode rowHeadNode = nodePointer.getD();

            boolean candidateFlag = true;
            // delete the whole row
            DLXNode rightNode = rowHeadNode.getR();
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
                candidateStack.push(rowHeadNode);
            } else {
                usedStack.push(rowHeadNode);
            }
            nodePointer = rowHeadNode;
        }
    }

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

    private String[][] printResult(Map<String, String> coveredMap) {
        String[][] boardDisplay = new String[getRowSize()][getColSize()];
        for (Map.Entry<String, String> entry : coveredMap.entrySet()) {
            String idx = entry.getKey();
            String pieceId = entry.getValue();
            if (idx.charAt(0) == '#') {
                int idxNum = Integer.parseInt(idx.substring(1));
                int row = idxNum / getRowSize();
                int col = idxNum % getRowSize();
                boardDisplay[row][col] = pieceId;
            }
        }
        for (String[] row : boardDisplay) {
            for (String cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
        System.out.println();

        return boardDisplay;
    }


//    private List<List<String>> extractResult(Stack<DLXLevel> levelStack) {
//        Stack<DLXLevel> tempLevelStack = new Stack<>();
//        List<List<String>> pieceDisplays = new ArrayList<>();
//        while (!levelStack.isEmpty()) {
//            DLXLevel level = levelStack.pop();
//            tempLevelStack.add(level);
//            if (level.getCandidateStack().isEmpty()) {
//                continue;
//            }
//            DLXNode resultNode = level.getCandidateStack().peek();
//            List<String> pieceDisplay = new ArrayList<>();
//            while (resultNode != null) {
//                pieceDisplay.add(resultNode.getC().getN());
//                resultNode = resultNode.getR();
//            }
//            pieceDisplays.add(pieceDisplay);
//        }
//        while (!tempLevelStack.isEmpty()) {
//            levelStack.add(tempLevelStack.pop());
//        }
//        printResult(pieceDisplays);
//        return pieceDisplays;
//    }
//
//    private char[][] printResult(List<List<String>> pieceDisplays) {
//        char[][] boardDisplay = new char[getRowSize()][getColSize()];
//        char identityChar = 'A';
//        for (List<String> pieceDisplay : pieceDisplays) {
//            for (String pieceCell : pieceDisplay) {
//                if (pieceCell.charAt(0) == '#') {
//                    int idxNum = Integer.parseInt(pieceCell.substring(1));
//                    int row = idxNum / getRowSize();
//                    int col = idxNum % getRowSize();
//                    boardDisplay[row][col] = identityChar;
//                }
//            }
//            identityChar++;
//        }
//        for (char[] row : boardDisplay) {
//            for (char cell : row) {
//                System.out.print(cell);
//            }
//            System.out.println();
//        }
//        System.out.println();
//        return boardDisplay;
//    }


}
