package outfromapp.TestService.TestDto;

import com.aigreentick.services.contacts.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private Long roleId;
    private String name;
    private String email;
    private String password; // Plain text password (will be hashed in service)
    private String avatarUrl;
    private String timezone;
    private String locale;
    private String phone;
    private User.Status status;
}