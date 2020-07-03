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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;

import org.planqk.atlas.core.model.Algorithm;
import org.planqk.atlas.core.model.AlgorithmRelation;
import org.planqk.atlas.core.model.ApplicationArea;
import org.planqk.atlas.core.model.ComputingResourceProperty;
import org.planqk.atlas.core.model.ComputingResourcePropertyType;
import org.planqk.atlas.core.model.PatternRelation;
import org.planqk.atlas.core.model.PatternRelationType;
import org.planqk.atlas.core.model.ProblemType;
import org.planqk.atlas.core.model.Publication;
import org.planqk.atlas.core.services.AlgoRelationService;
import org.planqk.atlas.core.services.AlgoRelationTypeService;
import org.planqk.atlas.core.services.AlgorithmService;
import org.planqk.atlas.core.services.ApplicationAreaService;
import org.planqk.atlas.core.services.ComputingResourcePropertyService;
import org.planqk.atlas.core.services.PatternRelationService;
import org.planqk.atlas.core.services.PatternRelationTypeService;
import org.planqk.atlas.core.services.ProblemTypeService;
import org.planqk.atlas.core.services.PublicationService;
import org.planqk.atlas.web.Constants;
import org.planqk.atlas.web.dtos.AlgorithmDto;
import org.planqk.atlas.web.dtos.AlgorithmRelationDto;
import org.planqk.atlas.web.dtos.ApplicationAreaDto;
import org.planqk.atlas.web.dtos.ComputingResourcePropertyDto;
import org.planqk.atlas.web.dtos.ComputingResourcePropertyTypeDto;
import org.planqk.atlas.web.dtos.PatternRelationDto;
import org.planqk.atlas.web.dtos.PatternRelationTypeDto;
import org.planqk.atlas.web.dtos.ProblemTypeDto;
import org.planqk.atlas.web.dtos.PublicationDto;
import org.planqk.atlas.web.linkassembler.AlgorithmAssembler;
import org.planqk.atlas.web.linkassembler.AlgorithmRelationAssembler;
import org.planqk.atlas.web.linkassembler.ApplicationAreaAssembler;
import org.planqk.atlas.web.linkassembler.ComputingResourcePropertyAssembler;
import org.planqk.atlas.web.linkassembler.PatternRelationAssembler;
import org.planqk.atlas.web.linkassembler.ProblemTypeAssembler;
import org.planqk.atlas.web.linkassembler.PublicationAssembler;
import org.planqk.atlas.web.utils.HateoasUtils;
import org.planqk.atlas.web.utils.ModelMapperUtils;
import org.planqk.atlas.web.utils.RestUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

//import org.planqk.atlas.web.linkassembler.TagAssembler;

