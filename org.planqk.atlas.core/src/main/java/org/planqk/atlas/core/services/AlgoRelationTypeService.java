package org.planqk.atlas.core.services;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.planqk.atlas.core.model.AlgoRelationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlgoRelationTypeService {

	AlgoRelationType save(AlgoRelationType algoRelationType);
	
	AlgoRelationType update(UUID id, AlgoRelationType algoRelationType);
	
	void delete(UUID id);
	
	Optional<AlgoRelationType> findById(UUID id);
	
	Optional<AlgoRelationType> findByName(String name);
	
	Page<AlgoRelationType> findAll(Pageable pageable);
	
	Set<AlgoRelationType> createOrUpdateAll(Set<AlgoRelationType> algoRelationTypes);
}