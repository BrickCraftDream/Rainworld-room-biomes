/*package net.brickcraftdream.room_creation_tool.gui;

import net.brickcraftdream.room_creation_tool.templates.JsonExporter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.brickcraftdream.room_creation_tool.gui.DataHandler.*;
import static net.brickcraftdream.room_creation_tool.gui.GuiComponents.*;

public class MainGui_old extends Screen {
    // Constants
    private final Player player;
    private GuiType type;
    public static JsonExporter exporter = new JsonExporter("/assets/room_creation_tool/data/biomes.json");

    // Size constants
    public static int WIDGET_WIDTH = 50;
    public static int BUTTON_HEIGHT = 20;
    public static int SPACING = 22;
    public static int RESET_BUTTON_WIDTH = 20;
    public static int nameLength = 0;
    public static int regionLength = 13;
    public static int roomLength = 11;
    public static int screenLength = 13;
    public static int leftMargin = 20;
    public static int topMargin = 40;
    public static int textMargin = 4;
    public static int scrollbarWidth = 6;

    private static boolean hasFinalValueSelected_edit = false;
    private static boolean hasFinalValueSelected_create = false;

    public enum GuiType {
        ROOM_CREATE,
        ROOM_REGION_SELECT,
        ROOM_EDIT
    }

    public MainGui_old(Player player, GuiType type) {
        super(Component.translatable("gui.room_creation_tool.main"));
        this.type = type;
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        DataHandler.init();
        setUpGUIContents();
        GuiComponents.init(type);

        // Set left and top margins
        this.leftMargin = 20;
        this.topMargin = 40;

        switch (type) {
            case ROOM_CREATE -> initRoomCreateGui();
            case ROOM_REGION_SELECT -> initRoomRegionSelectGui();
            case ROOM_EDIT -> initRoomEditGui();
        }
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> @NotNull T addRenderableWidget(T guiEventListener) {
        DataHandler.addRenderable(guiEventListener);
        DataHandler.addGuiContent(guiEventListener);
        return super.addRenderableWidget(guiEventListener);
    }

    public static Map<String, Double> convertToDoubleMap(Map<String, Object> input) {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Double) {
                result.put(entry.getKey(), (Double) value);
            } else if (value instanceof Number) {
                result.put(entry.getKey(), ((Number) value).doubleValue());
            } else {
                System.out.println("Skipping non-numeric value for key: " + entry.getKey());
            }
        }
        return result;
    }

    public void sanityCheck() {
        if(roomCreateGui$title == null) roomCreateGui$title = new StringWidget(leftMargin, roomCreateGui$title$offsetY, WIDGET_WIDTH * 4, BUTTON_HEIGHT, Component.translatable("gui.room_creation_tool.create_title"), this.font).alignLeft();
        if(roomCreateGui$templateDropdown == null) roomCreateGui$templateDropdown = new SearchableDropdown(leftMargin, roomCreateGui$templateDropdown$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT, Component.literal(DataHandler.getTemplateRoomCreateName()), DataHandler.getTemplateOptions(), this.font, this::onTemplateSelected, "template.room_create");
        if(roomCreateGui$paletteBox == null) roomCreateGui$paletteBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$paletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "palette");
        if(roomCreateGui$paletteResetButton == null) roomCreateGui$paletteResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$paletteBox.setValue(DataHandler.paletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$paletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomCreateGui$fadePaletteBox == null) roomCreateGui$fadePaletteBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$fadePaletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_palette");
        if(roomCreateGui$fadePaletteResetButton == null) roomCreateGui$fadePaletteResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$fadePaletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomCreateGui$fadeStrengthBox == null) roomCreateGui$fadeStrengthBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$fadeStrengthBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_strength");
        if(roomCreateGui$fadeStrengthResetButton == null) roomCreateGui$fadeStrengthResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$fadeStrengthBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomCreateGui$grimeBox == null) roomCreateGui$grimeBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$grimeBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "grime");
        if(roomCreateGui$grimeResetButton == null) roomCreateGui$grimeResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$grimeBox.setValue(DataHandler.grimeBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$grimeBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomCreateGui$effecColorABox == null) roomCreateGui$effecColorABox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$effectColorABox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_a");
        if(roomCreateGui$effectColorAResetButton == null) roomCreateGui$effectColorAResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$effecColorABox.setValue(DataHandler.effectColorABoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$effectColorABox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomCreateGui$effectColorBBox == null) roomCreateGui$effectColorBBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$effectColorBBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_b");
        if(roomCreateGui$effectColorBResetButton == null) roomCreateGui$effectColorBResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$effectColorBBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomCreateGui$createButton == null) roomCreateGui$createButton = Button.builder(Component.translatable("gui.room_creation_tool.create"), button -> {/* Handle room creation *//*}).bounds(leftMargin, roomCreateGui$createButton$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.create_tooltip"))).build();

        if(roomRegionSelect$title == null) roomRegionSelect$title = new StringWidget(leftMargin, roomRegionSelect$title$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.room_creation_tool.select_title"), this.font).alignLeft();
        if(roomRegionSelect$regionDropdown == null) roomRegionSelect$regionDropdown = new SearchableDropdown(leftMargin, roomRegionSelect$regionDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentRegion()), DataHandler.getRegionOptions(), this.font, this::onRegionSelected, "region.room_region_select");
        if(roomRegionSelect$roomDropdown == null) roomRegionSelect$roomDropdown = new SearchableDropdown(leftMargin * 2, roomRegionSelect$roomDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentRoom()), DataHandler.getRoomOptions(), this.font, this::onRoomSelected, "room.room_region_select");
        if(roomRegionSelect$screenDropdown == null) roomRegionSelect$screenDropdown = new SearchableDropdown(leftMargin * 3, roomRegionSelect$screenDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentScreen() != null ? DataHandler.getCurrentScreen() : ""), DataHandler.getScreenOptions(), this.font, this::onScreenSelected, "screen.room_region_select");
        if(roomRegionSelect$editButton == null) roomRegionSelect$editButton = Button.builder(Component.translatable("gui.room_creation_tool.edit"), button -> minecraft.setScreen(new MainGui_old(player, GuiType.ROOM_EDIT))).bounds(leftMargin, roomRegionSelect$editButton$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.edit_tooltip"))).build();

        if(roomEditGui$title == null) roomEditGui$title = new StringWidget(leftMargin, roomEditGui$title$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.room_creation_tool.edit_title"), this.font).alignLeft();
        if(roomEditGui$templateDropdown == null) roomEditGui$templateDropdown = new SearchableDropdown(leftMargin, roomEditGui$templateDropdown$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT, Component.literal(DataHandler.getTemplateRoomEditName()), DataHandler.getTemplateOptions(), this.font, this::onTemplateSelected, "template.room_edit");
        if(roomEditGui$toggleBiomeEditMode == null) roomEditGui$toggleBiomeEditMode = Checkbox.builder(Component.translatable("gui.room_creation_tool.edit_all_biomes"), this.font).pos(leftMargin, roomEditGui$toggleBiomeEditMode$offsetY).selected(false).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.edit_all_biomes"))).build();
        if(roomEditGui$paletteBox == null) roomEditGui$paletteBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$paletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "palette");
        if(roomEditGui$paletteResetButton == null) roomEditGui$paletteResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$paletteBox.setValue(DataHandler.paletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$paletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomEditGui$fadePaletteBox == null) roomEditGui$fadePaletteBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$fadePaletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_palette");
        if(roomEditGui$fadePaletteResetButton == null) roomEditGui$fadePaletteResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$fadePaletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomEditGui$fadeStrengthBox == null) roomEditGui$fadeStrengthBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$fadeStrengthBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_strength");
        if(roomEditGui$fadeStrengthResetButton == null) roomEditGui$fadeStrengthResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$fadeStrengthBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomEditGui$grimeBox == null) roomEditGui$grimeBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$grimeBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "grime");
        if(roomEditGui$grimeResetButton == null) roomEditGui$grimeResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$grimeBox.setValue(DataHandler.grimeBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$grimeBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomEditGui$effecColorABox == null) roomEditGui$effecColorABox = new ClearingTextBox(this.font, leftMargin, roomEditGui$effectColorABox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_a");
        if(roomEditGui$effectColorAResetButton == null) roomEditGui$effectColorAResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$effecColorABox.setValue(DataHandler.effectColorABoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$effectColorABox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomEditGui$effectColorBBox == null) roomEditGui$effectColorBBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$effectColorBBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_b");
        if(roomEditGui$effectColorBResetButton == null) roomEditGui$effectColorBResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$effectColorBBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        if(roomEditGui$saveButton == null) roomEditGui$saveButton = Button.builder(Component.translatable("gui.room_creation_tool.save"), button -> {/* Save changes *//*}).bounds(leftMargin, roomEditGui$saveButton$offsetY, WIDGET_WIDTH / 2 - 5, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.save_tooltip"))).build();
        if(roomEditGui$backButton == null) roomEditGui$backButton = Button.builder(Component.translatable("gui.room_creation_tool.back"), button -> minecraft.setScreen(new MainGui_old(player, GuiType.ROOM_REGION_SELECT))).bounds(leftMargin + WIDGET_WIDTH / 2 + 5, roomEditGui$backButton$offsetY, WIDGET_WIDTH / 2 - 5, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.back_tooltip"))).build();
        if(roomEditGui$hideGuiButton == null) roomEditGui$hideGuiButton = Button.builder(Component.translatable("gui.room_creation_tool.hide"), button -> {/* Hide GUI logic *//*}).bounds(leftMargin, roomEditGui$hideGuiButton$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.hide_tooltip"))).build();

        DataHandler.templateDropdown = type == MainGui_old.GuiType.ROOM_CREATE ? roomCreateGui$templateDropdown : roomEditGui$templateDropdown;
        DataHandler.regionDropdown = roomRegionSelect$regionDropdown;
        DataHandler.roomDropdown = roomRegionSelect$roomDropdown;
        DataHandler.screenDropdown = roomRegionSelect$screenDropdown;
    }

    private void initRoomCreateGui() {
        //sanityCheck();
        this.addRenderableWidget(roomCreateGui$title);
        this.addRenderableWidget(roomCreateGui$templateDropdown);
        this.addRenderableWidget(roomCreateGui$paletteBox);
        this.addRenderableWidget(roomCreateGui$paletteResetButton);
        this.addRenderableWidget(roomCreateGui$fadePaletteBox);
        this.addRenderableWidget(roomCreateGui$fadePaletteResetButton);
        this.addRenderableWidget(roomCreateGui$fadeStrengthBox);
        this.addRenderableWidget(roomCreateGui$fadeStrengthResetButton);
        this.addRenderableWidget(roomCreateGui$grimeBox);
        this.addRenderableWidget(roomCreateGui$grimeResetButton);
        this.addRenderableWidget(roomCreateGui$effecColorABox);
        this.addRenderableWidget(roomCreateGui$effectColorAResetButton);
        this.addRenderableWidget(roomCreateGui$effectColorBBox);
        this.addRenderableWidget(roomCreateGui$effectColorBResetButton);
        this.addRenderableWidget(roomCreateGui$createButton);

        DataHandler.addTextBox(roomCreateGui$paletteBox);
        DataHandler.addTextBox(roomCreateGui$fadePaletteBox);
        DataHandler.addTextBox(roomCreateGui$fadeStrengthBox);
        DataHandler.addTextBox(roomCreateGui$grimeBox);
        DataHandler.addTextBox(roomCreateGui$effecColorABox);
        DataHandler.addTextBox(roomCreateGui$effectColorBBox);
    }

    private void initRoomRegionSelectGui() {
        //sanityCheck();
        this.addRenderableWidget(roomRegionSelect$title);
        this.addRenderableWidget(roomRegionSelect$regionDropdown);
        this.addRenderableWidget(roomRegionSelect$roomDropdown);
        this.addRenderableWidget(roomRegionSelect$screenDropdown);
        this.addRenderableWidget(roomRegionSelect$editButton);
    }

    private void initRoomEditGui() {
        //sanityCheck();
        this.addRenderableWidget(roomEditGui$title);
        this.addRenderableWidget(roomEditGui$templateDropdown);
        this.addRenderableWidget(roomEditGui$toggleBiomeEditMode);
        this.addRenderableWidget(roomEditGui$paletteBox);
        this.addRenderableWidget(roomEditGui$paletteResetButton);
        this.addRenderableWidget(roomEditGui$fadePaletteBox);
        this.addRenderableWidget(roomEditGui$fadePaletteResetButton);
        this.addRenderableWidget(roomEditGui$fadeStrengthBox);
        this.addRenderableWidget(roomEditGui$fadeStrengthResetButton);
        this.addRenderableWidget(roomEditGui$grimeBox);
        this.addRenderableWidget(roomEditGui$grimeResetButton);
        this.addRenderableWidget(roomEditGui$effecColorABox);
        this.addRenderableWidget(roomEditGui$effectColorAResetButton);
        this.addRenderableWidget(roomEditGui$effectColorBBox);
        this.addRenderableWidget(roomEditGui$effectColorBResetButton);
        this.addRenderableWidget(roomEditGui$saveButton);
        this.addRenderableWidget(roomEditGui$backButton);
        this.addRenderableWidget(roomEditGui$hideGuiButton);

        DataHandler.addTextBox(roomEditGui$paletteBox);
        DataHandler.addTextBox(roomEditGui$fadePaletteBox);
        DataHandler.addTextBox(roomEditGui$fadeStrengthBox);
        DataHandler.addTextBox(roomEditGui$grimeBox);
        DataHandler.addTextBox(roomEditGui$effecColorABox);
        DataHandler.addTextBox(roomEditGui$effectColorBBox);
    }

    // Callback methods for dropdowns
    private void onTemplateSelected(String template) {
        String[] parts = template.split("\\.");
        DataHandler.setCurrentRegion(parts.length > 0 ? parts[0] : "");
        DataHandler.setCurrentRoom(parts.length > 1 ? parts[1] : "");
        DataHandler.setCurrentScreen(parts.length > 2 ? parts[2] : "");
        DataHandler.setTemplateRoomEditName(template);
        DataHandler.setTemplateRoomCreateName(template);
        //DataHandler.currentRegion = parts.length > 0 ? parts[0] : "";
        //DataHandler.currentRoom = parts.length > 1 ? parts[1] : "";
        //DataHandler.currentScreen = parts.length > 2 ? parts[2] : "";
        // Handle template selection
    }

    private void onRoomSelected(String room) {
        //DataHandler.currentRoom = room;
        DataHandler.setCurrentRoom(room);
        // Handle room selection
    }

    private void onRegionSelected(String region) {
        //DataHandler.currentRegion = region;
        DataHandler.setCurrentRegion(region);
        // Handle region selection
    }

    private void onScreenSelected(String screen) {
        //DataHandler.currentScreen = screen;
        DataHandler.setCurrentScreen(screen);
        // Handle screen selection
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        // Nothing should be rendered as background to allow for a clear view of the world
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(guiGraphics == null) {
            return;
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render any additional elements or dropdown expansions
        if (DataHandler.templateDropdown != null && (type == GuiType.ROOM_CREATE || type == GuiType.ROOM_EDIT)) {
            DataHandler.templateDropdown.renderExpanded(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (DataHandler.regionDropdown != null && type == GuiType.ROOM_REGION_SELECT) {
            DataHandler.regionDropdown.renderExpanded(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (DataHandler.roomDropdown != null && type == GuiType.ROOM_REGION_SELECT) {
            DataHandler.roomDropdown.renderExpanded(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (DataHandler.screenDropdown != null && type == GuiType.ROOM_REGION_SELECT) {
            DataHandler.screenDropdown.renderExpanded(guiGraphics, mouseX, mouseY, partialTick);
        }
        for(GuiEventListener guiEventListener : DataHandler.getGuiContent()) {
            if(guiEventListener == null) {
                continue;
            }
            if(guiEventListener instanceof SearchableDropdown box) {
                String name = !DataHandler.getCurrentRegion().isEmpty() && !DataHandler.getCurrentRegion().equals(Component.translatable("gui.room_creation_tool.select_region").getString())
                                    && !DataHandler.getCurrentRoom().isEmpty() && !DataHandler.getCurrentRoom().equals(Component.translatable("gui.room_creation_tool.select_room").getString())
                                    && !DataHandler.getCurrentScreen().isEmpty() && !DataHandler.getCurrentScreen().equals(Component.translatable("gui.room_creation_tool.select_screen").getString())
                                    ? DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() + "." + DataHandler.getCurrentScreen() :
                        (!DataHandler.getCurrentRegion().isEmpty() && !DataHandler.getCurrentRegion().equals(Component.translatable("gui.room_creation_tool.select_region").getString())
                                    && !DataHandler.getCurrentRoom().isEmpty() && !DataHandler.getCurrentRoom().equals(Component.translatable("gui.room_creation_tool.select_room").getString())
                                    ? DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() :
                        (!DataHandler.getCurrentRegion().isEmpty() && !DataHandler.getCurrentRegion().equals(Component.translatable("gui.room_creation_tool.select_region").getString())
                                    ? DataHandler.getCurrentRegion() : "")
                        );
                DataHandler.setTemplateRoomEditName(name);
                DataHandler.setTemplateRoomCreateName(name);
                if(box.identifier.equals("template.room_edit")) {
                    if(box.getValue().length() != nameLength && !box.getValue().isEmpty() && !box.expanded) {
                        nameLength = box.getValue().length();
                    }
                    else if(box.getValue().isEmpty() && !box.expanded) {
                        nameLength = 13;
                    }
                    box.setWidth(textMargin + nameLength * 6 + textMargin + scrollbarWidth);
                }
                if(box.identifier.equals("template.room_create")) {
                    if(box.getValue().length() != nameLength && !box.getValue().isEmpty() && !box.expanded) {
                        nameLength = box.getValue().length();
                    }
                    else if(box.getValue().isEmpty() && !box.expanded) {
                        nameLength = 13;
                    }
                    box.setWidth(textMargin + nameLength * 6 + textMargin + scrollbarWidth);
                }
                if(box.identifier.equals("region.room_region_select")) {
                    if(box.getValue().length() != regionLength && !box.getValue().isEmpty() && !box.expanded) {
                        regionLength = box.getValue().length();
                    }
                    else if(box.getValue().isEmpty() && !box.expanded) {
                        regionLength = 13;
                    }
                    box.setWidth(textMargin + regionLength * 6 + textMargin + scrollbarWidth);
                    //20 + 4 + (7 * 6) + 4 = 20 + 4 + 42 + 4 = 70
                }
                if(box.identifier.equals("room.room_region_select")) {
                    if(box.getValue().length() != roomLength && !box.getValue().isEmpty() && !box.expanded) {
                        roomLength = box.getValue().length();
                    }
                    else if(box.getValue().isEmpty() && !box.expanded) {
                        roomLength = 13;
                    }
                    box.setWidth(textMargin + roomLength * 6 + textMargin + scrollbarWidth);
                    box.setX(leftMargin + textMargin + regionLength * 6 + textMargin + scrollbarWidth);
                    //20 + 4 + (7 * 6) + 4 = 20 + 4 + 42 + 4 = 70
                }
                if(box.identifier.equals("screen.room_region_select")) {
                    if(box.getValue().length() != screenLength && !box.getValue().isEmpty() && !box.expanded) {
                        screenLength = box.getValue().length();
                    }
                    else if(box.getValue().isEmpty() && !box.expanded) {
                        screenLength = 13;
                    }
                    box.setWidth(textMargin + screenLength * 6 + textMargin + scrollbarWidth);
                    box.setX(leftMargin + textMargin + regionLength * 6 + roomLength * 6 + textMargin + scrollbarWidth * 3);
                }

                if(box.identifier.equals("screen.room_region_select")) {
                    if(((!DataHandler.regionDropdown.getValue().isEmpty() && !DataHandler.roomDropdown.getValue().isEmpty()) || (!DataHandler.regionDropdown.getValue().isEmpty() && !DataHandler.roomDropdown.getValue().isEmpty()))) {
                        if(!box.getValue().contains(Component.translatable("gui.room_creation_tool.select_screen").getString())) {
                            box.allOptions = List.copyOf(exporter.getAllScreenNames(DataHandler.regionDropdown.getValue(), DataHandler.roomDropdown.getValue()));
                        }
                        else {
                            box.allOptions = List.copyOf(DataHandler.getScreenOptions());
                        }
                    }
                }
                if(box.identifier.equals("room.room_region_select")) {
                    if(!DataHandler.regionDropdown.getValue().isEmpty() || !DataHandler.regionDropdown.getValue().isEmpty()) {
                        if(!box.getValue().contains(Component.translatable("gui.room_creation_tool.select_room").getString())) {
                            box.allOptions = List.copyOf(exporter.getAllRoomNames(DataHandler.regionDropdown.getValue()));
                        }
                        else {
                            box.allOptions = List.copyOf(DataHandler.getRoomOptions());
                        }
                    }
                }
                if(box.identifier.equals("region.room_region_select")) {
                    if(!box.getValue().contains(Component.translatable("gui.room_creation_tool.select_region").getString())) {
                        box.allOptions = List.copyOf(exporter.getAllRegionNames());
                    }
                    else {
                        box.allOptions = List.copyOf(DataHandler.getRegionOptions());
                    }
                }

                if(box.identifier.equals("screen.room_region_select")) {
                    if(!box.expanded && !box.getValue().equals(DataHandler.getCurrentScreen()) && !box.getValue().equals(Component.translatable("gui.room_creation_tool.select_screen").getString())) {
                        //box.setValue(currentScreen);
                    }
                }
                if(box.identifier.equals("room.room_region_select")) {
                    if(!box.expanded && !box.getValue().equals(DataHandler.getCurrentRoom()) && !box.getValue().equals(Component.translatable("gui.room_creation_tool.select_room").getString())) {
                        //box.setValue(currentRoom);
                    }
                }
                if(box.identifier.equals("region.room_region_select")) {
                    if(!box.expanded && !box.getValue().equals(DataHandler.getCurrentRoom()) && !box.getValue().equals(Component.translatable("gui.room_creation_tool.select_region").getString())) {
                        //box.setValue(currentRoom);
                    }
                }

                if(box.identifier.equals("template.room_edit")) {
                    String boxValue = box.getValue();
                    if (!hasFinalValueSelected_edit && box.expanded) {
                        if (!DataHandler.getCurrentRegion().isEmpty() && !DataHandler.getCurrentRegion().isBlank() && !DataHandler.getCurrentRegion().equals(Component.translatable("gui.room_creation_tool.select_region").getString()) && !box.getValue().contains(DataHandler.getCurrentRegion())) {
                            if (!DataHandler.getCurrentRoom().isEmpty() && !DataHandler.getCurrentRoom().isBlank() && !DataHandler.getCurrentRoom().equals(Component.translatable("gui.room_creation_tool.select_room").getString()) && !box.getValue().contains(DataHandler.getCurrentRoom())) {
                                if (!DataHandler.getCurrentScreen().isEmpty() && !DataHandler.getCurrentScreen().isBlank() && !DataHandler.getCurrentScreen().equals(Component.translatable("gui.room_creation_tool.select_screen").getString()) && !box.getValue().contains(DataHandler.getCurrentScreen())) {
                                    //box.setValue(currentRegion + "." + currentRoom + "." + currentScreen);
                                    //hasFinalValueSelected_edit = true;
                                    //box.mouseClicked(93.0, 90.5, 0);
                                    //box.updateInternalStuff(0, List.of(currentRegion + "." + currentRoom + "." + currentScreen));
                                } else {
                                    //box.updateInternalStuff(0, List.of(currentRegion + "." + currentRoom + (boxValue.length() > 0 ? "." + boxValue : "")));
                                    //box.setValue(currentRegion + "." + currentRoom + "." + boxValue);
                                }
                            } else {
                                //box.updateInternalStuff(0, List.of(currentRegion + (boxValue.length() > 0 ? "." + boxValue : "")));
                                //box.setValue(currentRegion + "." + boxValue);
                            }
                        } else {
                            //box.updateInternalStuff(0, List.of(boxValue.length() > 0 ? "." + boxValue : ""));
                            //box.setValue(boxValue);
                        }
                    }
                }
                if(box.identifier.equals("template.room_create")) {
                    String boxValue = box.getValue();
                    if (!hasFinalValueSelected_create && box.expanded) {
                        if (!DataHandler.getCurrentRegion().isEmpty() && !DataHandler.getCurrentRegion().isBlank() && !DataHandler.getCurrentRegion().equals(Component.translatable("gui.room_creation_tool.select_region").getString()) && !box.getValue().contains(DataHandler.getCurrentRegion())) {
                            if (!DataHandler.getCurrentRoom().isEmpty() && !DataHandler.getCurrentRoom().isBlank() && !DataHandler.getCurrentRoom().equals(Component.translatable("gui.room_creation_tool.select_room").getString()) && !box.getValue().contains(DataHandler.getCurrentRoom())) {
                                if (!DataHandler.getCurrentScreen().isEmpty() && !DataHandler.getCurrentScreen().isBlank() && !DataHandler.getCurrentScreen().equals(Component.translatable("gui.room_creation_tool.select_screen").getString()) && !box.getValue().contains(DataHandler.getCurrentScreen())) {
                                    //box.setValue(currentRegion + "." + currentRoom + "." + currentScreen);
                                    //hasFinalValueSelected_create = true;
                                    // box.mouseClicked(93.0, 90.5, 0);
                                    //box.updateInternalStuff(0, List.of(box.getValue()));
                                    //box.updateInternalStuff(0, List.of(currentRegion + "." + currentRoom + "." + currentScreen));
                                    //contentValues.put(box.identifier, currentRegion + "." + currentRoom + "." + currentScreen);
                                } else {
                                    //box.updateInternalStuff(0, List.of(currentRegion + "." + currentRoom + (boxValue.length() > 0 ? "." + boxValue : "")));
                                    //box.setValue(currentRegion + "." + currentRoom + "." + boxValue);
                                    //contentValues.put(box.identifier, currentRegion + "." + currentRoom + (boxValue.length() > 0 ? "." + boxValue : ""));
                                }
                            } else {
                                //box.updateInternalStuff(0, List.of(currentRegion + (boxValue.length() > 0 ? "." + boxValue : "")));
                                //box.setValue(currentRegion + "." + boxValue);
                                //contentValues.put(box.identifier, currentRegion + (boxValue.length() > 0 ? "." + boxValue : ""));
                            }
                        } else {
                            //box.updateInternalStuff(0, List.of(boxValue.length() > 0 ? "." + boxValue : ""));
                            //box.setValue(boxValue);
                            //contentValues.put(box.identifier, boxValue.length() > 0 ? boxValue : "");
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public class ClearingTextBox extends EditBox {
        private String lastValue = getValue();
        public String identifier = "";

        public ClearingTextBox(Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component component) {
            super(font, x, y, width, height, editBox, component);
        }

        public ClearingTextBox(Font font, int x, int y, int width, int height, Component component, String identifier) {
            super(font, x, y, width, height, component);
            this.identifier = identifier;
            List<ClearingTextBox> boses = new ArrayList<>();
            for(ClearingTextBox box : DataHandler.getTextBoxes()) {
                if(box.getIdentifier().equals(identifier)) {
                    continue;
                }
                else {
                    boses.add(box);
                }
            }
            boses.add(this);
            DataHandler.getTextBoxes().clear();
            for(ClearingTextBox box : boses) {
                DataHandler.addTextBox(box);
            }
            //DataHandler.setTextBoxes(boses);

        }

        public ClearingTextBox(Font font, int x, int y, Component component) {
            super(font, x, y, component);
        }

        @Override
        public void setValue(String string) {
            lastValue = string;
            switch (identifier) {
                case "palette":
                    DataHandler.paletteBoxContent = string;
                    break;
                case "fade_palette":
                    DataHandler.fadePaletteBoxContent = string;
                    break;
                case "grime":
                    DataHandler.grimeBoxContent = string;
                    break;
                case "effect_color_a":
                    DataHandler.effectColorABoxContent = string;
                    break;
                case "effect_color_b":
                    DataHandler.effectColorBBoxContent = string;
                    break;
                default:
                    break;
            }
            super.setValue(string);
        }

        public void setReplacingValue(String string) {
            super.setValue(string);
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean result = super.mouseClicked(mouseX, mouseY, button);
            if (isMouseOver(mouseX, mouseY)) {
                setReplacingValue("");
            }
            else {
                if(getValue().length() > 0) {
                    lastValue = getValue();
                }
                else {
                    setValue(lastValue);
                    setFocused(false);
                }
            }
            return result;
        }

        @Override
        public void insertText(String string) {
            if(getIdentifier().equals("palette")
                    || getIdentifier().equals("fade_palette")
                    || getIdentifier().equals("effect_color_a")
                    || getIdentifier().equals("effect_color_b")) {
                if(!string.matches("^\\d+$")) {
                    // If the string is not a number, do not insert it
                    return;
                }
                else {
                    super.insertText(string);
                }
            }
            else if(getIdentifier().equals("fade_strength") || getIdentifier().equals("grime")) {
                if(!string.matches("^\\d+(\\.\\d+)?$")) {
                    // If the string is not a number or a decimal, do not insert it
                    return;
                }
                else {
                    super.insertText(string);
                }
            }
            else {
                super.insertText(string);
            }
        }
    }

    // Searchable dropdown implementation
    public class SearchableDropdown extends EditBox {
        public List<String> allOptions;
        private final Consumer<String> selectionCallback;
        private final Component placeholder;
        private final int dropdownHeight;
        private boolean expanded = false;
        private List<String> filteredOptions;
        private int scrollOffset = 0;
        private final int maxVisibleOptions = 9;
        private int selectedIndex = -1;
        private int xOffset = -12345;
        private int addedOffset = 0;
        public String identifier = "";

        private boolean isExpanded = false;

        private float mouseX, mouseY = 0;
        private float clickX, clickY = 0;

        public SearchableDropdown(int x, int y, int width, int height,
                                  Component placeholder, List<String> options,
                                  Font font, Consumer<String> selectionCallback, String identifier) {
            super(font, x, y, width, height, Component.empty());
            this.placeholder = placeholder;
            //if(this.placeholder != null && !expanded) {
                //this.setValue(this.placeholder.getString());
            //}
            this.allOptions = identifier.equals("region.room_region_select") ? List.copyOf(exporter.getAllRegionNames()) : options;
            if(identifier.equals("room.room_region_select")) {
                //this.allOptions = List.copyOf(exporter.getAllRoomNames(allOptions.get(0)));
                this.allOptions = List.of("Select a region first");
            }
            if(identifier.equals("screen.room_region_select")) {
                this.allOptions = List.of("Select a room first");//List.copyOf(exporter.getAllScreenNames(allOptions.get(1), List.copyOf(exporter.getAllRoomNames(allOptions.get(1))).get(0)));
            }
            this.filteredOptions = new ArrayList<>(allOptions);
            this.selectionCallback = selectionCallback;
            this.dropdownHeight = maxVisibleOptions * height;
            DataHandler.contentValues.put(identifier, placeholder.getString());

            this.setMaxLength(100);
            this.setHint(placeholder);

            // Filter options based on input
            this.setResponder(text -> {
                if (text.isEmpty()) {
                    this.filteredOptions = new ArrayList<>(allOptions);
                } else {
                    this.filteredOptions = allOptions.stream()
                            .filter(option -> option.toLowerCase().contains(text.toLowerCase()))
                            .collect(Collectors.toList());
                }
                this.scrollOffset = 0;
                expandDropdown();
            });
            this.identifier = identifier;
        }

        public void updateInternalStuff(int selectedIndex, List<String> filteredOptions) {
            for(GuiEventListener guiEventListener : DataHandler.getGuiContent()) {
                if(guiEventListener instanceof ClearingTextBox box) {
                    for(String template : DataHandler.valuesMapMap.keySet()) {
                        if(template.toLowerCase().trim().equals(filteredOptions.get(selectedIndex).toLowerCase().trim())) {
                            for(Map.Entry<String, Double> entry : DataHandler.valuesMapMap.get(template).entrySet()) {
                                String key = entry.getKey();
                                double value = entry.getValue();
                                if(box.getIdentifier().equals(key)) {
                                    box.setValue(String.valueOf((int) value));
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (expanded) {
                int optionHeight = BUTTON_HEIGHT;
                int startY = this.getY() + BUTTON_HEIGHT;

                // Check if clicking on scrollbar
                if (filteredOptions.size() > maxVisibleOptions &&
                        mouseX >= this.getX() + this.width - 6 &&
                        mouseX <= this.getX() + this.width &&
                        mouseY >= startY && mouseY < startY + dropdownHeight) {

                    // Handle scrollbar click
                    int scrollbarHeight = Math.max(20, (maxVisibleOptions * dropdownHeight) / filteredOptions.size());
                    int maxScrollOffset = filteredOptions.size() - maxVisibleOptions;

                    // Calculate new scrollOffset based on click position
                    double clickPositionRatio = (mouseY - startY) / (double)dropdownHeight;
                    scrollOffset = (int)(clickPositionRatio * maxScrollOffset);
                    scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));

                    return true;
                }

                // Handle option selection
                if (mouseX >= this.getX() && mouseX <= this.getX() + this.width) {
                    for (int i = 0; i < Math.min(maxVisibleOptions, filteredOptions.size()); i++) {
                        int optionIndex = i + scrollOffset;
                        // Add additional bounds check to ensure optionIndex is valid
                        if (optionIndex >= 0 && optionIndex < filteredOptions.size()) {
                            int optionY = startY + (i * optionHeight);
                            if (mouseY >= optionY && mouseY < optionY + optionHeight) {
                                // Select this option
                                //System.out.println("Options before try-catch: " + filteredOptions);
                                String selectedOption = filteredOptions.get(optionIndex);
                                try {
                                    //updateInternalStuff(optionIndex, List.copyOf(filteredOptions));
                                    System.out.println("X: " + mouseX + " Y: " + mouseY + " button: " + button);
                                    //System.out.println("Options after updateInternalStuff: " + filteredOptions);
                                    setValue(selectedOption);
                                    //System.out.println("Options after setValue: " + filteredOptions);
                                    selectionCallback.accept(selectedOption);
                                    //System.out.println("Options after selectionCallback: " + filteredOptions);
                                    collapseDropdown();
                                    //System.out.println("Options after collapseDropdown: " + filteredOptions);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    //System.out.println("Options after catching: " + filteredOptions);
                                    //this.mouseX = (float)mouseX;
                                    //this.mouseY = (float)mouseY;
                                    //this.clickX = (float)mouseX;
                                    //this.clickY = optionIndex * optionHeight + startY;
                                }
                                //System.out.println("Options after everything: " + filteredOptions);


                                return true;
                            }
                        }
                    }

                    // Click outside options but inside dropdown area
                    if (mouseY >= startY && mouseY < startY + dropdownHeight) {
                        return true;
                    }
                }

                // Click outside dropdown area - collapse it

                collapseDropdown();
                return true;
            }
            else {
                // Expand dropdown when clicking on the field
                boolean result = super.mouseClicked(mouseX, mouseY, button);
                if (isMouseOver(mouseX, mouseY)) {
                    expandDropdown();
                }
                return result;
            }
        }

        private void expandDropdown() {
            if (expanded) {return;}
            if(getValue().length() > 0) {
                setValue("");
                // Why does it work when I return here? If I don't it behaves as if it fired twice, which shouldn't be possible. So why?
                return;
            }
            //setValue(null);

            List<Renderable> renderables = new ArrayList<>(DataHandler.getActiveRenderables());

            for(GuiEventListener guiEventListener : DataHandler.getGuiContent()) {
                if(guiEventListener instanceof EditBox box) {
                    if(guiEventListener instanceof SearchableDropdown dropdown) {

                    }
                    else {
                        box.setX(box.getX() + xOffset);
                    }
                }
                else if(guiEventListener instanceof Button button3) {
                    button3.setX(button3.getX() + xOffset);
                }
                else if(guiEventListener instanceof SearchableDropdown dropdown) {

                }
                else if(guiEventListener instanceof Checkbox checkbox) {
                    checkbox.setX(checkbox.getX() + xOffset);
                }
            }

            for(Renderable renderable : renderables) {
                addInactiveRenderable(renderable);
            }

            addRenderable(this);
            expanded = true;
        }

        private void collapseDropdown() {
            if (!expanded) {return;}

            List<Renderable> renderables = new ArrayList<>(getInactiveRenderables());
            for(Renderable renderable : renderables) {
                addRenderable(renderable);
            }
            for(GuiEventListener guiEventListener : DataHandler.getGuiContent()) {
                if(guiEventListener instanceof EditBox box) {
                    if(guiEventListener instanceof SearchableDropdown dropdown) {

                    }
                    else {
                        box.setX(box.getX() - (xOffset));
                    }
                }
                else if(guiEventListener instanceof Button button2) {
                    button2.setX(button2.getX() - (xOffset));
                }
                else if(guiEventListener instanceof SearchableDropdown dropdown) {

                }
                else if(guiEventListener instanceof Checkbox checkbox) {
                    checkbox.setX(checkbox.getX() - (xOffset));
                }
            }

            expanded = false;
            this.setHeight(BUTTON_HEIGHT);
            if(identifier.equals("region.room_region_select")) {
                //DataHandler.currentRegion = this.getValue();
            }
            if(identifier.equals("room.room_region_select")) {
                //DataHandler.currentRoom = this.getValue();
            }
            if(identifier.equals("screen.room_region_select")) {
                //DataHandler.currentScreen = this.getValue();
            }
            
            if(identifier.contains("template")) {


            }
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (expanded && filteredOptions.size() > maxVisibleOptions) {
                int startY = this.getY() + height;

                // Check if dragging scrollbar
                if (mouseX >= this.getX() + this.width - 6 &&
                        mouseX <= this.getX() + this.width &&
                        mouseY >= startY && mouseY < startY + dropdownHeight) {

                    // Calculate new scrollOffset based on drag position
                    int maxScrollOffset = filteredOptions.size() - maxVisibleOptions;
                    double dragPositionRatio = (mouseY - startY) / (double)dropdownHeight;
                    scrollOffset = (int)(dragPositionRatio * maxScrollOffset);
                    scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));

                    return true;
                }
            }

            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (expanded && (isMouseOverDropdown(mouseX, mouseY) || isFocused())) {
                // Scroll through options
                if (scrollY > 0) {
                    scrollOffset = Math.max(0, scrollOffset - 1);
                } else if (scrollY < 0) {
                    scrollOffset = Math.min(Math.max(0, filteredOptions.size() - maxVisibleOptions), scrollOffset + 1);
                }
                return true;
            }
            return false;
        }

        private boolean isMouseOverDropdown(double mouseX, double mouseY) {
            int startY = this.getY() + BUTTON_HEIGHT;
            return mouseX >= this.getX() && mouseX <= this.getX() + this.width &&
                    mouseY >= startY && mouseY < startY + dropdownHeight;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            /*
            for(String ident : DataHandler.contentValues.keySet()) {
                if(this.identifier.equals(ident) && !this.getValue().equals(DataHandler.contentValues.get(ident)) && !expanded) {
                    this.setValue(DataHandler.contentValues.get(ident));
                    if(this.getValue().length() > 0 && DataHandler.valuesMapMap.containsValue(this.getValue())) {
                        for(GuiEventListener guiEventListener : DataHandler.guiContent) {
                            if(guiEventListener instanceof ClearingTextBox box) {
                                for(Map.Entry<String, Double> entry : DataHandler.valuesMapMap.get(this.getValue()).entrySet()) {
                                    String key = entry.getKey();
                                    double value = entry.getValue();
                                    if(box.getIdentifier().equals(key)) {
                                        box.setValue(String.valueOf((int) value));
                                    }
                                }
                            }
                        }
                    }
                }
            }
             *//*
            super.renderWidget(guiGraphics, i, j, f);
        }

        public void renderExpanded(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (!expanded || filteredOptions.isEmpty()) {
                return;
            }

            List<String> optionVisible = new ArrayList<>();
            for (int i = 0; i < Math.min(maxVisibleOptions, filteredOptions.size() - scrollOffset); i++) {
                optionVisible.add(filteredOptions.get(i + scrollOffset));
            }
            if(this.identifier.equals("template.room_edit")) {
                nameLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > nameLength) {
                        nameLength = option.length();
                    }
                }
                //this.setWidth(nameLength * 6);
            }
            if(this.identifier.equals("template.room_create")) {
                nameLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > nameLength) {
                        nameLength = option.length();
                    }
                }
                //this.setWidth(nameLength * 6);
            }
            if(this.identifier.equals("region.room_region_select")) {
                regionLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > regionLength) {
                        regionLength = option.length();
                    }
                }
                //this.setWidth(regionLength * 6);
            }
            if(this.identifier.equals("room.room_region_select")) {
                roomLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > roomLength) {
                        roomLength = option.length();
                    }
                }
                //this.setWidth(roomLength * 6);
            }
            if(this.identifier.equals("screen.room_region_select")) {
                screenLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > screenLength) {
                        screenLength = option.length();
                    }
                }
                //this.setWidth(screenLength * 6);
            }


            int startY = this.getY() + BUTTON_HEIGHT;
            int optionHeight = BUTTON_HEIGHT;
            this.setHeight(BUTTON_HEIGHT + Math.min(dropdownHeight, filteredOptions.size() * optionHeight));


            //guiGraphics.fill(Math.round(this.mouseX), Math.round(this.mouseY), Math.round(this.mouseX) + 2, Math.round(this.mouseY) + 2, 0xFF0000FF);

            //guiGraphics.fill(Math.round(this.clickX), Math.round(this.clickY), Math.round(this.clickX) + 2, Math.round(this.clickY) + 2,  0xFFFFFFFF);

            // Draw dropdown background
            guiGraphics.fill(this.getX(), startY, this.getX() + this.width, startY + Math.min(dropdownHeight, filteredOptions.size() * optionHeight),
                    0xAA000000);

            // Draw options


            for (int i = 0; i < Math.min(maxVisibleOptions, filteredOptions.size() - scrollOffset); i++) {
                int optionIndex = i + scrollOffset;
                if (optionIndex < filteredOptions.size()) {
                    int optionY = startY + (i * optionHeight);
                    boolean isHovered = mouseX >= this.getX() && mouseX <= this.getX() + this.width &&
                            mouseY >= optionY && mouseY < optionY + optionHeight;

                    // Draw option background (highlight if hovered)
                    guiGraphics.fill(this.getX(), optionY, this.getX() + this.width, optionY + optionHeight,
                            isHovered ? 0xAA505050 : 0xAA303030);

                    // Draw option text
                    guiGraphics.drawString(font, filteredOptions.get(optionIndex),
                            this.getX() + 5, optionY + (optionHeight - 8) / 2, 0xFFFFFF);
                }
            }

            // Draw scrollbar if needed
            if (filteredOptions.size() > maxVisibleOptions) {
                int scrollbarHeight = Math.max(20, (maxVisibleOptions * dropdownHeight) / filteredOptions.size());
                int scrollbarY = startY + (scrollOffset * (dropdownHeight - scrollbarHeight)) /
                        Math.max(1, filteredOptions.size() - maxVisibleOptions);

                // Draw scrollbar
                guiGraphics.fill(this.getX() + this.width - scrollbarWidth, startY,
                        this.getX() + this.width, startY + dropdownHeight, 0xAA202020);

                guiGraphics.fill(this.getX() + this.width - scrollbarWidth, scrollbarY,
                        this.getX() + this.width, scrollbarY + scrollbarHeight, 0xAAA0A0A0);
            }

        }
    }

    public void setUpGUIContents() {
        roomCreateGui$title = new StringWidget(leftMargin, roomCreateGui$title$offsetY, WIDGET_WIDTH * 4, BUTTON_HEIGHT, Component.translatable("gui.room_creation_tool.create_title"), this.font).alignLeft();
        roomCreateGui$templateDropdown = new SearchableDropdown(leftMargin, roomCreateGui$templateDropdown$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT, Component.literal(DataHandler.getTemplateRoomCreateName()), DataHandler.getTemplateOptions(), this.font, this::onTemplateSelected, "template.room_create");
        roomCreateGui$paletteBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$paletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "palette");
        roomCreateGui$paletteResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$paletteBox.setValue(DataHandler.paletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$paletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomCreateGui$fadePaletteBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$fadePaletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_palette");
        roomCreateGui$fadePaletteResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$fadePaletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomCreateGui$fadeStrengthBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$fadeStrengthBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_strength");
        roomCreateGui$fadeStrengthResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$fadeStrengthBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomCreateGui$grimeBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$grimeBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "grime");
        roomCreateGui$grimeResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$grimeBox.setValue(DataHandler.grimeBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$grimeBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomCreateGui$effecColorABox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$effectColorABox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_a");
        roomCreateGui$effectColorAResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$effecColorABox.setValue(DataHandler.effectColorABoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$effectColorABox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomCreateGui$effectColorBBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$effectColorBBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_b");
        roomCreateGui$effectColorBResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$effectColorBBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomCreateGui$createButton = Button.builder(Component.translatable("gui.room_creation_tool.create"), button -> {/* Handle room creation *//*}).bounds(leftMargin, roomCreateGui$createButton$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.create_tooltip"))).build();


        roomRegionSelect$title = new StringWidget(leftMargin, roomRegionSelect$title$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.room_creation_tool.select_title"), this.font).alignLeft();
        roomRegionSelect$regionDropdown = new SearchableDropdown(leftMargin, roomRegionSelect$regionDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentRegion()), DataHandler.getRegionOptions(), this.font, this::onRegionSelected, "region.room_region_select");
        roomRegionSelect$roomDropdown = new SearchableDropdown(leftMargin * 2, roomRegionSelect$roomDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentRoom()), DataHandler.getRoomOptions(), this.font, this::onRoomSelected, "room.room_region_select");
        roomRegionSelect$screenDropdown = new SearchableDropdown(leftMargin * 3, roomRegionSelect$screenDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentScreen() != null ? DataHandler.getCurrentScreen() : ""), DataHandler.getScreenOptions(), this.font, this::onScreenSelected, "screen.room_region_select");
        roomRegionSelect$editButton = Button.builder(Component.translatable("gui.room_creation_tool.edit"), button -> minecraft.setScreen(new MainGui_old(player, GuiType.ROOM_EDIT))).bounds(leftMargin, roomRegionSelect$editButton$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.edit_tooltip"))).build();


        roomEditGui$title = new StringWidget(leftMargin, roomEditGui$title$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.room_creation_tool.edit_title"), this.font).alignLeft();
        roomEditGui$templateDropdown = new SearchableDropdown(leftMargin, roomEditGui$templateDropdown$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT, Component.literal(DataHandler.getTemplateRoomEditName()), DataHandler.getTemplateOptions(), this.font, this::onTemplateSelected, "template.room_edit");
        roomEditGui$toggleBiomeEditMode = Checkbox.builder(Component.translatable("gui.room_creation_tool.edit_all_biomes"), this.font).pos(leftMargin, roomEditGui$toggleBiomeEditMode$offsetY).selected(false).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.edit_all_biomes"))).build();
        roomEditGui$paletteBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$paletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "palette");
        roomEditGui$paletteResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$paletteBox.setValue(DataHandler.paletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$paletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomEditGui$fadePaletteBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$fadePaletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_palette");
        roomEditGui$fadePaletteResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$fadePaletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomEditGui$fadeStrengthBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$fadeStrengthBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_strength");
        roomEditGui$fadeStrengthResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$fadeStrengthBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomEditGui$grimeBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$grimeBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "grime");
        roomEditGui$grimeResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$grimeBox.setValue(DataHandler.grimeBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$grimeBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomEditGui$effecColorABox = new ClearingTextBox(this.font, leftMargin, roomEditGui$effectColorABox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_a");
        roomEditGui$effectColorAResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$effecColorABox.setValue(DataHandler.effectColorABoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$effectColorABox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomEditGui$effectColorBBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$effectColorBBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_b");
        roomEditGui$effectColorBResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$effectColorBBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.reset_button"))).build();
        roomEditGui$saveButton = Button.builder(Component.translatable("gui.room_creation_tool.save"), button -> {/* Save changes *//*}).bounds(leftMargin, roomEditGui$saveButton$offsetY, WIDGET_WIDTH / 2 - 5, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.save_tooltip"))).build();
        roomEditGui$backButton = Button.builder(Component.translatable("gui.room_creation_tool.back"), button -> minecraft.setScreen(new MainGui_old(player, GuiType.ROOM_REGION_SELECT))).bounds(leftMargin + WIDGET_WIDTH / 2 + 5, roomEditGui$backButton$offsetY, WIDGET_WIDTH / 2 - 5, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.back_tooltip"))).build();
        roomEditGui$hideGuiButton = Button.builder(Component.translatable("gui.room_creation_tool.hide"), button -> {/* Hide GUI logic *//*}).bounds(leftMargin, roomEditGui$hideGuiButton$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.hide_tooltip"))).build();


        roomCreateGui$templateDropdown.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.template")));
        roomCreateGui$paletteBox.setMaxLength(3);
        roomCreateGui$paletteBox.setValue(DataHandler.paletteBoxContent);
        roomCreateGui$paletteBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.palette")));
        roomCreateGui$fadePaletteBox.setMaxLength(3);
        roomCreateGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent);
        roomCreateGui$fadePaletteBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.fade_palette")));
        roomCreateGui$fadeStrengthBox.setMaxLength(5);
        roomCreateGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent);
        roomCreateGui$fadeStrengthBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.fade_strength")));
        roomCreateGui$grimeBox.setMaxLength(5);
        roomCreateGui$grimeBox.setValue(DataHandler.grimeBoxContent);
        roomCreateGui$grimeBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.grime")));
        roomCreateGui$effecColorABox.setMaxLength(3);
        roomCreateGui$effecColorABox.setValue(DataHandler.effectColorABoxContent);
        roomCreateGui$effecColorABox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.effect_color_a")));
        roomCreateGui$effectColorBBox.setMaxLength(3);
        roomCreateGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent);
        roomCreateGui$effectColorBBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.effect_color_b")));


        roomRegionSelect$regionDropdown.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.select_region")));
        roomRegionSelect$roomDropdown.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.select_room")));
        roomRegionSelect$screenDropdown.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.select_screen")));


        roomEditGui$paletteBox.setMaxLength(3);
        roomEditGui$paletteBox.setValue(DataHandler.paletteBoxContent);
        roomEditGui$paletteBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.palette")));
        roomEditGui$fadePaletteBox.setMaxLength(3);
        roomEditGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent);
        roomEditGui$fadePaletteBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.fade_palette")));
        roomEditGui$fadeStrengthBox.setMaxLength(5);
        roomEditGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent);
        roomEditGui$fadeStrengthBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.fade_strength")));
        roomEditGui$grimeBox.setMaxLength(5);
        roomEditGui$grimeBox.setValue(DataHandler.grimeBoxContent);
        roomEditGui$grimeBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.grime")));
        roomEditGui$effecColorABox.setMaxLength(3);
        roomEditGui$effecColorABox.setValue(DataHandler.effectColorABoxContent);
        roomEditGui$effecColorABox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.effect_color_a")));
        roomEditGui$effectColorBBox.setMaxLength(3);
        roomEditGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent);
        roomEditGui$effectColorBBox.setTooltip(Tooltip.create(Component.translatable("gui.room_creation_tool.effect_color_b")));

    }
}
*/