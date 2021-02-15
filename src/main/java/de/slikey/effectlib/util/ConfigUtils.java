package de.slikey.effectlib.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigUtils {
    public static Logger logger;

    public static Collection<ConfigurationSection> getNodeList(ConfigurationSection node, String path) {
        Collection<ConfigurationSection> results = new ArrayList<ConfigurationSection>();
        List<Map<?, ?>> mapList = node.getMapList(path);
        for (Map<?, ?> map : mapList) {
            results.add(toConfigurationSection(map));
        }

        return results;
    }

    @Deprecated
    public static ConfigurationSection toNodeList(Map<?, ?> nodeMap) {
        return toConfigurationSection(nodeMap);
    }

    public static ConfigurationSection toConfigurationSection(Map<?, ?> nodeMap) {
        ConfigurationSection newSection = new MemoryConfiguration();
        for (Map.Entry<?, ?> entry : nodeMap.entrySet()) {
            newSection.set(entry.getKey().toString(), entry.getValue());
        }

        return newSection;
    }

    public static ConfigurationSection convertConfigurationSection(Map<?, ?> nodeMap) {
        ConfigurationSection newSection = new MemoryConfiguration();
        for (Map.Entry<?, ?> entry : nodeMap.entrySet()) {
            set(newSection, entry.getKey().toString(), entry.getValue());
        }

        return newSection;
    }

    public static ConfigurationSection toStringConfiguration(Map<String, String> stringMap) {
        if (stringMap == null) {
            return null;
        }
        ConfigurationSection configMap = new MemoryConfiguration();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            configMap.set(entry.getKey(), entry.getValue());
        }

        return configMap;
    }


    public static void set(ConfigurationSection node, String path, Object value)
    {
        if (value == null) {
            node.set(path, null);
            return;
        }

        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        if (isTrue || isFalse) {
            node.set(path, isTrue);
        } else {
            try {
                Integer i = (value instanceof Integer) ? (Integer)value : Integer.parseInt(value.toString());
                node.set(path, i);
            } catch (Exception ex) {
                try {
                    Double d;
                    if (value instanceof Double) {
                        d = (Double)value;
                    } else if (value instanceof Float) {
                        d = (double)(Float)value;
                    } else {
                        d = Double.parseDouble(value.toString());
                    }
                    node.set(path, d);
                } catch (Exception ex2) {
                    node.set(path, value);
                }
            }
        }
    }

    public static ConfigurationSection getConfigurationSection(ConfigurationSection base, String key)
    {
        ConfigurationSection section = base.getConfigurationSection(key);
        if (section != null) {
            return section;
        }
        Object value = base.get(key);
        if (value == null) return null;

        if (value instanceof ConfigurationSection)
        {
            return (ConfigurationSection)value;
        }

        if (value instanceof Map)
        {
            ConfigurationSection newChild = base.createSection(key);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)value;
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                newChild.set(entry.getKey(), entry.getValue());
            }
            base.set(key, newChild);
            return newChild;
        }

        return null;
    }

    public static boolean isMaxValue(String stringValue) {
        return stringValue.equalsIgnoreCase("infinite")
                || stringValue.equalsIgnoreCase("forever")
                || stringValue.equalsIgnoreCase("infinity")
                || stringValue.equalsIgnoreCase("max");
    }

    public static long getInterval(ConfigurationSection section, String path) {
        return getInterval(section, path, 0);
    }

    public static long getInterval(ConfigurationSection section, String path, long defaultValue) {
         if (!section.contains(path)) {
             return defaultValue;
         }
         if (section.isLong(path)) {
             return section.getLong(path, defaultValue);
         }
         return toInterval(section.getString(path));
    }

    public static long toInterval(String value) {
         if (value == null || value.isEmpty()) {
             return 0;
         }
        // Find last numeric character
        int finalIndex = value.length() - 1;
        int lastIndex = finalIndex;
        while (lastIndex > 0 && !Character.isDigit(value.charAt(lastIndex))) {
            lastIndex--;
        }
        int multiplier = 1;
        if (lastIndex != finalIndex) {
            String suffix = value.substring(lastIndex + 1).trim();
            switch (suffix) {
                case "ms":
                    multiplier = 1;
                    break;
                case "s":
                    multiplier = 1000;
                    break;
                case "m":
                    multiplier = 60000;
                    break;
                case "h":
                    multiplier = 3600000;
                    break;
                case "d":
                    multiplier = 24 * 3600000;
                    break;
                default:
                    if (logger != null) logger.warning("Invalid time unit: " + suffix + " in interval: " + value);
            }
            value = value.substring(0, lastIndex + 1);
        }
        long interval = 0;
        try {
            interval = (long)(Double.parseDouble(value) * multiplier);
        } catch (NumberFormatException ex) {
            if (logger != null) logger.warning("Invalid interval format: " + value);
        }
        return interval;
    }
}
