package net.brickcraftdream.rainworldmc_biomes.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.brickcraftdream.rainworldmc_biomes.client.BoxRenderer;
import net.brickcraftdream.rainworldmc_biomes.data.storage.ConfigManagerServer;
import net.brickcraftdream.rainworldmc_biomes.gui.widget.BlockViewWidget;
import net.brickcraftdream.rainworldmc_biomes.networking.NetworkManager;
import net.brickcraftdream.rainworldmc_biomes.templates.JsonExporter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;
import static net.brickcraftdream.rainworldmc_biomes.client.Rainworld_MC_BiomesClient.resetSelections;
import static net.brickcraftdream.rainworldmc_biomes.gui.DataHandler.*;
import static net.brickcraftdream.rainworldmc_biomes.gui.GuiComponents.*;
public class MainGui extends Screen {
    // Constants
    private final Player player;
    private final GuiType type;
    private BlockViewWidget blockView;
    public static JsonExporter exporter = new JsonExporter("/assets/rainworld/data/biomes.json");

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

    public static String previousRegion = "";
    public static String previousRoom = "";
    public static String previousScreen = "";

    private static boolean previousTickMalformedBiomeDetected = false;


    public static boolean biomeEdit = false;
    public static boolean validBiomeSelected = false;

    public enum GuiType {
        ROOM_CREATE,
        ROOM_REGION_SELECT,
        ROOM_EDIT
    }

    public MainGui(Player player, GuiType type) {
        super(Component.translatable("gui.rainworld.main"));
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
        leftMargin = 20;
        topMargin = 40;

        //switch (type) {
        //    case ROOM_CREATE -> initRoomCreateGui();
        //    case ROOM_REGION_SELECT -> initRoomRegionSelectGui();
        //    case ROOM_EDIT -> initRoomEditGui();
        //}



        initMainGui();
    }

