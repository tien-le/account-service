package com.learn4you.account_service.model;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDTO {
//    @Size(min=3, message="INVALID_NAME")
    String name;

//    @Size(min=3, message="INVALID_EMAIL")
    String email;
}