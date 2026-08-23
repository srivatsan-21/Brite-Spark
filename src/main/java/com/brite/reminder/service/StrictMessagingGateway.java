package com.brite.reminder.service;

import com.brite.reminder.model.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class StrictMessagingGateway {

    @Autowired
    private MockChannelService mockChannelService;

    public boolean send(Contact contact, String message, LocalDateTime now) {
        // Enforce Quiet Hours: 8 PM to 8 AM
        if (now.getHour() >= 20 || now.getHour() < 8) {
            System.out.println("[Gateway] Blocked message to " + contact.getResidentId() + " due to Quiet Hours.");
            return false; 
        }

        // Try SMS (if not opted out)
        if (!"Y".equalsIgnoreCase(contact.getSmsOptout()) && contact.getMobile() != null && !contact.getMobile().isEmpty()) {
            Map<String, String> res = mockChannelService.sendSms(contact.getMobile(), message, now, 1);
            if ("delivered".equals(res.get("status"))) return true;
        }

        // Try Voice (if not opted out)
        if (!"Y".equalsIgnoreCase(contact.getVoiceOptout())) {
            String number = (contact.getLandline() != null && !contact.getLandline().isEmpty()) ? contact.getLandline() : contact.getMobile();
            if (number != null && !number.isEmpty()) {
                Map<String, String> res = mockChannelService.sendVoice(number, message, now, 1);
                if ("answered".equals(res.get("status"))) return true;
            }
        }

        // Try Email (if not opted out)
        if (!"Y".equalsIgnoreCase(contact.getEmailOptout()) && contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            Map<String, String> res = mockChannelService.sendEmail(contact.getEmail(), message, now, 1);
            if ("delivered".equals(res.get("status"))) return true;
        }

        return false;
    }
}
