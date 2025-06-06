package net.brickcraftdream.rainworldmc_biomes;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileExtractor {
    private static final String TEMPLATE_PATH = "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/room_files/templates/";
    private static final String OPTIONS_PATH = "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap/rooms/room_files/options/";
    
    public static Map<String, Map<String, String[]>> extractFiles(String rootPath) {
        Map<String, Map<String, String[]>> result = new HashMap<>();
        try {
            Files.walk(Paths.get(rootPath))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith("_settings.txt"))
                .filter(path -> !path.toString().startsWith(TEMPLATE_PATH) && !path.toString().startsWith(OPTIONS_PATH))
                .forEach(path -> processFile(path, result));
        } catch (IOException e) {
            System.err.println("Error walking through directory: " + rootPath);
            e.printStackTrace();
        }
        return result;
    }

    private static void processFile(Path filePath, Map<String, Map<String, String[]>> result) {
        boolean advancedDebug = filePath.toString().contains("ss_a11_settddddings");
        try {
            // Get filename without "_settings.txt"
            String fileName = filePath.getFileName().toString();
            String baseFileName = fileName.substring(0, fileName.length() - "_settings.txt".length());

            boolean isGate = baseFileName.toLowerCase().contains("gate_");
            String region = isGate ? baseFileName.substring(baseFileName.toLowerCase().indexOf("_") + 1, baseFileName.toLowerCase().indexOf("_") + 3) : baseFileName.substring(0, baseFileName.indexOf("_"));

            List<String> processedLines = new ArrayList<>();
            boolean hasTemplate = false;
            boolean hasPalette = false;

            // First pass - check for template
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (advancedDebug) System.out.println("Processing line: " + line);
                    if (line.toLowerCase().startsWith("template:")) {
                        hasTemplate = true;
                        String templateName = line.substring("template:".length()).trim();
                        if (templateName.equalsIgnoreCase("none")) {
                            if (advancedDebug) System.out.println("No template found");
                            hasTemplate = false;
                        }
                        if (!templateName.equalsIgnoreCase("none")) {
                            // Add template contents
                            processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                        }
                    }
                    else {
                        /*
                        if(filePath.toString().contains("whir")) {
                            hasTemplate = true;
                            String templateName = "whir_settingstemplate_rot";
                            if(!processedLines.contains("Palette: 466")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wbla")) {
                            hasTemplate = true;
                            String templateName = "wbla_settingstemplate_badlandssurface";
                            if(!processedLines.contains("Palette: 98")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wvwa")) {
                            hasTemplate = true;
                            String templateName = "wvwa_settingstemplate_pillars";
                            if(!processedLines.contains("Palette: 452")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wtda")) {
                            hasTemplate = true;
                            String templateName = "wtda_settingstemplate_tomb";
                            if(!processedLines.contains("Palette: 336")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wssr")) {
                            hasTemplate = true;
                            String templateName = "wssr_settingstemplate_calcified";
                            if(!processedLines.contains("Palette: 10")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wgwr")) {
                            hasTemplate = true;
                            String templateName = "wgwr_settingstemplate_rot";
                            if(!processedLines.contains("Palette: 469")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wdsr")) {
                            hasTemplate = true;
                            String templateName = "wdsr_settingstemplate_rot";
                            if(!processedLines.contains("Palette: 468")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wora")) {
                            hasTemplate = true;
                            String templateName = "wora_settingstemplate_rotpalace";
                            if(!processedLines.contains("Palette: 78")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wtdb")) {
                            hasTemplate = true;
                            String templateName = "wtdb_settingstemplate_tract";
                            if(!processedLines.contains("Palette: 333")) {
                                processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                System.out.println("Added template for " + filePath);
                            }
                        }
                        if(filePath.toString().contains("wskd")) {
                            hasTemplate = true;
                            //String templateName = "wskd_settingstemplate_rot";
                            //if(!processedLines.contains("Palette: 466")) {
                            //    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                            //    System.out.println("Added template for " + filePath);
                            //}
                            String templateName = "";
                            // Manual additions till I figure out how to do it
                            //wskd_b42 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b42")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b26 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b26")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b28 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b28")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b25 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b25")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b38 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b38")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b33 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b33")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b34 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b34")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b35 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b35")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b36 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b36")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b30 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b30")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskd_b31 - Outside
                            templateName = "wskd_settingstemplate_outside";
                            if(filePath.toString().contains("wskd_b31")) {
                                if(!processedLines.contains("Palette: 457")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                        }
                        if(filePath.toString().contains("wtda")) {
                            String templateName = "";
                            //wtda_b02 - Desert
                            templateName = "wtda_settingstemplate_desert";
                            if(filePath.toString().contains("wtda_b02")) {
                                if(!processedLines.contains("Palette: 335")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                        }
                        if(filePath.toString().contains("wrra")) {
                            String templateName = "";
                            //wrra_b01 - Inside
                            templateName = "wrra_settingstemplate_inside";
                            if(filePath.toString().contains("wrra_b01")) {
                                if(!processedLines.contains("Palette: 465")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wrra_b08 - Outside
                            templateName = "wrra_settingstemplate_outside";
                            if(filePath.toString().contains("wrra_b08")) {
                                if(!processedLines.contains("Palette: 464")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wrra_b06 - Outside
                            templateName = "wrra_settingstemplate_outside";
                            if(filePath.toString().contains("wrra_b06")) {
                                if(!processedLines.contains("Palette: 464")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wrra_b04 - Outside
                            templateName = "wrra_settingstemplate_outside";
                            if(filePath.toString().contains("wrra_b04")) {
                                if(!processedLines.contains("Palette: 464")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wrra_b10 - Outside
                            templateName = "wrra_settingstemplate_outside";
                            if(filePath.toString().contains("wrra_b10")) {
                                if(!processedLines.contains("Palette: 464")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                        }
                        if(filePath.toString().contains("wskc")) {
                            String templateName = "";
                            //wskc_a29 - Outside
                            templateName = "wskc_settingstemplate_outside";
                            if(filePath.toString().contains("wskc_a29")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskc_a25 - Outside
                            templateName = "wskc_settingstemplate_outside";
                            if(filePath.toString().contains("wskc_a25")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskc_a23 - Outside
                            templateName = "wskc_settingstemplate_outside";
                            if(filePath.toString().contains("wskc_a23")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskc_a08 - Outside
                            templateName = "wskc_settingstemplate_outside";
                            if(filePath.toString().contains("wskc_a08")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wskc_a06 - Outside
                            templateName = "wskc_settingstemplate_outside";
                            if(filePath.toString().contains("wskc_a06")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                        }
                        if(filePath.toString().contains("wska")) {
                            String templateName = "";
                            //wska_d22 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d22")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d20 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d20")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d10 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d10")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d11 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d11")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d14 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d14")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d15 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d15")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d13 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d13")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d18 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d18")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d19 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d19")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d16 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d16")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d09 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d09")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d01 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d01")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d02 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d02")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d07 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d07")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                            //wska_d06 - Outside
                            templateName = "wska_settingstemplate_outside";
                            if(filePath.toString().contains("wska_d06")) {
                                if(!processedLines.contains("Palette: 456")) {
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    System.out.println("Added template for " + filePath);
                                }
                            }
                        }
                         */
                        processedLines.add(line);
                    }
                }
            }

            String substring = "palette";

            for (String s : processedLines) {
                if (s.toLowerCase().startsWith(substring)) {
                    hasPalette = true;
                    break;  // Exit the loop as soon as we find a match
                }
            }


            // Check options file for room setting templates
            String roomPrefix = baseFileName.contains("_") ? baseFileName.substring(0, baseFileName.indexOf("_")) : baseFileName;
            if(isGate) {
                roomPrefix = region;
            }
            String optionsFile = OPTIONS_PATH + roomPrefix.toLowerCase() + ".txt";
            boolean hasSettingsTemplate = false;

            try {
                List<String> optionsLines = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new FileReader(optionsFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        //if(line.toLowerCase().startsWith("palette:")) {break;}
                        if (advancedDebug) System.out.println("Processing line in options file: " + line);
                        //System.out.println("Processing line in options file: " + line + hasPalette + hasTemplate);
                        if (line.toLowerCase().startsWith("room setting templates:") && !hasPalette && !hasTemplate) {
                            //System.out.println("Processing line in options file: " + optionsLines);
                            String templateValues = line.substring("room setting templates:".length()).trim();
                            if (!templateValues.isEmpty()) {
                                // Get the first value (e.g., "Outside" from "Outside, Inside")
                                String firstValue = templateValues.split(",")[0].trim();
                                if (!firstValue.isEmpty()) {
                                    // Get the base room name without anything after the first underscore
                                    String roomBaseName = baseFileName.contains("_") ? baseFileName.substring(0, baseFileName.indexOf("_")) : baseFileName;

                                    // Build template name: roomBaseName + "_settingstemplate_" + firstValue.toLowerCase()
                                    String templateName = roomBaseName + "_settingstemplate_" + firstValue.toLowerCase();

                                    // Add template contents
                                    processedLines.addAll(readTemplateFile(templateName, fileName, filePath.toString()));
                                    hasSettingsTemplate = true;
                                }
                            }
                        } else {
                            optionsLines.add(line);
                        }
                    }
                }

                // Add options lines if no template and no settings template
                if (!hasTemplate && !hasSettingsTemplate) {
                    processedLines.addAll(optionsLines);
                } else if (!hasTemplate) {
                    // Only add options lines that are not related to settings template
                    processedLines.addAll(optionsLines);
                }
            } catch (IOException e) {
                // Only log error if no template was found in the main file
                if (!hasTemplate) {
                    System.err.println("Could not read options file: " + optionsFile + ",  Parent file: " + fileName);
                }
            }

            // Process all lines into map structure
            Map<String, List<String>> tempMap = new HashMap<>();
            for (String line : processedLines) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    // Split by comma and trim each value
                    String[] values = value.split(",");
                    List<String> trimmedValues = new ArrayList<>();
                    for (String v : values) {
                        String trimmed = v.trim();
                        if (!trimmed.isEmpty()) {
                            trimmedValues.add(trimmed);
                        }
                    }

                    tempMap.computeIfAbsent(key, k -> new ArrayList<>()).addAll(trimmedValues);
                }
            }

            // Convert Lists to Arrays
            Map<String, String[]> finalMap = new HashMap<>();
            tempMap.forEach((key, list) -> finalMap.put(key, list.toArray(new String[0])));

            result.put(baseFileName, finalMap);

        } catch (IOException e) {
            System.err.println("Error processing file: " + filePath);
            e.printStackTrace();
        }
    }

    private static List<String> readTemplateFile(String templateName, String parentName, String parentPath) throws IOException {
        List<String> bad = new ArrayList<>();
        //bad.add("lc_settingstemplate_slums");
        //bad.add("sb_settingstemplate_filter");
        //bad.add("sb_settingstemplate_temple");
        bad.add("gate");

        // Extract region code from parent filename
        String parentBaseName = parentName.substring(0, parentName.length() - "_settings.txt".length());
        String regionCode = parentBaseName.contains("_") ?
                parentBaseName.substring(0, parentBaseName.indexOf("_")) :
                parentBaseName;

        // Initialize the modified template name with the original
        String modifiedTemplateName = templateName;

        // Handle wsk* regions (wska, wskb, wskc, wskd)
        /*
        if (regionCode.startsWith("wsk") && regionCode.length() == 4) {
            char lastChar = regionCode.charAt(3);

            // If the template is something like "sk_settingstemplate_inside"
            if (templateName.toLowerCase().startsWith("sk_settingstemplate_")) {
                // Extract the environment part (inside, outside, etc.)
                String envType = templateName.substring("sk_settingstemplate_".length());

                // Construct the new template name
                modifiedTemplateName = "wsk" + lastChar + "_settingstemplate_" + envType;
                //System.out.println("Mapped template: " + templateName + " -> " + modifiedTemplateName + " for file with region code " + regionCode);
            }
        }
         */
        if(modifiedTemplateName.startsWith("SK")) {
            modifiedTemplateName = regionCode + modifiedTemplateName.substring(2);
        }

        // Skip known bad templates
        if (bad.contains(modifiedTemplateName.toLowerCase())) {
            System.out.println("Detected known bad template: " + parentName + " has reference to " + modifiedTemplateName + ", skipping");
            return new ArrayList<>();
        }

        // Try to load the modified template
        Path templatePath = Paths.get(TEMPLATE_PATH + modifiedTemplateName.toLowerCase() + ".txt");
        if (Files.exists(templatePath)) {
            //System.out.println("Loading mapped template: " + modifiedTemplateName + " for " + parentName);
            return Files.readAllLines(templatePath);
        }

        // If modified template doesn't exist, try the original as fallback
        if (!modifiedTemplateName.equals(templateName)) {
            System.out.println("Modified template " + modifiedTemplateName + " not found, trying original: " + templateName);
            Path originalPath = Paths.get(TEMPLATE_PATH + templateName.toLowerCase() + ".txt");

            if (Files.exists(originalPath)) {
                System.out.println("Loading original template: " + templateName + " for " + parentName);
                return Files.readAllLines(originalPath);
            }
        }

        // If we get here, neither template was found
        System.out.println("No template found for " + parentPath + ". Tried " + modifiedTemplateName + " and " + templateName);

        // Return empty list instead of throwing exception
        return new ArrayList<>();
    }

    private static List<String> readOptionsFile(String optionsPath) throws IOException {
        List<String> paletteLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(optionsPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Palette:")) {
                    paletteLines.add(line);
                }
            }
        }
        return paletteLines;
    }
} 