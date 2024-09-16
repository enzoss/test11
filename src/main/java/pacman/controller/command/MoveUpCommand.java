/*MoveUpCommand.java*/
package pacman.controller.command;

import pacman.model.engine.GameEngine;

public class MoveUpCommand implements Command {
    private final GameEngine gameEngine;

    public MoveUpCommand(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void execute() {
        gameEngine.moveUp();
    }
}