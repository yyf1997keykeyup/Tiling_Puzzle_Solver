package solver;

public class DLXNode {

    private DLXNode L;  // left

    private DLXNode R;  // right

    private DLXNode U;  // up

    private DLXNode D;  // down

    private DLXHeader C; // column

    public DLXNode() {
    }

    public DLXNode getL() {
        return L;
    }

    public void setL(DLXNode l) {
        L = l;
    }

    public DLXNode getR() {
        return R;
    }

    public void setR(DLXNode r) {
        R = r;
    }

    public DLXNode getU() {
        return U;
    }

    public void setU(DLXNode u) {
        U = u;
    }

    public DLXNode getD() {
        return D;
    }

    public void setD(DLXNode d) {
        D = d;
    }

    public DLXHeader getC() {
        return C;
    }

    public void setC(DLXHeader c) {
        C = c;
    }
}
