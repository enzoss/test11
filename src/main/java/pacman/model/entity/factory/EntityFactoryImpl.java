/*EntityFactoryImpl.java*/
package pacman.model.entity.factory;

import javafx.scene.image.Image;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.ghost.GhostImpl;
import pacman.model.entity.dynamic.player.Pacman;
import pacman.model.entity.dynamic.physics.*;
import pacman.model.entity.staticentity.StaticEntity;
import pacman.model.entity.staticentity.StaticEntityImpl;
import pacman.model.entity.staticentity.collectable.Pellet;
import pacman.model.maze.RenderableType;
import pacman.model.entity.staticentity.collectable.Collectable;
import pacman.model.level.Level;
import pacman.model.entity.dynamic.player.PacmanVisual;
import pacman.model.entity.dynamic.ghost.GhostMode;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EntityFactoryImpl implements EntityFactory {

    private static final String RESOURCE_PATH = "/maze/";
    private final Random random = new Random();

    @Override
    public Renderable createEntity(char entityType, int x, int y) {
        switch (entityType) {
            case RenderableType.HORIZONTAL_WALL:
                return createWall("horizontal.png", x, y);
            case RenderableType.VERTICAL_WALL:
                return createWall("vertical.png", x, y);
            case RenderableType.UP_LEFT_WALL:
                return createWall("upLeft.png", x, y);
            case RenderableType.UP_RIGHT_WALL:
                return createWall("upRight.png", x, y);
            case RenderableType.DOWN_LEFT_WALL:
                return createWall("downLeft.png", x, y);
            case RenderableType.DOWN_RIGHT_WALL:
                return createWall("downRight.png", x, y);
            case RenderableType.PELLET:
                return createPellet(x, y);
            case RenderableType.PACMAN:
                return createPacman(x, y);
            case RenderableType.GHOST:
                return createGhost(x, y);
            default:
                return null;
        }
    }

    private StaticEntity createWall(String imageName, int x, int y) {
        Image image = new Image(RESOURCE_PATH + "walls/" + imageName);
        BoundingBox boundingBox = new BoundingBoxImpl(new Vector2D(x, y), image.getHeight(), image.getWidth());
        return new StaticEntityImpl(boundingBox, Renderable.Layer.FOREGROUND, image);
    }

    private Pellet createPellet(int x, int y) {
        Image image = new Image(RESOURCE_PATH + "pellet.png");
        BoundingBox boundingBox = new BoundingBoxImpl(new Vector2D(x, y), image.getHeight(), image.getWidth());
        return new Pellet(boundingBox, Renderable.Layer.FOREGROUND, image, 10);
    }

    private Pacman createPacman(int x, int y) {
        Map<PacmanVisual, Image> images = new HashMap<>();
        images.put(PacmanVisual.UP, new Image(RESOURCE_PATH + "pacman/playerUp.png"));
        images.put(PacmanVisual.DOWN, new Image(RESOURCE_PATH + "pacman/playerDown.png"));
        images.put(PacmanVisual.LEFT, new Image(RESOURCE_PATH + "pacman/playerLeft.png"));
        images.put(PacmanVisual.RIGHT, new Image(RESOURCE_PATH + "pacman/playerRight.png"));
        images.put(PacmanVisual.CLOSED, new Image(RESOURCE_PATH + "pacman/playerClosed.png"));

        Image currentImage = images.get(PacmanVisual.LEFT);
        BoundingBox boundingBox = new BoundingBoxImpl(new Vector2D(x, y), currentImage.getHeight(), currentImage.getWidth());
        KinematicState kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                .setPosition(new Vector2D(x, y))
                .setDirection(Direction.LEFT)
                .build();

        return new Pacman(currentImage, images, boundingBox, kinematicState);
    }

    private Ghost createGhost(int x, int y) {
        Image image = new Image(RESOURCE_PATH + "ghosts/ghost.png");
        BoundingBox boundingBox = new BoundingBoxImpl(new Vector2D(x, y), image.getHeight(), image.getWidth());
        KinematicState kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                .setPosition(new Vector2D(x, y))
                .setDirection(getRandomDirection())
                .build();
        Vector2D targetCorner = getRandomCorner();
        return new GhostImpl(image, boundingBox, kinematicState, GhostMode.SCATTER, targetCorner, getRandomDirection());
    }

    private Direction getRandomDirection() {
        return Direction.values()[random.nextInt(Direction.values().length)];
    }

    private Vector2D getRandomCorner() {
        int corner = random.nextInt(4);
        switch (corner) {
            case 0: return new Vector2D(0, 0);
            case 1: return new Vector2D(448, 0);
            case 2: return new Vector2D(0, 576);
            default: return new Vector2D(448, 576);
        }
    }
}