package outfromapp.TestService.TestDto;

import com.aigreentick.services.contacts.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String uuid;
    private Long roleId;
    private String name;
    private String email;
    private LocalDateTime emailVerifiedAt;
    private String avatarUrl;
    private String timezone;
    private String locale;
    private String phone;
    private LocalDateTime phoneVerifiedAt;
    private User.Status status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .roleId(user.getRoleId())
                .name(user.getName())
                .email(user.getEmail())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .avatarUrl(user.getAvatarUrl())
                .timezone(user.getTimezone())
                .locale(user.getLocale())
                .phone(user.getPhone())
                .phoneVerifiedAt(user.getPhoneVerifiedAt())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockedUntil(user.getLockedUntil())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}