package solver;

public class DLXHeader extends DLXNode {

    private DLXHeader L;

    private DLXHeader R;

    private int S; // size

    private String N;  // name

    public DLXHeader() {
        super();
        S = 0;
    }

    public void setL(DLXHeader l) {
        L = l;
    }

    public void setR(DLXHeader r) {
        R = r;
    }

    @Override
    public DLXHeader getL() {
        return L;
    }

    @Override
    public DLXHeader getR() {
        return R;
    }

    public int getS() {
        return S;
    }

    public void incrementS() {
        S++;
    }

    public void decrementS() {
        S--;
    }

    public String getN() {
        return N;
    }

    public void setN(String n) {
        N = n;
    }
}
