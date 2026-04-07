package com.learn4you.account_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@NoArgsConstructor
@ToString
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name= "to_email", nullable = false, length = 255)
    String to;

    @Column(name= "to_name", length = 100)
    String toName;

    @Column(nullable = false, length = 255)
    String subject;

    @Column(nullable = false)
    String content;

    boolean status;
}