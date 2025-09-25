package com.azure.model;


import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "user_settings")
public class UserSetting {
    @Id @Column(name = "user_id") private Long userId;
    @OneToOne(fetch = FetchType.LAZY) @MapsId @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "receive_email") private Boolean receiveEmail;
    @Column(name = "receive_push") private Boolean receivePush;
    @Column(name = "default_view") private String defaultView;
}
