/*
 *  /*******************************************************************************
 *  * Copyright (c) 2020 University of Stuttgart
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License. You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License
 *  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  * or implied. See the License for the specific language governing permissions and limitations under
 *  * the License.
 *  ******************************************************************************
 */

package org.planqk.quality.api.controller;

import java.util.Objects;
import java.util.Optional;

import org.planqk.quality.api.Constants;
import org.planqk.quality.api.dtos.entities.SdkDto;
import org.planqk.quality.api.dtos.entities.SdkListDto;
import org.planqk.quality.model.Sdk;
import org.planqk.quality.repository.SdkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller to access and manipulate software development kits (SDKs).
 */
@RestController
@RequestMapping("/" + Constants.SDKS)
public class SdkController {

    final private static Logger LOG = LoggerFactory.getLogger(SdkController.class);

    private final SdkRepository sdkRepository;

    public SdkController(SdkRepository sdkRepository) {
        this.sdkRepository = sdkRepository;
    }

    @GetMapping("/")
    public HttpEntity<SdkListDto> getSdks() {
        LOG.debug("Get to retrieve all SDKs received.");
        SdkListDto sdkListDto = new SdkListDto();

        // add all available Sdks to the response
        for (Sdk sdk : sdkRepository.findAll()) {
            sdkListDto.add(createSdkDto(sdk));
            sdkListDto.add(linkTo(methodOn(SdkController.class).getSdk(sdk.getId())).withRel(sdk.getName()));
        }

        sdkListDto.add(linkTo(methodOn(SdkController.class).getSdks()).withSelfRel());
        return new ResponseEntity<>(sdkListDto, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public HttpEntity<SdkDto> getSdk(@PathVariable Long id) {
        LOG.debug("Get to retrieve SDK with id: {}.", id);

        Optional<Sdk> sdkOptional = sdkRepository.findById(id);
        if (!sdkOptional.isPresent()) {
            LOG.error("Unable to retrieve Sdk with id {} from the repository.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(createSdkDto(sdkOptional.get()), HttpStatus.OK);
    }

    @PostMapping("/")
    public HttpEntity<SdkDto> createImplementation(@RequestBody SdkDto sdkDto) {
        LOG.debug("Post to create new Sdk received.");

        if (Objects.isNull(sdkDto.getName())) {
            LOG.error("Received invalid sdk object for post request: {}", sdkDto.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // store and return implementation
        Sdk sdk = sdkRepository.save(SdkDto.Converter.convert(sdkDto));
        return new ResponseEntity<>(createSdkDto(sdk), HttpStatus.OK);
    }

    /**
     * Create a DTO object for a given {@link Sdk}.
     *
     * @param sdk the {@link Sdk} to create the DTO for
     * @return the created DTO
     */
    private SdkDto createSdkDto(Sdk sdk) {
        SdkDto sdkDto = SdkDto.Converter.convert(sdk);
        sdkDto.add(linkTo(methodOn(SdkController.class).getSdk(sdk.getId())).withSelfRel());
        return sdkDto;
    }
}