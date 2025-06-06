package net.brickcraftdream.rainworldmc_biomes;

import org.joml.Vector3f;

import java.util.*;

import static net.brickcraftdream.rainworldmc_biomes.ColorPicker.combineColors;
import static net.brickcraftdream.rainworldmc_biomes.ColorPicker.combineColorsVec3;
import static net.brickcraftdream.rainworldmc_biomes.FileExtractor.extractFiles;
import static net.brickcraftdream.rainworldmc_biomes.FileLister.listTextFiles;

public class EverythingProvider {
    public static Map<String, int[]> position = new HashMap<>();
    public static Map<String, Integer> palette = new HashMap<>();
    public static Map<String, Integer> fadepalette = new HashMap<>();
    public static Map<String, Double> fadestrength = new HashMap<>();
    public static Map<String, Integer> effectColorA = new HashMap<>();
    public static Map<String, Integer> effectColorB = new HashMap<>();
    public static Map<String, Float> grime = new HashMap<>();
    public static Map<String, Integer> dangertype = new HashMap<>();
    public static Map<String, Integer> sparks = new HashMap<>();
    public static List<String> keys = new ArrayList<>();

    public static List<String[]> maybeEverything = new ArrayList<>();
    public static Map<String, Map<String, String[]>> extractedRoomFiles = new HashMap<>();

    //private static RoomDataScanner roomDataScanner;

    public static String[] getEverything() {
        return new String[1];
    }

