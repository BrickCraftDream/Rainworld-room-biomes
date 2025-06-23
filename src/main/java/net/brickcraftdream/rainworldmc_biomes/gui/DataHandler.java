package net.brickcraftdream.rainworldmc_biomes.gui;

import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

import static net.brickcraftdream.rainworldmc_biomes.gui.MainGui.*;

public class DataHandler {
    public static List<String> identifiers = new ArrayList<>();
    public static Map<String, String> dropdownValuesByIdentifier = new HashMap<>();

    public static final List<String> templateOptions = new ArrayList<>();
    public static List<String> screenOptions = new ArrayList<>();
    public static List<String> roomOptions = new ArrayList<>();
    public static List<String> regionOptions = new ArrayList<>();
    public static Map<String, Map<String, Double>> valuesMapMap = new HashMap<>();
    public static Map<String, String> contentValues = new HashMap<>();
    // Dropdowns
    public static MainGui.SearchableDropdown templateDropdown;
    public static MainGui.SearchableDropdown screenDropdown;
    public static MainGui.SearchableDropdown roomDropdown;
    public static MainGui.SearchableDropdown regionDropdown;
    // Lists
    private static List<Renderable> activeRenderables = new ArrayList<>();
    private static List<Renderable> inactiveRenderables = new ArrayList<>();
    private static List<GuiEventListener> guiContent = new ArrayList<>();
    private static List<MainGui.ClearingTextBox> textBoxes = new ArrayList<>();
    public static List<MainGui.SearchableDropdown> dropdowns = new ArrayList<>();
    // Element Values
    public static String paletteBoxContent = "0";
    public static String fadePaletteBoxContent = "0";
    public static String fadeStrengthBoxContent = "0.5";
    public static String grimeBoxContent = "0.0";
    public static String effectColorABoxContent = "000";
    public static String effectColorBBoxContent = "000";
    public static String dangerTypeContent = "None";

    private static String currentRegion = Component.translatable("gui.rainworld.select_region").getString();
    private static String currentRoom = Component.translatable("gui.rainworld.select_room").getString();
    private static String currentScreen = Component.translatable("gui.rainworld.select_screen").getString();
    private static String templateRoomCreateName = Component.translatable("gui.rainworld.template").getString();
    private static String templateRoomEditName = Component.translatable("gui.rainworld.template").getString();

    private static String lastPlacedRegion = "";
    private static String lastPlacedRoom = "";
    private static String lastPlacedScreen = "";

    public static BufferedImage palette1 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/palettes/palette" + DataHandler.paletteBoxContent + ".png"));
    public static BufferedImage palette2 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/palettes/palette" + DataHandler.fadePaletteBoxContent + ".png"));
    public static float opacity = Float.parseFloat(DataHandler.fadeStrengthBoxContent);


    private static final Deque<String> storage = new ArrayDeque<>();
    private static final Deque<String> lastRegions = new ArrayDeque<>();
    private static final Deque<String> lastRooms = new ArrayDeque<>();
    private static final Deque<String> lastScreens = new ArrayDeque<>();

    public static void sortStringsAlphabetically(List<String> strings) {
        if(strings == null || strings.isEmpty()) return;
        strings.sort(String.CASE_INSENSITIVE_ORDER);
    }

