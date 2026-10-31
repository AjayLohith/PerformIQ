package com.performiq.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight, robust, RFC-4180 compliant CSV reader.
 * Handles quoted fields, escaped quotes (""), commas, and trims whitespace.
 */
public final class CsvReader {

    private CsvReader() {}

    public static List<Map<String, String>> readCsv(Path filePath) throws IOException {
        try (InputStream is = Files.newInputStream(filePath)) {
            return readCsv(is);
        }
    }

    public static List<Map<String, String>> readCsv(InputStream inputStream) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return records;
            }

            // Remove UTF-8 BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            List<String> headers = parseCsvLine(headerLine);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> values = parseCsvLine(line);
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i).trim();
                    String value = i < values.size() ? values.get(i).trim() : "";
                    row.put(header, value);
                }
                records.add(row);
            }
        }
        return records;
    }

    public static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }
}
