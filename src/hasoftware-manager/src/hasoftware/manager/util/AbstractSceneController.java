package hasoftware.manager.util;

import hasoftware.util.AbstractController;
import java.util.List;
import javafx.fxml.Initializable;

public abstract class AbstractSceneController extends AbstractController implements Initializable, ISceneController {

    @Override
    public boolean startUp() {
        return true;
    }

    @Override
    public boolean readyToShutDown() {
        return true;
    }

    @Override
    public boolean shutDown() {
        return true;
    }

    public abstract void addFunctionCodes(List<Integer> functionCodeList);
}
