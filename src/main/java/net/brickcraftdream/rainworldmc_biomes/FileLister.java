package net.brickcraftdream.rainworldmc_biomes;

import java.io.*;
import java.util.*;


public class FileLister {
    // Define data fields as an enum
    public enum DataField {
        FILENAME("Template: ", null, null, String.class),
        GRIME("Grime: ", "0.0", null, String.class),
        DANGERTYPE("DangerType", "0", new ValueMapping()
                .add(": Rain", "1")
                .add(": Flood", "2")
                .add(": None", "0")
                .add(": AerieBlizzard", "3")
                .add(": Blizzard", "4")
                .add(": FloodAndRain", "5")
                .add(": Thunder", "6"), 
                Integer.class),
        SPARK("GreenSparks", "0", null, Integer.class),
        PALETTE("Palette: ", "-0", null, Integer.class),
        FADEPALETTE("FadePalette: ", "0", null, Integer.class),
        FADESTRENGTH("FadePalette: ", "0.0", null, Double.class);
        // Example of a field with value mapping:
        // NEW_FIELD("NewField: ", "default", new ValueMapping()
        //     .add("low", "0")
        //     .add("medium", "1")
        //     .add("high", "2"));


        private final String prefix;
        private final String defaultValue;
        private final ValueMapping valueMapping;
        private final Class<?> type;

        DataField(String prefix, String defaultValue, ValueMapping valueMapping, Class<?> type) {
            this.prefix = prefix;
            this.defaultValue = defaultValue;
            this.valueMapping = valueMapping;
            this.type = type;
        }

        public Object convertValue(String input) {
            if (input == null || input.trim().isEmpty()) {
                return convertStringToType(defaultValue);
            }
            
            String cleanInput = input.trim();
            if (cleanInput.contains(",")) {
                cleanInput = cleanInput.split(",")[0].trim();
            }
            
            String valueToConvert = valueMapping != null ? 
                valueMapping.map(cleanInput) : cleanInput;
                
            if (valueToConvert == null) {
                return convertStringToType(defaultValue);
            }
            
            try {
                return convertStringToType(valueToConvert);
            } catch (NumberFormatException e) {
                System.err.println("Error converting value '" + input + "' to " + type.getSimpleName());
                return convertStringToType(defaultValue);
            }
        }

        private Object convertStringToType(String value) {
            if (value == null) return null;
            
            try {
                if (type == Integer.class) {
                    return Integer.parseInt(value.trim());
                } else if (type == Double.class) {
                    return Double.parseDouble(value.trim());
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.err.println("Error converting value '" + value + "' to " + type.getSimpleName());
                return null;
            }
        }

        public String getPrefix() { return prefix; }
        public String getDefaultValue() { return defaultValue; }
        public int index() { return ordinal(); }
    }

    // Helper class for value mapping
    public static class ValueMapping {
        private final Map<String, String> mappings = new HashMap<>();

        public ValueMapping add(String from, String to) {
            mappings.put(from.toLowerCase(), to);
            return this;
        }

        public String map(String input) {
            if (input == null) return null;
            return mappings.getOrDefault(input.toLowerCase(), input);
        }
    }

    public static Map<String, Integer> fileIndexMap = new HashMap<>();

    public static List<String[]> listTextFiles(String directoryPath) {
        List<String[]> fileDataList = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            // Call the recursive method to scan for files
            scanDirectory(directory, fileDataList);
        }

        // Adding stuff manually that wouldn't get added. Idk why
        //fileDataList.add(new String[]{"rm_b04", "44", "10", "0.173913", "0.1630435", "0.1630435", "0.1521739"});
        //System.out.println(Arrays.toString(new String[]{"rm_b04", "44", "10", "0.173913", "0.1630435", "0.1630435", "0.1521739"}));
        //System.out.println(Arrays.toString(fileDataList.get(1)));


        //some manual checks to make sure that the thing is built right

