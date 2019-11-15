package solver;

import java.util.Stack;

public class DLXLevel {
    private DLXHeader header;
    private Stack<DLXNode> usedStack;
    private Stack<DLXNode> candidateStack;

    public DLXLevel(DLXHeader header) {
        this.header = header;
        this.usedStack = new Stack<>();
        this.candidateStack = new Stack<>();
    }

    public DLXHeader getHeader() {
        return header;
    }

    public Stack<DLXNode> getUsedStack() {
        return usedStack;
    }

    public Stack<DLXNode> getCandidateStack() {
        return candidateStack;
    }
}