package net.brickcraftdream.rainworldmc_biomes.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;

import static net.brickcraftdream.rainworldmc_biomes.gui.MainGui.SPACING;
import static net.brickcraftdream.rainworldmc_biomes.gui.MainGui.topMargin;

public class GuiComponents {
    public static StringWidget roomCreateGui$title;
        public static int roomCreateGui$title$offsetY = topMargin;

    public static MainGui.SearchableDropdown roomCreateGui$templateDropdown;
        public static int roomCreateGui$templateDropdown$offsetY = roomCreateGui$title$offsetY + SPACING;

    public static MainGui.ClearingTextBox roomCreateGui$paletteBox;
        public static int roomCreateGui$paletteBox$offsetY = roomCreateGui$templateDropdown$offsetY + SPACING;
        public static Button roomCreateGui$paletteResetButton;

    public static MainGui.ClearingTextBox roomCreateGui$fadePaletteBox;
        public static int roomCreateGui$fadePaletteBox$offsetY = roomCreateGui$paletteBox$offsetY + SPACING;
        public static Button roomCreateGui$fadePaletteResetButton;

    public static MainGui.ClearingTextBox roomCreateGui$fadeStrengthBox;
        public static int roomCreateGui$fadeStrengthBox$offsetY = roomCreateGui$fadePaletteBox$offsetY + SPACING;
        public static Button roomCreateGui$fadeStrengthResetButton;

    public static MainGui.ClearingTextBox roomCreateGui$grimeBox;
        public static int roomCreateGui$grimeBox$offsetY = roomCreateGui$fadeStrengthBox$offsetY + SPACING;
        public static Button roomCreateGui$grimeResetButton;

    public static MainGui.ClearingTextBox roomCreateGui$effecColorABox;
        public static int roomCreateGui$effectColorABox$offsetY = roomCreateGui$grimeBox$offsetY + SPACING;
        public static Button roomCreateGui$effectColorAResetButton;

    public static MainGui.ClearingTextBox roomCreateGui$effectColorBBox;
        public static int roomCreateGui$effectColorBBox$offsetY = roomCreateGui$effectColorABox$offsetY + SPACING;
        public static Button roomCreateGui$effectColorBResetButton;

    public static Button roomCreateGui$createButton;
        public static int roomCreateGui$createButton$offsetY = roomCreateGui$effectColorBBox$offsetY + SPACING + 5;


    public static StringWidget roomRegionSelect$title;
        public static int roomRegionSelect$title$offsetY = topMargin;

    public static MainGui.SearchableDropdown roomRegionSelect$regionDropdown;
        public static int roomRegionSelect$regionDropdown$offsetY = roomRegionSelect$title$offsetY + SPACING;

    public static MainGui.SearchableDropdown roomRegionSelect$roomDropdown;
        public static int roomRegionSelect$roomDropdown$offsetY = roomRegionSelect$regionDropdown$offsetY;

    public static MainGui.SearchableDropdown roomRegionSelect$screenDropdown;
        public static int roomRegionSelect$screenDropdown$offsetY = roomRegionSelect$roomDropdown$offsetY;

    public static Button roomRegionSelect$editButton;
        public static int roomRegionSelect$editButton$offsetY = roomRegionSelect$screenDropdown$offsetY + SPACING;


    public static StringWidget roomEditGui$title;
        public static int roomEditGui$title$offsetY = topMargin;

    public static MainGui.SearchableDropdown roomEditGui$templateDropdown;
        public static int roomEditGui$templateDropdown$offsetY = roomEditGui$title$offsetY + SPACING;

    public static Checkbox roomEditGui$toggleBiomeEditMode;
        public static int roomEditGui$toggleBiomeEditMode$offsetY = roomEditGui$templateDropdown$offsetY + SPACING;

    public static MainGui.ClearingTextBox roomEditGui$paletteBox;
        public static int roomEditGui$paletteBox$offsetY = roomEditGui$toggleBiomeEditMode$offsetY + SPACING;
        public static Button roomEditGui$paletteResetButton;

