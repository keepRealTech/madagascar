package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.IosOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IosOrderRepository extends JpaRepository<IosOrder, String> {

}
