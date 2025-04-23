package com.myproject.busticket.models;

import com.myproject.busticket.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue
    @Column(name = "notification_id")
    private int notificationId;

    @Column(name = "type", columnDefinition = "enum('group', 'account') default 'group'", nullable = false)
    private NotificationType type;

    @Column(name = "subject", nullable = false, length = 50)
    private String subject;

    @Column(name = "message", nullable = false, length = 255)
    private String message;
}
