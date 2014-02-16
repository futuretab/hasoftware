package hasoftware.manager.util;

import de.jensd.fx.fontawesome.AwesomeIcon;
import hasoftware.api.DeviceType;
import hasoftware.api.classes.Location;
import hasoftware.api.classes.Point;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LocationFormatCell<T> extends TreeCell<LocationPoint> {

    private static final String FONT_AWESOME = "FontAwesome";

    private final GridPane _grid = new GridPane();
    private final Label _icon = new Label();
    private final Label _name = new Label();

    public LocationFormatCell() {
        _grid.setHgap(2);
        _grid.setVgap(2);
        _grid.setPadding(new Insets(0, 1, 0, 1));
        _icon.setPrefWidth(14);
        _icon.setFont(Font.font(FONT_AWESOME, FontWeight.BOLD, 14));
        _grid.add(_icon, 0, 0);
        _grid.add(_name, 1, 0);
    }

    @Override
    protected void updateItem(LocationPoint locationPoint, boolean empty) {
        super.updateItem(locationPoint, empty);
        if (empty) {
            clearContent();
        } else {
            addContent(locationPoint);
        }
    }

    private void addContent(LocationPoint locationPoint) {
        setText(null);
        if (locationPoint.isLocation()) {
            Location location = locationPoint.getLocation();
            _icon.setText(AwesomeIcon.FOLDER_ALT.toString());
            _icon.setTextFill(Color.GRAY);
            _name.setText(location.getName());
        } else {
            Point point = locationPoint.getPoint();
            if (point.getDeviceTypeCode().equals(DeviceType.TEMP.getCode())) {
                _icon.setText(AwesomeIcon.TINT.toString());
                _icon.setTextFill(Color.GREEN);
            } else if (point.getDeviceTypeCode().equals(DeviceType.SENSOR.getCode())) {
                _icon.setText(AwesomeIcon.FLASH.toString());
                _icon.setTextFill(Color.RED);
            } else {
                _icon.setText(AwesomeIcon.SIGN_IN.toString());
                _icon.setTextFill(Color.BLUE);
            }
            _name.setText(point.getName());
        }
        setGraphic(_grid);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }
}
