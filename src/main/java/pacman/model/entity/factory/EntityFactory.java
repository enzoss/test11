/*EntityFactory.java*/
package pacman.model.entity.factory;

import pacman.model.entity.Renderable;

public interface EntityFactory {
    Renderable createEntity(char entityType, int x, int y);
}