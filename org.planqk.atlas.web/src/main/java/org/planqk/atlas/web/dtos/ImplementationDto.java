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

package org.planqk.atlas.web.dtos;

import java.net.URL;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.planqk.atlas.web.utils.Identifyable;
import org.planqk.atlas.web.utils.ValidationGroups;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

/**
 * Data transfer object for the model class Implementation ({@link org.planqk.atlas.core.model.Implementation}).
 */
@EqualsAndHashCode
@Data
@NoArgsConstructor
@Relation(itemRelation = "implementation", collectionRelation = "implementations")
public class ImplementationDto implements Identifyable {

    @NotNull(groups = {ValidationGroups.Update.class}, message = "An id is required to perform an update")
    @Null(groups = {ValidationGroups.Create.class}, message = "The id must be null for creating an implementation")
    private UUID id;

    private UUID implementedAlgorithmId;

    @NotNull(groups = {ValidationGroups.Update.class, ValidationGroups.Create.class},
            message = "Implementation-Name must not be null!")
    private String name;
    @Schema(description = "URL of implementation", example = "http://www.github.com/planqk", required = false)
    private URL link;

    private String inputFormat;
    private String outputFormat;
    private String description;
    private String contributors;
    private String assumptions;
    private String parameter;
    private String dependencies;
}
