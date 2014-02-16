package hasoftware.manager.util;

import hasoftware.api.classes.TimeUTC;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.scene.control.TableCell;

public class TimeUTCFormatCell<T>
        extends TableCell<T, TimeUTC> {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public TimeUTCFormatCell() {
    }

    @Override
    protected void updateItem(TimeUTC timeUTC, boolean empty) {
        super.updateItem(timeUTC, empty);
        if (!empty && null == timeUTC) {
            setText("<n/a>");
        } else if (timeUTC == null) {
            setText("");
        } else {
            setText(dateFormat.format(new Date(timeUTC.getTimeUTC())));
        }
    }
}
