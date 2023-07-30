package com.example.phonepe.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Slot {

    LocalDateTime startTime;

    LocalDateTime endTime;
}
