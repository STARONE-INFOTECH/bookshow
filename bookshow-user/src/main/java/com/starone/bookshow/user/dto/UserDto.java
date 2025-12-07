package com.starone.bookshow.user.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.starone.bookshow.user.enums.EProvider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private String userId; // MongoDB uses String IDs (ObjectId)

    private String name;
    private String email;
    private String phoneNo;
    private String password;

    private EProvider provider;
    private String providerId;

    private boolean emailVerified;
    private boolean phoneVerified;

    private List<String> favoriteGenres; // Preferred movie genres
    private List<String> preferredCinemas; // Frequently visited cinemas

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
