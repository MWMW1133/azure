package com.azure.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class PriorityDTO {
    private Short id;
    private String name;
    private Byte level;


    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getLevel() {
        return level;
    }

    public void setLevel(Byte level) {
        this.level = level;
    }
}
