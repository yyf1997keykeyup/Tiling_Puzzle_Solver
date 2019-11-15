package solver;

import util.Piece;

import java.util.*;

public class DLX {

    private DLXHeader headerDummy;
    private DLXHeader DLXSpaceHeaderEnd;  // the end of space headers, start of piece headers.
    private char[][] boardDisplay;
    private HashMap<Integer, DLXHeader> idx2spaceHeader;
    private HashMap<DLXHeader, Integer> spaceHeader2idx;
    private HashMap<String, DLXHeader> id2PieceHeader;
    private HashMap<DLXHeader, DLXNode> header2lowestNode;

    public DLX(Piece board, List<Piece> pieces) {
        headerDummy = new DLXHeader();
        boardDisplay = board.getDisplay();
        idx2spaceHeader = new HashMap<>();
        spaceHeader2idx = new HashMap<>();
        id2PieceHeader = new HashMap<>();
        header2lowestNode = new HashMap<>();

        addHeaders(pieces);
        addRows(pieces);
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
        return color == boardDisplay[y][x];
    }

    public void run() {
        Set<String> coveredSet = new HashSet<>();

        Stack<DLXLevel> levelStack = new Stack<>();
        DLXHeader headerPointer = headerDummy.getR();

        levelStack.push(createLevel(headerPointer, coveredSet));

        do {
            DLXLevel level = levelStack.peek();
            Stack<DLXNode> usedStack = level.getUsedStack();
            Stack<DLXNode> candidateStack = level.getCandidateStack();

            // 从候选行中选择下一个行，作为当前尝试的路径
            // 如果这不是第一个候选行（coveredSet中有该候选行的数据），先转移当前候选行，并删除在coveredSet中的数据
            // 选用下一个候选行。
            if (!candidateStack.isEmpty() && coveredSet.contains(candidateStack.peek().getC().getN())) {
                DLXNode candidate = candidateStack.pop();
                DLXNode nextNode = candidate;
                while (nextNode != null) {
                    coveredSet.remove(nextNode.getC().getN());
                    nextNode = nextNode.getR();
                }
                usedStack.add(candidate);
            }
            if (!candidateStack.isEmpty()) {
                DLXNode coveredNode = candidateStack.peek();
                // 加入map中，标记以覆盖的区域
                while (coveredNode != null) {
                    coveredSet.add(coveredNode.getC().getN());
                    coveredNode = coveredNode.getR();
                }
                // todo: Get one solution!
                if (coveredSet.size() == getHeaderSize()) {
                    System.out.println("[INFO]Get one solution!!!!!!!!!");
                }
                // 如果还有下一个level，向下一个level继续尝试
                while (headerPointer != null && coveredSet.contains(headerPointer.getN())) {
                    headerPointer = headerPointer.getR();
                }
                if (headerPointer != null) {
                    levelStack.push(createLevel(headerPointer, coveredSet));
                }
            }

            // 如果没有候选行或没有下一个level了，则恢复这一level的所有节点，在栈中删除这一个level的信息，返回到上一个level
            if (candidateStack.isEmpty() || headerPointer == null) {
                // 恢复这一level的所有节点（usedStack中的所有行）
                while (!usedStack.isEmpty()) {
                    DLXNode usedNode = usedStack.pop();
                    do {
                        recoverNode(usedNode);
                        usedNode = usedNode.getR();
                    } while (usedNode != null);
                }
                if (headerPointer != null) {
                    recoverHeadNode(headerPointer);
                }
                levelStack.pop();
                if (!levelStack.isEmpty()) {
                    headerPointer = levelStack.peek().getHeader();
                }
            }

        } while (!levelStack.isEmpty());
    }

    private DLXLevel createLevel(DLXHeader headerPointer, Set<String> coveredSet) {
        deleteHeadNode(headerPointer);
        DLXLevel nextLevel = new DLXLevel(headerPointer);
        initLevel(headerPointer, coveredSet, nextLevel);
        return nextLevel;
    }

    private void initLevel(DLXNode headerPointer, Set<String> coveredSet, DLXLevel level) {
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
                deleteNode(rightNode);
                // if overlap the header, than push to used, otherwise candidate
                if (coveredSet.contains(rightNode.getC().getN())) {
                    candidateFlag = false;
                }
                rightNode = rightNode.getR();
            }
            if (candidateFlag) {
                candidateStack.push(rowHeadNode);
            } else {
                usedStack.push(rowHeadNode);
            }
            nodePointer = rowHeadNode;
        }
    }

    private void deleteHeadNode(DLXHeader headerNode) {
        DLXNode leftNode = headerNode.getL();
        DLXNode rightNode = headerNode.getR();
        if (leftNode != null) {
            leftNode.setR(rightNode);
        }
        if (rightNode != null) {
            rightNode.setL(leftNode);
        }
    }

    private void recoverHeadNode(DLXHeader headerNode) {
        DLXNode leftNode = headerNode.getL();
        DLXNode rightNode = headerNode.getR();
        if (leftNode != null) {
            leftNode.setR(headerNode);
        }
        if (rightNode != null) {
            rightNode.setL(headerNode);
        }
    }

    private void deleteNode(DLXNode node) {
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

    private void recoverNode(DLXNode node) {
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


}
