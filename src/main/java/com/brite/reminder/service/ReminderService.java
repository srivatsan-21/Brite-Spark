package com.brite.reminder.service;

import com.brite.reminder.model.Appointment;
import com.brite.reminder.model.Contact;
import com.brite.reminder.repository.AppointmentRepository;
import com.brite.reminder.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private StrictMessagingGateway strictMessagingGateway;

    // Trigger process
    public void processReminders() {
        List<Appointment> pending = appointmentRepository.findAll().stream()
                .filter(a -> !a.isReminderSent() && "Booked".equals(a.getStatus()))
                .toList();

        LocalDateTime now = LocalDateTime.now();

        // Stats tracking
        int totalProcessed = 0;
        int languageFallbacks = 0;
        int successfullyReached = 0;

        // Group appointments by Resident ID first
        Map<String, List<Appointment>> byResident = pending.stream()
                .collect(Collectors.groupingBy(Appointment::getResidentId));

        // Now group by the contact point (e.g., mobile number) to handle duplicates
        Map<String, List<Appointment>> byContactPoint = new HashMap<>();
        Map<String, Contact> pointToContact = new HashMap<>();

        for (Map.Entry<String, List<Appointment>> entry : byResident.entrySet()) {
            Contact contact = contactRepository.findById(entry.getKey()).orElse(null);
            if (contact == null) continue;

            String contactPoint = determinePrimaryContactPoint(contact);
            if (contactPoint == null) continue; // Unreachable

            byContactPoint.computeIfAbsent(contactPoint, k -> new ArrayList<>()).addAll(entry.getValue());
            pointToContact.put(contactPoint, contact); // Use the first contact object found for this point
        }

        // Process each contact point
        for (Map.Entry<String, List<Appointment>> entry : byContactPoint.entrySet()) {
            String contactPoint = entry.getKey();
            List<Appointment> appointments = entry.getValue();
            Contact contact = pointToContact.get(contactPoint);

            totalProcessed += appointments.size();

            // Language checking
            String lang = contact.getLanguage();
            if (lang == null || (!lang.equals("en") && !lang.equals("es"))) {
                languageFallbacks++;
                lang = "en"; // silent fallback per requirements handling (but we track it!)
            }

            String consolidatedMessage = buildConsolidatedMessage(lang, appointments);
            boolean sent = strictMessagingGateway.send(contact, consolidatedMessage, now);
            
            if (sent) {
                successfullyReached += appointments.size();
                for (Appointment appt : appointments) {
                    appt.setReminderSent(true);
                    appointmentRepository.save(appt);
                }
            }
        }

        // Output measurable success report
        System.out.println("====== REMINDER JOB REPORT ======");
        System.out.println("Total Appointments Processed: " + totalProcessed);
        System.out.println("Appointments Reached: " + successfullyReached);
        System.out.println("Language Fallbacks (to English): " + languageFallbacks);
        System.out.println("=================================");
    }

    private String determinePrimaryContactPoint(Contact contact) {
        if (!"Y".equalsIgnoreCase(contact.getSmsOptout()) && contact.getMobile() != null && !contact.getMobile().isEmpty()) {
            return "MOBILE:" + contact.getMobile();
        }
        if (!"Y".equalsIgnoreCase(contact.getVoiceOptout()) && contact.getLandline() != null && !contact.getLandline().isEmpty()) {
            return "VOICE:" + contact.getLandline();
        }
        if (!"Y".equalsIgnoreCase(contact.getEmailOptout()) && contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            return "EMAIL:" + contact.getEmail();
        }
        return null;
    }

    private String buildConsolidatedMessage(String lang, List<Appointment> appointments) {
        StringBuilder sb = new StringBuilder();
        if ("es".equals(lang)) {
            sb.append("Recordatorio de cita:\n");
        } else {
            sb.append("Appointment reminder:\n");
        }

        for (Appointment appt : appointments) {
            sb.append("- ").append(appt.getScheduledAt().toString()).append(" at ").append(appt.getLocation()).append("\n");
        }
        return sb.toString();
    }
}
