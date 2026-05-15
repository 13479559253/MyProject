package com.example.demo.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionData {
    private Double latitude;
    private Double longitude;
    private Integer id;
}
