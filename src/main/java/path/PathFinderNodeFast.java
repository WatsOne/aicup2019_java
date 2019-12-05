package path;

public class PathFinderNodeFast {
    private int f;
    private int g;
    private short px;
    private short py;
    private byte status;
    private byte pz;
    private short jumpLength;

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public short getPx() {
        return px;
    }

    public void setPx(short px) {
        this.px = px;
    }

    public short getPy() {
        return py;
    }

    public void setPy(short py) {
        this.py = py;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getPz() {
        return pz;
    }

    public void setPz(byte pz) {
        this.pz = pz;
    }

    public short getJumpLength() {
        return jumpLength;
    }

    public void setJumpLength(short jumpLength) {
        this.jumpLength = jumpLength;
    }
}
