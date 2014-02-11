package hasoftware.configuration;

public class ConfigurationSetting {
    private String _name;
    private String _value;
    
    public ConfigurationSetting(String name) {
        _name = name;
    }
    
    public String getName() {
        return _name;
    }
    
    public String getValue() {
        return _value;
    }
    
    public void setValue(String value) {
        _value = value;
    }
}
