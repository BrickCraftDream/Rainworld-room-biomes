package net.brickcraftdream.rainworldmc_biomes;

import org.joml.Vector3f;
import java.io.*;
import java.util.*;

public class SpreadsheetGenerator_old {
    private final StringBuilder htmlContent;
    private final Set<String> uniqueFirstElements = new HashSet<>();
    private final Map<String, List<String[]>> groupedRows = new HashMap<>();
    private final Map<String, List<Map<String, Vector3f>>> groupedColors = new HashMap<>();
    
    private static final String[] HEADER_ROW = {
        "Biome name", "In-game Name", "Biome Temperature",
        "Biome Grime \n (downfall)", "Biome Palette", "Biome Fade palette",
        "Biome Fade strength", "In-game additions", "Biome Sky-color",
        "Biome Water-color","Biome Water-fog-color", "Biome Fog-color"
    };
    
    public SpreadsheetGenerator_old() {
        this.htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html><html><head><style>");
        htmlContent.append("table { border-collapse: collapse; margin-bottom: 20px; width: auto; }");
        htmlContent.append("td { border: 1px solid black; padding: 5px; white-space: nowrap; }");
        htmlContent.append("tr { border-bottom: 2px solid #ddd; }");
        htmlContent.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        htmlContent.append("tr:hover { background-color: #f5f5f5; }");
        htmlContent.append(".controls { position: fixed; bottom: 0; left: 0; background: white; ");
        htmlContent.append("padding: 10px; border: 1px solid black; box-shadow: 0 0 10px rgba(0,0,0,0.1); ");
        htmlContent.append("z-index: 9999; max-height: 80vh; overflow-y: auto; min-width: 150px; }");
        htmlContent.append(".table-container { display: none; margin: 20px; overflow-x: auto; }");
        htmlContent.append(".table-container.active { display: block; }");
        htmlContent.append("body { margin-bottom: 150px; padding: 20px; }");
        htmlContent.append("</style>");
        
        // Updated JavaScript with proper function definitions
        htmlContent.append("<script>");
        
        // Define functions in global scope first
        htmlContent.append("function filterRows() {");
        htmlContent.append("  const table = document.getElementById(window.currentPrefix);");
        htmlContent.append("  if (!table) return;");
        htmlContent.append("  const rows = table.getElementsByTagName('tr');");
        htmlContent.append("  for (let i = 1; i < rows.length; i++) {");
        htmlContent.append("    const firstCell = rows[i].cells[0].textContent;");
        htmlContent.append("    if (window.activeScreens.size === 0) {");
        htmlContent.append("      rows[i].style.display = '';");
        htmlContent.append("    } else {");
        htmlContent.append("      let showRow = false;");
        htmlContent.append("      window.activeScreens.forEach(screen => {");
        htmlContent.append("        if (firstCell.includes('screen_' + screen)) showRow = true;");
        htmlContent.append("      });");
        htmlContent.append("      rows[i].style.display = showRow ? '' : 'none';");
        htmlContent.append("    }");
        htmlContent.append("  }");
        htmlContent.append("}");
        
        htmlContent.append("function selectPrefix(prefixId, radio) {");
        htmlContent.append("  window.currentPrefix = prefixId;");
        htmlContent.append("  document.querySelectorAll('input[name=\"prefixSelect\"]').forEach(btn => {");
        htmlContent.append("    btn.checked = (btn === radio);");
        htmlContent.append("  });");
        htmlContent.append("  document.querySelectorAll('.table-container').forEach(container => {");
        htmlContent.append("    container.classList.remove('active');");
        htmlContent.append("  });");
        htmlContent.append("  document.getElementById(prefixId).classList.add('active');");
        htmlContent.append("  filterRows();");
        htmlContent.append("  return false;");
        htmlContent.append("}");
        
        htmlContent.append("function toggleScreen(screenId, checkbox) {");
        htmlContent.append("  if (checkbox.checked) {");
        htmlContent.append("    window.activeScreens.add(screenId);");
        htmlContent.append("  } else {");
        htmlContent.append("    window.activeScreens.delete(screenId);");
        htmlContent.append("  }");
        htmlContent.append("  filterRows();");
        htmlContent.append("  return true;");
        htmlContent.append("}");
        
        // Initialize variables when DOM is loaded
        htmlContent.append("document.addEventListener('DOMContentLoaded', function() {");
        htmlContent.append("  window.currentPrefix = 'fullData';");
        htmlContent.append("  window.activeScreens = new Set();");
        htmlContent.append("});");
        
        htmlContent.append("</script>");
        
        htmlContent.append("</head><body>");
    }
    
    public void addColoredRow(String[] values, Map<String, Vector3f> colors) {
        if (values.length > 0) {
            String[] firstElementValues = values[0].split(",");
            if (firstElementValues.length > 0) {
                String prefix = firstElementValues[0].substring(0, 2);
                uniqueFirstElements.add(prefix);
                
                // Store rows
                groupedRows.computeIfAbsent(prefix, k -> new ArrayList<>()).add(values);
                
                // Store colors - create a deep copy for each row
                Map<String, Vector3f> colorsCopy = new HashMap<>();
                colors.forEach((key, value) -> colorsCopy.put(key, new Vector3f(value)));
                groupedColors.computeIfAbsent(prefix, k -> new ArrayList<>()).add(colorsCopy);
            }
        }
    }
    
