package hasoftware.manager.view;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.Location;
import hasoftware.api.classes.Point;
import hasoftware.api.messages.LocationRequest;
import hasoftware.api.messages.LocationResponse;
import hasoftware.api.messages.PointRequest;
import hasoftware.api.messages.PointResponse;
import hasoftware.cdef.CDEFAction;
import hasoftware.manager.util.AbstractSceneController;
import hasoftware.manager.util.LocationFormatCell;
import hasoftware.manager.util.LocationPoint;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.OutstandingRequest;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class SiteController extends AbstractSceneController {

    @FXML
    private TreeView<LocationPoint> treeSite;

    @FXML
    private TextField textName;

    @FXML
    private TextField textAddress;

    @FXML
    private TextField textMessage1;

    @FXML
    private TextField textMessage2;

    @FXML
    private Label labelCreatedOn;

    @FXML
    private Label labelUpdatedOn;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private LinkedList<OutstandingRequest<TreeItem<LocationPoint>>> _requests;
    private LinkedBlockingQueue<Event> _eventQueue;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Callback<TreeView<LocationPoint>, TreeCell<LocationPoint>> _locationCellFactory = (TreeView<LocationPoint> p) -> new LocationFormatCell();
        treeSite.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeSite.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TreeItem<LocationPoint>> obj, TreeItem<LocationPoint> o, TreeItem<LocationPoint> n) -> {
                    onSiteItemChanged(obj, o, n);
                });
        treeSite.setCellFactory(_locationCellFactory);

        textName.setText("");
        labelCreatedOn.setText("");
        labelUpdatedOn.setText("");

        _requests = new LinkedList<>();
    }

    @Override
    public void addFunctionCodes(List<Integer> functionCodeList) {
        // TODO
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        _eventQueue = eventQueue;
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        switch (event.getType()) {
            case ReceiveMessage:
                Message message = event.getMessage();
                if (message.getFunctionCode() == FunctionCode.Notify) {
                    //handleNotifyResponse((NotifyResponse) message);
                } else if (message.getFunctionCode() == FunctionCode.Location) {
                    handleLocationResponse((LocationResponse) message);
                } else if (message.getFunctionCode() == FunctionCode.Point) {
                    handlePointResponse((PointResponse) message);
                }
                break;
        }
        return true;
    }

    @Override
    public void onShown() {
        treeSite.setRoot(null);
        LocationRequest message = new LocationRequest(CDEFAction.List);
        message.setParentId(0);
        Event event = new Event(EventType.SendMessage, message);
        _requests.add(new OutstandingRequest(message.getTransactionNumber(), null));
        _eventQueue.add(event);
    }

    private void handlePointResponse(final PointResponse pointResponse) {
        if (!pointResponse.isError()) {
            int transactionNumber = pointResponse.getTransactionNumber();
            for (OutstandingRequest<TreeItem<LocationPoint>> request : _requests) {
                if (request.transactionNumber == transactionNumber) {
                    final TreeItem<LocationPoint> treeItem = request.data;
                    Platform.runLater(() -> {
                        for (Point point : pointResponse.getPoints()) {
                            LazyLocationPointItem item = new LazyLocationPointItem(new LocationPoint(null, point), 0);
                            if (treeItem == null) {
                                treeSite.setRoot(item);
                            } else {
                                treeItem.getChildren().add(item);
                            }
                        }
                    });
                    _requests.remove(request);
                    break;
                }
            }
        }
    }

    private void handleLocationResponse(final LocationResponse locationResponse) {
        if (!locationResponse.isError()) {
            int transactionNumber = locationResponse.getTransactionNumber();
            for (OutstandingRequest<TreeItem<LocationPoint>> request : _requests) {
                if (request.transactionNumber == transactionNumber) {
                    final TreeItem<LocationPoint> treeItem = request.data;
                    Platform.runLater(() -> {
                        for (Location location : locationResponse.getLocations()) {
                            LazyLocationPointItem item = new LazyLocationPointItem(new LocationPoint(location, null), 0);
                            if (treeItem == null) {
                                treeSite.setRoot(item);
                            } else {
                                treeItem.getChildren().add(item);
                            }
                        }
                    });
                    _requests.remove(request);
                    break;
                }
            }
        }
    }

    private void onSiteItemChanged(ObservableValue<? extends TreeItem<LocationPoint>> obj, TreeItem<LocationPoint> o, TreeItem<LocationPoint> n) {
        if (n == null) {
            textName.setText("");
            textAddress.setText("");
            textMessage1.setText("");
            textMessage2.setText("");
            labelCreatedOn.setText("");
            labelUpdatedOn.setText("");
        } else {
            LocationPoint locationPoint = n.getValue();
            if (locationPoint.isLocation()) {
                Location location = locationPoint.getLocation();
                textName.setText(location.getName());
                textAddress.setText("");
                textMessage1.setText("");
                textMessage2.setText("");
                labelCreatedOn.setText(dateFormat.format(new Date(location.getCreatedOn().getTimeUTC())));
                labelUpdatedOn.setText(dateFormat.format(new Date(location.getUpdatedOn().getTimeUTC())));
            } else {
                Point point = locationPoint.getPoint();
                textName.setText(point.getName());
                textAddress.setText(point.getAddress());
                textMessage1.setText(point.getMessage1());
                textMessage2.setText(point.getMessage2());
                labelCreatedOn.setText(dateFormat.format(new Date(point.getCreatedOn().getTimeUTC())));
                labelUpdatedOn.setText(dateFormat.format(new Date(point.getUpdatedOn().getTimeUTC())));
            }
        }
    }

    class LazyLocationPointItem extends TreeItem<LocationPoint> {

        private int _depth;
        private boolean _loaded;

        public LazyLocationPointItem(LocationPoint locationPoint, int depth) {
            super(locationPoint);
            _depth = depth;
            _loaded = false;
        }

        public int getDepth() {
            return _depth;
        }

        @Override
        public ObservableList<TreeItem<LocationPoint>> getChildren() {
            if (!_loaded) {
                load();
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            if (!_loaded) {
                load();
            }
            return super.getChildren().isEmpty();
        }

        private void load() {
            _loaded = true;
            LocationPoint locationPoint = getValue();
            if (locationPoint.isLocation()) {
                // Ask for all the child locations
                {
                    LocationRequest message = new LocationRequest(CDEFAction.List);
                    message.setParentId(locationPoint.getLocation().getId());
                    Event event = new Event(EventType.SendMessage, message);
                    _requests.add(new OutstandingRequest(message.getTransactionNumber(), this));
                    _eventQueue.add(event);
                }
                // Ask for all the child points
                {
                    PointRequest message = new PointRequest(CDEFAction.List);
                    message.setNodeId(locationPoint.getLocation().getId());
                    Event event = new Event(EventType.SendMessage, message);
                    _requests.add(new OutstandingRequest(message.getTransactionNumber(), this));
                    _eventQueue.add(event);
                }
            }
        }
    }

    //class OutstandingRequest {
//
    //       public final int transactionNumber;
    //      public final TreeItem<LocationPoint> treeItem;
//
    //      public OutstandingRequest(int transactionNumber, TreeItem<LocationPoint> treeItem) {
    //        this.transactionNumber = transactionNumber;
    //      this.treeItem = treeItem;
    //}
    //}
}
