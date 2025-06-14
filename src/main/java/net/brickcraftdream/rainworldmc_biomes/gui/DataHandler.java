package net.brickcraftdream.rainworldmc_biomes.gui;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    static String paletteBoxContent = "0";
    static String fadePaletteBoxContent = "0";
    static String fadeStrengthBoxContent = "0.5";
    static String grimeBoxContent = "0.0";
    static String effectColorABoxContent = "000";
    static String effectColorBBoxContent = "000";
    static String dangerTypeContent = "None";

    private static String currentRegion = Component.translatable("gui.rainworld.select_region").getString();
    private static String currentRoom = Component.translatable("gui.rainworld.select_room").getString();
    private static String currentScreen = Component.translatable("gui.rainworld.select_screen").getString();
    private static String templateRoomCreateName = Component.translatable("gui.rainworld.template").getString();
    private static String templateRoomEditName = Component.translatable("gui.rainworld.template").getString();

    public static void init() {
        templateOptions.clear();
        //regionOptions.clear();
        templateOptions.add("No Template");
        for(String region : exporter.getAllRegionNames()) {
            for(String room : exporter.getAllRoomNames(region)) {
                for(String screen : exporter.getAllScreenNames(region, room)) {
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

    public static void updateTextBoxes() {
        //System.out.println("Updating textboxes " + textBoxes.size());
        for(MainGui.ClearingTextBox box : textBoxes) {
            //System.out.println("Updating " + box.getIdentifier());
            for(String template : valuesMapMap.keySet()) {
                if(template.toLowerCase().trim().equals(templateRoomCreateName.toLowerCase().trim())) {
                    //System.out.println("Template: " + template + " equals " + templateRoomCreateName.toLowerCase().trim());
                    for(Map.Entry<String, Double> entry : valuesMapMap.get(template).entrySet()) {
                        //System.out.println("Entry: " + entry.getKey() + " , " + entry.getValue());
                        String key = entry.getKey();
                        double value = entry.getValue();
                        if(box.getIdentifier().equals(key)) {
                            box.setValue(String.valueOf((int) value));
                            //System.out.println("Updated value for " + key + " to " + value);
                        }
                    }
                }
            }
        }
    }

    public static String getCurrentRegion() {
        return currentRegion;
    }

    public static void setCurrentRegion(String currentRegion) {
        DataHandler.currentRegion = currentRegion;
    }

    public static String getCurrentRoom() {
        return currentRoom;
    }

    public static void setCurrentRoom(String currentRoom) {
        DataHandler.currentRoom = currentRoom;
        System.out.println(templateOptions.size());
        if(templateOptions.contains(currentRegion + "." + currentRoom)) {
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

    public static void setCurrentScreen(String currentScreen) {
        DataHandler.currentScreen = currentScreen;
        System.out.println(templateOptions.size());
        System.out.println(currentRegion + "." + currentRoom + "." + currentScreen);
        System.out.println(templateOptions.contains(currentRegion + "." + currentRoom + "." + currentScreen));
        System.out.println(templateOptions.contains(currentRegion + "." + currentRoom));
        if(templateOptions.contains(currentRegion + "." + currentRoom + "." + currentScreen)) {
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
        return List.of("None", "Rain", "Flood", "Flood and Rain");
    }

    public static String getCurrentDangerType() {
        return dangerTypeContent;
    }

    public static void setDangerType(String s) {
        dangerTypeContent = s;
    }
}