    @Override
    public void onClose() {
        super.onClose();
        if(BlockViewWidget.renderEffectChain != null) {
            BlockViewWidget.renderEffectChain.close();
            BlockViewWidget.renderEffectChain = null;
            Minecraft.getInstance().gameRenderer.shutdownEffect();
            BlockViewWidget.shouldRender = false;
            BlockViewWidget.shouldHaveActiveEffectChain = false;
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

    private void initMainGui() {
        this.addRenderableWidget(mainGui$title);
        this.addRenderableWidget(mainGui$regionDropdown);
        dropdowns.add(mainGui$regionDropdown);
        this.addRenderableWidget(mainGui$roomDropdown);
        dropdowns.add(mainGui$roomDropdown);
        this.addRenderableWidget(mainGui$screenDropdown);
        dropdowns.add(mainGui$screenDropdown);
        this.addRenderableWidget(mainGui$toggleBiomeEditMode);
        this.addRenderableWidget(mainGui$paletteTitle);
        this.addRenderableWidget(mainGui$paletteCoverButton);
        this.addRenderableWidget(mainGui$paletteBox);
        this.addRenderableWidget(mainGui$paletteResetButton);
        this.addRenderableWidget(mainGui$fadePaletteTitle);
        this.addRenderableWidget(mainGui$fadePaletteCoverButton);
        this.addRenderableWidget(mainGui$fadePaletteBox);
        this.addRenderableWidget(mainGui$fadePaletteResetButton);
        this.addRenderableWidget(mainGui$fadeStrengthTitle);
        this.addRenderableWidget(mainGui$fadeStrengthCoverButton);
        this.addRenderableWidget(mainGui$fadeStrengthBox);
        this.addRenderableWidget(mainGui$fadeStrengthResetButton);
        this.addRenderableWidget(mainGui$grimeTitle);
        this.addRenderableWidget(mainGui$grimeCoverButton);
        this.addRenderableWidget(mainGui$grimeBox);
        this.addRenderableWidget(mainGui$grimeResetButton);
        this.addRenderableWidget(mainGui$effectColorATitle);
        this.addRenderableWidget(mainGui$effectColorACoverButton);
        this.addRenderableWidget(mainGui$effectColorABox);
        this.addRenderableWidget(mainGui$effectColorAResetButton);
        this.addRenderableWidget(mainGui$effectColorBTitle);
        this.addRenderableWidget(mainGui$effectColorBCoverButton);
        this.addRenderableWidget(mainGui$effectColorBBox);
        this.addRenderableWidget(mainGui$effectColorBResetButton);
        this.addRenderableWidget(mainGui$dangerTypeTitle);
        this.addRenderableWidget(mainGui$dangerTypeCoverButton);
        this.addRenderableWidget(mainGui$dangerTypeDropdown);
        //dropdowns.add(mainGui$dangerTypeDropdown);
        this.addRenderableWidget(mainGui$dangerTypeResetButton);

        this.addRenderableWidget(mainGui$placeButton);
        this.addRenderableWidget(mainGui$cancelButton);
        this.addRenderableWidget(mainGui$nameBox);
        this.addRenderableWidget(mainGui$saveButton);

        this.addRenderableWidget(blockView);

        DataHandler.addTextBox(mainGui$paletteBox);
        DataHandler.addTextBox(mainGui$fadePaletteBox);
        DataHandler.addTextBox(mainGui$fadeStrengthBox);
        DataHandler.addTextBox(mainGui$grimeBox);
        DataHandler.addTextBox(mainGui$effectColorABox);
        DataHandler.addTextBox(mainGui$effectColorBBox);


        //this.addRenderableWidget(new EditBox(this.font, 0, 0, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal("text")));
    }

    private void initRoomCreateGui() {
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
        this.addRenderableWidget(roomRegionSelect$title);
        this.addRenderableWidget(roomRegionSelect$regionDropdown);
        this.addRenderableWidget(roomRegionSelect$roomDropdown);
        this.addRenderableWidget(roomRegionSelect$screenDropdown);
        this.addRenderableWidget(roomRegionSelect$editButton);
    }

    private void initRoomEditGui() {
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

    private void onTemplateSelected(String template) {
        String[] parts = template.split("\\.");
        //DataHandler.setCurrentRegion(parts.length > 0 ? parts[0] : "");
        //DataHandler.setCurrentRoom(parts.length > 1 ? parts[1] : "");
        //DataHandler.setCurrentScreen(parts.length > 2 ? parts[2] : "");
        DataHandler.setTemplateRoomEditName(template);
        DataHandler.setTemplateRoomCreateName(template);
    }

    private void onDangerTypeSelect(String s) {
        DataHandler.setDangerType(s);
    }

    private void onRoomSelected(String room) {
        if(mainGui$roomDropdown.expanded) return;
        DataHandler.setCurrentRoom(room);
        validBiomeSelected = false;
        try {
            String biomePath = DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() + (DataHandler.getCurrentScreen().equals(Component.translatable("gui.rainworld.select_screen").getString()) ? "" : (DataHandler.getCurrentScreen().isEmpty() ? "" : "_" + DataHandler.getCurrentScreen()));
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("rainworld", biomePath));
            Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registries.BIOME);
            Holder<Biome> biomeEntry = biomeRegistry.getHolderOrThrow(biomeKey);
            validBiomeSelected = true;
            //System.out.println("room");
        }
        catch (Exception e) {
            //System.out.println(e.getMessage());
            validBiomeSelected = false;
        }
    }

    private void onRegionSelected(String region) {
        if(mainGui$regionDropdown.expanded) return;
        DataHandler.setCurrentRegion(region);
        validBiomeSelected = false;
        try {
            String biomePath = DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() + (DataHandler.getCurrentScreen().equals(Component.translatable("gui.rainworld.select_screen").getString()) ? "" : (DataHandler.getCurrentScreen().isEmpty() ? "" : "_" + DataHandler.getCurrentScreen()));
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("rainworld", biomePath));
            Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registries.BIOME);
            Holder<Biome> biomeEntry = biomeRegistry.getHolderOrThrow(biomeKey);
            validBiomeSelected = true;
            //System.out.println("region");
        }
        catch (Exception e) {
            //System.out.println(e.getMessage());
            validBiomeSelected = false;
        }
    }

    private void onScreenSelected(String screen) {
        if(mainGui$screenDropdown.expanded) return;
        DataHandler.setCurrentScreen(screen);
        validBiomeSelected = false;
        try {
            String biomePath = DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() + (DataHandler.getCurrentScreen().equals(Component.translatable("gui.rainworld.select_screen").getString()) ? "" : (DataHandler.getCurrentScreen().isEmpty() ? "" : "_" + DataHandler.getCurrentScreen()));
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("rainworld", biomePath));
            Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registries.BIOME);
            Holder<Biome> biomeEntry = biomeRegistry.getHolderOrThrow(biomeKey);
            validBiomeSelected = true;
            //System.out.println("screen");
        }
        catch (Exception e) {
            //System.out.println(e.getMessage());
            validBiomeSelected = false;
        }
    }

    private void onSaveSelected(String string) {
        /*
        if(!locations.isEmpty()) {
            ServerLevel world = player.serverLevel();
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("rainworld", DataHandler.getTemplateRoomEditName()));

            Registry<Biome> biomeRegistry = world.registryAccess().registryOrThrow(Registries.BIOME);
            Holder<Biome> biomeEntry = biomeRegistry.getHolderOrThrow(biomeKey);

            for(GlobalPos pos : locations) {
                ResourceKey<Level> dimension = pos.dimension();
                if (world.dimension().equals(dimension)) {
                    ChunkAccess chunk = world.getChunk(pos.pos());
                    for (int sectionY = 0; sectionY < 16; sectionY++) {
                        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(pos.pos().getY() + sectionY * 16));
                        PalettedContainerRO<Holder<Biome>> biomeContainer = section.getBiomes();

                        if (biomeContainer instanceof PalettedContainer<Holder<Biome>> palettedContainer) {
                            for (int sx = 0; sx < 4; sx++) {
                                for (int sy = 0; sy < 4; sy++) {
                                    for (int sz = 0; sz < 4; sz++) {
                                        palettedContainer.set(sx, sy, sz, biomeEntry);
                                    }
                                }
                            }
                        }
                    }
                    chunk.setUnsaved(true);
                    world.getChunkSource().blockChanged(pos.pos());
                }
            }
        }

         */
        ClientPlayNetworking.send(new NetworkManager.BiomeUpdatePacket(
                !mainGui$nameBox.getValue().isEmpty() ? mainGui$nameBox.getValue() : DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() + (DataHandler.getCurrentScreen().equals(Component.translatable("gui.rainworld.select_screen").getString()) ? "" : "_" + DataHandler.getCurrentScreen()),
                "",
                Integer.parseInt(paletteBoxContent),
                Integer.parseInt(fadePaletteBoxContent),
                Float.parseFloat(fadeStrengthBoxContent),
                Float.parseFloat(grimeBoxContent),
                Integer.parseInt(effectColorABoxContent),
                Integer.parseInt(effectColorBBoxContent),
                0,
                //BiomeImageProcessorClient.resourceLocationToByteArray(ResourceLocation.fromNamespaceAndPath("rainworld", "textures/dynamic/shader_data.png"))
                ConfigManagerServer.readDataFromConfigFolder("shader_data.png")
        ));
    }

    private void onPlaceSelected(String string) {
        //if(!mainGui$regionDropdown.getValue().isEmpty()) {
        //    if(!mainGui$regionDropdown.getValue().equals(Component.translatable("gui.rainworld.select_region").getString())) {
        //        DataHandler.setCurrentRegion(mainGui$regionDropdown.getValue());
        //    }
        //}
        //if(!mainGui$roomDropdown.getValue().isEmpty()) {
        //    if(!mainGui$roomDropdown.getValue().equals(Component.translatable("gui.rainworld.select_room").getString())) {
        //        DataHandler.setCurrentRoom(mainGui$roomDropdown.getValue());
        //    }
        //}
        //if(!mainGui$screenDropdown.getValue().isEmpty()) {
        //    if(!mainGui$screenDropdown.getValue().equals(Component.translatable("gui.rainworld.select_screen").getString())) {
        //        DataHandler.setCurrentScreen(mainGui$screenDropdown.getValue());
        //    }
        //}

        if(!DataHandler.getLastPlacedRegion().isEmpty() && mainGui$regionDropdown.getValue().isEmpty() && !mainGui$regionDropdown.expanded) {
            DataHandler.setCurrentRegion(DataHandler.getLastPlacedRegion());
        }
        if(!DataHandler.getLastPlacedRoom().isEmpty() && mainGui$roomDropdown.getValue().isEmpty() && !mainGui$roomDropdown.expanded) {
            DataHandler.setCurrentRoom(DataHandler.getLastPlacedRoom());
        }
        if(!DataHandler.getLastPlacedScreen().isEmpty() && mainGui$screenDropdown.getValue().isEmpty() && !mainGui$screenDropdown.expanded) {
            DataHandler.setCurrentScreen(DataHandler.getLastPlacedScreen());
        }

        if(DataHandler.getCurrentRegion().isEmpty()) {
            player.sendSystemMessage(Component.literal("You have to first select a region." + mainGui$regionDropdown.getValue() + " " + mainGui$roomDropdown.getValue()));
        }
        else if(DataHandler.getCurrentRoom().isEmpty()) {
            player.sendSystemMessage(Component.literal("You have to first select a room."));
        }
        else {
            String biomeNamespace = "rainworld";
            String biomePath = DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() + (DataHandler.getCurrentScreen().equals(Component.translatable("gui.rainworld.select_screen").getString()) ? "" : (DataHandler.getCurrentScreen().isEmpty() ? "" : "_" + DataHandler.getCurrentScreen()));
            GlobalPos pos = BoxRenderer.getCenterOfAllBoxes();
            ClientPlayNetworking.send(new NetworkManager.BiomePlacePayload2(BoxRenderer.getLocations(), biomeNamespace, biomePath));
            resetSelections(player);
            //BoxRenderer.clearBoxes();
            DataHandler.setLastPlacedRegion(DataHandler.getCurrentRegion());
            DataHandler.setLastPlacedRoom(DataHandler.getCurrentRoom());
            DataHandler.setLastPlacedScreen(DataHandler.getCurrentScreen());
            assert minecraft != null;
            minecraft.setScreen(null);
        }
    }

    private void onCancelSelected(String string) {
        assert minecraft != null;
        minecraft.setScreen(null);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (DataHandler.templateDropdown != null && (type == GuiType.ROOM_CREATE || type == GuiType.ROOM_EDIT)) {
            DataHandler.templateDropdown.renderExpanded(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (DataHandler.regionDropdown != null) {
            DataHandler.regionDropdown.renderExpanded(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (DataHandler.roomDropdown != null) {
            DataHandler.roomDropdown.renderExpanded(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (DataHandler.screenDropdown != null) {
            DataHandler.screenDropdown.renderExpanded(guiGraphics, mouseX, mouseY, partialTick);
        }

        if(previousTickMalformedBiomeDetected) {
            regionDropdown.collapseDropdown();
            roomDropdown.collapseDropdown();
            screenDropdown.collapseDropdown();
            previousTickMalformedBiomeDetected = false;
        }

        for(GuiEventListener guiEventListener : DataHandler.getGuiContent()) {
            if(guiEventListener == null) {
                continue;
            }

            if(guiEventListener instanceof ClearingTextBox box) {
                if(box.identifier.equals("danger_type")) {
                    mainGui$dangerTypeResetButton.setX(box.getValue().length() * 6 + leftMargin);
                }
            }

            if(guiEventListener instanceof SearchableDropdown box) {
                String name = !DataHandler.getCurrentRegion().isEmpty() && !DataHandler.getCurrentRegion().equals(Component.translatable("gui.rainworld.select_region").getString())
                                    && !DataHandler.getCurrentRoom().isEmpty() && !DataHandler.getCurrentRoom().equals(Component.translatable("gui.rainworld.select_room").getString())
                                    && !DataHandler.getCurrentScreen().isEmpty() && !DataHandler.getCurrentScreen().equals(Component.translatable("gui.rainworld.select_screen").getString())
                                    ? DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() + "." + DataHandler.getCurrentScreen() :
                        (!DataHandler.getCurrentRegion().isEmpty() && !DataHandler.getCurrentRegion().equals(Component.translatable("gui.rainworld.select_region").getString())
                                    && !DataHandler.getCurrentRoom().isEmpty() && !DataHandler.getCurrentRoom().equals(Component.translatable("gui.rainworld.select_room").getString())
                                    ? DataHandler.getCurrentRegion() + "." + DataHandler.getCurrentRoom() :
                        (!DataHandler.getCurrentRegion().isEmpty() && !DataHandler.getCurrentRegion().equals(Component.translatable("gui.rainworld.select_region").getString())
                                    ? DataHandler.getCurrentRegion() : "")
                        );
                DataHandler.setTemplateRoomEditName(name);
                DataHandler.setTemplateRoomCreateName(name);
                //if(box.identifier.equals("template.room_edit")) {
                //    if(box.getValue().length() != nameLength && !box.getValue().isEmpty() && !box.expanded) {
                //        nameLength = box.getValue().length();
                //    }
                //    else if(box.getValue().isEmpty() && !box.expanded) {
                //        nameLength = 13;
                //    }
                //    box.setWidth(textMargin + nameLength * 6 + textMargin + scrollbarWidth);
                //}
                //if(box.identifier.equals("template.room_create")) {
                //    if(box.getValue().length() != nameLength && !box.getValue().isEmpty() && !box.expanded) {
                //        nameLength = box.getValue().length();
                //    }
                //    else if(box.getValue().isEmpty() && !box.expanded) {
                //        nameLength = 13;
                //    }
                //    box.setWidth(textMargin + nameLength * 6 + textMargin + scrollbarWidth);
                //}
                if(box.identifier.equals("region.room_region_select")) {
                    if(box.getValue().length() != regionLength && !box.getValue().isEmpty() && !box.expanded) {
                        regionLength = box.getValue().length();
                    }
                    else if(box.getValue().isEmpty() && !box.expanded) {
                        regionLength = 13;
                    }
                    box.setWidth(textMargin + regionLength * 6 + textMargin + scrollbarWidth);
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
                        if(!box.getValue().contains(Component.translatable("gui.rainworld.select_screen").getString())) {
                            List<String> allOptions = getSortedStrings(List.copyOf(exporter.getAllScreenNames(regionDropdown.getValue(), roomDropdown.getValue())));
                            screenOptions = allOptions;
                            box.allOptions = allOptions;
                        }
                        else {
                            box.allOptions = List.copyOf(DataHandler.getScreenOptions());
                        }
                    }
                    //if(box.getValue().isEmpty() && !getCurrentScreen().isEmpty()) {
                    //    box.setValue(getCurrentScreen(), true);
                    //}
                    //if(!box.getValue().isEmpty() && getCurrentScreen().isEmpty()) {
                    //    setCurrentScreen(box.getValue());
                    //}
                    if(!Objects.equals(getCurrentScreen(), box.getValue()) && !box.expanded) {
                        //System.out.println(getCurrentScreen() + " " + box.getValue());
                        DataHandler.setCurrentScreen(box.getValue());
                    }
                }
                if(box.identifier.equals("room.room_region_select")) {
                    if(!DataHandler.regionDropdown.getValue().isEmpty() || !DataHandler.regionDropdown.getValue().isEmpty()) {
                        if(!box.getValue().contains(Component.translatable("gui.rainworld.select_room").getString())) {
                            List<String> allOptions = getSortedStrings(List.copyOf(exporter.getAllRoomNames(regionDropdown.getValue())));
                            roomOptions = allOptions;
                            box.allOptions = allOptions;
                        }
                        else {
                            box.allOptions = List.copyOf(DataHandler.getRoomOptions());
                        }
                    }
                    //if(box.getValue().isEmpty() && !getCurrentRoom().isEmpty()) {
                    //    box.setValue(getCurrentRoom(), true);
                    //}
                    //if(!box.getValue().isEmpty() && getCurrentRoom().isEmpty()) {
                    //    setCurrentRoom(box.getValue());
                    //}
                    if(!Objects.equals(getCurrentRoom(), box.getValue()) && !box.expanded) {
                        //System.out.println(getCurrentRoom() + " " + box.getValue());
                        DataHandler.setCurrentRoom(box.getValue());
                    }
                }
                if(box.identifier.equals("region.room_region_select")) {
                    if(!box.getValue().contains(Component.translatable("gui.rainworld.select_region").getString())) {
                        List<String> allOptions = getSortedStrings(List.copyOf(exporter.getAllRegionNames()));
                        regionOptions = allOptions;
                        box.allOptions = allOptions;
                    }
                    else {
                        box.allOptions = List.copyOf(DataHandler.getRegionOptions());
                    }
                    //if(box.getValue().isEmpty() && !getCurrentRegion().isEmpty()) {
                    //    box.setValue(getCurrentRegion(), true);
                    //}
                    //if(!box.getValue().isEmpty() && getCurrentRegion().isEmpty()) {
                    //    setCurrentRegion(box.getValue());
                    //}
                    if(!Objects.equals(getCurrentRegion(), box.getValue()) && !box.expanded) {
                        //System.out.println(getCurrentRegion() + " " + box.getValue());
                        DataHandler.setCurrentRegion(box.getValue());
                    }
                }
            }
            if(guiEventListener instanceof Checkbox checkbox) {
                checkbox.setX(leftMargin + textMargin + regionLength * 6 + roomLength * 6 + screenLength * 6 + textMargin + scrollbarWidth * 6 + 12345);
                biomeEdit = checkbox.selected();
            }
            if(guiEventListener instanceof IdButton button) {
                if(button.getIdentifier().equals("palette")) {
                    if(!mainGui$paletteBox.getValue().equals(DataHandler.paletteBoxContent)) {
                        button.setX(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
                if(button.getIdentifier().equals("fade_palette")) {
                    if(!mainGui$fadePaletteBox.getValue().equals(DataHandler.fadePaletteBoxContent)) {
                        button.setX(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
                if(button.getIdentifier().equals("fade_strength")) {
                    if(!mainGui$fadeStrengthBox.getValue().equals(DataHandler.fadeStrengthBoxContent)) {
                        button.setX(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
                if(button.getIdentifier().equals("grime")) {
                    if(!mainGui$grimeBox.getValue().equals(grimeBoxContent)) {
                        button.setX(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
                if(button.getIdentifier().equals("effect_color_a")) {
                    if(!mainGui$effectColorABox.getValue().equals(DataHandler.effectColorABoxContent)) {
                        button.setX(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
                if(button.getIdentifier().equals("effect_color_b")) {
                    if(!mainGui$effectColorBBox.getValue().equals(DataHandler.effectColorBBoxContent)) {
                        button.setX(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
                if(button.getIdentifier().equals("danger_type")) {
                    if(!mainGui$dangerTypeDropdown.getValue().equals(dangerTypeContent)) {
                        button.setX(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
                if(button.getIdentifier().equals("cover")) {
                    if(biomeEdit) {
                        button.setX(-12345);
                    }
                    else {
                        button.setX(leftMargin);
                    }
                }
                if(button.getIdentifier().equals("save")) {
                    if(biomeEdit) {
                        button.setX(leftMargin);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
                if(button.getIdentifier().equals("place")) {
                    if(validBiomeSelected) {
                        button.setX(leftMargin);
                    }
                    else {
                        button.setX(-12345);
                    }
                }
            }
            if(guiEventListener instanceof EditBox box && !(guiEventListener instanceof SearchableDropdown) && !(guiEventListener instanceof ClearingTextBox)) {
                if(biomeEdit) {
                    box.setX(leftMargin + WIDGET_WIDTH + Component.translatable("gui.rainworld.save").getString().length() * 6);
                }
                else {
                    box.setX(-12345);
                }
            }
        }
        if(!validBiomeSelected && !mainGui$roomDropdown.getValue().isEmpty()) {
            if(!mainGui$roomDropdown.getValue().equals(Component.translatable("gui.rainworld.select_room").getString())) {
                mainGui$roomDropdown.setValue(Component.translatable("gui.rainworld.select_room").getString());
                previousTickMalformedBiomeDetected = true;
            }
        }

        if(!validBiomeSelected && !mainGui$screenDropdown.getValue().isEmpty()) {
            if(!mainGui$screenDropdown.getValue().equals(Component.translatable("gui.rainworld.select_screen").getString())) {
                mainGui$screenDropdown.setValue(Component.translatable("gui.rainworld.select_screen").getString());
                previousTickMalformedBiomeDetected = true;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static class ClearingTextBox extends EditBox {
        private String lastValue = getValue();
        public String identifier;

        public ClearingTextBox(Font font, int x, int y, int width, int height, Component component, String identifier) {
            super(font, x, y, width, height, component);
            this.identifier = identifier;
            List<ClearingTextBox> boxes = new ArrayList<>();
            for(ClearingTextBox box : DataHandler.getTextBoxes()) {
                if(!box.getIdentifier().equals(identifier)) {
                    boxes.add(box);
                }
            }
            boxes.add(this);
            DataHandler.getTextBoxes().clear();
            for(ClearingTextBox box : boxes) {
                DataHandler.addTextBox(box);
            }
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
                case "fade_strength":
                    DataHandler.fadeStrengthBoxContent = string;
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
                case "danger_type":
                    dangerTypeContent = string;
                    break;
                default:
                    break;
            }
            super.setValue(string);
        }

        public void setReplacingValue(String string) {
            super.setValue(string);
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(!biomeEdit) return false;
            boolean result = super.mouseClicked(mouseX, mouseY, button);
            if (isMouseOver(mouseX, mouseY)) {
                setReplacingValue("");
            }
            else {
                if(!getValue().isEmpty()) {
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
            if(getIdentifier().equals("palette") || getIdentifier().equals("fade_palette") || getIdentifier().equals("effect_color_a") || getIdentifier().equals("effect_color_b")) {
                if(string.matches("^\\d+$")) {
                    super.insertText(string);
                }
            }
            else if(getIdentifier().equals("fade_strength") || getIdentifier().equals("grime")) {
                if(string.matches("^\\d+(\\.\\d+)?$")) {
                    super.insertText(string);
                }
            }
            else {
                super.insertText(string);
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            String string = this.getValue();
            if(!string.isEmpty()) {
                switch (identifier) {
                    case "palette":
                        DataHandler.paletteBoxContent = string;
                        break;
                    case "fade_palette":
                        DataHandler.fadePaletteBoxContent = string;
                        break;
                    case "fade_strength":
                        DataHandler.fadeStrengthBoxContent = string;
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
                    case "danger_type":
                        dangerTypeContent = string;
                        break;
                    default:
                        break;
                }
            }
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public class SearchableDropdown extends EditBox {
        public List<String> allOptions;
        private final Consumer<String> selectionCallback;
        private final int dropdownHeight;
        public boolean expanded = false;
        private List<String> filteredOptions;
        private int scrollOffset = 0;
        private final int maxVisibleOptions = 9;
        private final int xOffset = -12345;
        public String identifier;
        private Predicate<String> filter = Objects::nonNull;
        private int maxLength = 32;

        public SearchableDropdown(int x, int y, int width, int height,
                                  Component placeholder, List<String> options,
                                  Font font, Consumer<String> selectionCallback, String identifier) {
            super(font, x, y, width, height, Component.empty());
            List<String> regions = getSortedStrings(List.copyOf(exporter.getAllRegionNames()));
            //DataHandler.sortStringsAlphabetically(regions);
            //DataHandler.sortStringsAlphabetically(options);
            //options = getSortedStrings(options);
            this.allOptions = identifier.equals("region.room_region_select") ? regions : options;
            if(identifier.equals("room.room_region_select")) {
                if(!DataHandler.roomOptions.isEmpty()) {
                    this.allOptions = List.copyOf(DataHandler.roomOptions);
                } else {
                    this.allOptions = List.of("Select a region first");
                }
                //this.allOptions = List.of("Select a region first");
            }
            if(identifier.equals("screen.room_region_select")) {
                if(!DataHandler.screenOptions.isEmpty()) {
                    this.allOptions = List.copyOf(DataHandler.screenOptions);
                } else {
                    this.allOptions = List.of("Select a room first");
                }
                //this.allOptions = List.of("Select a room first");
            }
            if(identifier.equals("danger_type")) {
                this.allOptions = List.of("None", "Rain", "Flood", "Rain and Flood");
            }
            this.filteredOptions = new ArrayList<>(allOptions);
            this.selectionCallback = selectionCallback;
            this.dropdownHeight = maxVisibleOptions * height;
            DataHandler.contentValues.put(identifier, placeholder.getString());

            this.setMaxLength(100);
            this.setHint(placeholder);

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

        public void setValue(String text, boolean silent) {
            if(silent) {
                setValueSilent(text);
                this.moveCursorToEnd(false);
                this.setHighlightPos(this.cursorPos);
            }
            else {
                super.setValue(text);
            }
        }

        public void setValueSilent(String text) {
            if (this.filter.test(text)) {
                if (text.length() > this.maxLength) {
                    this.value = text.substring(0, this.maxLength);
                } else {
                    this.value = text;
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(Objects.equals(identifier, "danger_type")) return false;
            if (expanded) {
                int optionHeight = BUTTON_HEIGHT;
                int startY = this.getY() + BUTTON_HEIGHT;

                if (filteredOptions.size() > maxVisibleOptions &&
                        mouseX >= this.getX() + this.width - 6 &&
                        mouseX <= this.getX() + this.width &&
                        mouseY >= startY && mouseY < startY + dropdownHeight) {

                    int maxScrollOffset = filteredOptions.size() - maxVisibleOptions;

                    double clickPositionRatio = (mouseY - startY) / (double)dropdownHeight;
                    scrollOffset = (int)(clickPositionRatio * maxScrollOffset);
                    scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));

                    return true;
                }

                if (mouseX >= this.getX() && mouseX <= this.getX() + this.width) {
                    for (int i = 0; i < Math.min(maxVisibleOptions, filteredOptions.size()); i++) {
                        int optionIndex = i + scrollOffset;
                        if (optionIndex >= 0 && optionIndex < filteredOptions.size()) {
                            int optionY = startY + (i * optionHeight);
                            if (mouseY >= optionY && mouseY < optionY + optionHeight) {
                                String selectedOption = filteredOptions.get(optionIndex);
                                setValue(selectedOption);
                                selectionCallback.accept(selectedOption);
                                collapseDropdown();
                                return true;
                            }
                        }
                    }
                    if (mouseY >= startY && mouseY < startY + dropdownHeight) {
                        return true;
                    }
                }
                collapseDropdown();
                return true;
            }
            else {
                boolean result = super.mouseClicked(mouseX, mouseY, button);
                if (isMouseOver(mouseX, mouseY)) {
                    expandDropdown();
                }
                return result;
            }
        }

        private void expandDropdown() {
            if (expanded) {return;}
            if(!getValue().isEmpty()) {
                //setValue("");
                setValue("", true);
                // Why does it work when I return here? If I don't it behaves as if it fired twice, which shouldn't be possible. So why?
                // Me from a few weeks later: It's because setValue seems to trigger the expanding logic too. Why? I still don't know
                // Me from a day later: I FUCKING HATE THIS SHIT!
                return;
            }
            //if(this.getValue().equals(Component.translatable("gui.rainworld.select_region").getString())
            //        || this.getValue().equals(Component.translatable("gui.rainworld.select_room").getString())
            //        || this.getValue().equals(Component.translatable("gui.rainworld.select_screen").getString())) {return;}

            List<Renderable> undesirables = new ArrayList<>(DataHandler.getActiveRenderables());

            for(GuiEventListener guiEventListener : DataHandler.getGuiContent()) {
                if(guiEventListener instanceof EditBox box) {
                    if (!(guiEventListener instanceof SearchableDropdown)) {
                        box.setX(box.getX() + (xOffset));
                    }
                }
                else if(guiEventListener instanceof Button button3) {
                    button3.setX(button3.getX() + xOffset);
                }
                else if(guiEventListener instanceof IdButton button3) {
                    button3.setX(button3.getX() + xOffset);
                }
                else if(guiEventListener instanceof Checkbox checkbox) {
                    checkbox.setX(checkbox.getX() + xOffset);
                }
                else if(guiEventListener instanceof StringWidget widget) {
                    if(widget.getY() != mainGui$title$offsetY) widget.setX(widget.getX() + xOffset);
                }
            }

            for(Renderable renderable : undesirables) {
                addInactiveRenderable(renderable);
            }
            addRenderable(this);
            expanded = true;
        }

        private void collapseDropdown() {
            if (!expanded) {return;}

            List<Renderable> undesirables = new ArrayList<>(getInactiveRenderables());
            for(Renderable renderable : undesirables) {
                addRenderable(renderable);
            }
            for(GuiEventListener guiEventListener : DataHandler.getGuiContent()) {
                if(guiEventListener instanceof EditBox box) {
                    if (!(guiEventListener instanceof SearchableDropdown)) {
                        box.setX(box.getX() - (xOffset));
                    }
                }
                else if(guiEventListener instanceof IdButton button2) {
                    button2.setX(button2.getX() - (xOffset));
                }
                else if(guiEventListener instanceof Button button3) {
                    button3.setX(button3.getX() - xOffset);
                }
                else if(guiEventListener instanceof Checkbox checkbox) {
                    checkbox.setX(checkbox.getX() - (xOffset));
                }
                else if(guiEventListener instanceof StringWidget widget) {
                    if(widget.getY() != mainGui$title$offsetY) widget.setX(widget.getX() - xOffset);
                }
            }

            expanded = false;
            this.setHeight(BUTTON_HEIGHT);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (expanded && filteredOptions.size() > maxVisibleOptions) {
                int startY = this.getY() + height;
                if (mouseX >= this.getX() + this.width - 6 && mouseX <= this.getX() + this.width && mouseY >= startY && mouseY < startY + dropdownHeight) {
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
            return mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= startY && mouseY < startY + dropdownHeight;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
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
            }
            if(this.identifier.equals("template.room_create")) {
                nameLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > nameLength) {
                        nameLength = option.length();
                    }
                }
            }
            if(this.identifier.equals("region.room_region_select")) {
                regionLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > regionLength) {
                        regionLength = option.length();
                    }
                }
            }
            if(this.identifier.equals("room.room_region_select")) {
                roomLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > roomLength) {
                        roomLength = option.length();
                    }
                }
            }
            if(this.identifier.equals("screen.room_region_select")) {
                screenLength = 0;
                for(String option : optionVisible) {
                    if(option.length() > screenLength) {
                        screenLength = option.length();
                    }
                }
            }

            int startY = this.getY() + BUTTON_HEIGHT;
            int optionHeight = BUTTON_HEIGHT;
            this.setHeight(BUTTON_HEIGHT + Math.min(dropdownHeight, filteredOptions.size() * optionHeight));
            guiGraphics.fill(this.getX(), startY, this.getX() + this.width, startY + Math.min(dropdownHeight, filteredOptions.size() * optionHeight), 0xAA000000);

            for (int i = 0; i < Math.min(maxVisibleOptions, filteredOptions.size() - scrollOffset); i++) {
                int optionIndex = i + scrollOffset;
                if (optionIndex < filteredOptions.size()) {
                    int optionY = startY + (i * optionHeight);
                    boolean isHovered = mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= optionY && mouseY < optionY + optionHeight;
                    guiGraphics.fill(this.getX(), optionY, this.getX() + this.width, optionY + optionHeight, isHovered ? 0xAA505050 : 0xAA303030);
                    guiGraphics.drawString(font, filteredOptions.get(optionIndex), this.getX() + 5, optionY + (optionHeight - 8) / 2, 0xFFFFFF);
                }
            }

            if (filteredOptions.size() > maxVisibleOptions) {
                int scrollbarHeight = Math.max(20, (maxVisibleOptions * dropdownHeight) / filteredOptions.size());
                int scrollbarY = startY + (scrollOffset * (dropdownHeight - scrollbarHeight)) / Math.max(1, filteredOptions.size() - maxVisibleOptions);
                guiGraphics.fill(this.getX() + this.width - scrollbarWidth, startY, this.getX() + this.width, startY + dropdownHeight, 0xAA202020);
                guiGraphics.fill(this.getX() + this.width - scrollbarWidth, scrollbarY, this.getX() + this.width, scrollbarY + scrollbarHeight, 0xAAA0A0A0);
            }

        }
    }

    public static class IdButton extends AbstractButton {
        public static final int SMALL_WIDTH = 120;
        public static final int DEFAULT_WIDTH = 150;
        public static final int BIG_WIDTH = 200;
        public static final int DEFAULT_HEIGHT = 20;
        public static final int DEFAULT_SPACING = 8;
        protected static final IdButton.CreateNarration DEFAULT_NARRATION = supplier -> (MutableComponent)supplier.get();
        protected final IdButton.OnPress onPress;
        protected final IdButton.CreateNarration createNarration;
        protected final String identifier;

        public static IdButton.Builder builder(Component component, IdButton.OnPress onPress, String identifier) {
            return new IdButton.Builder(component, onPress, identifier);
        }

        protected IdButton(int i, int j, int k, int l, Component component, IdButton.OnPress onPress, IdButton.CreateNarration createNarration, String identifier) {
            super(i, j, k, l, component);
            this.onPress = onPress;
            this.createNarration = createNarration;
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return this.identifier;
        }

        @Override
        public void onPress() {
            this.onPress.onPress(this);
        }

        @Override
        protected @NotNull MutableComponent createNarrationMessage() {
            return this.createNarration.createNarrationMessage(super::createNarrationMessage);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Environment(value= EnvType.CLIENT)
        public static class Builder {
            private final Component message;
            private final IdButton.OnPress onPress;
            @Nullable
            private Tooltip tooltip;
            private int x;
            private int y;
            private int width = 150;
            private int height = 20;
            private IdButton.CreateNarration createNarration = DEFAULT_NARRATION;
            private final String identifier;

            public Builder(Component component, IdButton.OnPress onPress, String identifier) {
                this.message = component;
                this.onPress = onPress;
                this.identifier = identifier;
            }

            public IdButton.Builder pos(int x, int y) {
                this.x = x;
                this.y = y;
                return this;
            }

            public IdButton.Builder width(int width) {
                this.width = width;
                return this;
            }

            public IdButton.Builder size(int width, int height) {
                this.width = width;
                this.height = height;
                return this;
            }

            public IdButton.Builder bounds(int x, int y, int width, int height) {
                return this.pos(x, y).size(width, height);
            }

            public IdButton.Builder tooltip(@Nullable Tooltip tooltip) {
                this.tooltip = tooltip;
                return this;
            }

            public IdButton.Builder createNarration(IdButton.CreateNarration createNarration) {
                this.createNarration = createNarration;
                return this;
            }

            public IdButton build() {
                IdButton button = new IdButton(this.x, this.y, this.width, this.height, this.message, this.onPress, this.createNarration, this.identifier);
                button.setTooltip(this.tooltip);
                return button;
            }
        }

        @Environment(value=EnvType.CLIENT)
        public static interface OnPress {
            public void onPress(IdButton var1);
        }

        @Environment(value=EnvType.CLIENT)
        public static interface CreateNarration {
            public MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
        }
    }

    public static class IdText extends StringWidget {
        public String identifier;

        public IdText(Component message, Font font) {
            super(message, font);
        }

        public IdText(Component message, Font font, String identifier) {
            super(message, font);
            this.identifier = identifier;
        }

        public IdText(int width, int height, Component message, Font font) {
            super(width, height, message, font);
        }

        public IdText(int width, int height, Component message, Font font, String identifier) {
            super(width, height, message, font);
            this.identifier = identifier;
        }

        public IdText(int x, int y, int width, int height, Component message, Font font) {
            super(x, y, width, height, message, font);
        }

        public IdText(int x, int y, int width, int height, Component message, Font font, String identifier) {
            super(x, y, width, height, message, font);
            this.identifier = identifier;
        }
    }

    public void setUpGUIContents() {

        RenderTarget renderTarget = minecraft.getMainRenderTarget();

        int fbWidth = renderTarget.width;
        int fbHeight = renderTarget.height;

        roomCreateGui$title = new StringWidget(leftMargin, roomCreateGui$title$offsetY, WIDGET_WIDTH * 4, BUTTON_HEIGHT, Component.translatable("gui.rainworld.create_title"), this.font).alignLeft();
        roomCreateGui$templateDropdown = new SearchableDropdown(leftMargin, roomCreateGui$templateDropdown$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT, Component.literal(DataHandler.getTemplateRoomCreateName()), DataHandler.getTemplateOptions(), this.font, this::onTemplateSelected, "template.room_create");
        roomCreateGui$paletteBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$paletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "palette");
        roomCreateGui$paletteResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$paletteBox.setValue(DataHandler.paletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$paletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomCreateGui$fadePaletteBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$fadePaletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_palette");
        roomCreateGui$fadePaletteResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$fadePaletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomCreateGui$fadeStrengthBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$fadeStrengthBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_strength");
        roomCreateGui$fadeStrengthResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$fadeStrengthBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomCreateGui$grimeBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$grimeBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "grime");
        roomCreateGui$grimeResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$grimeBox.setValue(DataHandler.grimeBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$grimeBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomCreateGui$effecColorABox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$effectColorABox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_a");
        roomCreateGui$effectColorAResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$effecColorABox.setValue(DataHandler.effectColorABoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$effectColorABox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomCreateGui$effectColorBBox = new ClearingTextBox(this.font, leftMargin, roomCreateGui$effectColorBBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_b");
        roomCreateGui$effectColorBResetButton = Button.builder(Component.literal("↺"), button -> roomCreateGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomCreateGui$effectColorBBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomCreateGui$createButton = Button.builder(Component.translatable("gui.rainworld.create"), button -> {/* Handle room creation */}).bounds(leftMargin, roomCreateGui$createButton$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.create_tooltip"))).build();


        roomRegionSelect$title = new StringWidget(leftMargin, roomRegionSelect$title$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.rainworld.select_title"), this.font).alignLeft();
        roomRegionSelect$regionDropdown = new SearchableDropdown(leftMargin, roomRegionSelect$regionDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentRegion()), DataHandler.getRegionOptions(), this.font, this::onRegionSelected, "region.room_region_select");
        roomRegionSelect$roomDropdown = new SearchableDropdown(leftMargin * 2, roomRegionSelect$roomDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentRoom()), DataHandler.getRoomOptions(), this.font, this::onRoomSelected, "room.room_region_select");
        roomRegionSelect$screenDropdown = new SearchableDropdown(leftMargin * 3, roomRegionSelect$screenDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentScreen() != null ? DataHandler.getCurrentScreen() : ""), DataHandler.getScreenOptions(), this.font, this::onScreenSelected, "screen.room_region_select");
        roomRegionSelect$editButton = Button.builder(Component.translatable("gui.rainworld.edit"), button -> {
            assert minecraft != null;
            minecraft.setScreen(new MainGui(player, GuiType.ROOM_EDIT));
        }).bounds(leftMargin, roomRegionSelect$editButton$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.edit_tooltip"))).build();


        roomEditGui$title = new StringWidget(leftMargin, roomEditGui$title$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.rainworld.edit_title"), this.font).alignLeft();
        roomEditGui$templateDropdown = new SearchableDropdown(leftMargin, roomEditGui$templateDropdown$offsetY, WIDGET_WIDTH * 2, BUTTON_HEIGHT, Component.literal(DataHandler.getTemplateRoomEditName()), DataHandler.getTemplateOptions(), this.font, this::onTemplateSelected, "template.room_edit");
        roomEditGui$toggleBiomeEditMode = Checkbox.builder(Component.translatable("gui.rainworld.edit_all_biomes"), this.font).pos(leftMargin, roomEditGui$toggleBiomeEditMode$offsetY).selected(false).tooltip(Tooltip.create(Component.translatable("gui.rainworld.edit_all_biomes"))).build();
        roomEditGui$paletteBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$paletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "palette");
        roomEditGui$paletteResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$paletteBox.setValue(DataHandler.paletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$paletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomEditGui$fadePaletteBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$fadePaletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_palette");
        roomEditGui$fadePaletteResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$fadePaletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomEditGui$fadeStrengthBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$fadeStrengthBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_strength");
        roomEditGui$fadeStrengthResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$fadeStrengthBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomEditGui$grimeBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$grimeBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "grime");
        roomEditGui$grimeResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$grimeBox.setValue(DataHandler.grimeBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$grimeBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomEditGui$effecColorABox = new ClearingTextBox(this.font, leftMargin, roomEditGui$effectColorABox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_a");
        roomEditGui$effectColorAResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$effecColorABox.setValue(DataHandler.effectColorABoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$effectColorABox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomEditGui$effectColorBBox = new ClearingTextBox(this.font, leftMargin, roomEditGui$effectColorBBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_b");
        roomEditGui$effectColorBResetButton = Button.builder(Component.literal("↺"), button -> roomEditGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent)).bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, roomEditGui$effectColorBBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        roomEditGui$saveButton = Button.builder(Component.translatable("gui.rainworld.save"), button -> onSaveSelected(button.toString())).bounds(leftMargin, roomEditGui$saveButton$offsetY, WIDGET_WIDTH / 2 - 5, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.save_tooltip"))).build();
        roomEditGui$backButton = Button.builder(Component.translatable("gui.rainworld.back"), button -> {
            assert minecraft != null;
            minecraft.setScreen(new MainGui(player, GuiType.ROOM_REGION_SELECT));
        }).bounds(leftMargin + WIDGET_WIDTH / 2 + 5, roomEditGui$backButton$offsetY, WIDGET_WIDTH / 2 - 5, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.back_tooltip"))).build();
        roomEditGui$hideGuiButton = Button.builder(Component.translatable("gui.rainworld.hide"), button -> {/* Hide GUI logic */}).bounds(leftMargin, roomEditGui$hideGuiButton$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.hide_tooltip"))).build();


        List<String> splashMessages = Arrays.asList(
                "Idk what to call this",
                "Guten Morgen by the way",
                "Placeholder Title",
                "Select... something?",
                "TODO: Name this screen",
                "Work in progress...",
                "Not final!",
                "Buttons go here",
                "Insert title here",
                "Still thinking of a name",
                "This is temporary",
                "Under heavy construction",
                "UI not approved yet",
                "Rough draft GUI",
                "Untitled Selection Menu",
                "Please ignore",
                "Needs polish",
                "Mockup Mode",
                "Dev screen v0.1",
                "Alpha UI – beware!",
                "Might be broken",
                "Design coming soon™",
                "Just testing things",
                "Not meant to be seen",
                "¯\\_(ツ)_/¯",
                "Function > Form (for now)",
                "Some kind of selector",
                "Prototype Phase",
                "This shouldn't be live",
                "Here be widgets",
                "Something goes here...",
                "Very beta",
                "Name pending approval",
                "Non-final interface",
                "Probably broken",
                "Layout not final",
                "Draft menu thing",
                "Ignore this screen",
                "Needs designer input",
                "To be redesigned",
                "Dev slapped this together",
                "Screen01_Final_FINAL_v3_new.java",
                "First pass UI",
                "Assets missing",
                "Click stuff. Maybe it works.",
                "Waiting on UX team",
                "Barebones interface",
                "Minimum viable GUI",
                "UI in exile",
                "Scaffolding only",
                "Here until it's not"
        );

        Random random = new Random();
        String title = splashMessages.get(random.nextInt(splashMessages.size()));


        mainGui$title = new StringWidget(leftMargin, mainGui$title$offsetY, WIDGET_WIDTH + Component.literal(title).getString().length() * 6, BUTTON_HEIGHT, Component.literal(title), this.font).alignLeft();

        mainGui$regionDropdown = new SearchableDropdown(leftMargin, mainGui$regionDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentRegion()), DataHandler.getRegionOptions(), this.font, this::onRegionSelected, "region.room_region_select");
        mainGui$roomDropdown = new SearchableDropdown(leftMargin * 2, mainGui$roomDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentRoom()), DataHandler.getRoomOptions(), this.font, this::onRoomSelected, "room.room_region_select");
        mainGui$screenDropdown = new SearchableDropdown(leftMargin * 3, mainGui$screenDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentScreen() != null ? DataHandler.getCurrentScreen() : ""), DataHandler.getScreenOptions(), this.font, this::onScreenSelected, "screen.room_region_select");

        mainGui$toggleBiomeEditMode = Checkbox.builder(Component.translatable("gui.rainworld.edit_biome"), this.font).pos(leftMargin * 4, mainGui$toggleBiomeEditMode$offsetY - 12345).selected(false).tooltip(Tooltip.create(Component.translatable("gui.rainworld.edit_biome"))).build();

        mainGui$paletteTitle = new StringWidget(leftMargin, mainGui$paletteTitle$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.palette_title").getString().length() * 6, BUTTON_HEIGHT, Component.translatable("gui.rainworld.palette_title"), this.font).alignLeft();
        mainGui$paletteBox = new ClearingTextBox(this.font, leftMargin, mainGui$paletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "palette");
        mainGui$paletteResetButton = IdButton.builder(Component.literal("↺"), button -> mainGui$paletteBox.setValue(DataHandler.paletteBoxContent), "palette").bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, mainGui$paletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        mainGui$paletteCoverButton = IdButton.builder(Component.literal(""), button -> {}, "cover").bounds(leftMargin, mainGui$paletteBox$offsetY - 12345, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        mainGui$fadePaletteTitle = new StringWidget(leftMargin, mainGui$fadePaletteTitle$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.fade_palette_title").getString().length() * 6, BUTTON_HEIGHT, Component.translatable("gui.rainworld.fade_palette_title"), this.font).alignLeft();
        mainGui$fadePaletteBox = new ClearingTextBox(this.font, leftMargin, mainGui$fadePaletteBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_palette");
        mainGui$fadePaletteResetButton = IdButton.builder(Component.literal("↺"), button -> mainGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent), "fade_palette").bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, mainGui$fadePaletteBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        mainGui$fadePaletteCoverButton = IdButton.builder(Component.literal(""), button -> {}, "cover").bounds(leftMargin, mainGui$fadePaletteBox$offsetY - 12345, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        mainGui$fadeStrengthTitle = new StringWidget(leftMargin, mainGui$fadeStrengthTitle$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.fade_strength_title").getString().length() * 6, BUTTON_HEIGHT, Component.translatable("gui.rainworld.fade_strength_title"), this.font).alignLeft();
        mainGui$fadeStrengthBox = new ClearingTextBox(this.font, leftMargin, mainGui$fadeStrengthBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "fade_strength");
        mainGui$fadeStrengthResetButton = IdButton.builder(Component.literal("↺"), button -> mainGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent), "fade_strength").bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, mainGui$fadeStrengthBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        mainGui$fadeStrengthCoverButton = IdButton.builder(Component.literal(""), button -> {}, "cover").bounds(leftMargin, mainGui$fadeStrengthBox$offsetY - 12345, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        mainGui$grimeTitle = new StringWidget(leftMargin, mainGui$grimeTitle$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.grime_title").getString().length() * 6, BUTTON_HEIGHT, Component.translatable("gui.rainworld.grime_title"), this.font).alignLeft();
        mainGui$grimeBox = new ClearingTextBox(this.font, leftMargin, mainGui$grimeBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "grime");
        mainGui$grimeResetButton = IdButton.builder(Component.literal("↺"), button -> mainGui$grimeBox.setValue(DataHandler.grimeBoxContent), "grime").bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, mainGui$grimeBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        mainGui$grimeCoverButton = IdButton.builder(Component.literal(""), button -> {}, "cover").bounds(leftMargin, mainGui$grimeBox$offsetY - 12345, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        mainGui$effectColorATitle = new StringWidget(leftMargin, mainGui$effectColorATitle$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.effect_color_a_title").getString().length() * 6, BUTTON_HEIGHT, Component.translatable("gui.rainworld.effect_color_a_title"), this.font).alignLeft();
        mainGui$effectColorABox = new ClearingTextBox(this.font, leftMargin, mainGui$effectColorABox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_a");
        mainGui$effectColorAResetButton = IdButton.builder(Component.literal("↺"), button -> mainGui$effectColorABox.setValue(DataHandler.effectColorABoxContent), "effect_color_a").bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, mainGui$effectColorABox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        mainGui$effectColorACoverButton = IdButton.builder(Component.literal(""), button -> {}, "cover").bounds(leftMargin, mainGui$effectColorABox$offsetY - 12345, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        mainGui$effectColorBTitle = new StringWidget(leftMargin, mainGui$effectColorBTitle$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.effect_color_b_title").getString().length() * 6, BUTTON_HEIGHT, Component.translatable("gui.rainworld.effect_color_b_title"), this.font).alignLeft();
        mainGui$effectColorBBox = new ClearingTextBox(this.font, leftMargin, mainGui$effectColorBBox$offsetY, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), "effect_color_b");
        mainGui$effectColorBResetButton = IdButton.builder(Component.literal("↺"), button -> mainGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent), "effect_color_b").bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH, mainGui$effectColorBBox$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        mainGui$effectColorBCoverButton = IdButton.builder(Component.literal(""), button -> {}, "cover").bounds(leftMargin, mainGui$effectColorBBox$offsetY - 12345, WIDGET_WIDTH - RESET_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        mainGui$dangerTypeTitle = new StringWidget(leftMargin, mainGui$dangerTypeTitle$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.danger_type_title").getString().length() * 6, BUTTON_HEIGHT, Component.translatable("gui.rainworld.danger_type_title"), this.font).alignLeft();
        mainGui$dangerTypeDropdown = new ClearingTextBox(this.font, leftMargin, mainGui$dangerTypeDropdown$offsetY, "Flood and Rain".length() * 6, BUTTON_HEIGHT, Component.empty(), "danger_type");
        //SearchableDropdown(leftMargin, mainGui$dangerTypeDropdown$offsetY, WIDGET_WIDTH, BUTTON_HEIGHT, Component.literal(DataHandler.getCurrentDangerType()), DataHandler.getDangerTypeOptions(), this.font, this::onDangerTypeSelect, "danger_type");
        mainGui$dangerTypeResetButton = IdButton.builder(Component.literal("↺"), button -> mainGui$dangerTypeDropdown.setValue(dangerTypeContent), "danger_type").bounds(leftMargin + WIDGET_WIDTH - RESET_BUTTON_WIDTH + "Flood and Rain".length() * 6, mainGui$dangerTypeDropdown$offsetY, RESET_BUTTON_WIDTH, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.reset_button"))).build();
        mainGui$dangerTypeCoverButton = IdButton.builder(Component.literal(""), button -> {}, "cover").bounds(leftMargin, mainGui$dangerTypeDropdown$offsetY - 12345, "Flood and Rain".length() * 6, BUTTON_HEIGHT).build();

        mainGui$placeButton = IdButton.builder(Component.translatable("gui.rainworld.place"), button -> onPlaceSelected(button.toString()), "place").bounds(leftMargin, mainGui$placeButton$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.place").getString().length() * 6, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.place_tooltip"))).build();
        mainGui$cancelButton = IdButton.builder(Component.translatable("gui.rainworld.cancel"), button -> onCancelSelected(button.toString()), "cancel").bounds(leftMargin + WIDGET_WIDTH + Component.translatable("gui.rainworld.place").getString().length() * 6, mainGui$cancelButton$offsetY, WIDGET_WIDTH + Component.translatable("gui.rainworld.cancel").getString().length() * 6, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.cancel_tooltip"))).build();
        mainGui$nameBox = new EditBox(this.font, leftMargin + WIDGET_WIDTH + Component.translatable("gui.rainworld.save").getString().length() * 6, mainGui$nameBox$offsetY - 12345, WIDGET_WIDTH + Component.translatable("gui.rainworld.name").getString().length() * 6, BUTTON_HEIGHT, Component.translatable("gui.rainworld.name"));
        mainGui$saveButton = IdButton.builder(Component.translatable("gui.rainworld.save"), button -> onSaveSelected(button.toString()), "save").bounds(leftMargin, mainGui$saveButton$offsetY - 12345, WIDGET_WIDTH + Component.translatable("gui.rainworld.save").getString().length() * 6, BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable("gui.rainworld.save_tooltip"))).build();


        mainGui$dangerTypeResetButton.setY(-12345);


        blockView = new BlockViewWidget(
                this.width / 2 - 150, // x position
                this.height / 2 - 150, // y position
                300, // width
                300, // height
                8    // view size in blocks
        );

        // Set the biome for tinting if needed
        if (minecraft.level != null && minecraft.player != null) {
            blockView.setBiome(minecraft.level.getBiome(minecraft.player.blockPosition()).value());
        }

        // Generate a cliffside with varying height
        int[][] heightMap = {
                {3, 4, 5, 6, 7, 8, 9, 9},
                {3, 4, 5, 6, 7, 8, 9, 9},
                {2, 3, 4, 5, 6, 7, 8, 9},
                {2, 3, 4, 5, 6, 7, 7, 8},
                {1, 2, 3, 4, 5, 6, 6, 7},
                {1, 2, 3, 4, 5, 5, 6, 6},
                {0, 1, 2, 3, 4, 4, 5, 5},
                {0, 1, 2, 3, 3, 4, 4, 5}
        };

        //Random random = new Random();
        String[] topTextures = {
                "textures/block/rainstone_worn_1.png",
                "textures/block/rainstone_worn_2.png",
                "textures/block/rainstone_worn_3.png",
                "textures/block/rainstone_worn_4.png",
                "textures/block/rainstone_worn_5.png",
                "textures/block/rainstone_worn_6.png"
        };
        String[] nearTopTextures = {
                "textures/block/rainstone_clean_1.png",
                "textures/block/rainstone_clean_2.png",
                "textures/block/rainstone_clean_3.png",
                "textures/block/rainstone_clean_4.png",
                "textures/block/rainstone_clean_5.png",
                "textures/block/rainstone_clean_6.png"
        };
        String[] baseTextures = {
                "textures/block/concrete_worn_1.png",
                "textures/block/concrete_worn_2.png",
                "textures/block/concrete_worn_3.png",
                "textures/block/concrete_worn_4.png",
                "textures/block/concrete_worn_5.png",
                "textures/block/concrete_worn_6.png"
        };

        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                int height = heightMap[x][z];
                for (int y = 0; y <= height; y++) {
                    if (y == height) {
                        String randomTexture = topTextures[random.nextInt(topTextures.length)];
                        blockView.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState(), ResourceLocation.fromNamespaceAndPath(MOD_ID, randomTexture));
                    } else if (y > height - 2) {
                        String randomTexture = nearTopTextures[random.nextInt(nearTopTextures.length)];
                        blockView.setBlock(new BlockPos(x, y, z), Blocks.ANDESITE.defaultBlockState(), ResourceLocation.fromNamespaceAndPath(MOD_ID, randomTexture));
                    } else {
                        String randomTexture = baseTextures[random.nextInt(baseTextures.length)];
                        blockView.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState(), ResourceLocation.fromNamespaceAndPath(MOD_ID, randomTexture));
                    }
                }
            }
        }

        // Add sparse vegetation and details
        blockView.setBlock(new BlockPos(1, heightMap[1][1] + 1, 1), Blocks.DEAD_BUSH.defaultBlockState(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/block/dirt_3.png"));
        blockView.setBlock(new BlockPos(6, heightMap[6][3] + 1, 3), Blocks.TALL_GRASS.defaultBlockState(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/block/girder.png"));
        blockView.setBlock(new BlockPos(3, heightMap[3][6] + 1, 6), Blocks.COBWEB.defaultBlockState(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/block/trash_clean_4.png"));

        // Add a torch on a ledge
        blockView.setBlock(new BlockPos(2, heightMap[2][2] + 1, 2), Blocks.TORCH.defaultBlockState(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/block/metal_block_red_1.png"));

        // Get the current biome for tinting
        if (this.minecraft.level != null && this.minecraft.player != null) {
            Biome biome = this.minecraft.level.getBiome(this.minecraft.player.blockPosition()).value();

            blockView.setBiome(biome);
        }





        roomCreateGui$templateDropdown.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.template")));
        roomCreateGui$paletteBox.setMaxLength(3);
        roomCreateGui$paletteBox.setValue(DataHandler.paletteBoxContent);
        roomCreateGui$paletteBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.palette")));
        roomCreateGui$fadePaletteBox.setMaxLength(3);
        roomCreateGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent);
        roomCreateGui$fadePaletteBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.fade_palette")));
        roomCreateGui$fadeStrengthBox.setMaxLength(5);
        roomCreateGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent);
        roomCreateGui$fadeStrengthBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.fade_strength")));
        roomCreateGui$grimeBox.setMaxLength(5);
        roomCreateGui$grimeBox.setValue(DataHandler.grimeBoxContent);
        roomCreateGui$grimeBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.grime")));
        roomCreateGui$effecColorABox.setMaxLength(3);
        roomCreateGui$effecColorABox.setValue(DataHandler.effectColorABoxContent);
        roomCreateGui$effecColorABox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.effect_color_a")));
        roomCreateGui$effectColorBBox.setMaxLength(3);
        roomCreateGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent);
        roomCreateGui$effectColorBBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.effect_color_b")));


        roomRegionSelect$regionDropdown.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.select_region")));
        roomRegionSelect$roomDropdown.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.select_room")));
        roomRegionSelect$screenDropdown.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.select_screen")));


        roomEditGui$paletteBox.setMaxLength(3);
        roomEditGui$paletteBox.setValue(DataHandler.paletteBoxContent);
        roomEditGui$paletteBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.palette")));
        roomEditGui$fadePaletteBox.setMaxLength(3);
        roomEditGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent);
        roomEditGui$fadePaletteBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.fade_palette")));
        roomEditGui$fadeStrengthBox.setMaxLength(5);
        roomEditGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent);
        roomEditGui$fadeStrengthBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.fade_strength")));
        roomEditGui$grimeBox.setMaxLength(5);
        roomEditGui$grimeBox.setValue(DataHandler.grimeBoxContent);
        roomEditGui$grimeBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.grime")));
        roomEditGui$effecColorABox.setMaxLength(3);
        roomEditGui$effecColorABox.setValue(DataHandler.effectColorABoxContent);
        roomEditGui$effecColorABox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.effect_color_a")));
        roomEditGui$effectColorBBox.setMaxLength(3);
        roomEditGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent);
        roomEditGui$effectColorBBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.effect_color_b")));


        mainGui$paletteBox.setMaxLength(3);
        mainGui$paletteBox.setValue(DataHandler.paletteBoxContent);
        mainGui$paletteBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.palette")));
        mainGui$fadePaletteBox.setMaxLength(3);
        mainGui$fadePaletteBox.setValue(DataHandler.fadePaletteBoxContent);
        mainGui$fadePaletteBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.fade_palette")));
        mainGui$fadeStrengthBox.setMaxLength(5);
        mainGui$fadeStrengthBox.setValue(DataHandler.fadeStrengthBoxContent);
        mainGui$fadeStrengthBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.fade_strength")));
        mainGui$grimeBox.setMaxLength(5);
        mainGui$grimeBox.setValue(DataHandler.grimeBoxContent);
        mainGui$grimeBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.grime")));
        mainGui$effectColorABox.setMaxLength(3);
        mainGui$effectColorABox.setValue(DataHandler.effectColorABoxContent);
        mainGui$effectColorABox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.effect_color_a")));
        mainGui$effectColorBBox.setMaxLength(3);
        mainGui$effectColorBBox.setValue(DataHandler.effectColorBBoxContent);
        mainGui$effectColorBBox.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.effect_color_b")));
        mainGui$dangerTypeDropdown.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.dangerType_tooltip")));

        mainGui$regionDropdown.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.select_a_region")));
        mainGui$roomDropdown.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.select_a_room")));
        mainGui$screenDropdown.setTooltip(Tooltip.create(Component.translatable("gui.rainworld.select_a_screen")));

    }
}
