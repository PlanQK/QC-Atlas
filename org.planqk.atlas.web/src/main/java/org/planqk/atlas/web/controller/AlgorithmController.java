/*******************************************************************************
 * Copyright (c) 2020 University of Stuttgart
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.planqk.atlas.web.controller;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.UUID;

import org.planqk.atlas.core.model.Algorithm;
import org.planqk.atlas.core.model.AlgorithmRelation;
import org.planqk.atlas.core.model.ProblemType;
import org.planqk.atlas.core.model.Tag;
import org.planqk.atlas.core.model.exceptions.NotFoundException;
import org.planqk.atlas.core.services.AlgorithmService;
import org.planqk.atlas.web.Constants;
import org.planqk.atlas.web.dtos.AlgorithmDto;
import org.planqk.atlas.web.dtos.AlgorithmListDto;
import org.planqk.atlas.web.dtos.AlgorithmRelationDto;
import org.planqk.atlas.web.dtos.AlgorithmRelationListDto;
import org.planqk.atlas.web.dtos.ProblemTypeDto;
import org.planqk.atlas.web.dtos.TagListDto;
import org.planqk.atlas.web.linkassembler.AlgorithmAssembler;
import org.planqk.atlas.web.linkassembler.ProblemTypeAssembler;
import org.planqk.atlas.web.utils.DtoEntityConverter;
import org.planqk.atlas.web.utils.RestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller to access and manipulate quantum algorithms.
 */
@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
@RequestMapping("/" + Constants.ALGORITHMS)
public class AlgorithmController {

    final private static Logger LOG = LoggerFactory.getLogger(AlgorithmController.class);

    @Autowired
    private AlgorithmService algorithmService;
    @Autowired
    private DtoEntityConverter modelConverter;
    @Autowired
    private ProblemTypeAssembler problemTypeAssembler;
    @Autowired
    private AlgorithmAssembler algorithmAssembler;

    /**
     * Create a DTO object for a given {@link Algorithm} with the contained data and the links to related objects.
     *
     * @param algorithm the {@link Algorithm} to create the DTO for
     * @return the created DTO
     */
    public static AlgorithmDto createAlgorithmDto(Algorithm algorithm) {
    	AlgorithmDto dto = AlgorithmDto.Converter.convert(algorithm);
        dto.add(linkTo(methodOn(AlgorithmController.class).getAlgorithm(algorithm.getId())).withSelfRel());
        dto.add(linkTo(methodOn(AlgorithmController.class).getTags(algorithm.getId())).withRel(Constants.TAGS));
        dto.add(linkTo(methodOn(ImplementationController.class).getImplementations(algorithm.getId())).withRel(Constants.IMPLEMENTATIONS));
        dto.add(linkTo(methodOn(AlgorithmController.class).getProblemTypes(algorithm.getId())).withRel(Constants.PROBLEM_TYPES));
        return dto;
    }

	public static AlgorithmRelationListDto createAlgorithmRelationDtoList(Stream<AlgorithmRelation> stream) {
		AlgorithmRelationListDto algorithmRelationListDto = new AlgorithmRelationListDto();
		algorithmRelationListDto.add(stream.map(algorithmRelation -> createAlgorithmRelationDto(algorithmRelation)).collect(Collectors.toList()));
		return algorithmRelationListDto;
	}

	public static AlgorithmRelationDto createAlgorithmRelationDto(AlgorithmRelation algorithmRelation) {
		AlgorithmRelationDto dto = AlgorithmRelationDto.Converter.convert(algorithmRelation);
		dto.add(linkTo(methodOn(AlgorithmController.class).getAlgorithm(algorithmRelation.getSourceAlgorithm().getId())).withSelfRel());
		return dto;
	}

    @GetMapping("/")
    public HttpEntity<AlgorithmListDto> getAlgorithms(@RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size) {
        LOG.debug("Get to retrieve all algorithms received.");
        return new ResponseEntity<>(modelConverter.convert(algorithmService.findAll(RestUtils.getPageableFromRequestParams(page, size))), HttpStatus.OK);
    }

