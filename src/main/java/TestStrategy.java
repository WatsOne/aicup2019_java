import java.util.List;
import model.ColorFloat;
import model.CustomData;
import model.Game;
import model.Tile;
import model.Unit;
import model.UnitAction;
import model.Vec2Double;
import model.Vec2Float;
import path.Mover;
import path.PathFinder;
import path.Vector2i;

public class TestStrategy {

    private Simulator simulator = new Simulator();
    private PathFinder pathFinder;
    List<Vector2i> currentPath;
    Mover mover;

    public void simulate(Unit unit, Game game, Debug debug) {
//        simulator.reset(unit, game);
//        for (int i = 0; i < 200; i++) {
//            simulator.tick(getAction(simulator.getSimPlayer(), simulator.getSimGame(), debug), debug, true);
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

    private List<Vector2i> pathFind() {
        return pathFinder.find(new Vector2i(37, 1), new Vector2i(4,8), 1, 2, (short) 5);
    }

    private void init(Game game) {
        pathFinder = new PathFinder(initGrid(game), game.getLevel());
        currentPath = pathFind();
        mover = new Mover(currentPath);
    }

    public UnitAction getAction(Unit unit, Game game, Debug debug) {
        if (game.getCurrentTick() == 0) {
            init(game);
        }

        for (Vector2i vec : currentPath) {
            debug.draw(new CustomData.Rect(new Vec2Float((float) vec.getX(), (float) vec.getY()), new Vec2Float(1f, 1f), new ColorFloat(0f, 1f, 0f, 0.5f)));
        }

        UnitAction action = new UnitAction();
        action.setAim(new Vec2Double(10,10));
        mover.move(unit, action);
        return action;
    }
}
