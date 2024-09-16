/*MoveDownCommand.java*/
package pacman.controller.command;

import pacman.model.engine.GameEngine;

public class MoveDownCommand implements Command {
    private final GameEngine gameEngine;

    public MoveDownCommand(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void execute() {
        gameEngine.moveDown();
    }
}