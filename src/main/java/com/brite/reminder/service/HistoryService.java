package com.brite.reminder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class HistoryService {

    @Value("${OUTBOX_PATH:outbox.jsonl}")
    private String outboxPath;

    /**
     * Reads outbox.jsonl and calculates the number of contacts per contact point (the "to" field)
     * within the specified rolling window (in days) from the given 'now' time.
     */
    public Map<String, Integer> getRecentContactCounts(LocalDateTime now, int days) {
        Map<String, Integer> counts = new HashMap<>();
        File file = new File(outboxPath);
        if (!file.exists()) {
            return counts;
        }

        LocalDateTime cutoff = now.minusDays(days);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    String to = extractJsonField(line, "to");
                    String atStr = extractJsonField(line, "at");

                    if (to != null && atStr != null && !atStr.isEmpty()) {
                        LocalDateTime at = LocalDateTime.parse(atStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        // Using isBefore/isAfter to safely check time range
                        if (!at.isBefore(cutoff) && !at.isAfter(now)) {
                            counts.put(to, counts.getOrDefault(to, 0) + 1);
                        }
                    }
                } catch (Exception e) {
                    // Ignore parse errors for individual lines
                    System.err.println("[HistoryService] Failed to parse line: " + line);
                }
            }
        } catch (Exception e) {
            System.err.println("[HistoryService] Failed to read outbox file: " + e.getMessage());
        }

        return counts;
    }

    private String extractJsonField(String json, String field) {
        String searchStr = "\"" + field + "\":\"";
        int start = json.indexOf(searchStr);
        if (start == -1) return null;
        start += searchStr.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end).replace("\\\"", "\"").replace("\\n", "\n");
    }
}
