package com.keepreal.madagascar.tenrecs.model;

import com.keepreal.madagascar.common.ReactionType;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * Represents the reaction model in notification.
 */
@Builder
@Data
public class Reaction {

    private String id;
    private String feedId;
    private String authorId;
    private Set<ReactionType> types;
    private Long createdAt;

}