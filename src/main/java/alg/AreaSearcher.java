package alg;

import debug.Debug;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import model.ColorFloat;
import model.CustomData;
import model.Game;
import model.Tile;
import model.Unit;
import model.Vec2Float;
import path.Vector2i;

public class AreaSearcher {
    private List<Vector2i> areas = new ArrayList<>();

    public void init(Unit unit, Game game) {
        Queue<Vector2i> q = new LinkedBlockingQueue<>();
        Set<Vector2i> visited = new HashSet<>();
        Tile[][] tiles = game.getLevel().getTiles();

        Vector2i first = new Vector2i((int) unit.getPosition().getX(), (int) unit.getPosition().getY());

        q.add(first);
        visited.add(first);

        while (!q.isEmpty()) {
            Vector2i current = q.poll();
            Vector2i up = new Vector2i(current.getX(), current.getY() + 1);
            if (tiles[up.getX()][up.getY()] != Tile.WALL && tiles[up.getX()][up.getY()] != Tile.ON_PAD && !visited.contains(up)) {
                q.add(up);
                visited.add(up);
            }
            Vector2i down = new Vector2i(current.getX(), current.getY() - 1);
            if (tiles[down.getX()][down.getY()] != Tile.WALL && tiles[up.getX()][up.getY()] != Tile.ON_PAD && !visited.contains(down)) {
                q.add(down);
                visited.add(down);
            }
            Vector2i right = new Vector2i(current.getX() - 1, current.getY());
            if (tiles[right.getX()][right.getY()] != Tile.WALL && tiles[up.getX()][up.getY()] != Tile.ON_PAD && !visited.contains(right)) {
                q.add(right);
                visited.add(right);
            }
            Vector2i left = new Vector2i(current.getX() + 1, current.getY());
            if (tiles[left.getX()][left.getY()] != Tile.WALL && tiles[up.getX()][up.getY()] != Tile.ON_PAD && !visited.contains(left)) {
                q.add(left);
                visited.add(left);
            }
        }

        for (Vector2i v : visited) {
            Tile tile = tiles[v.getX()][v.getY()];
            Tile downTile = tiles[v.getX()][v.getY() - 1];
            if (tile == Tile.EMPTY && (downTile == Tile.EMPTY || downTile == Tile.JUMP_PAD) || tile == Tile.PLATFORM) {
                continue;
            }
            areas.add(v);
        }
    }

    public void draw(Debug debug) {
        areas.forEach(a -> {
            debug.draw(new CustomData.Rect(new Vec2Float((float)a.getX(), (float) a.getY()), new Vec2Float(1f,1f), new ColorFloat(1f, 1f, 1f, 0.3f)));
        });
    }

    public List<Vector2i> getAreas() {
        return areas;
    }
}
