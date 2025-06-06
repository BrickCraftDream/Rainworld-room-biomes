package net.brickcraftdream.rainworldmc_biomes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.gson.*;

public class JsonExporter {
    private Gson gson;
    private JsonObject rootNode;

    /**
     * Constructor that initializes the JSON structure
     */
    public JsonExporter() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        rootNode = new JsonObject();
    }

    /**
     * Constructor that loads an existing JSON file
     * @param filePath The path to the JSON file to load
     * @throws IOException If the file cannot be read
     */
    public JsonExporter(String filePath) throws IOException {
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadFromFile(filePath);
    }


    /**
     * Adds a region to the JSON structure
     * @param regionName The name of the region
     * @return The newly created region node
     */
    public JsonObject addRegion(String regionName) {
        if (rootNode.has(regionName)) {
            return rootNode.getAsJsonObject(regionName);
        }
        JsonObject regionNode = new JsonObject();
        rootNode.add(regionName, regionNode);
        return regionNode;
    }

    /**
     * Gets a region from the JSON structure
     * @param regionName The name of the region
     * @return The region node if it exists, otherwise null
     */
    public JsonObject getRegion(String regionName) {
        return rootNode.has(regionName) ? rootNode.getAsJsonObject(regionName) : null;
    }

    /**
     * Adds a room to a region
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @return The newly created room node
     */
    public JsonObject addRoom(String regionName, String roomName) {
        JsonObject regionNode = getRegion(regionName);
        if (regionNode == null) {
            regionNode = addRegion(regionName);
        }

        JsonObject roomNode = new JsonObject();
        regionNode.add(roomName, roomNode);
        return roomNode;
    }

    /**
     * Gets a room from a region
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @return The room node if it exists, otherwise null
     */
    public JsonObject getRoom(String regionName, String roomName) {
        JsonObject regionNode = getRegion(regionName);
        if (regionNode == null) {
            return null;
        }
        return regionNode.has(roomName) ? regionNode.getAsJsonObject(roomName) : null;
    }

    /**
     * Adds a screen to a room
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @param screenName The name of the screen
     * @return The newly created screen node
     */
    public JsonObject addScreen(String regionName, String roomName, String screenName) {
        JsonObject roomNode = getRoom(regionName, roomName);
        if (roomNode == null) {
            roomNode = addRoom(regionName, roomName);
        }

        JsonObject screenNode = new JsonObject();
        roomNode.add(screenName, screenNode);
        return screenNode;
    }

    /**
     * Gets a screen from a room
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @param screenName The name of the screen
     * @return The screen node if it exists, otherwise null
     */
    public JsonObject getScreen(String regionName, String roomName, String screenName) {
        JsonObject roomNode = getRoom(regionName, roomName);
        if (roomNode == null) {
            return null;
        }
        return roomNode.has(screenName) ? roomNode.getAsJsonObject(screenName) : null;
    }

    /**
     * Sets an integer property value on a node
     * @param node The node to set the property on
     * @param propertyName The name of the property
     * @param value The integer value to set
     */
    public void setIntProperty(JsonObject node, String propertyName, int value) {
        node.addProperty(propertyName, value);
    }

    /**
     * Sets a double property value on a node
     * @param node The node to set the property on
     * @param propertyName The name of the property
     * @param value The double value to set
     */
    public void setDoubleProperty(JsonObject node, String propertyName, double value) {
        node.addProperty(propertyName, value);
    }

    /**
     * Sets a string property value on a node
     * @param node The node to set the property on
     * @param propertyName The name of the property
     * @param value The string value to set
     */
    public void setStringProperty(JsonObject node, String propertyName, String value) {
        node.addProperty(propertyName, value);
    }

    /**
     * Adds a complete set of properties to a node (screen, room, or region)
     * @param node The node to add properties to
     * @param palette The palette value
     * @param fadePalette The fade palette value
     * @param fadeStrength The fade strength value
     * @param grime The grime value
     * @param effectColorA The effect color A value
     * @param effectColorB The effect color B value
     */
    public void addProperties(JsonObject node, int palette, int fadePalette,
                              double fadeStrength, double grime,
                              int effectColorA, int effectColorB) {
        node.addProperty("palette", palette);
        node.addProperty("fade_palette", fadePalette);
        node.addProperty("fade_strength", fadeStrength);
        node.addProperty("grime", grime);
        if(effectColorA != -10) {
            node.addProperty("effect_color_a", effectColorA);
        }
        if(effectColorB != -10) {
            node.addProperty("effect_color_b", effectColorB);
        }
    }

    /**
     * Adds a complete set of properties to a screen
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @param screenName The name of the screen
     * @param palette The palette value
     * @param fadePalette The fade palette value
     * @param fadeStrength The fade strength value
     * @param grime The grime value
     * @param effectColorA The effect color A value
     * @param effectColorB The effect color B value
     */
    public void addScreenProperties(String regionName, String roomName, String screenName,
                                    int palette, int fadePalette, double fadeStrength,
                                    double grime, int effectColorA, int effectColorB) {
        JsonObject screenNode = addScreen(regionName, roomName, screenName);
        addProperties(screenNode, palette, fadePalette, fadeStrength, grime, effectColorA, effectColorB);
    }

    /**
     * Adds a complete set of properties to a room
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @param palette The palette value
     * @param fadePalette The fade palette value
     * @param fadeStrength The fade strength value
     * @param grime The grime value
     * @param effectColorA The effect color A value
     * @param effectColorB The effect color B value
     */
    public void addRoomProperties(String regionName, String roomName,
                                  int palette, int fadePalette, double fadeStrength,
                                  double grime, int effectColorA, int effectColorB) {
        JsonObject roomNode = addRoom(regionName, roomName);
        addProperties(roomNode, palette, fadePalette, fadeStrength, grime, effectColorA, effectColorB);
    }

    /**
     * Exports the JSON structure to a string
     * @return The JSON structure as a string
     */
    public String exportToString() {
        return gson.toJson(rootNode);
    }

    /**
     * Exports the JSON structure to a file
     * @param filePath The path to the file
     */
    public void exportToFile(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(rootNode, writer);
        }
        catch (IOException ignored) {}
    }

    /**
     * Loads JSON data from a file
     * @param filePath The path to the file to load
     * @throws IOException If the file cannot be read
     */
    public void loadFromFile(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            rootNode = JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    /**
     * Gets all region names from the JSON structure
     * @return Set of region names
     */
    public Set<String> getAllRegionNames() {
        Set<String> regionNames = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : rootNode.entrySet()) {
            regionNames.add(entry.getKey());
        }
        return regionNames;
    }

    /**
     * Gets all room names for a specific region
     * @param regionName The name of the region
     * @return Set of room names, or empty set if region doesn't exist
     */
    public Set<String> getAllRoomNames(String regionName) {
        JsonObject regionNode = getRegion(regionName);
        if (regionNode == null) {
            return new HashSet<>();
        }

        Set<String> roomNames = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : regionNode.entrySet()) {
            roomNames.add(entry.getKey());
        }
        return roomNames;
    }

    /**
     * Gets all screen names for a specific room in a region
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @return Set of screen names, or empty set if region or room doesn't exist
     */
    public Set<String> getAllScreenNames(String regionName, String roomName) {
        JsonObject roomNode = getRoom(regionName, roomName);
        if (roomNode == null) {
            return new HashSet<>();
        }

        Set<String> screenNames = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : roomNode.entrySet()) {
            screenNames.add(entry.getKey());
        }
        return screenNames;
    }

    /**
     * Gets the property values for a given node (region, room, or screen)
     * @param node The node to get properties from
     * @return Map of property names to values
     */
    public Map<String, Object> getNodeProperties(JsonObject node) {
        Map<String, Object> properties = new HashMap<>();

        if (node == null) {
            return properties;
        }

        for (Map.Entry<String, JsonElement> entry : node.entrySet()) {
            String propertyName = entry.getKey();
            JsonElement element = entry.getValue();

            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    // Handle int vs double values
                    double value = primitive.getAsDouble();
                    if (value == Math.floor(value)) {
                        properties.put(propertyName, primitive.getAsInt());
                    } else {
                        properties.put(propertyName, value);
                    }
                } else if (primitive.isString()) {
                    properties.put(propertyName, primitive.getAsString());
                } else if (primitive.isBoolean()) {
                    properties.put(propertyName, primitive.getAsBoolean());
                }
            }
        }

        return properties;
    }

    /**
     * Gets the properties for a specific region
     * @param regionName The name of the region
     * @return Map of property names to values, or empty map if region doesn't exist
     */
    public Map<String, Object> getRegionProperties(String regionName) {
        return getNodeProperties(getRegion(regionName));
    }

    /**
     * Gets the properties for a specific room
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @return Map of property names to values, or empty map if region or room doesn't exist
     */
    public Map<String, Object> getRoomProperties(String regionName, String roomName) {
        return getNodeProperties(getRoom(regionName, roomName));
    }

    /**
     * Gets the properties for a specific screen
     * @param regionName The name of the region
     * @param roomName The name of the room
     * @param screenName The name of the screen
     * @return Map of property names to values, or empty map if region, room, or screen doesn't exist
     */
    public Map<String, Object> getScreenProperties(String regionName, String roomName, String screenName) {
        return getNodeProperties(getScreen(regionName, roomName, screenName));
    }

    /**
     * Gets the entire structure as a nested Map
     * @return Nested Map representing the entire JSON structure
     */
    public Map<String, Map<String, Map<String, Map<String, Object>>>> getCompleteStructure() {
        Map<String, Map<String, Map<String, Map<String, Object>>>> structure = new HashMap<>();

        // Iterate through regions
        for (String regionName : getAllRegionNames()) {
            JsonObject regionNode = getRegion(regionName);
            Map<String, Map<String, Map<String, Object>>> regionMap = new HashMap<>();
            structure.put(regionName, regionMap);

            // Iterate through rooms in this region
            for (String roomName : getAllRoomNames(regionName)) {
                JsonObject roomNode = getRoom(regionName, roomName);

                // Skip non-object entries at the room level
                if (roomNode == null || !roomNode.isJsonObject()) {
                    continue;
                }

                Map<String, Map<String, Object>> roomMap = new HashMap<>();
                regionMap.put(roomName, roomMap);

                // Iterate through screens in this room
                for (Map.Entry<String, JsonElement> entry : roomNode.entrySet()) {
                    String screenName = entry.getKey();
                    JsonElement element = entry.getValue();

                    // If it's a JSON object, it could be a screen
                    if (element.isJsonObject()) {
                        JsonObject screenNode = element.getAsJsonObject();
                        Map<String, Object> properties = getNodeProperties(screenNode);
                        roomMap.put(screenName, properties);
                    }
                }
            }
        }

        return structure;
    }

    /**
     * Gets a specific integer property from a node
     * @param node The node to get the property from
     * @param propertyName The name of the property
     * @param defaultValue The default value to return if the property doesn't exist
     * @return The property value, or the default value if the property doesn't exist
     */
    public int getIntProperty(JsonObject node, String propertyName, int defaultValue) {
        if (node != null && node.has(propertyName) && node.get(propertyName).isJsonPrimitive()) {
            try {
                return node.get(propertyName).getAsInt();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets a specific double property from a node
     * @param node The node to get the property from
     * @param propertyName The name of the property
     * @param defaultValue The default value to return if the property doesn't exist
     * @return The property value, or the default value if the property doesn't exist
     */
    public double getDoubleProperty(JsonObject node, String propertyName, double defaultValue) {
        if (node != null && node.has(propertyName) && node.get(propertyName).isJsonPrimitive()) {
            try {
                return node.get(propertyName).getAsDouble();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets a specific string property from a node
     * @param node The node to get the property from
     * @param propertyName The name of the property
     * @param defaultValue The default value to return if the property doesn't exist
     * @return The property value, or the default value if the property doesn't exist
     */
    public String getStringProperty(JsonObject node, String propertyName, String defaultValue) {
        if (node != null && node.has(propertyName) && node.get(propertyName).isJsonPrimitive()) {
            try {
                return node.get(propertyName).getAsString();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }


}
