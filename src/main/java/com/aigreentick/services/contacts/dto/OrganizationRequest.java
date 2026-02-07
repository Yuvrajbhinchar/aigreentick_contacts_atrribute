package com.aigreentick.services.contacts.dto;

import com.aigreentick.services.contacts.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {

    private Long parentOrganizationId;
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
    private String metadata;
}