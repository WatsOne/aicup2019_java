package path;

import java.util.Collections;
import java.util.List;
import model.Unit;
import model.UnitAction;

public class Mover {
    private List<Vector2i> path;
    private int mCurrentNodeId;
    private Double currentSpeed;

    public Mover(List<Vector2i> path) {
        this.path = path;
        Collections.reverse(path);
        mCurrentNodeId = 1;
    }

    public void move(Unit unit, UnitAction action) {
        if (reached(unit)) {
            mCurrentNodeId++;
            currentSpeed = null;
        }
        Vector2i target = path.get(mCurrentNodeId);
        int deltaY = (int) Math.round(target.getY() - unit.getPosition().getY());
        if (currentSpeed == null) {
            if (reachY(unit)) {
                currentSpeed = target.getX() < (unit.getPosition().getX() - 0.45) ? -10.0 : 10.0;
            } else {
                int deltaX = (int) (target.getX() - (unit.getPosition().getX() - 0.45));
                currentSpeed = deltaX * 10 / (double) Math.abs(deltaY);
            }
        }

        action.setVelocity(currentSpeed);
        if (reachY(unit) || deltaY < 0) {
            action.setJump(false);
        } else {
            action.setJump(true);
        }
    }

    private boolean reached(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        return Math.abs(unit.getPosition().getX() - 0.45 - target.getX()) < 0.1 && reachY(unit);
    }

    private boolean reachY(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        return Math.abs(unit.getPosition().getY() - target.getY()) < 0.1;
    }
}
