package com.brite.reminder.controller;

import com.brite.reminder.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @PostMapping("/trigger")
    public String triggerReminders() {
        reminderService.processReminders();
        return "Reminder job completed";
    }
}
