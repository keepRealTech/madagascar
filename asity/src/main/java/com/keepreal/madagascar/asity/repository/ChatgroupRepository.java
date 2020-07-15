package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.Chatgroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the chat group repository.
 */
@Repository
public interface ChatgroupRepository extends JpaRepository<Chatgroup, String> {

    Chatgroup findByIdAndDeletedIsFalse(String id);

    Long countByIslandIdAndDeletedIsFalse(String islandId);

    Page<Chatgroup> findAllByIslandIdAndDeletedIsFalse(String islandId, Pageable pageable);

    List<Chatgroup> findAllByIdInAndDeletedIsFalse(Iterable<String> ids);

}
