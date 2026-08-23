package com.brite.reminder.service;

import com.brite.reminder.model.Appointment;
import com.brite.reminder.model.Contact;
import com.brite.reminder.repository.AppointmentRepository;
import com.brite.reminder.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ReminderService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private MockChannelService mockChannelService;

    // Trigger process
    public void processReminders() {
        List<Appointment> pending = appointmentRepository.findAll().stream()
                .filter(a -> !a.isReminderSent() && "Booked".equals(a.getStatus()))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        // Quiet hours: 8 PM to 8 AM
        if (now.getHour() >= 20 || now.getHour() < 8) {
            System.out.println("Quiet hours active. Aborting reminder run.");
            return;
        }

        for (Appointment appt : pending) {
            Contact contact = contactRepository.findById(appt.getResidentId()).orElse(null);
            if (contact == null) continue;

            String template = getMessageTemplate(contact.getLanguage(), appt);
            boolean sent = trySend(contact, template, now);
            if (sent) {
                appt.setReminderSent(true);
                appointmentRepository.save(appt);
            }
        }
    }

    private boolean trySend(Contact contact, String message, LocalDateTime now) {
        // Try SMS
        if (!"Y".equalsIgnoreCase(contact.getSmsOptout()) && contact.getMobile() != null && !contact.getMobile().isEmpty()) {
            Map<String, String> res = mockChannelService.sendSms(contact.getMobile(), message, now, 1);
            if ("delivered".equals(res.get("status"))) return true;
        }
        
        // Try Voice
        if (!"Y".equalsIgnoreCase(contact.getVoiceOptout())) {
            String number = (contact.getLandline() != null && !contact.getLandline().isEmpty()) ? contact.getLandline() : contact.getMobile();
            if (number != null && !number.isEmpty()) {
                Map<String, String> res = mockChannelService.sendVoice(number, message, now, 1);
                if ("answered".equals(res.get("status"))) return true;
            }
        }

        // Try Email
        if (!"Y".equalsIgnoreCase(contact.getEmailOptout()) && contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            Map<String, String> res = mockChannelService.sendEmail(contact.getEmail(), message, now, 1);
            if ("delivered".equals(res.get("status"))) return true;
        }
        
        return false;
    }

    private String getMessageTemplate(String lang, Appointment appt) {
        // Fallback to english if language is unsupported or null
        if (lang == null || (!lang.equals("en") && !lang.equals("es"))) {
            lang = "en"; // silent fallback per requirements handling
        }
        
        if ("es".equals(lang)) {
            return "Recordatorio de cita: " + appt.getScheduledAt().toString();
        }
        return "Appointment reminder: " + appt.getScheduledAt().toString();
    }
}
