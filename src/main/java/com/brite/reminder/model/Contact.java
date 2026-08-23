package com.brite.reminder.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Contact {
    
    @Id
    private String residentId;
    
    private String name;
    private String mobile;
    private String landline;
    private String email;
    private String language;
    private String smsOptout;
    private String voiceOptout;
    private String emailOptout;
    private String numberLastVerified;
    private String fcmToken;
    private String pushOptout;

    // Getters and Setters
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getPushOptout() { return pushOptout; }
    public void setPushOptout(String pushOptout) { this.pushOptout = pushOptout; }

    public String getResidentId() { return residentId; }
    public void setResidentId(String residentId) { this.residentId = residentId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    
    public String getLandline() { return landline; }
    public void setLandline(String landline) { this.landline = landline; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getSmsOptout() { return smsOptout; }
    public void setSmsOptout(String smsOptout) { this.smsOptout = smsOptout; }
    
    public String getVoiceOptout() { return voiceOptout; }
    public void setVoiceOptout(String voiceOptout) { this.voiceOptout = voiceOptout; }
    
    public String getEmailOptout() { return emailOptout; }
    public void setEmailOptout(String emailOptout) { this.emailOptout = emailOptout; }
    
    public String getNumberLastVerified() { return numberLastVerified; }
    public void setNumberLastVerified(String numberLastVerified) { this.numberLastVerified = numberLastVerified; }
}
