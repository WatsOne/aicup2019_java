package path;

import java.util.Collections;
import java.util.List;
import model.Game;
import model.Unit;
import model.UnitAction;

public class Mover {
    private List<Vector2i> path;
    private int mCurrentNodeId;
    private Double currentSpeed;
    private Simulator simulator;

    public Mover(List<Vector2i> path, Game game) {
        this.path = path;
        Collections.reverse(path);
        mCurrentNodeId = 1;
        simulator = new Simulator(game);
    }

    public void move(Unit unit, UnitAction action) {
        if (reached(unit)) {
            mCurrentNodeId++;
            currentSpeed = null;
        }
        Vector2i target = path.get(mCurrentNodeId);
        double deltaY = target.getY() - unit.getPosition().getY();
        if (currentSpeed == null) {
            currentSpeed = getSpeed(deltaY, target, unit);

            if (!reachY(unit)) {
                simulator.reset(unit);
                action.setVelocity(currentSpeed);
                while (!reachY(simulator.getSimPlayer())) {
                    double deltaSimY = target.getY() - simulator.getSimPlayer().getPosition().getY();
                    action.setJump(getJump(simulator.getSimPlayer(), deltaSimY));
                    simulator.tick(action, null, false);
                }
                double deltaSimX = target.getX() - (simulator.getSimPlayer().getPosition().getX() - 0.45);
                if (Math.abs(deltaSimX) > 0.05) {
                    currentSpeed = currentSpeed < 0 ? -10.0 : 10.0;
                }
            }
        }
        action.setVelocity(reachX(unit) ? 0.0 : currentSpeed);
        action.setJump(getJump(unit, deltaY));
    }

    private double getSpeed(double deltaY, Vector2i target, Unit unit) {
        if (reachY(unit)) {
            return target.getX() < (unit.getPosition().getX() - 0.45) ? -10.0 : 10.0;
        } else {
            double deltaX = target.getX() - (unit.getPosition().getX() - 0.45);
            return deltaX * 10 / Math.abs(deltaY);
        }
    }

    private boolean getJump(Unit unit, double deltaY) {
        return !reachY(unit) && deltaY > 0;
    }

    private boolean reached(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        return Math.abs(unit.getPosition().getX() - 0.45 - target.getX()) < 0.1 && reachY(unit);
    }

    private boolean reachY(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        return Math.abs(unit.getPosition().getY() - target.getY()) < 0.1;
    }

    private boolean reachX(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        return Math.abs(unit.getPosition().getX() - target.getX() - 0.45) < 0.05;
    }
}
