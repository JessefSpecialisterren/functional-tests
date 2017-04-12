package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import nl.vpro.util.Env;
import nl.vpro.util.ReflectionUtils;

/**
 * @author Michiel Meeuwissen

 */
@Slf4j
public class Config {

    private static Map<String, String> PROPERTIES;
    private static String CONFIG_FILE = "npo-functional-tests.properties";

    public enum Prefix {
        npoapi,
        backendapi,
        parkpost,
        pageupdateapi,
        poms
    }

    static {

        try {
            PROPERTIES = ReflectionUtils.getProperties(ReflectionUtils.getConfigFilesInHome(CONFIG_FILE));
            log.info("Reading {} configuration from {}", env(), CONFIG_FILE);
            log.debug("{}", PROPERTIES);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static Optional<String> configOption(Prefix pref, String prop) {
        String value = PROPERTIES.getOrDefault(pref + "." + prop + "." + env().name().toLowerCase(), PROPERTIES.get(pref + "." + prop));
        return Optional.ofNullable(value);
    }

    public static String requiredOption(Prefix pref, String prop) {
        return configOption(pref, prop)
            .orElseThrow(notSet(prop));
    }


    public static String url(Prefix pref, String path) {
        String base  = requiredOption(pref, "baseUrl");
        if (! base.endsWith("/")) {
            base = base + "/";
        }
        return base + path;

    }

    public static Map<String, String> getProperties(Prefix prefix) {
        return PROPERTIES.entrySet()
            .stream()
            .filter(e -> prefix == null || e.getKey().startsWith(prefix.name()))
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().substring(prefix.name().length() + 1), e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Supplier<RuntimeException> notSet(String prop) {
        return () -> new RuntimeException(prop + " is not set in " + CONFIG_FILE);
    }

    public static Env env() {
        String pref = System.getProperty("env");
        if (pref == null) {
            return Env.valueOf(PROPERTIES.getOrDefault("env", "test").toUpperCase());
        } else {
            return Env.valueOf(pref.toUpperCase());
        }
    }

}
