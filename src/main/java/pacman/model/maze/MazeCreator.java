/*MazeCreator.java*/
package pacman.model.maze;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import pacman.model.entity.Renderable;
import pacman.model.entity.factory.EntityFactory;
import pacman.model.entity.factory.EntityFactoryImpl;

import static java.lang.System.exit;

public class MazeCreator {

    private final String fileName;
    public static final int RESIZING_FACTOR = 16;
    private final EntityFactory entityFactory;

    public MazeCreator(String fileName) {
        this.fileName = fileName;
        this.entityFactory = new EntityFactoryImpl();
    }

    public Maze createMaze() {
        File f = new File(this.fileName);
        Maze maze = new Maze();

        try {
            Scanner scanner = new Scanner(f);

            int y = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                char[] row = line.toCharArray();

                for (int x = 0; x < row.length; x++) {
                    Renderable entity = entityFactory.createEntity(row[x], x * RESIZING_FACTOR, y * RESIZING_FACTOR);
                    if (entity != null) {
                        maze.addRenderable(entity, row[x], x, y);
                    }
                }

                y += 1;
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("No maze file was found.");
            exit(0);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            exit(0);
        }

        return maze;
    }
}