/**
 * Controller to access and manipulate quantum algorithms.
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "algorithm")
@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
@RequestMapping("/" + Constants.API_VERSION + "/" + Constants.ALGORITHMS)
@AllArgsConstructor
public class AlgorithmController {

    final private static Logger LOG = LoggerFactory.getLogger(AlgorithmController.class);

    private final AlgorithmService algorithmService;
    private final AlgoRelationService algoRelationService;
    private final AlgoRelationTypeService algoRelationTypeService;
    private final ComputingResourcePropertyService computingResourcePropertyService;
    private final PatternRelationService patternRelationService;
    private final PatternRelationTypeService patternRelationTypeService;
    private final ProblemTypeService problemTypeService;
    private final ApplicationAreaService applicationAreaService;
    private final PublicationService publicationService;

    private final PatternRelationController patternRelationController;

    private final PagedResourcesAssembler<AlgorithmDto> algorithmPaginationAssembler;
    private final PagedResourcesAssembler<ComputingResourcePropertyDto> computingResourcePaginationAssembler;
    private final ProblemTypeAssembler problemTypeAssembler;
    private final ApplicationAreaAssembler applicationAreaAssembler;
    //    private final TagAssembler tagAssembler;
    private final AlgorithmAssembler algorithmAssembler;
    private final AlgorithmRelationAssembler algorithmRelationAssembler;
    private final PublicationAssembler publicationAssembler;
    private final ComputingResourcePropertyAssembler computingResourcePropertyAssembler;
    private final PatternRelationAssembler patternRelationAssembler;

    @Operation(responses = {@ApiResponse(responseCode = "200")}, description = "Retrieve all algorithms (quantum, hybrid and classic).")
    @GetMapping()
    public HttpEntity<PagedModel<EntityModel<AlgorithmDto>>> getAlgorithms(@RequestParam(required = true, defaultValue = "0") Integer page,
                                                                           @RequestParam(required = true, defaultValue = "50") Integer size,
                                                                           @RequestParam(required = false, defaultValue = "desc") String sort,
                                                                           @RequestParam(required = false) String sortBy,
                                                                           @RequestParam(required = false) String searchQuery) {
        LOG.debug("Get to retrieve all algorithms received.");
        // Generate Pageable
        Pageable p = RestUtils.getPageableFromRequestParams(page, size, sort, sortBy);
        // Get Page of DTOs
        Page<AlgorithmDto> pageDto = ModelMapperUtils.convertPage(algorithmService.findAll(p, searchQuery), AlgorithmDto.class);
        // Generate PagedModel
        PagedModel<EntityModel<AlgorithmDto>> outputDto = algorithmPaginationAssembler.toModel(pageDto);
        algorithmAssembler.addLinks(outputDto.getContent());
        return new ResponseEntity<>(outputDto, HttpStatus.OK);
    }

    @Operation(responses = {@ApiResponse(responseCode = "201")}, description = "Define the basic properties of an algorithm. References to subobjects (e.g. a problemtype) can be added via subroutes (e.g. /algorithm/id/problem-types). Custom ID will be ignored.")
    @PostMapping()
    public HttpEntity<EntityModel<AlgorithmDto>> createAlgorithm(@Valid @RequestBody AlgorithmDto algo) {
        LOG.debug("Post to create new algorithm received.");
        // store and return algorithm
        Algorithm algorithm = algorithmService.save(ModelMapperUtils.convert(algo, Algorithm.class));
        // Convert To EntityModel
        EntityModel<AlgorithmDto> dtoOutput = HateoasUtils
                .generateEntityModel(ModelMapperUtils.convert(algorithm, AlgorithmDto.class));
        // Fill EntityModel with links
        algorithmAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.CREATED);
    }

    @Operation(responses = {@ApiResponse(responseCode = "200")}, description = "Update the basic properties of an algorithm (e.g. name). References to subobjects (e.g. a problemtype) are not updated via this operation - use the corresponding subroute for updating them (e.g. algorithm/{id}/problem-type). Custom ID will be ignored.")
    @PutMapping("/{algoId}")
    public HttpEntity<EntityModel<AlgorithmDto>> updateAlgorithm(@PathVariable UUID algoId,
                                                                 @Valid @RequestBody AlgorithmDto algo) {
        LOG.debug("Put to update algorithm with id: {}.", algoId);
        Algorithm updatedAlgorithm = algorithmService.update(algoId, ModelMapperUtils.convert(algo, Algorithm.class));
        // Convert To EntityModel
        EntityModel<AlgorithmDto> dtoOutput = HateoasUtils
                .generateEntityModel(ModelMapperUtils.convert(updatedAlgorithm, AlgorithmDto.class));
        // Fill EntityModel with links
        algorithmAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

//    @Operation(responses = {@ApiResponse(responseCode = "200")})
//    @GetMapping("/{id}/" + Constants.TAGS)
//    public HttpEntity<CollectionModel<EntityModel<TagDto>>> getTags(@PathVariable UUID id) {
//        Algorithm algorithm = algorithmService.findById(id);
//        // Get Tags of Algorithm
//        Set<Tag> tags = algorithm.getTags();
//        // Translate Entity to DTO
//        Set<TagDto> dtoTags = ModelMapperUtils.convertSet(tags, TagDto.class);
//        // Create CollectionModel
//        CollectionModel<EntityModel<TagDto>> resultCollection = HateoasUtils.generateCollectionModel(dtoTags);
//        // Fill EntityModel Links
//        tagAssembler.addLinks(resultCollection);
//        // Fill Collection-Links
//        algorithmAssembler.addTagLink(resultCollection, id);
//        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
//    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm with given id doesn't exist")},
            description = "Delete an algorithm. This also deletes all entities that depend on it (e.g., the algorithm's relation to another algorithm).")
    @DeleteMapping("/{algoId}")
    public HttpEntity<?> deleteAlgorithm(@PathVariable UUID algoId) {
        LOG.debug("Delete to remove algorithm with id: {}.", algoId);
        algorithmService.findById(algoId);
        algorithmService.delete(algoId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(responses = {@ApiResponse(responseCode = "200")}, description = "Retrieve a specific algorithm and its basic properties.")
    @GetMapping("/{algoId}")
    public HttpEntity<EntityModel<AlgorithmDto>> getAlgorithm(@PathVariable UUID algoId) {
        LOG.debug("Get to retrieve algorithm with id: {}.", algoId);

        Algorithm algorithm = algorithmService.findById(algoId);
        // Convert To EntityModel
        EntityModel<AlgorithmDto> dtoOutput = HateoasUtils
                .generateEntityModel(ModelMapperUtils.convert(algorithm, AlgorithmDto.class));
        // Fill EntityModel with links
        algorithmAssembler.addLinks(dtoOutput);

        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

    @Operation(responses = {@ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content, description = "Algorithm doesn't exist")},
            description = "Get referenced publications for an algorithm.")
    @GetMapping("/{algoId}/" + Constants.PUBLICATIONS)
    public HttpEntity<CollectionModel<EntityModel<PublicationDto>>> getPublications(@PathVariable UUID algoId) {
        Algorithm algorithm = algorithmService.findById(algoId);
        // Get Publications of Algorithm
        Set<Publication> publications = algorithm.getPublications();
        // Translate Entity to DTO
        Set<PublicationDto> dtoPublications = ModelMapperUtils.convertSet(publications, PublicationDto.class);
        // Create CollectionModel
        CollectionModel<EntityModel<PublicationDto>> resultCollection = HateoasUtils.generateCollectionModel(dtoPublications);
        // Fill EntityModel Links
        publicationAssembler.addLinks(resultCollection);
        // Fill Collection-Links
        algorithmAssembler.addPublicationLink(resultCollection, algoId);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @Operation(responses = {@ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "404", content = @Content,
            description = "algorithm or publication does not exist")},
            description = "Add a reference to an existing publication (that was previously created via a POST on /publications/). Custom ID will be ignored. For publication only ID is required, other publication attributes will not change. If the publication doesn't exist yet, a 404 error is thrown.")
    @PostMapping("/{algoId}/" + Constants.PUBLICATIONS)
    public HttpEntity<CollectionModel<EntityModel<PublicationDto>>> addPublication(@PathVariable UUID algoId, @RequestBody PublicationDto publicationDto) {
        Algorithm algorithm = algorithmService.findById(algoId);
        // access publication in db to throw NoSuchElementException if it doesn't exist
        Publication publication = publicationService.findById(publicationDto.getId());
        // Get publications of Algorithm
        Set<Publication> publications = algorithm.getPublications();
        // add new publication reference
        publications.add(publication);
        // update and return update list:
        algorithm.setPublications(publications);
        Set<Publication> updatedPublications = algorithmService.save(algorithm).getPublications();
        Set<PublicationDto> dtoPublications = ModelMapperUtils.convertSet(updatedPublications, PublicationDto.class);
        // Create CollectionModel
        CollectionModel<EntityModel<PublicationDto>> resultCollection = HateoasUtils.generateCollectionModel(dtoPublications);
        // Fill EntityModel Links
        publicationAssembler.addLinks(resultCollection);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @Operation(responses = {@ApiResponse(responseCode = "200")}, description = "Get a specific referenced publication of an algorithm.")
    @GetMapping("/{algoId}/" + Constants.PUBLICATIONS + "/{publicationId}")
    public HttpEntity<EntityModel<PublicationDto>> getPublication(@PathVariable UUID algoId, @PathVariable UUID publicationId) {
        Publication publication = publicationService.findById(publicationId);
        Set<Publication> publications = algorithmService.findById(algoId).getPublications();
        if (!publications.contains(publication)) {
            LOG.info("Trying to get Publication that is not referenced by the algorithm");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Convert To EntityModel
        EntityModel<PublicationDto> dtoOutput = HateoasUtils.generateEntityModel(ModelMapperUtils.convert(publication, PublicationDto.class));
        // Fill EntityModel with links
        publicationAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm or publication with given ids do not exist or no relation between algorithm and publication")},
            description = "Delete a reference to a publication of the algorithm.")
    @DeleteMapping("/{algoId}/" + Constants.PUBLICATIONS + "/{publicationId}")
    public HttpEntity<EntityModel<ProblemTypeDto>> deleteReferenceToPublication(@PathVariable UUID algoId, @PathVariable UUID publicationId) {
        Algorithm algorithm = algorithmService.findById(algoId);
        publicationService.findById(publicationId);
        Set<Publication> publications = algorithm.getPublications();
        if (!publications.removeIf(publication -> publication.getId().equals(publicationId))) {
            new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        algorithm.setPublications(publications);
        algorithmService.save(algorithm);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm does not exists in the database")},
            description = "Get the problem types for an algorithm.")
    @GetMapping("/{algoId}/" + Constants.PROBLEM_TYPES)
    public HttpEntity<CollectionModel<EntityModel<ProblemTypeDto>>> getProblemTypes(@PathVariable UUID algoId) {
        Algorithm algorithm = algorithmService.findById(algoId);
        // Get ProblemTypes of Algorithm
        Set<ProblemType> problemTypes = algorithm.getProblemTypes();
        // Translate Entity to DTO
        Set<ProblemTypeDto> dtoTypes = ModelMapperUtils.convertSet(problemTypes, ProblemTypeDto.class);
        // Create CollectionModel
        CollectionModel<EntityModel<ProblemTypeDto>> resultCollection = HateoasUtils.generateCollectionModel(dtoTypes);
        // Fill EntityModel Links
        problemTypeAssembler.addLinks(resultCollection);
        // Fill Collection-Links
        algorithmAssembler.addProblemTypeLink(resultCollection, algoId);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", description = "The id of the problem type to reference is null"),
            @ApiResponse(responseCode = "404", description = "Problem type or algorithm does not exists in the database")},
            description = "Add a reference to an existing problemType (that was previously created via a POST on /problem-types/). Custom ID will be ignored. For problem type only ID is required, other problem type attributes will not change. If the problemType doesn't exist yet, a 404 error is thrown.")
    @PostMapping("/{algoId}/" + Constants.PROBLEM_TYPES)
    public HttpEntity<CollectionModel<EntityModel<ProblemTypeDto>>> addProblemType(@PathVariable UUID algoId, @RequestBody ProblemTypeDto problemTypeDto) {
        if (Objects.isNull(problemTypeDto.getId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Algorithm algorithm = algorithmService.findById(algoId);
        // access stored pattern relation -> if it does not exists, this throws a NoSuchElementException
        ProblemType problemType = problemTypeService.findById(problemTypeDto.getId());
        // Get ProblemTypes of Algorithm
        Set<ProblemType> problemTypes = algorithm.getProblemTypes();
        // add new problemtype
        problemTypes.add(problemType);
        // update and return update list:
        algorithm.setProblemTypes(problemTypes);
        Set<ProblemType> updatedProblemTypes = algorithmService.save(algorithm).getProblemTypes();
        Set<ProblemTypeDto> problemTypeDtos = ModelMapperUtils.convertSet(updatedProblemTypes, ProblemTypeDto.class);
        CollectionModel<EntityModel<ProblemTypeDto>> resultCollection = HateoasUtils.generateCollectionModel(problemTypeDtos);
        // Fill EntityModel Links
        problemTypeAssembler.addLinks(resultCollection);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm or problem type does not exists in the database")},
            description = "Get a specific problem type for an algorithm.")
    @GetMapping("/{algoId}/" + Constants.PROBLEM_TYPES + "/{problemTypeId}")
    public HttpEntity<EntityModel<ProblemTypeDto>> getSpecificProblemTypes(@PathVariable UUID algoId, @PathVariable UUID problemTypeId) {
        ProblemType problemType = problemTypeService.findById(problemTypeId);
        Algorithm algorithm = algorithmService.findById(algoId);
        // Get ProblemTypes of Algorithm
        Set<ProblemType> problemTypes = algorithm.getProblemTypes();
        if (!problemTypes.contains(problemType)) {
            LOG.info("Trying to get ApplicationArea that not referenced by the algorithm");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Create CollectionModel
        EntityModel<ProblemTypeDto> dtoOutput = HateoasUtils.generateEntityModel(ModelMapperUtils.convert(problemType, ProblemTypeDto.class));
        // Fill EntityModel Links
        problemTypeAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm or problem type does not exists in the database")},
            description = "Delete a reference to a problem types of the algorithm.")
    @DeleteMapping("/{algoId}/" + Constants.PROBLEM_TYPES + "/{problemTypeId}")
    public HttpEntity<EntityModel<ProblemTypeDto>> deleteReferenceToProblemTypes(@PathVariable UUID algoId, @PathVariable UUID problemTypeId) {
        problemTypeService.findById(problemTypeId);
        Algorithm algorithm = algorithmService.findById(algoId);
        // Get ProblemTypes of Algorithm
        Set<ProblemType> problemTypes = algorithm.getProblemTypes();
        problemTypes.removeIf(problemType -> problemType.getId().equals(problemTypeId));
        algorithm.setProblemTypes(problemTypes);
        algorithmService.save(algorithm);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm does not exists in the database")},
            description = "Get the problem types for an algorithm.")
    @GetMapping("/{algoId}/" + Constants.APPLICATION_AREAS)
    public HttpEntity<CollectionModel<EntityModel<ApplicationAreaDto>>> getApplicationAreas(@PathVariable UUID algoId) {
        Algorithm algorithm = algorithmService.findById(algoId);
        // Get ProblemTypes of Algorithm
        Set<ApplicationArea> applicationAreas = algorithm.getApplicationAreas();
        // Translate Entity to DTO
        Set<ApplicationAreaDto> dtoTypes = ModelMapperUtils.convertSet(applicationAreas, ApplicationAreaDto.class);
        // Create CollectionModel
        CollectionModel<EntityModel<ApplicationAreaDto>> resultCollection = HateoasUtils.generateCollectionModel(dtoTypes);
        // Fill EntityModel Links
        applicationAreaAssembler.addLinks(resultCollection);
        // Fill Collection-Links
        algorithmAssembler.addApplicationAreaLink(resultCollection, algoId);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm or application area does not exists in the database")},
            description = "Get a specific applicationArea of an algorithm.")
    @GetMapping("/{algoId}/" + Constants.APPLICATION_AREAS + "/{applicationAreaId}")
    public HttpEntity<EntityModel<ApplicationAreaDto>> getApplicationArea(@PathVariable UUID algoId, @PathVariable UUID applicationAreaId) {
        ApplicationArea applicationArea = applicationAreaService.findById(applicationAreaId);
        Set<ApplicationArea> applicationAreas = algorithmService.findById(algoId).getApplicationAreas();
        if (!applicationAreas.contains(applicationArea)) {
            LOG.info("Trying to get ApplicationArea that not referenced by the algorithm");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Convert To EntityModel
        EntityModel<ApplicationAreaDto> dtoOutput = HateoasUtils.generateEntityModel(ModelMapperUtils.convert(applicationArea, ApplicationAreaDto.class));
        // Fill EntityModel with links
        applicationAreaAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm does not exists in the database")},
            description = "Delete a reference to a applicationArea of an algorithm.")
    @DeleteMapping("/{algoId}/" + Constants.APPLICATION_AREAS + "/{applicationAreaId}")
    public HttpEntity<EntityModel<ApplicationAreaDto>> deleteReferenceToApplicationArea(@PathVariable UUID algoId, @PathVariable UUID applicationAreaId) {
        Algorithm algorithm = algorithmService.findById(algoId);
        Set<ApplicationArea> applicationAreas = algorithm.getApplicationAreas();
        applicationAreas.removeIf(applicationArea -> applicationArea.getId().equals(applicationAreaId));
        algorithm.setApplicationAreas(applicationAreas);
        algorithmService.save(algorithm);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "404", description = "Problem type or algorithm does not exists in the database")},
            description = "Add a reference to an existing application area (that was previously created via a POST on /application-area/). For application area only ID is required, other attributes will not change. If the applicationArea doesn't exist yet, a 404 error is thrown.")
    @PostMapping("/{algoId}/" + Constants.APPLICATION_AREAS)
    public HttpEntity<CollectionModel<EntityModel<ApplicationAreaDto>>> addApplicationArea(@PathVariable UUID algoId, @RequestBody ApplicationAreaDto applicationAreaDto) {
        Algorithm algorithm = algorithmService.findById(algoId);
        // access stored pattern relation -> if it does not exists, this throws a NoSuchElementException
        ApplicationArea applicationArea = applicationAreaService.findById(applicationAreaDto.getId());
        // Get applicationAreas of Algorithm
        Set<ApplicationArea> applicationAreas = algorithm.getApplicationAreas();
        // add new applicationArea
        applicationAreas.add(applicationArea);
        // update and return update list:
        algorithm.setApplicationAreas(applicationAreas);
        Set<ApplicationArea> updatedApplicationAreas = algorithmService.save(algorithm).getApplicationAreas();
        Set<ApplicationAreaDto> dtos = ModelMapperUtils.convertSet(updatedApplicationAreas, ApplicationAreaDto.class);
        CollectionModel<EntityModel<ApplicationAreaDto>> resultCollection = HateoasUtils.generateCollectionModel(dtos);
        // Fill EntityModel Links
        applicationAreaAssembler.addLinks(resultCollection);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400"),
            @ApiResponse(responseCode = "404", description = "Algorithm or Pattern Type doesn't exist in the database")},
            description = "Get pattern relations for an algorithms.")
    @GetMapping("/{algoId}/" + Constants.PATTERN_RELATIONS)
    public HttpEntity<CollectionModel<EntityModel<PatternRelationDto>>> getPatternRelations(@PathVariable UUID algoId) {
        Algorithm algorithm = algorithmService.findById(algoId);
        // Get PatternRelations of Algorithm
        Set<PatternRelation> patternRelations = algorithm.getRelatedPatterns();
        // Translate Entity to DTO
        Set<PatternRelationDto> dtoTypes = ModelMapperUtils.convertSet(patternRelations, PatternRelationDto.class);
        // Create CollectionModel
        CollectionModel<EntityModel<PatternRelationDto>> resultCollection = HateoasUtils.generateCollectionModel(dtoTypes);
        // Fill EntityModel Links
        patternRelationAssembler.addLinks(resultCollection);
        // Fill Collection-Links
        algorithmAssembler.addPatternRelationLink(resultCollection, algoId);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "404", description = "Algorithm or pattern type doesn't exist in the database")},
            description = "Add a Pattern Relation from this Algorithm to a given Pattern. Custom ID will be ignored. For pattern relation type only ID is required, other pattern relation type attributes will not change.")
    @PostMapping("/{algoId}/" + Constants.PATTERN_RELATIONS)
    public HttpEntity<EntityModel<PatternRelationDto>> createPatternRelation(@PathVariable UUID algoId,
                                                                             @RequestBody PatternRelationDto relationDto) {
        LOG.debug("Post to create new PatternRelation received.");

        // always use current state of this algorithm/pattern type and do not overwrite when saving relations
        Algorithm algorithm = algorithmService.findById(algoId);
        PatternRelationType patternRelationType = patternRelationTypeService.findById(relationDto.getPatternRelationType().getId());
        relationDto.setAlgorithm(ModelMapperUtils.convert(algorithm, AlgorithmDto.class));
        relationDto.setPatternRelationType(ModelMapperUtils.convert(patternRelationType, PatternRelationTypeDto.class));

        PatternRelation savedRelation = patternRelationService.save(ModelMapperUtils.convert(relationDto, PatternRelation.class));

        // Convert To EntityModel and add links
        EntityModel<PatternRelationDto> dtoOutput = HateoasUtils
                .generateEntityModel(ModelMapperUtils.convert(savedRelation, PatternRelationDto.class));
        patternRelationAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.CREATED);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "PatternRelation doesn't belong to this algorithm"),
            @ApiResponse(responseCode = "404", description = "Pattern relation or algorithm with given id doesn't exist")},
            description = "Get a certain pattern relation for an algorithm.")
    @GetMapping("/{algoId}/" + Constants.PATTERN_RELATIONS + "/{relationId}")
    public HttpEntity<EntityModel<PatternRelationDto>> getPatternRelation(@PathVariable UUID algoId, @PathVariable UUID relationId) {
        LOG.debug("Get to retrieve PatternRelation with Id {} received.", relationId);
        algorithmService.findById(algoId);
        PatternRelation patternRelation = patternRelationService.findById(relationId);
        if (!patternRelation.getAlgorithm().getId().equals(algoId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        PatternRelationDto dto = ModelMapperUtils.convert(patternRelation, PatternRelationDto.class);
        EntityModel<PatternRelationDto> dtoOutput = HateoasUtils
                .generateEntityModel(ModelMapperUtils.convert(dto, PatternRelationDto.class));
        patternRelationAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "PatternRelation doesn't belong to this algorithm"),
            @ApiResponse(responseCode = "404", description = "Pattern relation or algorithm with given id doesn't exist")},
            description = "Update a references to a pattern. Custom ID will be ignored. For pattern relation type only ID is required, other pattern relation type attributes will not change.")
    @PutMapping("/{algoId}/" + Constants.PATTERN_RELATIONS + "/{relationId}")
    public HttpEntity<EntityModel<PatternRelationDto>> updatePatternRelations(@PathVariable UUID algoId, @PathVariable UUID relationId, @Valid @RequestBody PatternRelationDto relationDto) {
        LOG.debug("Put to update pattern relation with Id {} received.", relationId);
        PatternRelation patternRelation = patternRelationService.findById(relationId);
        if (!patternRelation.getAlgorithm().getId().equals(algoId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // always use current state of this algorithm/pattern type and do not overwrite when saving relations
        Algorithm algorithm = algorithmService.findById(algoId);
        PatternRelationType patternRelationType = patternRelationTypeService.findById(relationDto.getPatternRelationType().getId());
        relationDto.setAlgorithm(ModelMapperUtils.convert(algorithm, AlgorithmDto.class));
        relationDto.setPatternRelationType(ModelMapperUtils.convert(patternRelationType, PatternRelationTypeDto.class));

        PatternRelation savedRelation = patternRelationService.save(ModelMapperUtils.convert(relationDto, PatternRelation.class));

        EntityModel<PatternRelationDto> dtoOutput = HateoasUtils
                .generateEntityModel(ModelMapperUtils.convert(savedRelation, PatternRelationDto.class));
        patternRelationAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Pattern relation or algorithm with given id doesn't exist")})
    @DeleteMapping("/{algoId}/" + Constants.PATTERN_RELATIONS + "/{relationId}")
    public HttpEntity<PatternRelationDto> deletePatternRelation(@PathVariable UUID algoId,
                                                                @PathVariable UUID relationId) {
        LOG.debug("Delete received to remove pattern relation with id {}.", relationId);
        algorithmService.findById(algoId);
        patternRelationService.findById(relationId);
        patternRelationService.deleteById(relationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "AlgorithmRelation doesn't contain this algorithm as source or target"),
            @ApiResponse(responseCode = "404", description = "Algorithm with given id doesn't exist")},
            description = "Add an algorithm relation from this algorithm to another given algorithm. Custom ID will be ignored. For algorithm relation type only ID is required, other algorithm relation type attributes will not change.")
    @PostMapping("/{algoId}/" + Constants.ALGORITHM_RELATIONS)
    public ResponseEntity<EntityModel<AlgorithmRelationDto>> addAlgorithmRelation(
            @PathVariable UUID algoId,
            @RequestBody AlgorithmRelationDto relationDto
    ) {
        LOG.debug("Post to create algorithm relations received.");
        algorithmService.findById(algoId);
        if (!relationDto.getSourceAlgorithm().getId().equals(algoId) && !relationDto.getTargetAlgorithm().getId().equals(algoId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        AlgorithmRelation updatedRelation = handleRelationUpdate(relationDto, null);
        EntityModel<AlgorithmRelationDto> updatedRelationDto = HateoasUtils.generateEntityModel(getAlgoRealtionDto(updatedRelation));
        algorithmRelationAssembler.addLinks(updatedRelationDto);
        return new ResponseEntity<>(updatedRelationDto, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm with the given id doesn't exist")},
            description = "Retrieve all relations for an algorithm.")
    @GetMapping("/{algoId}/" + Constants.ALGORITHM_RELATIONS)
    public HttpEntity<CollectionModel<EntityModel<AlgorithmRelationDto>>> getAlgorithmRelations(
            @PathVariable UUID algoId) {
        algorithmService.findById(algoId);
        Set<AlgorithmRelation> algorithmRelations = algorithmService.getAlgorithmRelations(algoId);
        // Get AlgorithmRelationDTOs of Algorithm
        Set<AlgorithmRelationDto> dtoAlgorithmRelation = new HashSet<>();
        for (AlgorithmRelation algorithmRelation : algorithmRelations) {
            dtoAlgorithmRelation.add(getAlgoRealtionDto(algorithmRelation));
        }
        CollectionModel<EntityModel<AlgorithmRelationDto>> resultCollection = HateoasUtils
                .generateCollectionModel(dtoAlgorithmRelation);
        // Fill EntityModel Links
        algorithmRelationAssembler.addLinks(resultCollection);
        // Fill Collection-Links
        algorithmAssembler.addAlgorithmRelationLink(resultCollection, algoId);
        return new ResponseEntity<>(resultCollection, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "AlgorithmRelation doesn't belong to this algorithm"),
            @ApiResponse(responseCode = "404", description = "Algorithm with the given id doesn't exist")})
    @GetMapping("/{algoId}/" + Constants.ALGORITHM_RELATIONS + "/{relationId}")
    public HttpEntity<EntityModel<AlgorithmRelationDto>> getAlgorithmRelation(
            @PathVariable UUID algoId, @PathVariable UUID relationId) {
        LOG.debug("Retrieving algorithm relation with id {} for algorithm with id {}", relationId, algoId);
        algorithmService.findById(algoId);
        AlgorithmRelation algorithmRelation = algoRelationService.findById(relationId);
        if (!algorithmRelation.getSourceAlgorithm().getId().equals(algoId) && !algorithmRelation.getTargetAlgorithm().getId().equals(algoId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        EntityModel<AlgorithmRelationDto> dtoOutput = HateoasUtils.generateEntityModel(getAlgoRealtionDto(algorithmRelation));
        algorithmRelationAssembler.addLinks(dtoOutput);
        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "AlgorithmRelation doesn't contain this algorithm as source or target"),
            @ApiResponse(responseCode = "404", description = "Algorithm with the given id doesn't exist")},
            description = "Change an algorithm relation from this algorithm to another given algorithm. Custom ID will be ignored. For algorithm relation type only ID is required, other algorithm relation type attributes will not change.")
    @PutMapping("/{algoId}/" + Constants.ALGORITHM_RELATIONS + "/{relationId}")
    public HttpEntity<EntityModel<AlgorithmRelationDto>> updateAlgorithmRelation(@PathVariable UUID algoId, @PathVariable UUID relationId,
                                                                                 @Valid @RequestBody AlgorithmRelationDto relationDto) {
        LOG.debug("Put to update algorithm relations with Id {} received.", relationId);

        // check if relation exists and if it uses this algorithm as source or target
        algorithmService.findById(algoId);
        algoRelationService.findById(relationId);
        if (!relationDto.getSourceAlgorithm().getId().equals(algoId) && !relationDto.getTargetAlgorithm().getId().equals(algoId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        AlgorithmRelation updatedRelation = handleRelationUpdate(relationDto, relationId);
        EntityModel<AlgorithmRelationDto> updatedRelationDto = HateoasUtils.generateEntityModel(getAlgoRealtionDto(updatedRelation));
        algorithmRelationAssembler.addLinks(updatedRelationDto);
        return new ResponseEntity<>(updatedRelationDto, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Algorithm relation or algorithm with given id doesn't exist")},
            description = "Delete a relation of the algorithm.")
    @DeleteMapping("/{algoId}/" + Constants.ALGORITHM_RELATIONS + "/{relationId}")
    public HttpEntity<AlgorithmRelationDto> deleteAlgorithmRelation(@PathVariable UUID algoId,
                                                                    @PathVariable UUID relationId) {
        LOG.debug("Delete received to remove algorithm relation with id {}.", relationId);
        algorithmService.findById(algoId);
        algoRelationService.findById(relationId);
        algoRelationService.delete(relationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Transform an AlgorithmRelation to a AlgorithmRelationDto and separately transform the source and target
     * algorithms to their Dto representation. By directly using the ModelMapperUtils for the transformation the quantum
     * specfific attributes of an algorithm are lost at the moment.
     * <p>
     * FIXME: move back to usgae of ModelMapperUtils
     */
    private AlgorithmRelationDto getAlgoRealtionDto(AlgorithmRelation algorithmRelation) {
        AlgorithmRelationDto relationDto = ModelMapperUtils.convert(algorithmRelation, AlgorithmRelationDto.class);
        relationDto.setSourceAlgorithm(ModelMapperUtils.convert(algorithmRelation.getSourceAlgorithm(), AlgorithmDto.class));
        relationDto.setTargetAlgorithm(ModelMapperUtils.convert(algorithmRelation.getTargetAlgorithm(), AlgorithmDto.class));
        return relationDto;
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400"),
            @ApiResponse(responseCode = "404")
    }, description = "Retrieve the required computing resources of an algorithm")
    @GetMapping("/{algoId}/" + Constants.COMPUTING_RESOURCES_PROPERTIES)
    public ResponseEntity<PagedModel<EntityModel<ComputingResourcePropertyDto>>> getComputingResources(
            @PathVariable UUID algoId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        algorithmService.findById(algoId);
        var resources = computingResourcePropertyService.findAllComputingResourcesPropertyByAlgorithmId(algoId, RestUtils.getPageableFromRequestParams(page, size));
        var typeDtoes = ModelMapperUtils.convertPage(resources, ComputingResourcePropertyDto.class);
        var pagedModel = computingResourcePaginationAssembler.toModel(typeDtoes);
        computingResourcePropertyAssembler.addLinks(pagedModel);
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "Id of the passed computing resource type is null"),
            @ApiResponse(responseCode = "404", description = "Computing resource type  or algorithm can not be found with the given Ids")
    }, description = "Add a computing resource (e.g. a certain number of qubits) that is required by an algorithm. Custom ID will be ignored. For computing resource type only ID is required, other computing resource type attributes will not change.")
    @PostMapping("/{algoId}/" + Constants.COMPUTING_RESOURCES_PROPERTIES)
    public ResponseEntity<EntityModel<ComputingResourcePropertyDto>> addComputingResource(
            @PathVariable UUID algoId,
            @Valid @RequestBody ComputingResourcePropertyDto resourceDto
    ) {
        var algorithm = algorithmService.findById(algoId);

        if (Objects.isNull(resourceDto.getType().getId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ComputingResourcePropertyType type = computingResourcePropertyService.findComputingResourcePropertyTypeById(resourceDto.getType().getId());
        resourceDto.setType(ModelMapperUtils.convert(type, ComputingResourcePropertyTypeDto.class));
        ComputingResourceProperty updatedComputeResource = computingResourcePropertyService.addComputingResourcePropertyToAlgorithm(
                algorithm,
                ModelMapperUtils.convert(resourceDto, ComputingResourceProperty.class)
        );
        EntityModel<ComputingResourcePropertyDto> dto = HateoasUtils.generateEntityModel(
                ModelMapperUtils.convert(updatedComputeResource, ComputingResourcePropertyDto.class));
        return ResponseEntity.ok(dto);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "Resource doesn't belong to this algorithm"),
            @ApiResponse(responseCode = "404", description = "Algorithm with the given id doesn't exist")})
    @GetMapping("/{algoId}/" + Constants.COMPUTING_RESOURCES_PROPERTIES + "/{resourceId}")
    public HttpEntity<EntityModel<ComputingResourcePropertyDto>> getComputingResource(
            @PathVariable UUID algoId, @PathVariable UUID resourceId) {
        LOG.debug("Get received to retrieve computing resource with id {}.", resourceId);

        algorithmService.findById(algoId);
        ComputingResourceProperty computingResourceProperty = computingResourcePropertyService.findComputingResourcePropertyById(resourceId);
        if (Objects.isNull(computingResourceProperty.getAlgorithm()) || !computingResourceProperty.getAlgorithm().getId().equals(algoId)) {
            LOG.debug("Algorithm is not referenced from the computing resource to retrieve!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        EntityModel<ComputingResourcePropertyDto> dtoOutput = HateoasUtils.generateEntityModel(ModelMapperUtils.convert(computingResourceProperty, ComputingResourcePropertyDto.class));
        return new ResponseEntity<>(dtoOutput, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400"),
            @ApiResponse(responseCode = "404", description = "Algorithm with the given id doesn't exist")},
            description = "Update a computing resource of the algorithm. Custom ID will be ignored. For computing resource type only ID is required, other computing resource type attributes will not change.")
    @PutMapping("/{algoId}/" + Constants.COMPUTING_RESOURCES_PROPERTIES + "/{resourceId}")
    public HttpEntity<EntityModel<ComputingResourcePropertyDto>> updateComputingResource(@PathVariable UUID algoId,
                                                                                         @PathVariable UUID resourceId, @RequestBody ComputingResourcePropertyDto resourceDto) {
        LOG.debug("Put received to update computing resource with id {}.", resourceId);
        ComputingResourceProperty computingResourceProperty = computingResourcePropertyService.findComputingResourcePropertyById(resourceId);
        Algorithm algorithm = algorithmService.findById(algoId);
        if (Objects.isNull(computingResourceProperty.getAlgorithm()) || !computingResourceProperty.getAlgorithm().getId().equals(algoId)) {
            LOG.debug("Algorithm is not referenced from the computing resource to update!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ComputingResourcePropertyType type = computingResourcePropertyService.findComputingResourcePropertyTypeById(resourceDto.getType().getId());
        resourceDto.setType(ModelMapperUtils.convert(type, ComputingResourcePropertyTypeDto.class));
        resourceDto.setId(resourceId);
        ComputingResourceProperty updatedComputeResource = computingResourcePropertyService.addComputingResourcePropertyToAlgorithm(
                algorithm,
                ModelMapperUtils.convert(resourceDto, ComputingResourceProperty.class)
        );
        EntityModel<ComputingResourcePropertyDto> dto = HateoasUtils.generateEntityModel(
                ModelMapperUtils.convert(updatedComputeResource, ComputingResourcePropertyDto.class));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "Computing resource with the given id doesn't belong to this algorithm"),
            @ApiResponse(responseCode = "404", description = "Algorithm or computing resource with given id doesn't exist")}, description = "Delete a computing resource of the algorithm.")
    @DeleteMapping("/{algoId}/" + Constants.COMPUTING_RESOURCES_PROPERTIES + "/{resourceId}")
    public HttpEntity<ComputingResourcePropertyDto> deleteComputingResource(@PathVariable UUID algoId,
                                                                            @PathVariable UUID resourceId) {
        LOG.debug("Delete received to remove computing resource with id {}.", resourceId);
        algorithmService.findById(algoId);
        ComputingResourceProperty computingResourceProperty = computingResourcePropertyService.findComputingResourcePropertyById(resourceId);
        if (Objects.isNull(computingResourceProperty.getAlgorithm()) || !computingResourceProperty.getAlgorithm().getId().equals(algoId)) {
            LOG.debug("Algorithm is not referenced from the computing resource to delete!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        computingResourcePropertyService.deleteComputingResourceProperty(resourceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private AlgorithmRelation handleRelationUpdate(AlgorithmRelationDto relationDto, UUID relationId) {
        AlgorithmRelation resource = new AlgorithmRelation();
        if (Objects.nonNull(relationId)) {
            resource.setId(relationId);
        }
        resource.setAlgoRelationType(algoRelationTypeService.findById(relationDto.getAlgoRelationType().getId()));
        resource.setSourceAlgorithm(algorithmService.findById(relationDto.getSourceAlgorithm().getId()));
        resource.setTargetAlgorithm(algorithmService.findById(relationDto.getTargetAlgorithm().getId()));
        resource.setDescription(relationDto.getDescription());
        return algoRelationService.save(resource);
    }
}
