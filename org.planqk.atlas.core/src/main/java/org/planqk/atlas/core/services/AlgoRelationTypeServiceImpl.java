package org.planqk.atlas.core.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.planqk.atlas.core.model.AlgoRelationType;
import org.planqk.atlas.core.model.exceptions.NotFoundException;
import org.planqk.atlas.core.model.exceptions.SqlConsistencyException;
import org.planqk.atlas.core.repository.AlgoRelationTypeRepository;
import org.planqk.atlas.core.repository.AlgorithmRelationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlgoRelationTypeServiceImpl implements AlgoRelationTypeService {

	private static final Logger LOG = LoggerFactory.getLogger(AlgoRelationType.class);
	
	@Autowired
	private AlgoRelationTypeRepository repo;
	@Autowired AlgorithmRelationRepository algorithmRelationRepository;

	@Override
	public AlgoRelationType save(AlgoRelationType algoRelationType) {
		return repo.save(algoRelationType);
	}

	@Override
	public AlgoRelationType update(UUID id, AlgoRelationType algoRelationType) throws NotFoundException {
		// Check for type in database
		Optional<AlgoRelationType> typeOpt = findOptionalById(id);
		// If Type exists
		if (typeOpt.isPresent()) {
			// Update fields
			AlgoRelationType persistedType = typeOpt.get();
			persistedType.setName(algoRelationType.getName());
			// Reference database type to set
			return save(persistedType);
		}
		LOG.info("Trying to update AlgoRelationType which does not exist.");
		throw new NotFoundException("Cannot update AlgoRelationType since it could not be found.");
	}

	@Override
	public void delete(UUID id) throws SqlConsistencyException, NotFoundException {
		if (algorithmRelationRepository.countRelationsUsingRelationType(id) > 0) {
			LOG.info("Trying to delete algoRelationType that is used in at least 1 algorithmRelation.");
			throw new SqlConsistencyException("Cannot delete algoRelationType since it is used by existing algorithmRelations.");
		}
		if (repo.findById(id).isEmpty()) {
			LOG.info("Trying to delete algoRelationType which does not exist.");
			throw new NotFoundException("Cannot delete algoRelationTypesince it could not be found.");
		}
		repo.deleteById(id);
	}

	@Override
	public AlgoRelationType findById(UUID id) throws NotFoundException {
		Optional<AlgoRelationType> algoRelationTypeOpt = findOptionalById(id);
		if (algoRelationTypeOpt.isEmpty()) {
			throw new NotFoundException("The AlgoRelationType could not be found.");
		}
		return algoRelationTypeOpt.get();
	}

	@Override
	public List<AlgoRelationType> findByName(String name) throws NotFoundException {
		Optional<List<AlgoRelationType>> algoRelationTypes = repo.findByName(name);
		if (algoRelationTypes.isEmpty()) {
			throw new NotFoundException("No AlgoRelationType found to match name '" + name + "'");
		}
		return algoRelationTypes.get();
	}

	@Override
	public Page<AlgoRelationType> findAll(Pageable pageable) {
		return repo.findAll(pageable);
	}

	@Override
	public Optional<AlgoRelationType> findOptionalById(UUID id) {
		return repo.findById(id);
	}

}