        for(int i = 0; i < fileDataList.size(); i++) {
            boolean specialCase = false;
            try {
                int test = Integer.parseInt(fileDataList.get(i)[2]);
            } catch (NumberFormatException e) {
                System.out.println("Palette position compromised! Immediate action required! " + Arrays.toString(fileDataList.get(i)));
            } catch (ArrayIndexOutOfBoundsException e) {
                specialCase = true;
                //System.out.println("What? How could this happen? Quick, assembling rescue! " + Arrays.toString(fileDataList.get(i)));
                List<String> a = new ArrayList<>(List.of(fileDataList.get(i)));
                a.add(1, "0.0");
                fileDataList.remove(i);
                fileDataList.add(i, a.toArray(new String[0]));
                //System.out.println("Help should have come to rescue. How does it look? " + Arrays.toString(fileDataList.get(i)));
            }
            try {
                int test = Integer.parseInt(fileDataList.get(i)[3]);
            } catch (NumberFormatException e) {
                //System.out.println("Fade palette position has fallen! But fear not, help is on its way! " + Arrays.toString(fileDataList.get(i)));
                List<String> a = new ArrayList<>(List.of(fileDataList.get(i)));
                a.add(1, "0.0");
                fileDataList.remove(i);
                fileDataList.add(i, a.toArray(new String[0]));
                //System.out.println("Help should have come to rescue. How does it look? " + Arrays.toString(fileDataList.get(i)));
            } catch (ArrayIndexOutOfBoundsException e) {
                if(specialCase) {
                    continue;
                }
                //...
            }
        }

        return fileDataList;
    }

    private static Map<String, Integer> getFileIndexMap() {
        return fileIndexMap;
    }