    public static void init() {
        extractedRoomFiles = extractFiles("/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/room_files");
        List<String> roomEffects = new ArrayList<>();
        // Initialize maps from room data

        for (Map.Entry<String, Map<String, String[]>> entry : extractedRoomFiles.entrySet()) {
            String outerKey = entry.getKey();
            Map<String, String[]> innerMap = entry.getValue();
            keys.add(outerKey);
            fadepalette.put(outerKey, 0);
            fadestrength.put(outerKey, 0.0d);
            sparks.put(outerKey, 0);
            dangertype.put(outerKey, 0);
            grime.put(outerKey, 0.0f);
            boolean debug_mode = outerKey.contains("su_a06tttt");

            for (Map.Entry<String, String[]> innerEntry : innerMap.entrySet()) {
                int roomcount = -1;
                String innerKey = innerEntry.getKey();
                String[] values = innerEntry.getValue();

                if(outerKey.contains("gate")) {
                    palette.put(outerKey, 0);
                }

                if(debug_mode) {
                    System.out.println(innerEntry);
                }
                if(Objects.equals(innerKey, "Palette")) {
                    palette.put(outerKey, Integer.valueOf(values[0]));
                }
                if(Objects.equals(innerKey, "FadePalette")) {
                    fadepalette.put(outerKey, Integer.valueOf(values[0]));
                    for(String s : values) {
                        roomcount++;
                        try {
                            fadestrength.put(outerKey, Double.valueOf(values[1]));
                        }
                        catch(ArrayIndexOutOfBoundsException e) {
                            System.out.println("No fade strength found for " + outerKey + " DEBUG DATA: " + innerKey + " " + Arrays.toString(values));
                            fadestrength.put(outerKey, 0d);
                        }
                    }
                }
                if(Objects.equals(innerKey, "EffectColorA")) {
                    effectColorA.put(outerKey, Integer.valueOf(values[0]));
                }
                if(Objects.equals(innerKey, "EffectColorB")) {
                    effectColorB.put(outerKey, Integer.valueOf(values[0]));
                }
                if(Objects.equals(innerKey, "Grime")) {
                    grime.put(outerKey, Float.valueOf(values[0]));
                }
                if(Objects.equals(innerKey, "DangerType")) {
                    if(values.length > 1) {
                        switch (values[1]) {
                            case "Rain":
                                dangertype.put(outerKey, 1);
                                break;
                            case "Flood":
                                dangertype.put(outerKey, 2);
                                break;
                            case "FloodAndRain":
                                dangertype.put(outerKey, 3);
                                break;
                            case "Blizzard":
                                dangertype.put(outerKey, 4);
                                break;
                            case "AerieBlizzard":
                                dangertype.put(outerKey, 5);
                                break;
                            default:
                                dangertype.put(outerKey, 0);
                                break;
                        }
                    }
                    else {
                        switch (values[0]) {
                            case "Rain":
                                dangertype.put(outerKey, 1);
                                break;
                            case "Flood":
                                dangertype.put(outerKey, 2);
                                break;
                            case "FloodAndRain":
                                dangertype.put(outerKey, 3);
                                break;
                            case "Blizzard":
                                dangertype.put(outerKey, 4);
                                break;
                            case "AerieBlizzard":
                                dangertype.put(outerKey, 5);
                                break;
                            default:
                                dangertype.put(outerKey, 0);
                                break;
                        }
                    }
                }
                if(Objects.equals(innerKey, "Effects")) {
                    for(String s : values) {
                        if(s.contains("GreenSpark")) {
                            sparks.put(outerKey, 1);
                        }
                    }
                }

                if(roomcount > 1) {
                    //roomcount--;
                    String kkey = outerKey;
                    for (int i = 0; i < roomcount; i++) {
                        String name = outerKey + "_screen" + i;

                        keys.add(name);
                        fadepalette.put(name, 0);
                        fadestrength.put(name, 0.0d);
                        sparks.put(name, 0);
                        dangertype.put(name, 0);
                        grime.put(name, 0.0f);
                        outerKey = name;

                        for (Map.Entry<String, String[]> innerEntry2 : innerMap.entrySet()) {
                            innerKey = innerEntry2.getKey();
                            values = innerEntry2.getValue();
                            //System.out.println(innerKey + Arrays.toString(Arrays.stream(values).toArray()));

                            if(Objects.equals(innerKey, "Palette")) {
                                palette.put(outerKey, Integer.valueOf(values[0]));
                            }
                            if(Objects.equals(innerKey, "FadePalette")) {
                                fadepalette.put(outerKey, Integer.valueOf(values[0]));
                                fadestrength.put(outerKey, Double.valueOf(values[i + 1]));
                            }
                            if(Objects.equals(innerKey, "EffectColorA")) {
                                effectColorA.put(outerKey, Integer.valueOf(values[0]));
                            }
                            if(Objects.equals(innerKey, "EffectColorB")) {
                                effectColorB.put(outerKey, Integer.valueOf(values[0]));
                            }
                            if(Objects.equals(innerKey, "Grime")) {
                                grime.put(outerKey, Float.valueOf(values[0]));
                            }
                            if(Objects.equals(innerKey, "DangerType")) {
                                //[None, Rain, Flood, FloodAndRain, Blizzard, AerieBlizzard]
                                //   0     1     2          3          4             5
                                if(values.length > 1) {
                                    switch (values[1]) {
                                        case "Rain":
                                            dangertype.put(outerKey, 1);
                                            break;
                                        case "Flood":
                                            dangertype.put(outerKey, 2);
                                            break;
                                        case "FloodAndRain":
                                            dangertype.put(outerKey, 3);
                                            break;
                                        case "Blizzard":
                                            dangertype.put(outerKey, 4);
                                            break;
                                        case "AerieBlizzard":
                                            dangertype.put(outerKey, 5);
                                            break;
                                        default:
                                            dangertype.put(outerKey, 0);
                                            break;
                                    }
                                }
                                else {
                                    switch (values[0]) {
                                        case "Rain":
                                            dangertype.put(outerKey, 1);
                                            break;
                                        case "Flood":
                                            dangertype.put(outerKey, 2);
                                            break;
                                        case "FloodAndRain":
                                            dangertype.put(outerKey, 3);
                                            break;
                                        case "Blizzard":
                                            dangertype.put(outerKey, 4);
                                            break;
                                        case "AerieBlizzard":
                                            dangertype.put(outerKey, 5);
                                            break;
                                        default:
                                            dangertype.put(outerKey, 0);
                                            break;
                                    }
                                }
                                //if(!roomEffects.contains(values[0])) {
                                //    roomEffects.add(values[0]);
                                //}
                                //System.out.println(name + Arrays.toString(Arrays.stream(values).toArray()));
                                //for(String s : values) {
                                //    if(Objects.equals(s, "Rain")) {
                                //        dangertype.put(outerKey, 1);
                                //    }
                                //    if(Objects.equals(s, "Flood")) {
                                //        dangertype.put(outerKey, 2);
                                //    }
                                //    if(Objects.equals(s, "None")) {
                                //        dangertype.put(outerKey, 0);
                                //    }
                                //    if(Objects.equals(s, "Rain") && Objects.equals(s, "F"))
                                //}

                            }
                            if(Objects.equals(innerKey, "Effects")) {
                                for(String s : values) {
                                    if(s.contains("GreenSpark")) {
                                        sparks.put(outerKey, 1);
                                    }
                                }
                            }
                        }
                        outerKey = kkey;
                    }
                }
            }
        }
        System.out.println(roomEffects);

        // Initialize position map
        position.put("sky", new int[]{0, 0});
        position.put("fog", new int[]{1, 0});
        position.put("water", new int[]{4, 0});
        position.put("water_fog", new int[]{4, 0});
    }

    //public static boolean hasScreens(String name) {return }

