package hasoftware.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private final static Logger logger = LoggerFactory.getLogger(Configuration.class);

    private String _filename;
    private String _section;
    private final List<ConfigurationSection> _sections;

    public Configuration() {
        _filename = "";
        _section = "";
        _sections = new LinkedList<>();
    }

    public boolean open(String filename) {
        boolean result = false;
        try {
            File file = new File(filename);
            if (file.exists()) {
                _filename = filename;
                BufferedReader reader = new BufferedReader(new FileReader(_filename));
                String line;
                ConfigurationSection section = findSection("", true);
                while ((line = reader.readLine()) != null) {
                    // An empty line or comment
                    if (line.length() == 0 || line.charAt(0) == '#') {
                        continue;
                    }
                    // A new section
                    if (line.startsWith("[") && line.endsWith("]")) {
                        String name = line.substring(1, line.length() - 1);
                        section = findSection(name, true);
                        //logger.debug("Found section " + name);
                        continue;
                    }
                    // A setting
                    if (line.indexOf("=") != -1) {
                        String name = line.substring(0, line.indexOf("=")).trim();
                        String value = line.substring(line.indexOf("=") + 1).trim();
                        // A quoted value
                        if (value.length() >= 3 && value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        section.set(name, value);
                        //logger.debug("Found setting " + name + " = " + value);
                    }
                }
                reader.close();
                result = true;
            }
        } catch (FileNotFoundException ex) {
            logger.error("Error opening configuration file", ex);
        } catch (IOException ex) {
            logger.error("Error opening configuration file", ex);
        }
        return result;
    }

    public void setSection(String section) {
        _section = section;
    }

    public String getString(String setting) {
        return getSectionString(_section, setting, null);
    }

    public int getInt(String setting) {
        return getSectionInt(_section, setting, 0);
    }

    public int getInt(String setting, int defaultValue) {
        return getSectionInt(_section, setting, defaultValue);
    }

    public String getSectionString(String sectionName, String settingName, String defaultValue) {
        ConfigurationSection section = findSection(sectionName, false);
        return (section == null) ? defaultValue : section.getString(settingName, defaultValue);
    }

    public int getSectionInt(String sectionName, String settingName, int defaultValue) {
        ConfigurationSection section = findSection(sectionName, false);
        return (section == null) ? defaultValue : section.getInt(settingName, defaultValue);
    }

    private ConfigurationSection findSection(String name, boolean create) {
        ConfigurationSection result = null;
        for (ConfigurationSection cs : _sections) {
            if (cs.getName().equals(name)) {
                result = cs;
            }
        }
        if (result == null && create) {
            result = new ConfigurationSection(name);
            _sections.add(result);
        }
        return result;
    }
}
