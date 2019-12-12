package alg;

import model.Game;
import model.Tile;
import path.Vector2i;

public class LineTracer {
    //Алгоритм Брезенхэма
    public static Vector2i canTrace(Game game, int x1, int y1, int x2, int y2) {
        Tile[][] tiles = game.getLevel().getTiles();

        int d = 0;
        int dy = Math.abs(y2 - y1);
        int dx = Math.abs(x2 - x1);
        int dy2 = dy << 1;
        int dx2 = dx << 1;
        int ix = x1 < x2 ? 1 : -1;
        int iy = y1 < y2 ? 1 : -1;
        int xx = x1;
        int yy = y1;

        if (dy <= dx) {
            while (true) {
                if (tiles[xx][yy] == Tile.WALL) {
                    return new Vector2i(xx, yy);
                }
                if (xx == x2) {
                    break;
                }
                xx += ix;
                d  += dy2;
                if (d > dx) {
                    yy += iy;
                    d  -= dx2;
                }
            }
        }
        else {
            while (true) {
                if (tiles[xx][yy] == Tile.WALL) {
                    return new Vector2i(xx, yy);
                }
                if (yy == y2) {
                    break;
                }
                yy += iy;
                d  += dx2;
                if (d > dy) {
                    xx += ix;
                    d  -= dy2;
                }
            }
        }

        return null;
    }
}
