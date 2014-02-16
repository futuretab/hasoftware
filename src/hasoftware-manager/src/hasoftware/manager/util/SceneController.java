package hasoftware.manager.util;

import javafx.scene.Scene;

public class SceneController {

    private final Scene _scene;
    private final AbstractSceneController _controller;

    public SceneController(Scene scene, AbstractSceneController controller) {
        _scene = scene;
        _controller = controller;
    }

    public Scene getScene() {
        return _scene;
    }

    public AbstractSceneController getController() {
        return _controller;
    }
}
