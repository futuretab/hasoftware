package hasoftware.server.data;

import java.util.LinkedList;
import java.util.List;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

public class DataManager {

    private static DataManager _instance;

    public static DataManager instance() {
        if (_instance == null) {
            _instance = new DataManager();
        }
        return _instance;
    }

    private ObjectContext _context;
    private Node _root;

    private DataManager() {
    }

    public boolean initialize() {
        ServerRuntime cayenneRuntime = new ServerRuntime("cayenne-project.xml");
        _context = cayenneRuntime.getContext();

        // Create root node if required
        {
            List<Node> rootNodes = getNodeByParent(null);
            _root = rootNodes.isEmpty() ? createNode("Site", null) : rootNodes.get(0);
        }
        return true;
    }

    public void close() {
    }

    // -------------------------------------------------------------------------
    // USERS
    // -------------------------------------------------------------------------
    public boolean doesUserHavePermission(User user, String permissionName) {
        // TODO We really want to do something like this
        // SELECT count(*)
        // FROM auser
        //    INNER JOIN users_to_groups ON (auser.id = users_to_groups.user_fk)
        //    INNER JOIN agroup ON (users_to_groups.group_fk = agroup.id)
        //    INNER JOIN groups_to_permissions ON (agroup.id = groups_to_permissions.group_fk)
        //    INNER JOIN permission ON (groups_to_permissions.permission_fk = permission.id)
        // WHERE auser.username = 'nascocan' AND permission.name = 'CREATE INPUT EVENT';
        for (Group group : user.getGroups()) {
            for (Permission permission : group.getPermissions()) {
                if (permission.getName().equals(permissionName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public User getUserByUsernamePassword(String username, String password) {
        Expression where = ExpressionFactory.matchExp(User.USERNAME_PROPERTY, username)
                .andExp(ExpressionFactory.matchExp(User.PASSWORD_PROPERTY, password));
        SelectQuery selector = new SelectQuery(User.class, where);
        return (User) Cayenne.objectForQuery(_context, selector);
    }

    public List<User> getUsers() {
        SelectQuery selector = new SelectQuery(User.class);
        return _context.performQuery(selector);
    }

    public List<User> getUsers(List<Integer> ids) {
        List<User> result = new LinkedList<>();
        for (Integer id : ids) {
            User user = getUserById(id);
            if (user != null) {
                result.add(user);
            }
        }
        return result;
    }

    public User getUserById(int id) {
        return Cayenne.objectForPK(_context, User.class, id);
    }

    public User createUser(String firstName, String lastName, String username, String password, List<String> groups) {
        User user = _context.newObject(User.class);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        long now = System.currentTimeMillis();
        user.setCreatedOn(now);
        user.setUpdatedOn(now);
        for (String groupName : groups) {
            user.addToGroups(getGroupByName(groupName));
        }
        _context.commitChanges();
        return user;
    }

    public void deleteUser(User user) {
        _context.deleteObjects(user);
        _context.commitChanges();
    }

    // -------------------------------------------------------------------------
    // GROUPS
    // -------------------------------------------------------------------------
    public List<Group> getGroups() {
        SelectQuery selector = new SelectQuery(Group.class);
        return _context.performQuery(selector);
    }

    public List<Group> getGroups(List<Integer> ids) {
        List<Group> result = new LinkedList<>();
        for (Integer id : ids) {
            Group group = getGroupById(id);
            if (group != null) {
                result.add(group);
            }
        }
        return result;
    }

    public Group getGroupById(int id) {
        return Cayenne.objectForPK(_context, Group.class, id);
    }

    public Group createGroup(String name, List<String> permissions) {
        Group group = _context.newObject(Group.class);
        group.setName(name);
        long now = System.currentTimeMillis();
        group.setCreatedOn(now);
        group.setUpdatedOn(now);
        for (String permissionName : permissions) {
            group.addToPermissions(getPermissionByName(permissionName));
        }
        _context.commitChanges();
        return group;
    }

    public Group getGroupByName(String name) {
        Expression where = ExpressionFactory.matchExp(Group.NAME_PROPERTY, name);
        SelectQuery selector = new SelectQuery(Group.class, where);
        return (Group) Cayenne.objectForQuery(_context, selector);
    }

    public void deleteGroup(Group group) {
        _context.deleteObjects(group);
        _context.commitChanges();
    }

    // -------------------------------------------------------------------------
    // PERMISSIONS
    // -------------------------------------------------------------------------
    public List<Permission> getPermissions() {
        SelectQuery selector = new SelectQuery(Permission.class);
        return _context.performQuery(selector);
    }

    public List<Permission> getPermissions(List<Integer> ids) {
        List<Permission> result = new LinkedList<>();
        for (Integer id : ids) {
            Permission permission = getPermissionById(id);
            if (permission != null) {
                result.add(permission);
            }
        }
        return result;
    }

    public Permission getPermissionById(int id) {
        return Cayenne.objectForPK(_context, Permission.class, id);
    }

    public Permission createPermission(String name) {
        Permission permission = _context.newObject(Permission.class);
        permission.setName(name);
        long now = System.currentTimeMillis();
        permission.setCreatedOn(now);
        permission.setUpdatedOn(now);
        _context.commitChanges();
        return permission;
    }

    public Permission getPermissionByName(String name) {
        Expression where = ExpressionFactory.matchExp(Permission.NAME_PROPERTY, name);
        SelectQuery selector = new SelectQuery(Group.class, where);
        return (Permission) Cayenne.objectForQuery(_context, selector);
    }

    // -------------------------------------------------------------------------
    // INPUT EVENTS
    // -------------------------------------------------------------------------
    public List<InputEvent> getInputEvents() {
        SelectQuery selector = new SelectQuery(InputEvent.class);
        return _context.performQuery(selector);
    }

    public List<InputEvent> getInputEvents(List<Integer> ids) {
        List<InputEvent> result = new LinkedList<>();
        for (Integer id : ids) {
            InputEvent inputEvent = getInputEventById(id);
            if (inputEvent != null) {
                result.add(inputEvent);
            }
        }
        return result;
    }

    public InputEvent getInputEventById(int id) {
        return Cayenne.objectForPK(_context, InputEvent.class, id);
    }

    public InputEvent createInputEvent(DeviceType deviceType, String data, long createdOn) {
        InputEvent inputEvent = _context.newObject(InputEvent.class);
        inputEvent.setDeviceType(deviceType);
        inputEvent.setData(data);
        inputEvent.setCreatedOn(System.currentTimeMillis());
        _context.commitChanges();
        return inputEvent;
    }

    public void deleteInputEvent(InputEvent inputEvent) {
        _context.deleteObjects(inputEvent);
        _context.commitChanges();
    }

    // -------------------------------------------------------------------------
    // OUTPUT EVENTS
    // -------------------------------------------------------------------------
    public List<OutputEvent> getOutputEvents() {
        SelectQuery selector = new SelectQuery(OutputEvent.class);
        return _context.performQuery(selector);
    }

    public List<OutputEvent> getOutputEvents(List<Integer> ids) {
        List<OutputEvent> result = new LinkedList<>();
        for (Integer id : ids) {
            OutputEvent outputEvent = getOutputEventById(id);
            if (outputEvent != null) {
                result.add(outputEvent);
            }
        }
        return result;
    }

    public OutputEvent getOutputEventById(int id) {
        return Cayenne.objectForPK(_context, OutputEvent.class, id);
    }

    public OutputEvent createOutputEvent(DeviceType deviceType, String data) {
        OutputEvent outputEvent = _context.newObject(OutputEvent.class);
        outputEvent.setDeviceType(deviceType);
        outputEvent.setData(data);
        outputEvent.setCreatedOn(System.currentTimeMillis());
        _context.commitChanges();
        return outputEvent;
    }

    public void deleteOutputEvent(OutputEvent outputEvent) {
        _context.deleteObjects(outputEvent);
        _context.commitChanges();
    }

    // -------------------------------------------------------------------------
    // OUTPUT DEVICES
    // -------------------------------------------------------------------------
    public List<OutputDevice> getOutputDevices() {
        SelectQuery selector = new SelectQuery(OutputDevice.class);
        return _context.performQuery(selector);
    }

    public List<OutputDevice> getOutputDevices(List<Integer> ids) {
        List<OutputDevice> result = new LinkedList<>();
        for (Integer id : ids) {
            OutputDevice outputDevice = getOutputDeviceById(id);
            if (outputDevice != null) {
                result.add(outputDevice);
            }
        }
        return result;
    }

    public OutputDevice getOutputDeviceById(int id) {
        return Cayenne.objectForPK(_context, OutputDevice.class, id);
    }

    public OutputDevice createOutputDevice(String name, String description, String address, DeviceType deviceType, String serialNumber) {
        OutputDevice outputDevice = _context.newObject(OutputDevice.class);
        outputDevice.setName(name);
        outputDevice.setDescription(description);
        outputDevice.setAddress(address);
        outputDevice.setDeviceType(deviceType);
        outputDevice.setSerialNumber(serialNumber);
        outputDevice.setUpdatedOn(System.currentTimeMillis());
        outputDevice.setCreatedOn(outputDevice.getUpdatedOn());
        _context.commitChanges();
        return outputDevice;
    }

    public OutputDevice updateOutputDevice(int id, String name, String description, String address, DeviceType deviceType, String serialNumber) {
        OutputDevice outputDevice = getOutputDeviceById(id);
        if (outputDevice != null) {
            outputDevice.setName(name);
            outputDevice.setDescription(description);
            outputDevice.setAddress(address);
            outputDevice.setDeviceType(deviceType);
            outputDevice.setSerialNumber(serialNumber);
            outputDevice.setUpdatedOn(System.currentTimeMillis());
            _context.commitChanges();
        }
        return outputDevice;
    }

    public void deleteOutputDevice(OutputDevice outputDevice) {
        _context.deleteObjects(outputDevice);
        _context.commitChanges();
    }

    // -------------------------------------------------------------------------
    // DEVICE TYPES
    // -------------------------------------------------------------------------
    public DeviceType getDeviceTypeByCode(String code) {
        Expression where = ExpressionFactory.matchExp(DeviceType.CODE_PROPERTY, code);
        SelectQuery selector = new SelectQuery(DeviceType.class, where);
        return (DeviceType) Cayenne.objectForQuery(_context, selector);
    }

    public DeviceType createDeviceType(String code, String description) {
        DeviceType deviceType = _context.newObject(DeviceType.class);
        deviceType.setCode(code);
        deviceType.setDescription(description);
        _context.commitChanges();
        return deviceType;
    }

    // -------------------------------------------------------------------------
    // DEVICES
    // -------------------------------------------------------------------------
    public final List<Device> getDeviceByNode(Node node) {
        Expression where = ExpressionFactory.matchExp(Device.NODE_PROPERTY, node);
        SelectQuery selector = new SelectQuery(Device.class, where);
        return _context.performQuery(selector);
    }

    public final List<Device> getDeviceByAddress(String address) {
        Expression where = ExpressionFactory.matchExp(Device.ADDRESS_PROPERTY, address);
        SelectQuery selector = new SelectQuery(Device.class, where);
        return _context.performQuery(selector);
    }

    // -------------------------------------------------------------------------
    // NODES
    // -------------------------------------------------------------------------
    public Node getNodeById(int id) {
        return Cayenne.objectForPK(_context, Node.class, id);
    }

    public final List<Node> getNodeByParent(Node node) {
        Expression where = ExpressionFactory.matchExp(Node.PARENT_PROPERTY, node);
        SelectQuery selector = new SelectQuery(Node.class, where);
        return _context.performQuery(selector);
    }

    public final Node createNode(String name, Node parent) {
        Node node = _context.newObject(Node.class);
        node.setName(name);
        node.setParent(parent);
        long now = System.currentTimeMillis();
        node.setCreatedOn(now);
        node.setUpdatedOn(now);
        _context.commitChanges();
        return node;
    }

    // -------------------------------------------------------------------------
    // DEVICES
    // -------------------------------------------------------------------------
    public List<Device> getDevicesByType(DeviceType deviceType) {
        Expression where = ExpressionFactory.matchExp(Device.DEVICE_TYPE_PROPERTY, deviceType);
        SelectQuery selector = new SelectQuery(Device.class, where);
        return _context.performQuery(selector);
    }

    public Device getDeviceByTypeAddress(DeviceType deviceType, String address) {
        Expression where1 = ExpressionFactory.matchExp(Device.DEVICE_TYPE_PROPERTY, deviceType);
        Expression where2 = ExpressionFactory.matchExp(Device.ADDRESS_PROPERTY, address);
        SelectQuery selector = new SelectQuery(Device.class, where1.andExp(where2));
        return (Device) Cayenne.objectForQuery(_context, selector);
    }

    public Device createDevice(String name, String address, String message1, String message2, DeviceType deviceType) {
        Device device = _context.newObject(Device.class);
        device.setName(name);
        device.setAddress(address);
        device.setMessage1(message1);
        device.setMessage2(message2);
        device.setDeviceType(deviceType);
        long now = System.currentTimeMillis();
        device.setCreatedOn(now);
        device.setUpdatedOn(now);
        _context.commitChanges();
        return device;
    }
}