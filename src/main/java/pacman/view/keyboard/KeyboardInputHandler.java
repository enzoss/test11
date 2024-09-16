/*KeyboardInputHandler.java*/
package pacman.view.keyboard;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import pacman.model.engine.GameEngine;
import pacman.controller.command.*;

public class KeyboardInputHandler {
    private final GameEngine gameEngine;

    public KeyboardInputHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public void handlePressed(KeyEvent keyEvent) {
        KeyCode keyCode = keyEvent.getCode();
        Command command = null;
        switch (keyCode) {
            case LEFT:
                command = new MoveLeftCommand(gameEngine);
                break;
            case RIGHT:
                command = new MoveRightCommand(gameEngine);
                break;
            case DOWN:
                command = new MoveDownCommand(gameEngine);
                break;
            case UP:
                command = new MoveUpCommand(gameEngine);
                break;
        }
        if (command != null) {
            command.execute();
        }
    }
}