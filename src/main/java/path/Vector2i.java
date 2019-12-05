package path;

public class Vector2i {
    private final int x;
    private final int y;

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Vector2i{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
