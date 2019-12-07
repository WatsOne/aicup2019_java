package path;

import debug.Debug;
import model.ColorFloat;
import model.CustomData.Line;
import model.Game;
import model.Tile;
import model.Unit;
import model.UnitAction;
import model.Vec2Float;

public class Simulator {
    private final double yDeltaPath = 10.0 / 60;
    private final double yDeltaTime = 1.0 / 60;

    private Unit simPlayer;
    //private Game simGame;
    private Tile[][] tiles;

    public Simulator(Game game) {
        tiles = game.getLevel().getTiles();
    }

    public void reset(Unit unit) {
        simPlayer = unit.clone();
        //simGame = Game.clone(game);
    }

    public void tick(UnitAction action, Debug debug, boolean draw) {
        double path = action.getVelocity() / 60;

        double xBefore = simPlayer.getPosition().getX();
        double yBefore = simPlayer.getPosition().getY();

        double x = simPlayer.getPosition().getX();
        double y = simPlayer.getPosition().getY();

        if (action.getVelocity() > 0) {
            //чекаем ПРАВЫЙ верхний и нижний угол, надо посмотреть что следующие тайлы стена или не стена
            int xInt = (int)(x + 0.45 + path);
            if (tiles[xInt][(int)y] == Tile.WALL || tiles[xInt][(int)(y + 1.8)] == Tile.WALL) {
                simPlayer.getPosition().setX(xInt - 0.45);
            } else {
                simPlayer.getPosition().incX(path);
            }
        } else {
            //чекаем ЛЕВЫЙ верхний и нижний угол, надо посмотреть что следующие тайлы стена или не стена
            int xInt = (int)(x - 0.45 + path);
            if (tiles[xInt][(int)y] == Tile.WALL || tiles[xInt][(int)(y + 1.8)] == Tile.WALL) {
                simPlayer.getPosition().setX(xInt + 1.45);
            } else {
                simPlayer.getPosition().incX(path);
            }
        }

        x = simPlayer.getPosition().getX();

        int yIntDown = (int)(y - yDeltaPath);
        boolean onGround = tiles[(int)(x + 0.45)][yIntDown] != Tile.EMPTY || tiles[(int)(x - 0.45)][yIntDown] != Tile.EMPTY;

        if (onGround) {
            simPlayer.getJumpState().setMaxTime(0.55);
        }

        if (action.isJump() && (onGround || simPlayer.getJumpState().getMaxTime() > 0)) {
            //летим вверх
            int yInt = (int)(y + 1.8 + yDeltaPath);
            if (tiles[(int)(x + 0.45)][yInt] == Tile.WALL || tiles[(int)(x - 0.45)][yInt] == Tile.WALL) {
                simPlayer.getPosition().setY(yInt - 1.8);
                simPlayer.getJumpState().setCanJump(false);
                simPlayer.getJumpState().setMaxTime(0);
            } else {
                simPlayer.getPosition().incY(yDeltaPath);
                simPlayer.getJumpState().decMaxTime(yDeltaTime);
            }
        } else {
            //падаем
            if (onGround) {
                simPlayer.getPosition().setY(yIntDown + 1.0);
                simPlayer.getJumpState().setCanJump(false);
            } else {
                simPlayer.getPosition().incY(-yDeltaPath);
            }
        }

        if (draw) {
            debug.draw(new Line(new Vec2Float((float)xBefore, (float)yBefore),
                    new Vec2Float((float)x, (float)simPlayer.getPosition().getY()),
                    0.2f,
                    new ColorFloat(1.0f, 0.0f, 0.0f, 1.0f)));
        }

        //simGame.incCurrentTick();
    }

    public Unit getSimPlayer() {
        return simPlayer;
    }

//    public Game getSimGame() {
//        return simGame;
//    }
}
