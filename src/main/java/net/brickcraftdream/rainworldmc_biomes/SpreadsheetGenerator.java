package net.brickcraftdream.rainworldmc_biomes;

import org.joml.Vector3f;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SpreadsheetGenerator {
    private final Set<String> uniqueFirstElements = new HashSet<>();
    private final Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();
    private final Set<String> screenGroups = new TreeSet<>();

    private static final String[] HEADER_ROW = {
            "Biome name", "In-game Name", "Biome Temperature",
            "Biome Grime \n (downfall)", "Biome Palette", "Biome Fade palette",
            "Biome Fade strength", "In-game additions", "Biome Sky-color",
            "Biome Water-color","Biome Water-fog-color", "Biome Fog-color"
    };

    public SpreadsheetGenerator() {
        // Constructor simplified - we'll just be generating a JSON data file
    }

    public void addColoredRow(String[] values, Map<String, Vector3f> colors) {
        if (values.length > 0) {
            String[] firstElementValues = values[0].split(",");
            if (firstElementValues.length > 0) {
                String prefix = firstElementValues[0].substring(0, 2);
                uniqueFirstElements.add(prefix);

                // Extract screen information
                String firstCell = values[0];
                for (int i = 0; i <= 20; i++) {
                    String screenPrefix = String.format("screen_%d", i);
                    if (firstCell.contains(screenPrefix)) {
                        screenGroups.add(screenPrefix);
                    }
                }

                // Build a data row as a map
                Map<String, Object> rowData = new HashMap<>();

                // Process the values into columns based on HEADER_ROW
                String[] splitValues = new String[HEADER_ROW.length];
                int sourceIndex = 0;

                for (int targetIndex = 0; targetIndex < HEADER_ROW.length && sourceIndex < values.length; targetIndex++) {
                    String[] parts = values[sourceIndex].split(",");
                    splitValues[targetIndex] = parts[0].trim();

                    if (parts.length > 1) {
                        // Handle multiple comma-separated values
                        for (int i = 1; i < parts.length; i++) {
                            targetIndex++;
                            if (targetIndex < HEADER_ROW.length) {
                                splitValues[targetIndex] = parts[i].trim();
                            }
                        }
                    }
                    sourceIndex++;
                }

                // Add values to the map
                for (int i = 0; i < HEADER_ROW.length && i < splitValues.length; i++) {
                    rowData.put(HEADER_ROW[i].replace("\n", " ").trim(), splitValues[i]);
                }

                // Add colors to the map
                Map<String, List<Float>> colorMap = new HashMap<>();
                colors.forEach((key, value) -> {
                    List<Float> rgbValues = Arrays.asList(value.x, value.y, value.z);
                    colorMap.put(key, rgbValues);
                });
                rowData.put("colors", colorMap);

                // Add region info
                rowData.put("region", prefix);

                // Add screen info
                List<String> screens = new ArrayList<>();
                for (String screen : screenGroups) {
                    if (firstCell.contains(screen)) {
                        screens.add(screen);
                    }
                }
                rowData.put("screens", screens);

                // Store data
                groupedData.computeIfAbsent(prefix, k -> new ArrayList<>()).add(rowData);
            }
        }
    }

    public void saveSpreadsheet(String filePath) {
        // Change the extension to .json instead of .html
        String jsonFilePath = filePath.replaceAll("\\.html$", ".json");
        if (jsonFilePath.equals(filePath)) {
            jsonFilePath = filePath + ".json";
        }

        Map<String, Object> fullData = new HashMap<>();

        // Add all the collected data
        List<Map<String, Object>> allRows = new ArrayList<>();
        groupedData.values().forEach(allRows::addAll);

        fullData.put("data", allRows);
        fullData.put("regions", new ArrayList<>(uniqueFirstElements));
        fullData.put("screens", new ArrayList<>(screenGroups));
        fullData.put("headers", Arrays.asList(HEADER_ROW));

        // Create compact JSON file
        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(fullData, writer);
            System.out.println("Data saved to: " + jsonFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}