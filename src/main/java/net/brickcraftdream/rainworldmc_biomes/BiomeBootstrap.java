package net.brickcraftdream.rainworldmc_biomes;

import net.brickcraftdream.rainworldmc_biomes.data.RainworldBiomeTagProvider;
/*
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;

 */
import net.brickcraftdream.rainworldmc_biomes.templates.JsonExporter;
import net.minecraft.core.registries.Registries;

///VERSION SPECIFIC: 1.21.1
import net.minecraft.data.worldgen.BootstrapContext;
///VERSION SPECIFIC: 1.20.1
///import net.minecraft.data.worldgen.BootstapContext;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.joml.Vector4f;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import static net.brickcraftdream.rainworldmc_biomes.EverythingProvider.*;
import static net.brickcraftdream.rainworldmc_biomes.image.ImageGenerator.*;

public class BiomeBootstrap {
    public static List<String> stuff = new ArrayList<>();
    public static List<String> biome_list = new ArrayList<>();

    //private static RoomDataScanner roomDataScanner;


    ///VERSION SPECIFIC: 1.21.1
    public static void bootstrap(BootstrapContext<Biome> biomeRegisterable) {
    ///VERSION SPECIFIC: 1.20.1
    ///public static void bootstrap(BootstapContext<Biome> biomeRegisterable) {


        int highestTemp = 0;
        //System.out.println("ASDEWGASDFGASD");
        SpreadsheetGenerator_old generator = new SpreadsheetGenerator_old();

        JsonExporter jsonExporter = new JsonExporter();

        //String directoryPath = "/home/deck/IdeaProjects/Rainworld-MC_Biomes/rooms/room_files";
        //roomDataScanner = new RoomDataScanner(directoryPath);
        //roomDataScanner.scanRooms();

        
        EverythingProvider.init();

        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        int indexTemp = 0;
        for (String currentRoom : keys) {
            if(indexTemp + 2 > highestTemp) highestTemp = indexTemp + 2;
            if(currentRoom.contains("wtdb_a18dd") && !currentRoom.contains("screen")) {
                System.out.println("Palette 0: " + getPalette(currentRoom));
                System.out.println("Palette 1: " + getFadePalette(currentRoom));
                System.out.println("Fade: " + getFadePaletteStrength(currentRoom));
                System.out.println("Grime: " + getGrime(currentRoom));
                System.out.println("Effect Color A: " + getEffectColorA(currentRoom));
                System.out.println("Effect Color B: " + getEffectColorB(currentRoom));
                System.out.println("Danger Type: " + getDangerType(currentRoom));

                System.out.println("Datattt: " + Arrays.toString(getCoordsFromLinear(indexTemp)));
                System.out.println("Dataaaa: " + Arrays.toString(getCoordsFromLinear(3526)));

                System.out.println("65535: " + Arrays.toString(getCoordsFromLinear(65278)));
                System.out.println("65535 / 3: " + Arrays.toString(getCoordsFromLinear(65278 / 3)));
                System.out.println("f: " + Arrays.toString(getCoordsFromLinear((10580 - 2) / 3)));

                System.out.println("Palette as data: " + Arrays.toString(splitPaletteIntoBytes(getPalette(currentRoom))));
                System.out.println("Fade palette as data: " + Arrays.toString(splitPaletteIntoBytes(getFadePalette(currentRoom))));

                System.out.println("Linear from coords: " + getLinearFromCoords(3, 5));
                System.out.println("Linear from coords: " + getLinearFromCoords(4, 5));
                System.out.println("Linear from coords: " + getLinearFromCoords(5, 5));

                System.out.println("Linear from coords: " + getLinearFromCoords(198, 13));
                System.out.println("Linear from coords: " + getLinearFromCoords(254, 254));

                //System.out.println("Test 1: " + indexToUV(getPalette(currentRoom)).x + " " + indexToUV(getPalette(currentRoom)).y);
                //System.out.println("Test 1: " + indexToUV(getPalette(currentRoom) / 3).x * 255 + " " + indexToUV(getPalette(currentRoom) / 3).y * 255);

            }
            //if(indexTemp == 3526 || indexTemp == 3525 || indexTemp == 3524 || indexTemp == 3527 || indexTemp == 3528) {
            //    System.out.println("Current Room: " + currentRoom);
            //}


            if(currentRoom.contains("su_b04")) {
                System.out.println(currentRoom);
                System.out.println(getRegionStandalone(currentRoom));
            }

            registerBiome(biomeRegisterable, currentRoom, indexTemp + 2);
            int currentPalette = getPalette(currentRoom);
            int currentFadePalette = getFadePalette(currentRoom);
            if(currentPalette < 0) {
                System.out.println("No palette found for " + currentRoom);
            }
            if(currentFadePalette < 0) {
                System.out.println("No fade palette found for " + currentRoom);
            }
            indexTemp = roomToImage(image,
                    currentPalette,
                    currentFadePalette,
                    getFadePaletteStrength(currentRoom),
                    getGrime(currentRoom),
                    getEffectColorA(currentRoom),
                    getEffectColorB(currentRoom),
                    getDangerType(currentRoom),
                    indexTemp,
                    currentRoom);


            if(currentRoom.contains("_screen")) {
                jsonExporter.addScreenProperties(getRegionStandalone(currentRoom),
                                                 currentRoom.substring(0, currentRoom.indexOf("_screen")),
                                                 currentRoom.substring(currentRoom.indexOf("screen")),
                                                 getPalette(currentRoom),
                                                 getFadePalette(currentRoom),
                                                 getFadePaletteStrength(currentRoom),
                                                 getGrime(currentRoom),
                                                 getEffectColorA(currentRoom),
                                                 getEffectColorB(currentRoom),
                                                 getDangerType(currentRoom));
            }
            else {
                jsonExporter.addRoomProperties(getRegionStandalone(currentRoom),
                                               currentRoom,
                                               getPalette(currentRoom),
                                               getFadePalette(currentRoom),
                                               getFadePaletteStrength(currentRoom),
                                               getGrime(currentRoom),
                                               getEffectColorA(currentRoom),
                                               getEffectColorB(currentRoom),
                                               getDangerType(currentRoom));
            }

            Map<String, Vector3f> colorMap = new HashMap<>();
            colorMap.put("Sky", getColorFromPaletteVec3(currentRoom, "0", "0"));
            colorMap.put("Fog", getColorFromPaletteVec3(currentRoom, "1", "0"));
            colorMap.put("Water", getColorFromPaletteVec3(currentRoom, "4", "0"));
            colorMap.put("Water Fog", getColorFromPaletteVec3(currentRoom, "4", "0"));
            generator.addColoredRow(new String[]{
                    currentRoom + ",",
                    getRegion(currentRoom) + ",",
                    temperature(currentRoom) + ",",
                    grime(currentRoom) + ",",
                    getPalette(currentRoom) + ",",
                    getFadePalette(currentRoom) + ",",
                    getFadePaletteStrength(currentRoom) + ",",
                    (
                            (getDangerType(currentRoom) == 0 ? "none" : "") +
                                    (getDangerType(currentRoom) == 1 ? "rain" : "") +
                                    (getDangerType(currentRoom) == 2 ? "flood" : "") +
                                    (getDangerType(currentRoom) == 3 ? "floodandrain" : "") +
                                    (getDangerType(currentRoom) == 4 ? "blizzard" : "") +
                                    (getDangerType(currentRoom) == 5 ? "aerieblizzard" : "") //+
                            //", "
                    )
            }, colorMap);
        }
        System.out.println("Highest temp biome: " + highestTemp);
        System.out.println("Biome name: " + keys.get((highestTemp - 2) / 3));
        for(int i = 0; i < 366; i++) {
            String biomeName = String.valueOf(i);
            registerTempBiome(biomeRegisterable, biomeName, indexTemp + 2);
            indexTemp = roomToImage(image,
                    0,
                    0,
                    0,
                    0.5f,
                    0,
                    0,
                    0,
                    indexTemp,
                    biomeName);
            jsonExporter.addRoomProperties(getRegionStandalone(biomeName),
                                           biomeName,
                                           0,
                                           0,
                                           0,
                                           0.5f,
                                           0,
                                           0,
                                           0);
        }
        try {
            saveImageToFile(image, "png", "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/src/main/resources/assets/rainworld/textures/dynamic/shader_data.png");
            saveImageToFile(image, "png", "/home/deck/IdeaProjects/room_creation_tool/src/main/resources/assets/room_creation_tool/textures/dynamic/shader_data.png");
        }
        catch (IOException e) {
            System.out.println("Error saving image: " + e.getMessage());
        }
        generator.saveSpreadsheet("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/build/datagen/colored_new.html");
        jsonExporter.exportToFile("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/build/datagen/biomes.json");
        jsonExporter.exportToFile("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/src/main/resources/assets/rainworld/data/biomes.json");
    }

