package path;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import model.Game;
import model.Unit;
import model.UnitAction;

public class Mover {
    private List<Vector2i> path;
    private int mCurrentNodeId;
    private Double currentSpeed;
    private Simulator simulator;
    private Game game;

    public Mover(List<Vector2i> path, Game game) {
        this.path = path;
        Collections.reverse(path);
        mCurrentNodeId = 1;
        simulator = new Simulator(game);
        this.game = game;
        filterPathForPads();
    }

    private void filterPathForPads() {
        for (Vector2i p : path) {
            if (game.getLevel().isLadderFake(p.getX() + 1, p.getY())) {
                p.setX(p.getX() + 1);
            }
            if (game.getLevel().isLadderFake(p.getX() - 1, p.getY())) {
                p.setX(p.getX() - 1);
            }
            if (game.getLevel().isPad(p.getX(), p.getY())) {
                p.setY(p.getY() + 1);
            }
        }
    }

    public boolean move(Unit unit, UnitAction action) {
        if (mCurrentNodeId == path.size()) {
            action.setVelocity(0.0);
            return true;
        }

        boolean resetJump = false;

        Vector2i target = path.get(mCurrentNodeId);
        double deltaY = target.getY() - unit.getPosition().getY();

        if (reached(unit)) {
            Vector2i currentReached = path.get(mCurrentNodeId);
            Vector2i prevReached = path.get(mCurrentNodeId - 1);
            boolean onPlatform = game.getLevel().isPlatform(currentReached.getX(), currentReached.getY() - 1);

            boolean onFakeLadder = game.getLevel().isLadderFake(currentReached.getX(), currentReached.getY());
            boolean onFakeLadderPrev = game.getLevel().isLadderFake(prevReached.getX(), prevReached.getY());
            boolean resetForPad = onFakeLadder && !onFakeLadderPrev;

            if (onPlatform || resetForPad) {
                resetJump = true;
            }
            if (resetForPad) {
                if (game.getLevel().isPad(target.getX(), target.getY() - 1)) {
                    path.add(mCurrentNodeId + 1, new Vector2i(target.getX(), target.getY()));
                } else {
                    path.add(mCurrentNodeId, new Vector2i(target.getX(), target.getY() - 1));
                    path.add(mCurrentNodeId + 1, new Vector2i(target.getX(), target.getY()));
                }
            }

            mCurrentNodeId++;
            currentSpeed = null;

            if (mCurrentNodeId == path.size()) {
                action.setVelocity(0.0);
                return true;
            }

            target = path.get(mCurrentNodeId);
            deltaY = target.getY() - unit.getPosition().getY();
        }

        if (currentSpeed == null) {
            currentSpeed = getSpeed(deltaY, target, unit);

            if (!reachYSim(unit) && deltaY > 0) {
                simulator.reset(unit);
                action.setVelocity(currentSpeed);
                int simTicks = 0;
                while (!reachYSim(simulator.getSimPlayer()) && simTicks < 100) {
                    double deltaSimY = target.getY() - simulator.getSimPlayer().getPosition().getY();
                    action.setJump(getJump(simulator.getSimPlayer(), deltaSimY));
                    simulator.tick(action, null, false);
                    simTicks++;
                }
                double deltaSimX = target.getX() - (simulator.getSimPlayer().getPosition().getX() - 0.45);
                if (Math.abs(deltaSimX) > 0.1) {
                    currentSpeed = currentSpeed < 0 ? -10.0 : 10.0;
                }
            }
        }

        boolean reachX = reachX(unit);

        action.setVelocity(reachX ? 0.0 : currentSpeed);

        if (resetJump) {
            action.setJump(false);
        } else {
            action.setJump(getJump(unit, deltaY));
        }

        action.setJumpDown(reachX && deltaY < 0);
        return false;
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
        return !reachYSim(unit) && deltaY > 0;
    }

    private boolean reached(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        boolean reachX = Math.abs(unit.getPosition().getX() - 0.45 - target.getX()) < 0.1;

        if (game.getLevel().isLadderFake(target.getX(), target.getY())) {
            return reachX && reachYExtended(unit);
        }

        if (game.getLevel().isGround(target.getX(), target.getY())) {
            return reachX && reachY(unit);
        } else {
            return reachX && reachYSim(unit);
        }
    }

    private boolean reachY(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        double deltaY = unit.getPosition().getY() - target.getY();
        return deltaY >= 0.0 && deltaY < 0.08;
    }

    private boolean reachYSim(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        return Math.abs(unit.getPosition().getY() - target.getY()) < 0.1;
    }

    private boolean reachYExtended(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        return Math.abs(unit.getPosition().getY() - target.getY()) < 0.2;
    }

    private boolean reachX(Unit unit) {
        Vector2i target = path.get(mCurrentNodeId);
        return Math.abs(unit.getPosition().getX() - target.getX() - 0.45) < 0.08;
    }
}
