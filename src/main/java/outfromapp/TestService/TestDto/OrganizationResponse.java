package outfromapp.TestService.TestDto;

import com.aigreentick.services.contacts.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private Long id;
    private Long parentOrganizationId;
    private String uuid;
    private String slug;
    private String name;
    private String displayName;
    private String description;
    private String logoUrl;
    private String website;
    private Organization.OrganizationType organizationType;
    private Integer resellerLevel;
    private Organization.PricingStrategy pricingStrategy;
    private Integer maxChildResellers;
    private Long ownerUserId;
    private Organization.Status status;
    private LocalDateTime trialEndsAt;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static OrganizationResponse fromEntity(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .parentOrganizationId(organization.getParentOrganizationId())
                .uuid(organization.getUuid())
                .slug(organization.getSlug())
                .name(organization.getName())
                .displayName(organization.getDisplayName())
                .description(organization.getDescription())
                .logoUrl(organization.getLogoUrl())
                .website(organization.getWebsite())
                .organizationType(organization.getOrganizationType())
                .resellerLevel(organization.getResellerLevel())
                .pricingStrategy(organization.getPricingStrategy())
                .maxChildResellers(organization.getMaxChildResellers())
                .ownerUserId(organization.getOwnerUserId())
                .status(organization.getStatus())
                .trialEndsAt(organization.getTrialEndsAt())
                .metadata(organization.getMetadata())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .deletedAt(organization.getDeletedAt())
                .build();
    }
}