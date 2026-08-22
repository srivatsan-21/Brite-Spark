package com.brite.reminder.service;

import com.brite.reminder.model.Appointment;
import com.brite.reminder.model.Contact;
import com.brite.reminder.repository.AppointmentRepository;
import com.brite.reminder.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DataImportService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void importData() {
        importContacts("contacts.csv");
        importAppointments("appointments.csv");
        System.out.println("Data import complete!");
    }

    private void importContacts(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] values = line.split(",", -1);
                Contact c = new Contact();
                c.setResidentId(values[0]);
                c.setName(values[1]);
                c.setMobile(values[2]);
                c.setLandline(values[3]);
                c.setEmail(values[4]);
                c.setLanguage(values[5]);
                c.setSmsOptout(values[6]);
                c.setVoiceOptout(values[7]);
                c.setEmailOptout(values[8]);
                c.setNumberLastVerified(values[9]);
                contactRepository.save(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importAppointments(String filePath) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] values = line.split(",", -1);
                Appointment a = new Appointment();
                a.setAppointmentId(values[0]);
                a.setResidentId(values[1]);
                a.setScheduledAt(LocalDateTime.parse(values[2], formatter));
                a.setLocation(values[3]);
                a.setServiceType(values[4]);
                a.setStatus(values[5]);
                appointmentRepository.save(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