    public static MainGui.ClearingTextBox roomEditGui$fadePaletteBox;
        public static int roomEditGui$fadePaletteBox$offsetY = roomEditGui$paletteBox$offsetY + SPACING;
        public static Button roomEditGui$fadePaletteResetButton;

    public static MainGui.ClearingTextBox roomEditGui$fadeStrengthBox;
        public static int roomEditGui$fadeStrengthBox$offsetY = roomEditGui$fadePaletteBox$offsetY + SPACING;
        public static Button roomEditGui$fadeStrengthResetButton;

    public static MainGui.ClearingTextBox roomEditGui$grimeBox;
        public static int roomEditGui$grimeBox$offsetY = roomEditGui$fadeStrengthBox$offsetY + SPACING;
        public static Button roomEditGui$grimeResetButton;

    public static MainGui.ClearingTextBox roomEditGui$effecColorABox;
        public static int roomEditGui$effectColorABox$offsetY = roomEditGui$grimeBox$offsetY + SPACING;
        public static Button roomEditGui$effectColorAResetButton;

    public static MainGui.ClearingTextBox roomEditGui$effectColorBBox;
        public static int roomEditGui$effectColorBBox$offsetY = roomEditGui$effectColorABox$offsetY + SPACING;
        public static Button roomEditGui$effectColorBResetButton;

    public static Button roomEditGui$saveButton;
        public static int roomEditGui$saveButton$offsetY = roomEditGui$effectColorBBox$offsetY + SPACING + 5;

    public static Button roomEditGui$backButton;
        public static int roomEditGui$backButton$offsetY = roomEditGui$saveButton$offsetY;

    public static Button roomEditGui$hideGuiButton;
        public static int roomEditGui$hideGuiButton$offsetY = roomEditGui$backButton$offsetY + SPACING;



    public static StringWidget mainGui$title;
        public static int mainGui$title$offsetY = topMargin;

    public static MainGui.SearchableDropdown mainGui$regionDropdown;
        public static int mainGui$regionDropdown$offsetY = mainGui$title$offsetY + SPACING;
        public static MainGui.IdText mainGui$regionDropdown$text;

    public static MainGui.SearchableDropdown mainGui$roomDropdown;
        public static int mainGui$roomDropdown$offsetY = mainGui$regionDropdown$offsetY;
        public MainGui.IdText mainGui$roomDropdown$text;

    public static MainGui.SearchableDropdown mainGui$screenDropdown;
        public static int mainGui$screenDropdown$offsetY = mainGui$roomDropdown$offsetY;
        public MainGui.IdText mainGui$screenDropdown$text;

    public static Checkbox mainGui$toggleBiomeEditMode;
        public static int mainGui$toggleBiomeEditMode$offsetY = mainGui$screenDropdown$offsetY;


    public static StringWidget mainGui$paletteTitle;
        public static int mainGui$paletteTitle$offsetY = mainGui$toggleBiomeEditMode$offsetY + SPACING / 2 + SPACING / 4 + SPACING / 6 + SPACING / 6;
    public static MainGui.ClearingTextBox mainGui$paletteBox;
        public static int mainGui$paletteBox$offsetY = mainGui$paletteTitle$offsetY + SPACING / 2 + SPACING / 6;
        public static MainGui.IdButton mainGui$paletteResetButton;
        public static MainGui.IdButton mainGui$paletteCoverButton;

    public static StringWidget mainGui$fadePaletteTitle;
        public static int mainGui$fadePaletteTitle$offsetY = mainGui$paletteBox$offsetY + SPACING / 2 + SPACING / 4 + SPACING / 6 + SPACING / 6;
    public static MainGui.ClearingTextBox mainGui$fadePaletteBox;
        public static int mainGui$fadePaletteBox$offsetY = mainGui$fadePaletteTitle$offsetY + SPACING / 2 + SPACING / 6;
        public static MainGui.IdButton mainGui$fadePaletteResetButton;
        public static MainGui.IdButton mainGui$fadePaletteCoverButton;


