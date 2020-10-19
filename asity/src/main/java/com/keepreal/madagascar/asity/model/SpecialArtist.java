package com.keepreal.madagascar.asity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *  Represents the special artist entity.
 */
@Data
@Table(name = "special_artists_temp")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialArtist {
    @Id
    String id;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
}
