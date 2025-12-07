package com.starone.bookshow.user.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.starone.bookshow.user.enums.EProvider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class User {

    @Id
    private String userId;  // MongoDB uses String IDs (ObjectId)

    private String name;
    private String email;
    private String phoneNo;
    private String password;

    private EProvider provider;
    private String providerId;

    private boolean emailVerified;
    private boolean phoneVerified;
    
    private List<String> favoriteGenres;  // Preferred movie genres
    private List<String> preferredCinemas;  // Frequently visited cinemas

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
