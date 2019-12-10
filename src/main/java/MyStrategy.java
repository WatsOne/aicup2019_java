import alg.AreaSearcher;
import alg.LineTracer;
import debug.Debug;
import java.util.Collections;
import java.util.List;
import model.ColorFloat;
import model.CustomData;
import model.CustomData.Line;
import model.Game;
import model.Item;
import model.LootBox;
import model.Tile;
import model.Unit;
import model.UnitAction;
import model.Vec2Double;
import model.Vec2Float;
import path.Mover;
import path.PathFinder;
import path.Simulator;
import path.Vector2i;

public class MyStrategy {

    private Simulator simulator;
    private PathFinder pathFinder;
    List<Vector2i> currentPath;
    Mover mover;
    State state;
    AreaSearcher areaSearcher = new AreaSearcher();

    boolean skipJump = false;
    boolean healthExist = true;

    public void simulate(Unit unit, Game game, Debug debug) {
//        simulator.reset(unit, game);
//        for (int i = 0; i < 200; i++) {
//            UnitAction action = getActionTest(simulator.getSimPlayer(), simulator.getSimGame(), debug);
//            System.out.println(">>> tick: " + i + ": " + action.isJump() + " : " + action.getVelocity());
//            simulator.tick(action, debug, true);
//        }
    }

    private byte[][] initGrid(Game game) {
        Tile[][] tiles = game.getLevel().getTiles();
        int x = tiles.length;
        int y = tiles[0].length;
        byte[][] grid = new byte[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                grid[i][j] = tiles[i][j] == Tile.WALL ? (byte) 0 : (byte) 1;
            }
        }

