import alg.AreaSearcher;
import alg.LineTracer;
import debug.Debug;
import java.util.List;
import model.ColorFloat;
import model.CustomData;
import model.Game;
import model.Item;
import model.LootBox;
import model.Tile;
import model.Unit;
import model.UnitAction;
import model.Vec2Double;
import model.Vec2Float;
import model.WeaponType;
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
//        System.out.println(">>> tick " + game.getCurrentTick());

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

        //Vec2Double aim = new Vec2Double(Math.atan2(enemy.getPosition().getY() - unit.getPosition().getY(), enemy.getPosition().getX() - unit.getPosition().getX()));
        Vec2Double aim = new Vec2Double(enemy.getPosition().getX() - unit.getPosition().getX(), enemy.getPosition().getY() - unit.getPosition().getY());
        action.setAim(aim);

        if (currentPath != null) {
            for (Vector2i vec : currentPath) {
                debug.draw(new CustomData.Rect(new Vec2Float((float) vec.getX(), (float) vec.getY()), new Vec2Float(1f, 1f), new ColorFloat(0f, 1f, 0f, 0.5f)));
            }
        }

        action.setShoot(iSeeEnemy(enemy, unit, game, debug, action.getVelocity() < 0));
//
//        debug.draw(new CustomData.Line(new Vec2Float((float)unit.getPosition().getX(), (float)unit.getPosition().getY() + 0.9f),
//                new Vec2Float((float)unit.getPosition().getX() + (float)(enemy.getPosition().getX() - unit.getPosition().getX()),
//                        (float)unit.getPosition().getY() + 0.9f + (float)(enemy.getPosition().getY() - unit.getPosition().getY())), 0.2f,
//                new ColorFloat(1.0f, 0.0f, 0.0f, 0.8f)));
//
//        if (unit.getWeapon() != null) {
//            double atan = Math.atan2(enemy.getPosition().getY() - unit.getPosition().getY(), enemy.getPosition().getX() - unit.getPosition().getX());
//            double atan2 = Math.atan2(enemy.getPosition().getY() + 2 - unit.getPosition().getY(), enemy.getPosition().getX() - unit.getPosition().getX());
//            debug.draw(new CustomData.Log("Spread1: " + unit.getWeapon().getSpread() + "a:" + atan));
//            debug.draw(new CustomData.Log("a2: " + atan2));
//            double atanMax = atan + unit.getWeapon().getSpread();
//
//            float deltaY = (float)(enemy.getPosition().getY() - unit.getPosition().getY());
//            float deltaX = (float)(enemy.getPosition().getX() - unit.getPosition().getX());
////            debug.draw(new CustomData.Log("x: " + deltaX));
////            debug.draw(new CustomData.Log("x: " + Math.cos(atanMax)));
////            debug.draw(new CustomData.Log("x2: " + (deltaX * (float)Math.cos(atanMax) - deltaY * (float) Math.sin(atanMax))));
//            debug.draw(new CustomData.Log("y: " + deltaY));
//            debug.draw(new CustomData.Log("y2: " + (deltaX * (float) Math.sin(unit.getWeapon().getSpread()) + deltaY * (float)Math.cos(unit.getWeapon().getSpread()))));
//            debug.draw(new CustomData.Log("y2: " + (deltaX * (float) Math.sin(unit.getWeapon().getSpread()) + deltaY * (float)Math.cos(unit.getWeapon().getSpread()))));
//
//
//
////            deltaX * (float)Math.cos(atanMax) - deltaY * (float) Math.sin(atanMax)
////            deltaX * (float) Math.sin(atanMax) + deltaY * (float)Math.cos(atanMax)
//            double spread = unit.getWeapon().getSpread();
//            debug.draw(new CustomData.Line(new Vec2Float((float)unit.getPosition().getX(), (float)unit.getPosition().getY() + 0.9f),
//                    new Vec2Float((float)unit.getPosition().getX()  + (deltaX * (float)Math.cos(spread) - deltaY * (float) Math.sin(spread)),
//                            (float)unit.getPosition().getY() + 0.9f + (deltaX * (float) Math.sin(spread) + deltaY * (float)Math.cos(spread))), 0.2f,
//                    new ColorFloat(0.0f, 1.0f, 0.0f, 0.8f)));
//
//            debug.draw(new CustomData.Line(new Vec2Float((float)unit.getPosition().getX(), (float)unit.getPosition().getY() + 0.9f),
//                    new Vec2Float((float)unit.getPosition().getX()  + (deltaX * (float)Math.cos(-spread) - deltaY * (float) Math.sin(-spread)),
//                            (float)unit.getPosition().getY() + 0.9f + (deltaX * (float) Math.sin(-spread) + deltaY * (float)Math.cos(-spread))), 0.2f,
//                    new ColorFloat(0.0f, 1.0f, 0.0f, 0.8f)));
//
//
//            debug.draw(new CustomData.Line(new Vec2Float((float)(int)unit.getPosition().getX(), (float)(int)(unit.getPosition().getY() + 1)),
//                            new Vec2Float((float)(int)enemy.getPosition().getX(), (float)(int)(enemy.getPosition().getY())),0.2f,
//                    new ColorFloat(0.0f, 0.0f, 1.0f, 0.8f)));
//
//            debug.draw(new CustomData.Line(new Vec2Float((float)(int)unit.getPosition().getX(), (float)(int)(unit.getPosition().getY()+1)),
//                    new Vec2Float((float)(int)enemy.getPosition().getX(), (float)(int)(enemy.getPosition().getY() + 2)),0.2f,
//                    new ColorFloat(0.0f, 0.0f, 1.0f, 0.8f)));
//        }
//
//        debug.draw(new CustomData.Line(new Vec2Float((float)(int)unit.getPosition().getX(), (float)(int)(unit.getPosition().getY() + 1)),
//                        new Vec2Float((float)(int)enemy.getPosition().getX(), (float)(int)(enemy.getPosition().getY())),0.2f,
//                new ColorFloat(0.0f, 0.0f, 1.0f, 0.8f)));
//
//        debug.draw(new CustomData.Line(new Vec2Float((float)(int)unit.getPosition().getX(), (float)(int)(unit.getPosition().getY()+1)),
//                new Vec2Float((float)(int)enemy.getPosition().getX(), (float)(int)(enemy.getPosition().getY() + 2)),0.2f,
//                new ColorFloat(0.0f, 0.0f, 1.0f, 0.8f)));

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
            if (LineTracer.canTrace(game, a.getX(), a.getY() + 1, (int) enemyX, (int) enemyY) == null && LineTracer.canTrace(game, a.getX() + 1, a.getY(), (int) enemyX, (int) (enemyY + 2)) == null) {
                double distance = Math.abs(a.getX() - enemyX) + Math.abs(a.getY() - enemyY);
                if (distance > max) {
                    result = a;
                    max = distance;
                }
            }
        }

        return result;
    }

    private boolean iSeeEnemy(Unit enemy, Unit unit, Game game, Debug debug, boolean isLeft) {
        if (unit.getWeapon() != null && unit.getWeapon().typ == WeaponType.ROCKET_LAUNCHER) {
            double spread = unit.getWeapon().getSpread();
            Vec2Double unitPosition = unit.getPosition();
            Vec2Double enemyPosition = enemy.getPosition();
            double deltaX = enemyPosition.getX() - unitPosition.getX();
            double deltaY = enemyPosition.getY() - unitPosition.getY();
            double spreadCos = Math.cos(spread);
            double spreadSin = Math.sin(spread);
            double newUpperX = deltaX * spreadCos - deltaY * spreadSin;
            double newUpperY = deltaX * spreadSin + deltaY * spreadCos;
            double spreadCosMinus = Math.cos(-spread);
            double spreadSinMinus = Math.sin(-spread);
            double newDownX = deltaX * spreadCosMinus - deltaY * spreadSinMinus;
            double newDownY = deltaX * spreadSinMinus + deltaY * spreadCosMinus;

//            debug.draw(new CustomData.Line(new Vec2Float((float)(int)(unit.getPosition().getX() + (isLeft ? 1 : 0)), (float)(int)(unit.getPosition().getY() + 1)),
//                    new Vec2Float((float)(int)(unit.getPosition().getX() + newUpperX), (float)(int)(unit.getPosition().getY() + 0.9 + newUpperY)),
//                    0.2f, new ColorFloat(1.0f, 0.0f, 0.0f, 0.8f)));
//
//            debug.draw(new CustomData.Line(new Vec2Float((float)(int)(unit.getPosition().getX() + (isLeft ? 1 : 0)), (float)(int)(unit.getPosition().getY() + 1)),
//                    new Vec2Float((float)(int)(unit.getPosition().getX() + newDownX), (float)(int)(unit.getPosition().getY() + 0.9 + newDownY)),
//                    0.2f, new ColorFloat(1.0f, 0.0f, 0.0f, 0.8f)));

            Vector2i upper = LineTracer.canTrace(game, (int) (unit.getPosition().getX() + (isLeft ? 1 : 0)), (int) (unit.getPosition().getY() + 1),
                    (int) (unit.getPosition().getX() + newUpperX), (int) (unit.getPosition().getY() + 0.9 + newUpperY));

            if (upper != null && distance(unitPosition, upper) < 3) {
                return false;
            }

            Vector2i down = LineTracer.canTrace(game, (int) (unit.getPosition().getX() + (isLeft ? 1 : 0)), (int) (unit.getPosition().getY() + 1),
                    (int) (unit.getPosition().getX() + newDownX), (int) (unit.getPosition().getY() + 0.9 + newDownY));

            if (down != null && distance(unitPosition, down) < 3) {
                return false;
            }
        }

        return LineTracer.canTrace(game, (int)unit.getPosition().getX(), (int)(unit.getPosition().getY()+1), (int) enemy.getPosition().getX(), (int) enemy.getPosition().getY()) == null && LineTracer.canTrace(game, (int)unit.getPosition().getX(), (int)(unit.getPosition().getY()+1), (int) enemy.getPosition().getX(), (int) (enemy.getPosition().getY() + 2)) == null;
    }

    private double distance(Vec2Double a, Vector2i b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
