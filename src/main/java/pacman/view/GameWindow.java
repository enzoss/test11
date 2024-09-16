/*GameWindow.java*/
package pacman.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import pacman.model.engine.GameEngine;
import pacman.model.engine.GameObserver;
import pacman.model.entity.Renderable;
import pacman.view.background.BackgroundDrawer;
import pacman.view.background.StandardBackgroundDrawer;
import pacman.view.entity.EntityView;
import pacman.view.entity.EntityViewImpl;
import pacman.view.keyboard.KeyboardInputHandler;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for managing the Pac-Man Game View
 */
public class GameWindow implements GameObserver {
    public static final File FONT_FILE = new File("src/main/resources/maze/PressStart2P-Regular.ttf");
    private final Scene scene;
    private final Pane pane;
    private final GameEngine model;
    private final List<EntityView> entityViews;
    private Text scoreText;
    private Text livesText;
    private Text messageText;

    public GameWindow(GameEngine model, int width, int height) {
        this.model = model;
        model.addObserver(this);

        pane = new Pane();
        scene = new Scene(pane, width, height);

        entityViews = new ArrayList<>();

        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(model);
        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);

        BackgroundDrawer backgroundDrawer = new StandardBackgroundDrawer();
        backgroundDrawer.draw(model, pane);

        initializeUIElements();
    }

    private void initializeUIElements() {
        try {
            scoreText = new Text("Score: 0");
            scoreText.setFont(Font.loadFont(new FileInputStream(FONT_FILE), 20));
            scoreText.setFill(Color.WHITE);
            scoreText.setX(10);
            scoreText.setY(20);

            livesText = new Text("Lives: 3");
            livesText.setFont(Font.loadFont(new FileInputStream(FONT_FILE), 20));
            livesText.setFill(Color.WHITE);
            livesText.setX(300);
            livesText.setY(20);

            messageText = new Text("");
            messageText.setFont(Font.loadFont(new FileInputStream(FONT_FILE), 30));
            messageText.setFill(Color.YELLOW);
            messageText.setX(150);
            messageText.setY(300);

            pane.getChildren().addAll(scoreText, livesText, messageText);
        } catch (IOException e) {
            System.err.println("Error loading font: " + e.getMessage());
        }
    }

    @Override
    public void update(GameEngine gameEngine) {
        scoreText.setText("Score: " + gameEngine.getScore());
        livesText.setText("Lives: " + gameEngine.getLives());

        if (gameEngine.isGameOver()) {
            showMessage("GAME OVER");
        } else if (gameEngine.isLevelComplete()) {
            showMessage("YOU WIN!");
        } else if (gameEngine.isReadyToStart()) {
            showMessage("READY!");
        } else {
            hideMessage();
        }
    }

    private void showMessage(String message) {
        messageText.setText(message);
        messageText.setVisible(true);
    }

    private void hideMessage() {
        messageText.setVisible(false);
    }

    public Scene getScene() {
        return scene;
    }

    public void run() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(34),
                t -> this.draw()));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        model.startGame();
    }


    private void draw() {
        model.tick();

        List<Renderable> entities = model.getRenderables();

        for (EntityView entityView : entityViews) {
            entityView.markForDelete();
        }

        for (Renderable entity : entities) {
            boolean notFound = true;
            for (EntityView view : entityViews) {
                if (view.matchesEntity(entity)) {
                    notFound = false;
                    view.update();
                    break;
                }
            }
            if (notFound) {
                EntityView entityView = new EntityViewImpl(entity);
                entityViews.add(entityView);
                pane.getChildren().add(entityView.getNode());
            }
        }

        for (EntityView entityView : entityViews) {
            if (entityView.isMarkedForDelete()) {
                pane.getChildren().remove(entityView.getNode());
            }
        }

        entityViews.removeIf(EntityView::isMarkedForDelete);
    }
}
