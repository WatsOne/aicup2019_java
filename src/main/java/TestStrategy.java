import model.Game;
import model.Unit;
import model.UnitAction;
import model.Vec2Double;

public class TestStrategy {

    private Simulator simulator = new Simulator();

    public void simulate(Unit unit, Game game, Debug debug) {
        simulator.reset(unit, game);
        for (int i = 0; i < 200; i++) {
            simulator.tick(getAction(simulator.getSimPlayer(), simulator.getSimGame(), debug), debug, true);
        }
    }

    public UnitAction getAction(Unit unit, Game game, Debug debug) {
        UnitAction action = new UnitAction();
        action.setVelocity(-10.0);
        action.setAim(new Vec2Double(10,10));

        if (game.getCurrentTick() > 10) {
            action.setJump(true);
        }

        return action;
    }
}
