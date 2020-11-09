package castle.comp3021.assignment.gui.controllers;

import castle.comp3021.assignment.protocol.exception.ResourceNotFoundException;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class for loading resources from the filesystem.
 */
public class ResourceLoader {
    /**
     * Path to the resources directory.
     */
    @NotNull
    private static final Path RES_PATH;

    static {
        // TODO: Initialize RES_PATH
        // replace null to the actual path
        RES_PATH = Paths.get("src","main","resources");
    }

    /**
     * Retrieves a resource file from the resource directory.
     *
     * @param relativePath Path to the resource file, relative to the root of the resource directory.
     * @return Absolute path to the resource file.
     * @throws ResourceNotFoundException If the file cannot be found under the resource directory.
     */
    @NotNull
    public static String getResource(@NotNull final String relativePath) {
        // TODO
        Path p = RES_PATH.resolve(relativePath);
        if(!Files.exists(p))
            throw new ResourceNotFoundException("resource not found");
        return p.toAbsolutePath().toString();
    }

    /**
     * Return an image {@link Image} object
     * @param typeChar a character represents the type of image needed.
     *                 - 'K': white knight (whiteK.png)
     *                 - 'A': white archer (whiteA.png)
     *                 - 'k': black knight (blackK.png)
     *                 - 'a': black archer (blackA.png)
     *                 - 'c': central x (center.png)
     *                 - 'l': light board (lightBoard.png)
     *                 - 'd': dark board (darkBoard.png)
     * @return an image
     */
    @NotNull
    public static Image getImage(char typeChar) {
        // TODO
        switch (typeChar) {
            case 'K':return new Image(getResource("assets/images/whiteK.png"),true);
            case 'A':return new Image(getResource("assets/images/whiteA.png"),true);
            case 'a':return new Image(getResource("assets/images/blackA.png"),true);
            case 'k':return new Image(getResource("assets/images/blackK.png"),true);
            case 'c':return new Image(getResource("assets/images/center.png"),true);
            case 'l':return new Image(getResource("assets/images/lightBoard.png"),true);
            case 'd':return new Image(getResource("assets/images/darkBoard.png"),true);
        }
        return null;
    }


}