    private static void scanDirectory(File directory, List<String[]> fileDataList) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively scan subdirectories
                    scanDirectory(file, fileDataList);
                } else if (file.isFile() && file.getName().endsWith(".txt")) {
                    String fileName = file.getName().substring(0, file.getName().lastIndexOf('.')).toLowerCase();

                    // Check if the file name contains "rivulet" or "saint" bc they won't be needed
                    if (fileName.contains("rivulet") || fileName.contains("saint")) {
                        continue;
                    }

                    // Check for the corresponding "_settings" file
                    File settingsFile = new File(file.getParent(), fileName + "_settings.txt");

                    if (settingsFile.exists()) {
                        // Extract the required information from the settings file
                        String[] fileData = extractDataFromSettingsFile(settingsFile, fileName);
                        if (fileData.length > 0) {
                            fileDataList.add(fileData);
                            // Only add to fileIndexMap if the file data is valid and added to the list
                            fileIndexMap.put(fileName, fileDataList.size() - 1);
                        }
                    }
                }
            }
        }
    }

    private static String[] createDataArray(String fileName) {
        String[] data = new String[DataField.values().length];
        data[DataField.FILENAME.index()] = fileName;
        
        // Initialize other fields with default values
        for (DataField field : DataField.values()) {
            if (field != DataField.FILENAME && data[field.index()] == null) {
                data[field.index()] = field.getDefaultValue();
            }
        }
        return data;
    }

    private static String[] extractDataFromSettingsFile(File settingsFile, String fileName) {
        String[] data = createDataArray(fileName);
        boolean paletteFound = false;
        String templateName = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
            String line = reader.readLine();

            if (line != null && line.startsWith(DataField.FILENAME.getPrefix())) {
                templateName = line.substring(DataField.FILENAME.getPrefix().length()).trim().toLowerCase();
                if ("none".equalsIgnoreCase(templateName)) {
                    templateName = null;
                }
            }

            while ((line = reader.readLine()) != null) {
                // Skip irrelevant lines
                if (!containsRelevantData(line)) continue;

                // Process each field
                for (DataField field : DataField.values()) {
                    if (line.startsWith(field.getPrefix())) {
                        processField(field, line, data);
                        if (field == DataField.PALETTE) {
                            paletteFound = true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + settingsFile.getAbsolutePath());
        }

        if (!paletteFound) {
            handleTemplateFallback(data, templateName, fileName);
        }

        return data;
    }

    private static boolean containsRelevantData(String line) {
        if (line == null) return false;
        String lowerLine = line.toLowerCase();
        for (DataField field : DataField.values()) {
            if (lowerLine.contains(field.getPrefix().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static void processField(DataField field, String line, String[] data) {
        String value = line.substring(field.getPrefix().length()).trim();
        
        switch (field) {
            case FADEPALETTE:
                String[] parts = value.split(", ");
                if (parts.length > 0) {
                    // Store the fade palette value
                    data[DataField.FADEPALETTE.index()] = String.valueOf(field.convertValue(parts[0]));
                    
                    // If there are multiple fade strengths (screens)
                    if (parts.length > 1) {
                        // Set the original room's fade strength to the first value
                        data[DataField.FADESTRENGTH.index()] = String.valueOf(DataField.FADESTRENGTH.convertValue(parts[1]));
                        
                        // Generate additional entries for each screen
                        for (int i = 0; i < parts.length - 1; i++) {
                            String screenName = data[DataField.FILENAME.index()] + "_screen_" + i;
                            String[] screenData = data.clone();
                            screenData[DataField.FILENAME.index()] = screenName;
                            screenData[DataField.FADESTRENGTH.index()] = String.valueOf(
                                DataField.FADESTRENGTH.convertValue(parts[i + 1])
                            );
                            EverythingProvider.maybeEverything.add(screenData);
                        }
                    } else {
                        // Single screen room - just set the fade strength
                        data[DataField.FADESTRENGTH.index()] = String.valueOf(DataField.FADESTRENGTH.convertValue(parts[0]));
                    }
                }
                break;
            case PALETTE:
                String[] paletteData = value.split(", ");
                for (String part : paletteData) {
                    if (!part.toLowerCase().contains("palette") && !part.contains(",")) {
                        data[field.index()] = String.valueOf(field.convertValue(part));
                        break;
                    }
                }
                break;
            default:
                data[field.index()] = String.valueOf(field.convertValue(value));
        }
    }

    private static void handleTemplateFallback(String[] data, String templateName, String fileName) {
        if (templateName != null) {
            File templateFile = new File("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/room_files/templates", templateName + ".txt");
            if (templateFile.exists()) {
                String paletteValue = searchPaletteInTemplateFile(templateFile);
                data[DataField.PALETTE.index()] = paletteValue != null ? paletteValue : DataField.PALETTE.getDefaultValue();
            } else {
                data[DataField.PALETTE.index()] = DataField.PALETTE.getDefaultValue();
            }
        } else {
            String paletteValue = searchPaletteInOptionsDirectory(fileName);
            data[DataField.PALETTE.index()] = paletteValue != null ? paletteValue : DataField.PALETTE.getDefaultValue();
        }
    }

    private static String searchPaletteInTemplateFile(File templateFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(templateFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Palette: ")) {
                    String paletteData = line.substring("Palette: ".length()).trim();
                    return paletteData.length() > 2 ? paletteData.substring(0, 2) : paletteData;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading template file: " + templateFile.getAbsolutePath());
        }
        return null; // Return null if no "Palette: " is found in the template file
    }

    private static String searchPaletteInOptionsDirectory(String fileName) {
        // Extract the first two letters of the file name
        String prefix = fileName.length() >= 2 ? fileName.substring(0, 2) : fileName;

        File optionsDirectory = new File("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/room_files/options/");
        File[] optionFiles = optionsDirectory.listFiles((dir, name) -> name.toLowerCase().contains(prefix));

        if (optionFiles != null && optionFiles.length > 0) {
            // If there's a match, search for the "Palette: " in the first matching file
            for (File optionFile : optionFiles) {
                String paletteValue = searchPaletteInTemplateFile(optionFile);
                System.out.println("Palette value: " + paletteValue);
                if (paletteValue != null) {
                    return paletteValue;
                }
            }
        }

        return null; // Return null if no matching file or "Palette: " found
    }

    public static void main(String[] args) {
        // Example usage:
        String directoryPath = "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/room_files";
        List<String[]> fileDataList = listTextFiles(directoryPath);

        // Print out the extracted data
        for (String[] fileData : fileDataList) {
            System.out.println("File Data:");
            for (String data : fileData) {
                System.out.println(data);
            }
            System.out.println("-----");
        }
    }
}