import model.ColorFloat;
import model.CustomData;
import model.Game;
import model.Tile;
import model.Unit;
import model.UnitAction;
import model.Vec2Double;
import model.Vec2Float;
import path.PathFinder;
import path.Vector2i;

public class TestStrategy {

    private Simulator simulator = new Simulator();
    private PathFinder pathFinder;
    byte[][] grid;

    public void simulate(Unit unit, Game game, Debug debug) {
//        simulator.reset(unit, game);
//        for (int i = 0; i < 200; i++) {
//            simulator.tick(getAction(simulator.getSimPlayer(), simulator.getSimGame(), debug), debug, true);
//        }
    }

    public void pathFind(Game game, Debug debug) {
        if (pathFinder == null || grid == null) {
            Tile[][] tiles = game.getLevel().getTiles();
            int x = tiles.length;
            int y = tiles[0].length;
            grid = new byte[x][y];
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    grid[i][j] = tiles[i][j] == Tile.WALL ? (byte) 0 : (byte) 1;
                }
            }
        }

        pathFinder = new PathFinder(grid, game.getLevel());
        for (Vector2i vec : pathFinder.find(new Vector2i(37, 1), new Vector2i(4,8), 1, 1, (short) 5)) {
            debug.draw(new CustomData.Rect(new Vec2Float((float) vec.getX(), (float) vec.getY()), new Vec2Float(1f, 1f), new ColorFloat(0f, 1f, 0f, 0.5f)));
        }
    }

    public UnitAction getAction(Unit unit, Game game, Debug debug) {
        debug.draw(new CustomData.Rect(new Vec2Float(37f, 1f), new Vec2Float(1f, 1f), new ColorFloat(1f, 0f, 0f, 0.5f)));
        debug.draw(new CustomData.Rect(new Vec2Float(26f, 5f), new Vec2Float(1f, 1f), new ColorFloat(1f, 0f, 0f, 0.5f)));
        UnitAction action = new UnitAction();
        action.setVelocity(-10.0);
        action.setAim(new Vec2Double(10,10));

//        if (game.getCurrentTick() > 10) {
            action.setJump(true);
//        }

        return action;
    }
}