    public static List<String> getSortedStrings(List<String> strings) {
        return strings.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public static void init() {
        templateOptions.clear();
        //regionOptions.clear();
        templateOptions.add("No Template");
        List<String> regions = getSortedStrings(List.copyOf(exporter.getAllRegionNames()));
        //sortStringsAlphabetically(regions);
        for(String region : regions) {
            List<String> rooms = getSortedStrings(List.copyOf(exporter.getAllRoomNames(region)));
            //sortStringsAlphabetically(rooms);
            for(String room : rooms) {
                List<String> screens = getSortedStrings(List.copyOf(exporter.getAllScreenNames(region, room)));
                //sortStringsAlphabetically(screens);
                for(String screen : screens) {
                    Map<String, Double> valuesMap = convertToDoubleMap(exporter.getScreenProperties(region, room, screen));
                    valuesMapMap.put(region + "." + room + "." + screen, valuesMap);
                    templateOptions.add(region + "." + room + "." + screen);
                }
                Map<String, Double> valuesMap = convertToDoubleMap(exporter.getRoomProperties(region, room));
                valuesMapMap.put(region + "." + room, valuesMap);
                templateOptions.add(region + "." + room);
            }
        }
        for(String tmeplate : templateOptions) {
            if( tmeplate.length() > nameLength) {
                nameLength = tmeplate.length();
            }
        }
        //regionOptions.addAll(exporter.getAllRegionNames());
    }

    public static void addString(String newString) {
        storage.addFirst(newString); // Add the newest string to the "top"
        int maxLength = 5;
        if (storage.size() > maxLength) {
            storage.removeLast(); // Remove the oldest string if max length is exceeded
        }
    }

    public static void addRegion(String newString) {
        lastRegions.addFirst(newString); // Add the newest string to the "top"
        int maxLength = 5;
        if (lastRegions.size() > maxLength) {
            lastRegions.removeLast(); // Remove the oldest string if max length is exceeded
        }
    }

    public static void addRoom(String newString) {
        lastRooms.addFirst(newString); // Add the newest string to the "top"
        int maxLength = 5;
        if (lastRooms.size() > maxLength) {
            lastRooms.removeLast(); // Remove the oldest string if max length is exceeded
        }
    }

    public static void addScreen(String newString) {
        lastScreens.addFirst(newString); // Add the newest string to the "top"
        int maxLength = 5;
        if (lastScreens.size() > maxLength) {
            lastScreens.removeLast(); // Remove the oldest string if max length is exceeded
        }
    }

    public static void updateTextBoxes() {
        for(MainGui.ClearingTextBox box : textBoxes) {
            for(String template : valuesMapMap.keySet()) {
                if(template.toLowerCase().trim().equals(templateRoomCreateName.toLowerCase().trim())) {
                    for(Map.Entry<String, Double> entry : valuesMapMap.get(template).entrySet()) {
                        String key = entry.getKey();
                        double value = entry.getValue();
                        if(box.getIdentifier().equals(key)) {
                            if(box.identifier.equals("danger_type")) {
                                switch ((int) value) {
                                    case 0 -> box.setValue("None");
                                    case 1 -> box.setValue("Rain");
                                    case 2 -> box.setValue("Flood");
                                    case 3 -> box.setValue("Flood and Rain");
                                    default -> box.setValue("Unknown");
                                }
                            }
                            else {
                                box.setValue(String.valueOf((int) value));
                            }
                        }
                    }
                }
            }
        }
        for(MainGui.SearchableDropdown dropdown : dropdowns) {
            for(String template : valuesMapMap.keySet()) {
                if(template.toLowerCase().trim().equals(templateRoomCreateName.toLowerCase().trim())) {
                    for(Map.Entry<String, Double> entry : valuesMapMap.get(template).entrySet()) {
                        String key = entry.getKey();
                        double value = entry.getValue();
                        if(dropdown.identifier.equals(key)) {
                            //dropdown.setValue(String.valueOf((int) value));
                            switch ((int) value) {
                                case 0 -> dropdown.setValue("None");
                                case 1 -> dropdown.setValue("Rain");
                                case 2 -> dropdown.setValue("Flood");
                                case 3 -> dropdown.setValue("Flood and Rain");
                                default -> dropdown.setValue("Unknown");
                            }
                        }
                    }
                }
            }
        }
        palette1 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/palettes/palette" + DataHandler.paletteBoxContent + ".png"));
        palette2 = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/palettes/palette" + DataHandler.fadePaletteBoxContent + ".png"));
        opacity = Float.parseFloat(DataHandler.fadeStrengthBoxContent);
    }

    public static String getCurrentRegion() {
        return currentRegion;
    }

    public static String getLastPlacedRegion(){return lastPlacedRegion;}

    public static void setLastPlacedRegion(String lastPlacedRegion) {
        DataHandler.lastPlacedRegion = lastPlacedRegion;
    }

    public static void setCurrentRegion(String currentRegion) {
        System.out.println("Region before update: " + DataHandler.currentRegion);
        //if(!GuiComponents.mainGui$regionDropdown.getValue().equals(currentRegion)) currentRegion = GuiComponents.mainGui$regionDropdown.getValue();
        DataHandler.currentRegion = currentRegion;
        System.out.println("Region after update: " + DataHandler.currentRegion);
        addRegion(currentRegion);
    }

    public static String getCurrentRoom() {
        return currentRoom;
    }

    public static String getLastPlacedRoom(){return lastPlacedRoom;}

    public static void setLastPlacedRoom(String lastPlacedRoom) {
        DataHandler.lastPlacedRoom = lastPlacedRoom;
    }

    public static void setCurrentRoom(String currentRoom) {
        System.out.println("Room before update: " + DataHandler.currentRoom);
        //if(!GuiComponents.mainGui$roomDropdown.getValue().equals(currentRoom)) currentRoom = GuiComponents.mainGui$roomDropdown.getValue();
        DataHandler.currentRoom = currentRoom;
        System.out.println("Room after update: " + DataHandler.currentRoom);
        addRoom(currentRoom);
        //System.out.println(templateOptions.size());
        if(templateOptions.contains(currentRegion + "." + currentRoom) && !lastRooms.toArray()[0].toString().equals(currentRoom)) {
            setTemplateRoomCreateName(currentRegion + "." + currentRoom);
            setTemplateRoomEditName(currentRegion + "." + currentRoom);
            updateTextBoxes();
        }
        else if(templateOptions.isEmpty()) {
            init();
        }
    }

    public static String getCurrentScreen() {
        return currentScreen;
    }

    public static String getLastPlacedScreen(){return lastPlacedScreen;}

    public static void setLastPlacedScreen(String lastPlacedScreen) {
        DataHandler.lastPlacedScreen = lastPlacedScreen;
    }

    public static void setCurrentScreen(String currentScreen) {
        System.out.println("Screen before update: " + DataHandler.currentScreen);
        //if(!GuiComponents.mainGui$screenDropdown.getValue().equals(currentScreen)) currentScreen = GuiComponents.mainGui$screenDropdown.getValue();
        DataHandler.currentScreen = currentScreen;
        System.out.println("Screen after update: " + DataHandler.currentScreen);
        addScreen(currentScreen);
        //System.out.println(templateOptions.size());
        //System.out.println(currentRegion + "." + currentRoom + "." + currentScreen);
        //System.out.println(templateOptions.contains(currentRegion + "." + currentRoom + "." + currentScreen));
        //System.out.println(templateOptions.contains(currentRegion + "." + currentRoom));
        if(templateOptions.contains(currentRegion + "." + currentRoom + "." + currentScreen) && !lastScreens.toArray()[0].toString().equals(currentScreen)) {
            setTemplateRoomCreateName(currentRegion + "." + currentRoom + "." + currentScreen);
            setTemplateRoomEditName(currentRegion + "." + currentRoom + "." + currentScreen);
            updateTextBoxes();
        }
        else if(templateOptions.isEmpty()) {
            init();
        }
    }

    public static String getTemplateRoomCreateName() {
        return templateRoomCreateName;
    }

    public static void setTemplateRoomCreateName(String templateRoomCreateName) {
        DataHandler.templateRoomCreateName = templateRoomCreateName;
    }

    public static String getTemplateRoomEditName() {
        return templateRoomEditName;
    }

    public static void setTemplateRoomEditName(String templateRoomEditName) {
        DataHandler.templateRoomEditName = templateRoomEditName;
    }

    public static List<String> getTemplateOptions() {
        return templateOptions;
    }

    public static List<String> getScreenOptions() {
        return screenOptions;
    }

    public static List<String> getRoomOptions() {
        return roomOptions;
    }

    public static List<String> getRegionOptions() {
        return regionOptions;
    }

    public static List<Renderable> getActiveRenderables() {
        return DataHandler.activeRenderables;
    }

    public static void setActiveRenderables(List<Renderable> activeRenderables) {
        DataHandler.activeRenderables = activeRenderables;
    }

    public static void addRenderable(Renderable renderable) {
        DataHandler.activeRenderables.add(renderable);
        removeInactiveRenderable(renderable);
    }

    public static void removeRenderable(Renderable renderable) {
        DataHandler.activeRenderables.removeIf(r -> r.equals(renderable));
    }

    public static List<Renderable> getInactiveRenderables() {
        return DataHandler.inactiveRenderables;
    }

    public static void setInactiveRenderables(List<Renderable> inactiveRenderables) {
        DataHandler.inactiveRenderables = inactiveRenderables;
    }

    public static void addInactiveRenderable(Renderable renderable) {
        DataHandler.inactiveRenderables.add(renderable);
        removeRenderable(renderable);
    }

    public static void removeInactiveRenderable(Renderable renderable) {
        DataHandler.inactiveRenderables.removeIf(r -> r.equals(renderable));
    }

    public static List<GuiEventListener> getGuiContent() {
        return guiContent;
    }

    public static void setGuiContent(List<GuiEventListener> guiContent) {
        DataHandler.guiContent = guiContent;
    }

    public static void addGuiContent(GuiEventListener guiContent) {
        DataHandler.guiContent.add(guiContent);
    }

    public static List<MainGui.ClearingTextBox> getTextBoxes() {
        return textBoxes;
    }

    public static void setTextBoxes(List<MainGui.ClearingTextBox> textBoxes) {
        DataHandler.textBoxes = textBoxes;
    }

    public static void addTextBox(MainGui.ClearingTextBox textBox) {
        DataHandler.textBoxes.add(textBox);
    }

    public static List<String> getDangerTypeOptions() {
        return List.of(" None", " Rain", " Flood", " Flood and Rain");
    }

    public static String getCurrentDangerType() {
        return dangerTypeContent;
    }

    public static void setDangerType(String s) {
        dangerTypeContent = s;
    }
}
