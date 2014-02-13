package hasoftware.bus;

import hasoftware.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusInterface {

    private final static Logger logger = LoggerFactory.getLogger(BusInterface.class);

    private final static String ConfigurationFilename = "hasoftware.ini";
    private final static String ConfigurationSection = "Bus Interface";

    public static void main(String[] args) {
        Configuration config = new Configuration();
        if (!config.open(ConfigurationFilename)) {
            logger.error("Can't load configuration file - " + ConfigurationFilename);
            return;
        }
        config.setSection(ConfigurationSection);
    }
}
