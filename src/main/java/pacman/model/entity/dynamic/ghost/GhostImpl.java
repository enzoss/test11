/*GhostImpl.java*/
package pacman.model.entity.dynamic.ghost;

import javafx.scene.image.Image;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.physics.*;
import pacman.model.level.Level;
import pacman.model.maze.Maze;
import java.util.*;
import java.util.logging.Logger;

/**
 * Concrete implemention of Ghost entity in Pac-Man Game
 */
public class GhostImpl implements Ghost {

    private final Layer layer = Layer.FOREGROUND;
    private final Image image;
    private final BoundingBox boundingBox;
    private final Vector2D startingPosition;
    private final Vector2D targetCorner;
    private KinematicState kinematicState;
    private GhostMode ghostMode;
    private Vector2D targetLocation;
    private Direction currentDirection;
    private Set<Direction> possibleDirections;
    private Vector2D playerPosition;
    private Map<GhostMode, Double> speeds;
    private static final Logger LOGGER = Logger.getLogger(GhostImpl.class.getName());


    public GhostImpl(Image image, BoundingBox boundingBox, KinematicState kinematicState, GhostMode ghostMode, Vector2D targetCorner, Direction currentDirection) {
        this.image = image;
        this.boundingBox = boundingBox;
        this.kinematicState = kinematicState;
        this.startingPosition = kinematicState.getPosition();
        this.ghostMode = ghostMode;
        this.currentDirection = currentDirection;
        this.possibleDirections = new HashSet<>();
        this.targetCorner = targetCorner;
        this.targetLocation = getTargetLocation();
    }

    @Override
    public void setSpeeds(Map<GhostMode, Double> speeds) {
        this.speeds = speeds;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public void update() {
        this.updateDirection();
        this.kinematicState.update();
        this.boundingBox.setTopLeft(this.kinematicState.getPosition());
    }

    public void setPlayerPosition(Vector2D playerPosition) {
        this.playerPosition = playerPosition;
    }
    
    private void updateDirection() {
        Set<Direction> availableDirections = new HashSet<>(Arrays.asList(Direction.values()));
        System.out.println("All possible directions: " + availableDirections);
        
        availableDirections.retainAll(possibleDirections);
        
        System.out.println("Available directions after filtering: " + availableDirections);

        this.targetLocation = getTargetLocation();
        Direction newDirection = selectDirection(availableDirections);
        
 
        Vector2D newPosition = this.kinematicState.getPotentialPosition(newDirection);
        if (newPosition.equals(this.getPosition())) {
            System.out.println("Hit a wall, force changing direction");
            availableDirections.remove(newDirection);
            if (!availableDirections.isEmpty()) {
                newDirection = selectDirection(availableDirections);
            }
        }

        if (newDirection != null) {
            this.currentDirection = newDirection;
        }

        System.out.println("Final direction: " + this.currentDirection);

        switch (this.currentDirection) {
            case LEFT -> this.kinematicState.left();
            case RIGHT -> this.kinematicState.right();
            case UP -> this.kinematicState.up();
            case DOWN -> this.kinematicState.down();
        }
    }

    private Vector2D getTargetLocation() {
        return switch (this.ghostMode) {
            case CHASE -> this.playerPosition != null ? this.playerPosition : this.targetCorner;
            case SCATTER -> this.targetCorner;
        };
    }

    private Direction selectDirection(Set<Direction> availableDirections) {
        if (availableDirections.isEmpty()) {
            System.out.println("No available directions, keeping current direction: " + currentDirection);
            return currentDirection;
        }

        Map<Direction, Double> distances = new HashMap<>();

        for (Direction direction : availableDirections) {
            Vector2D potentialPosition = this.kinematicState.getPotentialPosition(direction);
            double distance = Vector2D.calculateEuclideanDistance(potentialPosition, this.targetLocation);
            distances.put(direction, distance);
            System.out.println("Direction: " + direction + ", Potential Position: " + potentialPosition + ", Distance: " + distance);
        }

        Direction chosenDirection = Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
        System.out.println("Chosen direction: " + chosenDirection);
        
        return chosenDirection;
    }

    @Override
    public void setGhostMode(GhostMode ghostMode) {
        this.ghostMode = ghostMode;
        this.kinematicState.setSpeed(speeds.get(ghostMode));
    }

    @Override
    public boolean collidesWith(Renderable renderable) {
        return boundingBox.collidesWith(kinematicState.getDirection(), renderable.getBoundingBox());
    }

    @Override
    public void collideWith(Level level, Renderable renderable) {
        if (level.isPlayer(renderable)) {
            level.handleLoseLife();
        }
    }

    @Override
    public Vector2D getPositionBeforeLastUpdate() {
        return this.kinematicState.getPreviousPosition();
    }

    @Override
    public double getHeight() {
        return this.boundingBox.getHeight();
    }

    @Override
    public double getWidth() {
        return this.boundingBox.getWidth();
    }

    @Override
    public Vector2D getPosition() {
        return this.kinematicState.getPosition();
    }

    @Override
    public void setPosition(Vector2D position) {
        this.kinematicState.setPosition(position);
    }

    @Override
    public Layer getLayer() {
        return this.layer;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public void reset() {
        // return ghost to starting position
        this.kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                .setPosition(startingPosition)
                .build();
    }

    @Override
    public void setPossibleDirections(Set<Direction> possibleDirections) {
        this.possibleDirections = possibleDirections;
    }

    @Override
    public Direction getDirection() {
        return this.kinematicState.getDirection();
    }

    @Override
    public Vector2D getCenter() {
        return new Vector2D(boundingBox.getMiddleX(), boundingBox.getMiddleY());
    }
}