    private void writeTable(StringBuilder sb, String tableId, List<String[]> rows, List<Map<String, Vector3f>> colors) {
        sb.append(String.format("<div id='%s' class='table-container%s'>", 
            tableId, 
            tableId.equals("fullData") ? " active" : ""));
        sb.append("<table>");
        
        // Add header row
        if (!rows.isEmpty() && !colors.isEmpty()) {
            sb.append("<tr style='background-color: #e0e0e0;'>");
            for (String header : HEADER_ROW) {
                sb.append("<th style='padding: 10px; white-space: nowrap;'>").append(header).append("</th>");
            }
            sb.append("</tr>");
        }
        
        // Add data rows
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            Map<String, Vector3f> rowColors = colors.get(i);
            
            sb.append("<tr>");
            for (String value : row) {
                String[] splitValues = value.split(",");
                for (String splitValue : splitValues) {
                    sb.append("<td>").append(splitValue.trim()).append("</td>");
                }
            }
            
            // Add colored cells using the row-specific colors
            for (Map.Entry<String, Vector3f> entry : rowColors.entrySet()) {
                Vector3f color = entry.getValue();
                String backgroundColor = String.format("rgb(%d,%d,%d)", 
                    (int)(color.x), 
                    (int)(color.y), 
                    (int)(color.z));
                    
                sb.append(String.format("<td style='background-color: %s'>%s</td>", 
                    backgroundColor, 
                    entry.getKey()));
            }
            sb.append("</tr>");
        }
        
        sb.append("</table></div>");
    }
    
    public void saveSpreadsheet(String filePath) {
        // Write full data table first
        List<Map<String, Vector3f>> allColors = groupedColors.values().stream()
            .flatMap(List::stream)
            .toList();
        writeTable(htmlContent, "fullData", 
            groupedRows.values().stream().flatMap(List::stream).toList(),
            allColors);
        
        // Write filtered tables
        for (String prefix : uniqueFirstElements) {
            String tableId = "table-" + prefix.replaceAll("[^a-zA-Z0-9]", "");
            writeTable(htmlContent, tableId, 
                groupedRows.getOrDefault(prefix, new ArrayList<>()),
                groupedColors.getOrDefault(prefix, new ArrayList<>()));
        }
        
        // Write screen-specific tables
        Set<String> screenGroups = new TreeSet<>(); // Using TreeSet for natural ordering
        for (List<String[]> rows : groupedRows.values()) {
            for (String[] row : rows) {
                if (row.length > 0) {
                    String firstCell = row[0];
                    // Check for screen_0 through screen_20
                    for (int i = 0; i <= 20; i++) {
                        String screenPrefix = String.format("screen_%d", i);
                        if (firstCell.contains(screenPrefix)) {
                            screenGroups.add(screenPrefix);
                        }
                    }
                }
            }
        }
        
        // Create screen-specific tables
        for (String screenGroup : screenGroups) {
            String tableId = "screen-" + screenGroup.replaceAll("[^a-zA-Z0-9]", "");
            List<String[]> screenRows = new ArrayList<>();
            List<Map<String, Vector3f>> screenColors = new ArrayList<>();
            
            for (Map.Entry<String, List<String[]>> entry : groupedRows.entrySet()) {
                List<String[]> rows = entry.getValue();
                List<Map<String, Vector3f>> colors = groupedColors.get(entry.getKey());
                
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i)[0].contains(screenGroup)) {
                        screenRows.add(rows.get(i));
                        screenColors.add(colors.get(i));
                    }
                }
            }
            
            writeTable(htmlContent, tableId, screenRows, screenColors);
        }

        /*
        // Write controls with updated onclick handlers
        htmlContent.append("<div class='controls'>");
        htmlContent.append("<strong>Data Groups:</strong><br>");
        htmlContent.append(String.format(
            "<label data-tableid='fullData'><input type='radio' name='prefixSelect' checked onclick='return selectPrefix(\"fullData\", this)'>Full Data</label><br>"
        ));
        
        // Add regular prefix toggles with updated onclick handlers
        for (String value : uniqueFirstElements) {
            String tableId = "table-" + value.replaceAll("[^a-zA-Z0-9]", "");
            htmlContent.append(String.format(
                "<label data-tableid='%s'><input type='radio' name='prefixSelect' onclick='return selectPrefix(\"%s\", this)'>%s</label><br>",
                tableId, tableId, value
            ));
        }
        
        // Add screen toggles if any found
        if (!screenGroups.isEmpty()) {
            htmlContent.append("<hr>"); // Separator
            htmlContent.append("<strong>Screen Filters (Multiple Choice):</strong><br>");
            for (String screenGroup : screenGroups) {
                String screenId = screenGroup.replaceAll("[^0-9]", "");
                htmlContent.append(String.format(
                    "<label><input type='checkbox' onclick='toggleScreen(\"%s\", this)'>%s</label><br>",
                    screenId, screenGroup
                ));
            }
        }
        htmlContent.append("</div>");
         */
        
        // Close HTML
        htmlContent.append("</body></html>");
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(htmlContent.toString());
        } catch (IOException ignored) {}
    }
}