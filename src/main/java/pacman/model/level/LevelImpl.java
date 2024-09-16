/*LevelImpl.java*/
package pacman.model.level;

import org.json.simple.JSONObject;
import pacman.ConfigurationParseException;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.DynamicEntity;
import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.ghost.GhostImpl;
import pacman.model.entity.dynamic.ghost.GhostMode;
import pacman.model.entity.dynamic.physics.PhysicsEngine;
import pacman.model.entity.dynamic.physics.Vector2D;
import pacman.model.entity.dynamic.player.Controllable;
import pacman.model.entity.dynamic.player.Pacman;
import pacman.model.entity.staticentity.StaticEntity;
import pacman.model.entity.staticentity.collectable.Collectable;
import pacman.model.maze.Maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Concrete implement of Pac-Man level
 */
public class LevelImpl implements Level {
    private int score = 0;
    private static final int START_LEVEL_TIME = 200;
    private final Maze maze;
    private List<Renderable> renderables;
    private Controllable player;
    private List<Ghost> ghosts;
    private int tickCount;
    private Map<GhostMode, Integer> modeLengths;
    private int numLives;
    private List<Renderable> collectables;
    private GhostMode currentGhostMode;
    private boolean gameOver = false;


    public LevelImpl(JSONObject levelConfiguration,
                     Maze maze) {
        this.renderables = new ArrayList<>();
        this.maze = maze;
        this.tickCount = 0;
        this.modeLengths = new HashMap<>();
        this.currentGhostMode = GhostMode.SCATTER;

        initLevel(new LevelConfigurationReader(levelConfiguration));
    }

    private void initLevel(LevelConfigurationReader levelConfigurationReader) {
        // Fetch all renderables for the level
        this.renderables = maze.getRenderables();

        // Set up player
        if (!(maze.getControllable() instanceof Controllable)) {
            throw new ConfigurationParseException("Player entity is not controllable");
        }
        this.player = (Controllable) maze.getControllable();
        this.player.setSpeed(levelConfigurationReader.getPlayerSpeed());
        setNumLives(maze.getNumLives());

        // Set up ghosts
        this.ghosts = maze.getGhosts().stream()
                .map(element -> (Ghost) element)
                .collect(Collectors.toList());
        Map<GhostMode, Double> ghostSpeeds = levelConfigurationReader.getGhostSpeeds();

        for (Ghost ghost : this.ghosts) {
            ghost.setSpeeds(ghostSpeeds);
            ghost.setGhostMode(this.currentGhostMode);
        }
        this.modeLengths = levelConfigurationReader.getGhostModeLengths();

        // Set up collectables
        this.collectables = new ArrayList<>(maze.getPellets());

    }

    private void updatePlayerPosition() {
        Vector2D playerPos = player.getPosition();
        for (Ghost ghost : ghosts) {
            if (ghost instanceof GhostImpl) {
                ((GhostImpl) ghost).setPlayerPosition(playerPos);
            }
        }
    }

    @Override
    public boolean isGameOver() {
        return gameOver || getNumLives() <= 0;
    }

    @Override
    public List<Renderable> getRenderables() {
        return this.renderables;
    }

    private List<DynamicEntity> getDynamicEntities() {
        return renderables.stream().filter(e -> e instanceof DynamicEntity).map(e -> (DynamicEntity) e).collect(
                Collectors.toList());
    }

    private List<StaticEntity> getStaticEntities() {
        return renderables.stream().filter(e -> e instanceof StaticEntity).map(e -> (StaticEntity) e).collect(
                Collectors.toList());
    }

    @Override
    public void tick() {
        
        if (tickCount == modeLengths.get(currentGhostMode)) {
            this.currentGhostMode = GhostMode.getNextGhostMode(currentGhostMode);
            for (Ghost ghost : this.ghosts) {
                ghost.setGhostMode(this.currentGhostMode);
            }
            tickCount = 0;
        }
    
        if (tickCount % Pacman.PACMAN_IMAGE_SWAP_TICK_COUNT == 0) {
            this.player.switchImage();
        }
        
        List<DynamicEntity> dynamicEntities = getDynamicEntities();

        Vector2D pacmanPosition = player.getPosition();
        
        for (DynamicEntity dynamicEntity : dynamicEntities) {
            maze.updatePossibleDirections(dynamicEntity);
            
            if (dynamicEntity instanceof Ghost) {
                Ghost ghost = (Ghost) dynamicEntity;
                ghost.setPlayerPosition(pacmanPosition);
            }
            
            dynamicEntity.update();
            
            for (StaticEntity wall : getStaticEntities()) {
                if (dynamicEntity.collidesWith(wall) && !wall.canPassThrough()) {
                    PhysicsEngine.resolveCollision(dynamicEntity, wall);
                }
            }
            
            if (dynamicEntity == player) {
                checkCollectables((Pacman)dynamicEntity);
            }
        }
        
        handleCollisions(dynamicEntities);
        updatePlayerPosition();
        tickCount++;
    }

    private void checkCollectables(Pacman pacman) {
        List<Renderable> toRemove = new ArrayList<>();
        for (Renderable collectable : collectables) {
            if (pacman.collidesWith(collectable)) {
                if (collectable instanceof Collectable) {
                    collect((Collectable) collectable);
                    toRemove.add(collectable);
                }
            }
        }
        collectables.removeAll(toRemove);
        renderables.removeAll(toRemove);
    }

    private void handleCollisions(List<DynamicEntity> dynamicEntities) {
        for (int i = 0; i < dynamicEntities.size(); ++i) {
            DynamicEntity entityA = dynamicEntities.get(i);
            
            for (int j = i + 1; j < dynamicEntities.size(); ++j) {
                DynamicEntity entityB = dynamicEntities.get(j);
                
                if (entityA.collidesWith(entityB)) {
                    if ((isPlayer(entityA) && entityB instanceof Ghost) ||
                        (isPlayer(entityB) && entityA instanceof Ghost)) {
                        handlePacmanGhostCollision();
                    }
                }
            }
        }
    }
    
    private void handlePacmanGhostCollision() {
        numLives--;
        maze.setNumLives(numLives);  
        if (numLives <= 0) {
            //handleGameEnd();
        } else {
            resetEntities();
        }
    }
    
    private void resetEntities() {
        player.reset();
        for (Ghost ghost : ghosts) {
            ghost.reset();
        }
        maze.updatePossibleDirections(player);
        for (Ghost ghost : ghosts) {
            maze.updatePossibleDirections(ghost);
        }
    }


    @Override
    public boolean isPlayer(Renderable renderable) {
        return renderable == this.player;
    }

    @Override
    public boolean isCollectable(Renderable renderable) {
        return maze.getPellets().contains(renderable) && ((Collectable) renderable).isCollectable();
    }

    @Override
    public void moveLeft() {
        player.left();
    }

    @Override
    public void moveRight() {
        player.right();
    }

    @Override
    public void moveUp() {
        player.up();
    }

    @Override
    public void moveDown() {
        player.down();
    }

    @Override
    public boolean isLevelFinished() {
        return collectables.isEmpty();
    }

    @Override
    public int getNumLives() {
        return this.numLives;
    }

    private void setNumLives(int numLives) {
        this.numLives = numLives;
    }

    @Override
    public void handleLoseLife() {
        numLives--;
        if (numLives <= 0) {
            gameOver = true;
        } else {
            resetEntities();
        }
    }

    @Override
    public void collect(Collectable collectable) {
        score += collectable.getPoints();
        collectable.collect();
    }

    @Override
    public int getScore() {
        return score;
    }


}
