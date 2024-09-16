/*MoveLeftCommand.java*/
package pacman.controller.command;

import pacman.model.engine.GameEngine;

public class MoveLeftCommand implements Command {
    private final GameEngine gameEngine;

    public MoveLeftCommand(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void execute() {
        gameEngine.moveLeft();
    }
}