    @PostMapping("/")
    public HttpEntity<AlgorithmDto> createAlgorithm(@Validated @RequestBody AlgorithmDto algo) {
        LOG.debug("Post to create new algorithm received.");

        // store and return algorithm
        Algorithm algorithm = algorithmService.save(modelConverter.convert(algo));

        return new ResponseEntity<>(modelConverter.convert(algorithm), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public HttpEntity<AlgorithmDto> updateAlgorithm(@PathVariable UUID id, @Validated @RequestBody AlgorithmDto algo) throws NotFoundException {
        LOG.debug("Put to update algorithm with id '" + id + "' received");
        
        return new ResponseEntity<>(modelConverter.convert(algorithmService.update(id, modelConverter.convert(algo))), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public HttpEntity<AlgorithmDto> deleteAlgorithm(@PathVariable UUID id) throws NotFoundException {
        LOG.debug("Delete to remove algorithm with id '" + id + "' received");

        algorithmService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public HttpEntity<AlgorithmDto> getAlgorithm(@PathVariable UUID id) {
        LOG.debug("Get to retrieve algorithm with id: {}.", id);

        Optional<Algorithm> algorithmOptional = algorithmService.findById(id);
        if (!algorithmOptional.isPresent()) {
            LOG.error("Unable to retrieve algorithm with id {} from the repository.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(modelConverter.convert(algorithmOptional.get()), HttpStatus.OK);
    }

    @GetMapping("/{id}/" + Constants.TAGS)
    public HttpEntity<TagListDto> getTags(@PathVariable UUID id) {
        Optional<Algorithm> algorithmOptional = algorithmService.findById(id);
        if (!algorithmOptional.isPresent()) {
            LOG.error("Unable to retrieve algorithm with id {} form the repository.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Set<Tag> tags = algorithmOptional.get().getTags();
        TagListDto tagListDto = TagController.createTagDtoList(tags.stream());
        tagListDto.add(linkTo(methodOn(AlgorithmController.class).getTags(id)).withSelfRel());
        return new ResponseEntity<>(tagListDto, HttpStatus.OK);
    }

    @GetMapping("/{id}/" + Constants.PROBLEM_TYPES)
    public HttpEntity<?> getProblemTypes(@PathVariable UUID id) {
        Optional<Algorithm> algorithmOptional = algorithmService.findById(id);
        if (!algorithmOptional.isPresent()) {
            LOG.error("Unable to retrieve algorithm with id {} form the repository.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Get ProblemTypes of Algorithm
        Set<ProblemType> problemTypes = algorithmOptional.get().getProblemTypes();
        // Translate Entity to DTO
        Set<ProblemTypeDto> dtoTypes = modelConverter.convertSet(problemTypes, ProblemTypeDto.class);
        // Create CollectionModel
        CollectionModel<EntityModel<ProblemTypeDto>> resultCollection = problemTypeAssembler.generateCollectionModel(dtoTypes);
        // Fill Collection-Links
        algorithmAssembler.addProblemTypeLink(resultCollection, id);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @GetMapping("/{sourceAlgorithm_id}/" + Constants.ALGORITHM_RELATIONS)
    public HttpEntity<AlgorithmRelationListDto> getAlgorithmRelations(@PathVariable UUID sourceAlgorithm_id) {
        Optional<Algorithm> optAlgorithm = algorithmService.findById(sourceAlgorithm_id);
        if (!optAlgorithm.isPresent()) {
            LOG.error("Unable to retrieve algorithm with id {} form the repository.", sourceAlgorithm_id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Set<AlgorithmRelation> algorithmRelations = optAlgorithm.get().getAlgorithmRelations();
        AlgorithmRelationListDto algorithmRelationListDto = AlgorithmController.createAlgorithmRelationDtoList(algorithmRelations.stream());
        algorithmRelationListDto.add(linkTo(methodOn(AlgorithmController.class).getAlgorithmRelations(sourceAlgorithm_id)).withSelfRel());
        return new ResponseEntity<>(algorithmRelationListDto, HttpStatus.OK);
    }

    @PutMapping("/{sourceAlgorithm_id}/" + Constants.ALGORITHM_RELATIONS)
    public HttpEntity<AlgorithmRelationDto> updateAlgorithmRelation(@PathVariable UUID sourceAlgorithm_id,
    		@Validated @RequestBody AlgorithmRelationDto relation) throws NotFoundException {
        LOG.debug("Post to add algorithm relation received.");
        
        
        AlgorithmRelation algorithmRelation = algorithmService.addUpdateAlgorithmRelation(sourceAlgorithm_id, modelConverter.convert(relation));
        return new ResponseEntity<>(modelConverter.convert(algorithmRelation), HttpStatus.OK);
    }

    @DeleteMapping("/{sourceAlgorithm_id}/" + Constants.ALGORITHM_RELATIONS + "/{relation_id}")
    public HttpEntity<AlgorithmDto> deleteAlgorithmRelation(@PathVariable UUID sourceAlgorithm_id, @PathVariable UUID relation_id)
    		throws NotFoundException {
        LOG.debug("Delete received to remove algorithm relation with id {}.", relation_id);
        if (!algorithmService.deleteAlgorithmRelation(sourceAlgorithm_id, relation_id)) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