    private static void registerBiome(BootstrapContext<Biome> biomeRegisterable, String filename, int indexTemp) {

        ResourceKey<Biome> BIOME = ResourceKey.create(
                Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Rainworld_MC_Biomes.MOD_ID, getRegion(filename)));

        if ((filename.contains("_s0") || filename.contains("_s1") || filename.contains("_s2")) 
                && filename.length() < 9) {
            RainworldBiomeTagProvider.addTag(BIOME);
        }

        biomeRegisterable.register(BIOME, createBiome(filename, indexTemp));
    }

    private static void registerTempBiome(BootstrapContext<Biome> biomeRegisterable, String filename, int indexTemp) {

        ResourceKey<Biome> BIOME = ResourceKey.create(
                Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Rainworld_MC_Biomes.MOD_ID, "temp_biome." + filename));

        biomeRegisterable.register(BIOME, createTempBiome(filename, indexTemp));
    }

    private static Biome createBiome(String filename, int indexTemp) {
        try {

            return new Biome.BiomeBuilder()
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .hasPrecipitation(false)
                //.temperature(temperature(filename))
                .temperature(indexTemp)
                .downfall(0)
                .specialEffects((new BiomeSpecialEffects.Builder())
                    .skyColor(sky(filename))
                    .fogColor(fog(filename))
                    .waterColor(water(filename))
                    .waterFogColor(waterfog(filename))
                    .build())
                .build();
        } catch (Exception e) {
            try {
                System.out.println(sky(filename));
            }
            catch (Exception a) {
                System.out.println("Sky");
            }

            try {
                System.out.println(fog(filename));
            }
            catch (Exception a) {
                System.out.println("Fog");
            }

            try {
                System.out.println(water(filename));
            }
            catch (Exception a) {
                System.out.println("Water");
            }

            try {
                System.out.println(waterfog(filename));
            }
            catch (Exception a) {
                System.out.println("Water Fog");
            }
            // Only log critical errors
            System.err.println("Critical error creating biome " + filename + ": " + e.getMessage() + ", Content of the list: " + formatMap(getExtractedRoomFiles().get(filename)));
            return new Biome.BiomeBuilder()
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .hasPrecipitation(false)
                .temperature(0.8f)
                .downfall(0.4f)
                .specialEffects(new BiomeSpecialEffects.Builder().build())
                .build();
        }
    }

    private static Biome createTempBiome(String filename, int indexTemp) {
        try {

            return new Biome.BiomeBuilder()
                    .generationSettings(BiomeGenerationSettings.EMPTY)
                    .mobSpawnSettings(MobSpawnSettings.EMPTY)
                    .hasPrecipitation(false)
                    .temperature(indexTemp)
                    .downfall(0.5f)
                    .specialEffects((new BiomeSpecialEffects.Builder())
                            .skyColor(0)
                            .fogColor(0)
                            .waterColor(0)
                            .waterFogColor(0)
                            .build())
                    .build();
        } catch (Exception e) {
            try {
                System.out.println(sky(filename));
            }
            catch (Exception a) {
                System.out.println("Sky");
            }

            try {
                System.out.println(fog(filename));
            }
            catch (Exception a) {
                System.out.println("Fog");
            }

            try {
                System.out.println(water(filename));
            }
            catch (Exception a) {
                System.out.println("Water");
            }

            try {
                System.out.println(waterfog(filename));
            }
            catch (Exception a) {
                System.out.println("Water Fog");
            }
            // Only log critical errors
            System.err.println("Critical error creating biome " + filename + ": " + e.getMessage() + ", Content of the list: " + formatMap(getExtractedRoomFiles().get(filename)));
            return new Biome.BiomeBuilder()
                    .generationSettings(BiomeGenerationSettings.EMPTY)
                    .mobSpawnSettings(MobSpawnSettings.EMPTY)
                    .hasPrecipitation(false)
                    .temperature(0.8f)
                    .downfall(0.4f)
                    .specialEffects(new BiomeSpecialEffects.Builder().build())
                    .build();
        }
    }


    public static String formatMap(Map<String, String[]> map) {
        StringBuilder formattedString = new StringBuilder();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            formattedString.append(entry.getKey()).append(": ");
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                formattedString.append("[");
                for (int i = 0; i < values.length; i++) {
                    formattedString.append(values[i]);
                    if (i < values.length - 1) {
                        formattedString.append(", ");
                    }
                }
                formattedString.append("]");
            } else {
                formattedString.append("[]");
            }
            formattedString.append(System.lineSeparator());
        }
        return formattedString.toString();
    }


    // HashMap to store the two-letter codes and their corresponding regions.
    private static final Map<String, String> regionMap = new HashMap<>();

    // Static block to initialize the region map.
    static {
        regionMap.put("sb", "subterranean");
        regionMap.put("sh", "shaded_citadel");
        regionMap.put("si", "sky_islands");
        regionMap.put("sl", "shoreline");
        regionMap.put("ss", "five_pebbles");
        regionMap.put("su", "outskirts");
        regionMap.put("uw", "the_exterior");
        regionMap.put("cc", "chimney_canopy");
        regionMap.put("ds", "drainage_system");
        regionMap.put("gw", "garbage_wastes");
        regionMap.put("hi", "industrial_complex");
        regionMap.put("lf", "farm_arrays");
        regionMap.put("oe", "outer_expanse");
        regionMap.put("rm", "the_rot");
        regionMap.put("ug", "undergrowth");
        regionMap.put("vs", "pipeyard");
        regionMap.put("cl", "silent_construct");
        regionMap.put("dm", "looks_to_the_moon");
        regionMap.put("hr", "rubicon");
        regionMap.put("lc", "metropolis");
        regionMap.put("lm", "waterfront_facility");
        regionMap.put("ms", "submerged_superstructure");
        regionMap.put("00", "subterranean");
        regionMap.put("wara", "shattered_terrace");
        regionMap.put("warb", "salination");
        regionMap.put("warc", "fetid_glen");
        regionMap.put("ward", "cold_storage");
        regionMap.put("ware", "heat_ducts");
        regionMap.put("warf", "aether_ridge");
        regionMap.put("warg", "the_surface");
        regionMap.put("waua", "ancient_urban");
        regionMap.put("wbla", "badlands");
        regionMap.put("wdsr", "decaying_tunnels");
        regionMap.put("wgwr", "infested_wastes");
        regionMap.put("whir", "corrupted_factories");
        regionMap.put("wora", "outer_rim");
        regionMap.put("wpta", "signal_spires");
        regionMap.put("wrfa", "coral_caves");
        regionMap.put("wrfb", "turbulent_pump");
        regionMap.put("wrra", "rusted_wrecks");
        regionMap.put("wrsa", "daemon");
        regionMap.put("wska", "torrential_railways");
        regionMap.put("wskb", "sunlit_port");
        regionMap.put("wskc", "stormy_coast");
        regionMap.put("wskd", "shrouded_coast");
        regionMap.put("wssr", "unfortunate_evolution");
        regionMap.put("wsur", "crumbling_fringes");
        regionMap.put("wtda", "torrid_desert");
        regionMap.put("wtdb", "desolate_tract");
        regionMap.put("wvwa", "verdant_waterways");
    }

    // Method to check the input string and return the corresponding region name with "gate" or "shelter" if applicable.
    public static String getRegion(String input) {
        String originalInput = input;  // Preserve the original input for final output

        // Ignore everything after "_scree" in the input for processing logic.
        int screeIndex = input.indexOf("_scree");
        if (screeIndex != -1) {
            input = input.substring(0, screeIndex);  // Truncate at "_scree"
        }

        String regionPart = null;
        String extraPart = "";
        boolean isGateDetected = false;

        // Check if "gate" is at the start of the input.
        if (input.startsWith("gate")) {
            extraPart = "gate";
            isGateDetected = true;
            input = input.substring(4); // Remove "gate" from the input for further processing.
        }

        // Only check for shelter if "gate" was not found.
        if (!isGateDetected && input.matches(".*_s\\d{1,2}$") || input.contains("_s0") || input.contains("_s1") || input.contains("_s2")) {
            extraPart = "shelter";
            input = input.substring(0, input.lastIndexOf("_s")); // Remove the "_sXX" part for further processing.
        }

        // Extract region code (everything before the first underscore)
        int firstUnderscore = originalInput.indexOf('_');
        if (firstUnderscore > 0) {
            // Extract the region code (characters before the first underscore)
            String code = originalInput.substring(0, firstUnderscore);


            //if(!code.contains("wsur") && !code.contains("wssr") && !code.contains("wdsr") && !code.contains("wgwr") && !code.contains("whir")) {

            //}
            //else {
                //System.out.println("wsur detected, skipping region map lookup for code: " + code + "  " + originalInput);

            //}

            // First try the full code
            regionPart = regionMap.get(code);

            // If not found and the code is longer than 2 characters, try the first 2 characters
            // (for backward compatibility with existing two-letter codes)
            if (regionPart == null && code.length() > 2) {
                regionPart = regionMap.get(code.substring(1, 3));
            }

            // If no match found in the map, use the original code
            if (regionPart == null) {
                regionPart = code;
            }

            if (extraPart.equals("gate")) {
                regionPart = "";
            }
            if(code.contains("su")) {
                if(regionPart.equals("outskirts") && code.equals("wsur")) {
                    regionPart = "crumbling_fringes";
                    //System.out.println("Region code 'su' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
            if(code.contains("ss")) {
                if(regionPart.equals("five_pebbles") && code.equals("wssr")) {
                    regionPart = "unfortunate_evolution";
                    System.out.println("Region code 'ss' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
            if(code.contains("ds")) {
                if(regionPart.equals("drainage_system") && code.equals("wdsr")) {
                    regionPart = "decaying_tunnels";
                    System.out.println("Region code 'ds' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
            if(code.contains("gw")) {
                if(regionPart.equals("garbage_wastes") && code.equals("wgwr")) {
                    regionPart = "infested_wastes";
                    System.out.println("Region code 'gw' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
            if(code.contains("hi")) {
                if(regionPart.equals("industrial_complex") && code.equals("whir")) {
                    regionPart = "corrupted_factories";
                    System.out.println("Region code 'hi' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
        }

        // If a valid region code is found, format and return the result.
        if (regionPart != null) {
            return regionPart + (extraPart.isEmpty() ? "" : "." + extraPart) + "." + originalInput;
        }

        // Fallback to using the original input with any extra parts
        return (extraPart.isEmpty() ? "" : extraPart + ".") + originalInput;
    }

    public static String getRegionStandalone(String input) {
        String originalInput = input;  // Preserve the original input for final output

        // Ignore everything after "_scree" in the input for processing logic.
        int screeIndex = input.indexOf("_scree");
        if (screeIndex != -1) {
            input = input.substring(0, screeIndex);  // Truncate at "_scree"
        }

        String regionPart = null;
        String extraPart = "";
        boolean isGateDetected = false;

        // Check if "gate" is at the start of the input.
        if (input.startsWith("gate")) {
            extraPart = "gate";
            isGateDetected = true;
            input = input.substring(4); // Remove "gate" from the input for further processing.
        }

        // Only check for shelter if "gate" was not found.
        if (!isGateDetected && input.matches(".*_s\\d{1,2}$") || input.contains("_s0") || input.contains("_s1") || input.contains("_s2")) {
            extraPart = "shelter";
            input = input.substring(0, input.lastIndexOf("_s")); // Remove the "_sXX" part for further processing.
        }

        // Extract region code (everything before the first underscore)
        int firstUnderscore = originalInput.indexOf('_');
        if (firstUnderscore > 0) {
            // Extract the region code (characters before the first underscore)
            String code = originalInput.substring(0, firstUnderscore);

            //if(!code.contains("wsur")) {
                // First try the full code
                regionPart = regionMap.get(code);
            //}
            //else {
            //    System.out.println("wsur detected, skipping region map lookup for code: " + code + "  " + originalInput);
            //}

            // If not found and the code is longer than 2 characters, try the first 2 characters
            // (for backward compatibility with existing two-letter codes)
            if (regionPart == null && code.length() > 2) {
                regionPart = regionMap.get(code.substring(0, 2));
            }

            // If no match found in the map, use the original code
            if (regionPart == null) {
                regionPart = code;
            }

            if (extraPart.equals("gate")) {
                regionPart = "";
            }
            if(code.contains("su")) {
                if(regionPart.equals("outskirts") && code.equals("wsur")) {
                    regionPart = "crumbling_fringes";
                    //System.out.println("Region code 'su' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
            if(code.contains("ss")) {
                if(regionPart.equals("five_pebbles") && code.equals("wssr")) {
                    regionPart = "unfortunate_evolution";
                    System.out.println("Region code 'ss' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
            if(code.contains("ds")) {
                if(regionPart.equals("drainage_system") && code.equals("wdsr")) {
                    regionPart = "decaying_tunnels";
                    System.out.println("Region code 'ds' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
            if(code.contains("gw")) {
                if(regionPart.equals("garbage_wastes") && code.equals("wgwr")) {
                    regionPart = "infested_wastes";
                    System.out.println("Region code 'gw' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
            if(code.contains("hi")) {
                if(regionPart.equals("industrial_complex") && code.equals("whir")) {
                    regionPart = "corrupted_factories";
                    System.out.println("Region code 'hi' detected in: " + originalInput + ", code: " + code + ", regionPart: " + regionPart);
                }
            }
        }

        // If a valid region code is found, format and return the result.
        if (regionPart != null) {
            return regionPart;
        }

        // Fallback to using the original input with any extra parts
        return "temp_biomes_currently_unused";
    }

    public static void removeDuplicates(List<String[]> list) {
        Map<String, String[]> uniqueEntries = new LinkedHashMap<>();
        List<String[]> toRemove = new ArrayList<>();

        // First pass: identify duplicates
        for (String[] currentArray : list) {
            String firstElement = currentArray[0];
            
            if (firstElement.contains("rm_s03-2")) {
                toRemove.add(currentArray);
                continue;
            }

            if (uniqueEntries.containsKey(firstElement)) {
                toRemove.add(currentArray);
            } else {
                uniqueEntries.put(firstElement, currentArray);
            }
        }

        // Second pass: remove duplicates
        list.removeAll(toRemove);

        // Debug output
        for (String[] removed : toRemove) {
            System.out.println("Removed duplicate: " + String.join(", ", removed));
        }
    }
    public static void main(String[] args) {
        System.out.println(new Vector4f(Float.parseFloat("1.2"), 0, 0, 0));
    }
}
