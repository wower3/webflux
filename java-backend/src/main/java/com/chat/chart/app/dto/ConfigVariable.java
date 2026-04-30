package com.chat.chart.app.dto;

import lombok.Data;

@Data
public class ConfigVariable {
    private String name;
    private String value;
    
    public ConfigVariable(){}

    public ConfigVariable(String name, String value){
        this.name = name;
        this.value = value;
    }
}
