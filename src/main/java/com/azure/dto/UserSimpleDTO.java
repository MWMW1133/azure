package com.azure.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UserSimpleDTO {
    private Long id;
    private String name;
    private String avatarUrl;
}
