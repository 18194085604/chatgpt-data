package com.gjy.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

@Data
@AllArgsConstructor
public class MessageEntity {
    private String role;
    private String content;
    private String name;
}
