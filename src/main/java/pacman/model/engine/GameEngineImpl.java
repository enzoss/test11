/*GameEngineImpl.java*/
package pacman.model.engine;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pacman.model.entity.Renderable;
import pacman.model.level.Level;
import pacman.model.level.LevelImpl;
import pacman.model.maze.Maze;
import pacman.model.maze.MazeCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of GameEngine - responsible for coordinating the Pac-Man model
 */
public class GameEngineImpl implements GameEngine {

    private int score = 0;
    private boolean gameOver = false;
    private boolean levelComplete = false;
    private boolean readyToStart = true;

    private Level currentLevel;
    private int numLevels;
    private int currentLevelNo; 
    private Maze maze;
    private JSONArray levelConfigs;
    public GameEngineImpl(String configPath) {
        this.currentLevelNo = 0;
        init(new GameConfigurationReader(configPath));
    }

    private void init(GameConfigurationReader gameConfigurationReader) {
        // Set up map
        String mapFile = gameConfigurationReader.getMapFile();
        MazeCreator mazeCreator = new MazeCreator(mapFile);
        this.maze = mazeCreator.createMaze();
        this.maze.setNumLives(gameConfigurationReader.getNumLives());

        // Get level configurations
        this.levelConfigs = gameConfigurationReader.getLevelConfigs();
        this.numLevels = levelConfigs.size();
        if (levelConfigs.isEmpty()) {
            System.exit(0);
        }
    }

    @Override
    public List<Renderable> getRenderables() {
        return this.currentLevel.getRenderables();
    }

    @Override
    public void moveUp() {
        currentLevel.moveUp();
    }

    @Override
    public void moveDown() {
        currentLevel.moveDown();
    }

    @Override
    public void moveLeft() {
        currentLevel.moveLeft();
    }

    @Override
    public void moveRight() {
        currentLevel.moveRight();
    }

    @Override
    public void startGame() {
        readyToStart = false;
        startLevel();
    }

    private void startLevel() {
        JSONObject levelConfig = (JSONObject) levelConfigs.get(currentLevelNo);
        maze.reset();
        this.currentLevel = new LevelImpl(levelConfig, maze);
        score = 0;
        levelComplete = false;
        gameOver = false;
        readyToStart = true;
        tickCount = 0;
    }

    private List<GameObserver> observers = new ArrayList<>();

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }
    
    private void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.update(this);
        }
    }

    @Override
    public void tick() {
        if (!gameOver && !levelComplete) {
            if (readyToStart) {
                if (tickCount > 100) { 
                    readyToStart = false;
                }
                tickCount++;
            } else {
                currentLevel.tick();
                score = currentLevel.getScore();
                
                if (currentLevel.isLevelFinished()) {
                    if (currentLevelNo < numLevels - 1) {
                        levelComplete = true;
                        currentLevelNo++;
                        startLevel(); 
                    } else {
                        gameOver = true;
                    }
                }

                if (currentLevel.isGameOver()) {
                    gameOver = true;
                }
            }
        }
        notifyObservers();
    }

    private int tickCount = 0;

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public int getLives() {
        return maze.getNumLives();
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public boolean isLevelComplete() {
        return levelComplete;
    }

    @Override
    public boolean isReadyToStart() {
        return readyToStart;
    }

}

