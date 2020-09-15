package com.keepreal.madagascar.hoopoe.dao;

import com.keepreal.madagascar.hoopoe.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  Represents the island activity repository.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {

    List<Activity> findAllByActiveIsTrueAndDeletedIsFalse();

}