        return grid;
    }

    private List<Vector2i> pathFind(Unit unit, Vector2i target, Game game) {
        Vector2i unitPos = new Vector2i((int) (unit.getPosition().getX() - 0.45), (int) unit.getPosition().getY());
        List<Vector2i> path = pathFinder.find(unitPos, target, 1, 1, (short) 5);
        if (path == null || path.isEmpty()) {
            System.out.println(">>> to pad");
            Vector2i padPos = getNearestPad(unit, game);
            path = pathFinder.find(unitPos, padPos, 1, 1, (short) 5);
        }

        return path;
    }

    private void init(Unit unit, Game game, Debug debug) {
        pathFinder = new PathFinder(initGrid(game), game.getLevel());
        currentPath = pathFind(unit, getNearestLoot(unit, game, Item.Weapon.class), game);
        simulator = new Simulator(game);
        mover = new Mover(currentPath, game);
        areaSearcher.init(unit, game);
        areaSearcher.draw(debug);
    }

    public UnitAction getAction(Unit unit, Game game, Debug debug) {
        System.out.println(">>> tick " + game.getCurrentTick());

        Unit enemy = unit;
        for (Unit u : game.getUnits()) {
            if (u.getId() != unit.getId()) {
                enemy = u;
            }
        }

        if (game.getCurrentTick() == 0) {
            init(unit, game, debug);
            state = State.WEAPON;
        }

        if (game.getCurrentTick() > 0 && game.getCurrentTick() % 70 == 0 && state == State.MOVE) {
            state = State.NOTHING;
        }

        updatePath(unit, game, enemy);
        UnitAction action = new UnitAction();
        if (unit.getJumpState().getMaxTime() > 0 && game.getLevel().isOnPad((int) unit.getPosition().getX(), (int) unit.getPosition().getY())) {
            state = State.NOTHING;
            mover.move(unit, action);
        } else {
            if (mover.move(unit, action)) {
                state = State.NOTHING;
                updatePath(unit, game, enemy);
                mover.move(unit, action);
            }

            if (action.isJump() && game.getCurrentTick() == 0) {
                action.setVelocity(0.0);
                skipJump = true;
            }

            if (skipJump) {
                action.setVelocity(0.0);
                action.setJump(false);
                skipJump = false;
            }
        }

        Vec2Double aim = new Vec2Double(enemy.getPosition().getX() - unit.getPosition().getX(), enemy.getPosition().getY() - unit.getPosition().getY());
        action.setAim(aim);

        if (currentPath != null) {
            for (Vector2i vec : currentPath) {
                debug.draw(new CustomData.Rect(new Vec2Float((float) vec.getX(), (float) vec.getY()), new Vec2Float(1f, 1f), new ColorFloat(0f, 1f, 0f, 0.5f)));
            }
        }

        action.setShoot(iSeeEnemy(enemy, unit, game));
        return action;
    }

    private void updatePath(Unit unit, Game game, Unit enemy) {
        if (unit.getWeapon() == null && state != State.WEAPON) {
            currentPath = pathFind(unit, getNearestLoot(unit, game, Item.Weapon.class), game);
            mover = new Mover(currentPath, game);
            state = State.WEAPON;
            return;
        }

        if (unit.getHealth() < 60 && state != State.HEALTH && healthExist) {
            state = State.HEALTH;
            Vector2i h = getNearestLoot(unit, game, Item.HealthPack.class);
            if (h != null) {
                currentPath = pathFind(unit, h, game);
                mover = new Mover(currentPath, game);
            } else {
                state = State.NOTHING;
                healthExist = false;
            }
            return;
        }

        if (state != State.HEALTH && state != State.WEAPON && state != State.MOVE) {
            currentPath = pathFind(unit, getNearestPosForShooting(game, enemy, areaSearcher), game);
            mover = new Mover(currentPath, game);
            state = State.MOVE;
        }
    }

    private Vector2i getNearestLoot(Unit unit, Game game, Class<? extends Item> loot) {
       Vector2i result = null;
       double minDistance = 9999;

        for (LootBox lootBox : game.getLootBoxes()) {
            if (loot.isInstance(lootBox.getItem())) {
                Vec2Double weaponPos = lootBox.getPosition();
                Vec2Double unitPos = unit.getPosition();
                double distance = Math.abs(unitPos.getX() - weaponPos.getX()) + Math.abs(unitPos.getY() - weaponPos.getY());
                if (distance < minDistance) {
                    result = new Vector2i((int) weaponPos.getX(), (int) weaponPos.getY());
                    minDistance = distance;
                }
            }
        }
        return result;
    }

    private Vector2i getNearestPad(Unit unit, Game game) {
        Vector2i result = null;
        double minDistance = 9999;

        for (Vector2i pos : game.getLevel().pads) {
            Vec2Double unitPos = unit.getPosition();
            double distance = Math.abs(unitPos.getX() - pos.getX()) + Math.abs(unitPos.getY() - pos.getY());
            if (distance < minDistance) {
                result = new Vector2i(pos.getX(), pos.getY() + 2);
                minDistance = distance;
            }
        }

        return result;
    }

    private Vector2i getNearestPosForShooting(Game game, Unit enemy, AreaSearcher areaSearcher) {
        double enemyX = enemy.getPosition().getX();
        double enemyY = enemy.getPosition().getY();

        Vector2i result = null;
        double max = 0;

        for (Vector2i a : areaSearcher.getAreas()) {
            if (LineTracer.canTrace(game, a.getX(), a.getY(), (int) enemyX, (int) enemyY) || LineTracer.canTrace(game, a.getX(), a.getY(), (int) enemyX, (int) (enemyY + 2))) {
                double distance = Math.abs(a.getX() - enemyX) + Math.abs(a.getY() - enemyY);
                if (distance > max) {
                    result = a;
                    max = distance;
                }
            }
        }

        return result;
    }

    private boolean iSeeEnemy(Unit enemy, Unit unit, Game game) {
        return LineTracer.canTrace(game, (int)unit.getPosition().getX(), (int)(unit.getPosition().getY()+0.9), (int) enemy.getPosition().getX(), (int) enemy.getPosition().getY()) || LineTracer.canTrace(game, (int)unit.getPosition().getX(), (int)(unit.getPosition().getY()+0.9), (int) enemy.getPosition().getX(), (int) (enemy.getPosition().getY() + 2));
    }
}