    public static Map<String, Map<String, String[]>> getExtractedRoomFiles() {
        return extractedRoomFiles;
    }

    public static int sky(String name) {
        return getSkyColorFromPalette(name);
    }

    public static int fog(String name) {
        return getFogColorFromPalette(name);
    }

    public static int water(String name) {
        return getWaterColorFromPalette(name);
    }

    public static int waterfog(String name) {
        return getWaterFogColorFromPalette(name);
    }

    public static float temperature(String name) {
        int palette = getPalette(name);
        int fadePalette = getFadePalette(name);
        double fadeStrength = getFadePaletteStrength(name);
        double grime = getGrime(name);
        int dangerType = getDangerType(name);
        int sparks = getSparks(name);

        //[None, Rain, Flood, FloodAndRain, Blizzard, AerieBlizzard]
        //   0     1     2          3          4             5

        double temperature = 0;
        temperature = temperature + (palette * 128);
        temperature = temperature + (fadePalette);
        temperature = temperature + (fadeStrength);
        temperature = temperature + (dangerType == 1 ? 16384 : dangerType == 2 ? 32768 : dangerType == 3 ? 32768 + 16384 : 0);
        temperature = temperature + (sparks == 1 ? 65536 : 0);

        return (float) temperature;
    }

    public static float grime(String name) {
        return getGrime(name);
    }

    private static int getColorFromPalette(String name, String x, String y) {
        if (name.contains("rm_s03-2")) {
            return 0;
        }

        //System.out.println(name);
        int paletteInt = palette.get(name);
        int fadepaletteInt = fadepalette.get(name);
        double fadestrengthDouble = fadestrength.get(name);


        String path = "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/palettes";

        return combineColors(
                path + "/palette" + paletteInt + ".png",
                x, y,
                path + "/palette" + fadepaletteInt + ".png",
                x, y,
                fadestrengthDouble
        );
    }

    public static Vector3f getColorFromPaletteVec3(String name, String x, String y) {
        if (name == null || !palette.containsKey(name)) {
            System.out.println("Warning: No palette found for name: " + name);
            return new Vector3f(0); // Default color
        }

        Integer paletteInt = palette.getOrDefault(name, 0);
        Integer fadepaletteInt = fadepalette.getOrDefault(name, 0);
        double fadestrengthDouble = fadestrength.getOrDefault(name, 0.0);

        String path = "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/palettes";

        return combineColorsVec3(
                path + "/palette" + paletteInt + ".png",
                x, y,
                path + "/palette" + fadepaletteInt + ".png",
                x, y,
                fadestrengthDouble
        );
    }

    public static int getSkyColorFromPalette(String name) {
        if (name == null || !palette.containsKey(name)) {
            System.out.println("No color found: " + name + " (sky)");
            return 0; // Default sky color
        }
        return getColorFromPalette(name, "0", "0");
    }

    public static int getFogColorFromPalette(String name) {
        if (name == null || !palette.containsKey(name)) {
            System.out.println("No color found: " + name + " (fog)");
            return 0; // Default fog color
        }
        return getColorFromPalette(name, "1", "0");
    }

    public static int getWaterColorFromPalette(String name) {
        if (name == null || !palette.containsKey(name)) {
            System.out.println("No color found: " + name + " (water)");
            return 0; // Default water color
        }
        return getColorFromPalette(name, "6", "0");
    }

    public static int getWaterFogColorFromPalette(String name) {
        return getColorFromPalette(name, "6", "0");
    }

    public static int getPalette(String name) {return palette.get(name);}

    public static int getFadePalette(String name) {return fadepalette.get(name);}

    public static Double getFadePaletteStrength(String name) {return fadestrength.get(name);}

    public static int getEffectColorA(String name) {
        //System.out.println("Effect color A: " + name + " (region " + BiomeBootstrap.getRegion(name) + ")");
        if(!effectColorA.containsKey(name)) {
            return -10;
        }
        return effectColorA.get(name);
    }

    public static int getEffectColorB(String name) {
        //System.out.println("Effect color B: " + name + " (region " + BiomeBootstrap.getRegion(name) + ")");
        if(!effectColorB.containsKey(name)) {
            return -10;
        }
        return effectColorB.get(name);
    }

    public static Float getGrime(String name) {
        return grime.get(name);
    }

    public static int getDangerType(String name) {
        return dangertype.get(name);
    }

    public static int getSparks(String name) {
        return sparks.get(name);
    }

    public static void main(String[] args) {
        init();

        String directoryPath = "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/room_files";
        List<String[]> fileDataList = listTextFiles(directoryPath);

        fileDataList.forEach(data -> {
            String key = data[0];
            //System.out.println(key);
            //System.out.println(palette.get(key));
        });
    }
}