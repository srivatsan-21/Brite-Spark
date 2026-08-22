package com.brite.reminder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class MockChannelService {

    @Value("${OUTBOX_PATH:outbox.jsonl}")
    private String outboxPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private double roll(Object... parts) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                sb.append(parts[i]);
                if (i < parts.length - 1) {
                    sb.append("|");
                }
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            
            // Get first 8 hex characters (4 bytes)
            long val = 0;
            for (int i = 0; i < 4; i++) {
                val = (val << 8) | (hash[i] & 0xFF);
            }
            
            return (double) val / 0xFFFFFFFFL;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isLandline(String number) {
        try {
            String[] parts = number.split("-");
            if (parts.length >= 2) {
                int mid = Integer.parseInt(parts[1]);
                return mid >= 200 && mid <= 249;
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    private Map<String, String> log(String channel, String to, String body, LocalDateTime at, String status, String detail) {
        Map<String, String> record = new HashMap<>();
        record.put("channel", channel);
        record.put("to", to);
        record.put("at", at != null ? at.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
        record.put("body_preview", body != null && body.length() > 60 ? body.substring(0, 60) : (body == null ? "" : body));
        record.put("status", status);
        record.put("detail", detail == null ? "" : detail);

        try (PrintWriter out = new PrintWriter(new FileWriter(outboxPath, true))) {
            out.println(objectMapper.writeValueAsString(record));
        } catch (IOException e) {
            // Ignore error
        }

        Map<String, String> result = new HashMap<>();
        result.put("status", status);
        result.put("detail", detail == null ? "" : detail);
        return result;
    }

    public Map<String, String> sendSms(String to, String body, LocalDateTime at, int attempt) {
        at = at != null ? at : LocalDateTime.now();
        if (to == null || to.trim().isEmpty()) {
            return log("sms", to, body, at, "failed", "no_number");
        }
        double r = roll("sms", to, body, attempt);
        if (isLandline(to)) {
            if (r < 0.55) {
                return log("sms", to, body, at, "delivered", "accepted_by_carrier");
            }
            return log("sms", to, body, at, "failed", "unroutable_landline");
        }
        if (r < 0.88) {
            return log("sms", to, body, at, "delivered", "");
        }
        if (r < 0.95) {
            return log("sms", to, body, at, "failed", "carrier_rejected");
        }
        return log("sms", to, body, at, "failed", "unknown_subscriber");
    }

    public Map<String, String> sendVoice(String to, String body, LocalDateTime at, int attempt) {
        at = at != null ? at : LocalDateTime.now();
        if (to == null || to.trim().isEmpty()) {
            return log("voice", to, body, at, "failed", "no_number");
        }
        double r = roll("voice", to, body, attempt);
        if (r < 0.42) return log("voice", to, body, at, "answered", "human");
        if (r < 0.60) return log("voice", to, body, at, "answered", "voicemail_left");
        if (r < 0.90) return log("voice", to, body, at, "no_answer", "");
        if (r < 0.96) return log("voice", to, body, at, "failed", "busy");
        return log("voice", to, body, at, "failed", "number_unobtainable");
    }

    public Map<String, String> sendEmail(String to, String body, LocalDateTime at, int attempt) {
        at = at != null ? at : LocalDateTime.now();
        if (to == null || to.trim().isEmpty()) {
            return log("email", to, body, at, "failed", "no_address");
        }
        double r = roll("email", to, body, attempt);
        if (r < 0.78) return log("email", to, body, at, "delivered", "");
        if (r < 0.86) return log("email", to, body, at, "delivered", "placed_in_spam");
        if (r < 0.93) return log("email", to, body, at, "failed", "soft_bounce");
        return log("email", to, body, at, "failed", "hard_bounce");
    }
}
