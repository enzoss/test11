/*Ghost.java*/
package pacman.model.entity.dynamic.ghost;

import pacman.model.entity.dynamic.DynamicEntity;
import pacman.model.entity.dynamic.physics.Vector2D;

import java.util.Map;

/**
 * Represents Ghost entity in Pac-Man Game
 */
public interface Ghost extends DynamicEntity {

    /***
     * Sets the speeds of the Ghost for each GhostMode
     * @param speeds speeds of the Ghost for each GhostMode
     */
    void setSpeeds(Map<GhostMode, Double> speeds);

    /**
     * Sets the mode of the Ghost used to calculate target position
     * @param ghostMode mode of the Ghost
     */
    void setGhostMode(GhostMode ghostMode);
    /**
     * Sets the current position of the player (Pac-Man)
     * @param playerPosition current position of the player
     */
    void setPlayerPosition(Vector2D playerPosition);
}