    public static StringWidget mainGui$fadeStrengthTitle;
        public static int mainGui$fadeStrengthTitle$offsetY = mainGui$fadePaletteBox$offsetY + SPACING / 2 + SPACING / 4 + SPACING / 6 + SPACING / 6;
    public static MainGui.ClearingTextBox mainGui$fadeStrengthBox;
        public static int mainGui$fadeStrengthBox$offsetY = mainGui$fadeStrengthTitle$offsetY + SPACING / 2 + SPACING / 6;
        public static MainGui.IdButton mainGui$fadeStrengthResetButton;
        public static MainGui.IdButton mainGui$fadeStrengthCoverButton;


    public static StringWidget mainGui$grimeTitle;
        public static int mainGui$grimeTitle$offsetY = mainGui$fadeStrengthBox$offsetY + SPACING / 2 + SPACING / 4 + SPACING / 6 + SPACING / 6;
    public static MainGui.ClearingTextBox mainGui$grimeBox;
        public static int mainGui$grimeBox$offsetY = mainGui$grimeTitle$offsetY + SPACING / 2 + SPACING / 6;
        public static MainGui.IdButton mainGui$grimeResetButton;
        public static MainGui.IdButton mainGui$grimeCoverButton;


    public static StringWidget mainGui$effectColorATitle;
        public static int mainGui$effectColorATitle$offsetY = mainGui$grimeBox$offsetY + SPACING / 2 + SPACING / 4 + SPACING / 6 + SPACING / 6;
    public static MainGui.ClearingTextBox mainGui$effectColorABox;
        public static int mainGui$effectColorABox$offsetY = mainGui$effectColorATitle$offsetY + SPACING / 2 + SPACING / 6;
        public static MainGui.IdButton mainGui$effectColorAResetButton;
        public static MainGui.IdButton mainGui$effectColorACoverButton;

    public static StringWidget mainGui$effectColorBTitle;
        public static int mainGui$effectColorBTitle$offsetY = mainGui$effectColorABox$offsetY + SPACING / 2 + SPACING / 4 + SPACING / 6 + SPACING / 6;
    public static MainGui.ClearingTextBox mainGui$effectColorBBox;
        public static int mainGui$effectColorBBox$offsetY = mainGui$effectColorBTitle$offsetY + SPACING / 2 + SPACING / 6;
        public static MainGui.IdButton mainGui$effectColorBResetButton;
        public static MainGui.IdButton mainGui$effectColorBCoverButton;

    public static StringWidget mainGui$dangerTypeTitle;
        public static int mainGui$dangerTypeTitle$offsetY = mainGui$effectColorBBox$offsetY + SPACING / 2 + SPACING / 4 + SPACING / 6 + SPACING / 6;
    public static MainGui.ClearingTextBox mainGui$dangerTypeDropdown;
        public static int mainGui$dangerTypeDropdown$offsetY = mainGui$dangerTypeTitle$offsetY + SPACING / 2 + SPACING / 6;
        public static MainGui.IdButton mainGui$dangerTypeCoverButton;
        public static MainGui.IdButton mainGui$dangerTypeResetButton;

    public static MainGui.IdButton mainGui$placeButton;
        public static int mainGui$placeButton$offsetY = mainGui$dangerTypeDropdown$offsetY + SPACING + 5;

    public static MainGui.IdButton mainGui$cancelButton;
        public static int mainGui$cancelButton$offsetY = mainGui$placeButton$offsetY;

    public static EditBox mainGui$nameBox;
        public static int mainGui$nameBox$offsetY = mainGui$cancelButton$offsetY + SPACING;

    public static MainGui.IdButton mainGui$saveButton;
        public static int mainGui$saveButton$offsetY = mainGui$cancelButton$offsetY + SPACING;

    public static void init(MainGui.GuiType type) {
        DataHandler.templateDropdown = type == MainGui.GuiType.ROOM_CREATE ? roomCreateGui$templateDropdown : roomEditGui$templateDropdown;
        DataHandler.regionDropdown = mainGui$regionDropdown;
        DataHandler.roomDropdown = mainGui$roomDropdown;
        DataHandler.screenDropdown = mainGui$screenDropdown;


    }
}
