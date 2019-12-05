package path;

public class Location {
    private final int xy;
    private final int z;

    public Location(int xy, int z) {
        this.xy = xy;
        this.z = z;
    }

    public int getXy() {
        return xy;
    }

    public int getZ() {
        return z;
    }
}
