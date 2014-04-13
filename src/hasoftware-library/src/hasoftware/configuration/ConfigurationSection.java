package hasoftware.configuration;

import java.util.LinkedList;
import java.util.List;

public class ConfigurationSection {

    private final String _name;
    private final List<ConfigurationSetting> _settings;

    public ConfigurationSection(String name) {
        _name = name;
        _settings = new LinkedList<>();
    }

    public void set(String name, String value) {
        findSetting(name, true).setValue(value);
    }

    public String getName() {
        return _name;
    }

    public String getString(String settingName, String defaultValue) {
        ConfigurationSetting setting = findSetting(settingName, false);
        return (setting == null) ? defaultValue : setting.getValue();
    }

    public int getInt(String settingName, int defaultValue) {
        ConfigurationSetting setting = findSetting(settingName, false);
        return (setting == null) ? defaultValue : Integer.parseInt(setting.getValue());
    }

    public boolean getBoolean(String settingName, boolean defaultValue) {
        ConfigurationSetting setting = findSetting(settingName, false);
        return (setting == null) ? defaultValue : Boolean.parseBoolean(setting.getValue());
    }

    private ConfigurationSetting findSetting(String name, boolean create) {
        ConfigurationSetting result = null;
        for (ConfigurationSetting cs : _settings) {
            if (cs.getName().equals(name)) {
                result = cs;
            }
        }
        if (result == null && create) {
            result = new ConfigurationSetting(name);
            _settings.add(result);
        }
        return result;
    }
}
