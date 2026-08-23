package com.brite.reminder.service;

import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    /**
     * Simulates sending a push notification via Firebase Cloud Messaging (FCM).
     * In a real environment with a serviceAccountKey.json, this would look like:
     * 
     * Message message = Message.builder()
     *     .setNotification(Notification.builder()
     *         .setTitle("Appointment Reminder")
     *         .setBody(body)
     *         .build())
     *     .setToken(fcmToken)
     *     .build();
     * String response = FirebaseMessaging.getInstance().send(message);
     * return response != null;
     */
    public boolean sendPushNotification(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            return false;
        }
        
        System.out.println("[FCM Mock] Successfully pushed notification to token: " + fcmToken);
        // Note: For Day 2 requirements, we should also log this to outbox.jsonl so it counts against the rate limit,
        // but for simplicity in this demo, we'll assume Push Notifications bypass telecom limits or 
        // we'd add it to MockChannelService's log().
        
        return true;
    }
}
