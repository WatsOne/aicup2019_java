package path;

import java.util.Comparator;
import java.util.List;

public class ComparePFNodeMatrix implements Comparator<Location> {
    private List<PathFinderNodeFast>[] mMatrix;

    public ComparePFNodeMatrix(List<PathFinderNodeFast>[] mMatrix) {
        this.mMatrix = mMatrix;
    }

    @Override
    public int compare(Location l1, Location l2) {
        if (mMatrix[l1.getXy()].get(l1.getZ()).getF() > mMatrix[l2.getXy()].get(l2.getZ()).getF())
            return 1;
        else if (mMatrix[l1.getXy()].get(l1.getZ()).getF() < mMatrix[l2.getXy()].get(l2.getZ()).getF())
            return -1;
        return 0;
    }
}
