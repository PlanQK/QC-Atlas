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

package org.planqk.atlas.core.repository;

import java.util.List;
import java.util.UUID;

import org.planqk.atlas.core.model.Algorithm;
import org.planqk.atlas.core.model.Implementation;
import org.planqk.atlas.core.model.SoftwarePlatform;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository to access {@link Implementation}s available in the data base with different queries.
 */
@RepositoryRestResource(exported = false)
public interface ImplementationRepository extends JpaRepository<Implementation, UUID> {

    boolean existsImplementationById(UUID id);

    Page<Implementation> findByImplementedAlgorithm(Algorithm implementedAlgorithm, Pageable pageable);

    @Query("SELECT sp FROM Implementation i INNER JOIN i.softwarePlatforms sp WHERE i.id = :implId")
    Page<SoftwarePlatform> findLinkedSoftwarePlatforms(@Param("implId") UUID implId, Pageable pageable);

    List<Implementation> findByImplementedAlgorithm(Algorithm implementedAlgorithm);

    @Query("SELECT i FROM Implementation i JOIN i.softwarePlatforms sp WHERE sp.id = :id")
    Page<Implementation> findImplementationsBySoftwarePlatformId(@Param("id") UUID id, Pageable p);
}
