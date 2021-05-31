/*******************************************************************************
 * Copyright (c) 2020 the qc-atlas contributors.
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

package org.planqk.atlas.core.services;

import java.util.UUID;

import org.planqk.atlas.core.model.CloudService;
import org.planqk.atlas.core.model.ComputeResource;
import org.planqk.atlas.core.model.SoftwarePlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for operations related to interacting and modifying {@link ComputeResource}s in the database.
 */
public interface ComputeResourceService {

    /**
     * Retrieve multiple {@link ComputeResource} entries from the database where their name matches the name search
     * parameter. If there are no matches found an empty {@link Page} will be returned.
     * <p>
     * The amount of entries is based on the given {@link Pageable} parameter. If the {@link Pageable} is unpaged a
     * {@link Page} with all entries is queried.
     *
     * @param name     The string based on which a search for {@link ComputeResource}s with a matching name will be
     *                 executed
     * @param pageable The page information, namely page size and page number, of the page we want to retrieve
     * @return The page of queried {@link ComputeResource} entries which match the search name
     */
    Page<ComputeResource> searchAllByName(String name, Pageable pageable);

    /**
     * Creates a new database entry for a given {@link ComputeResource} and save it to the database.
     * <p>
     * The ID of the {@link ComputeResource} parameter should be null, since the ID will be generated by the database
     * when creating the entry. The validation for this is done by the Controller layer, which will reject {@link
     * ComputeResource}s with a given ID in its create path.
     *
     * @param computeResource The {@link ComputeResource} that should be saved to the database
     * @return The {@link ComputeResource} object that represents the saved status from the database
     */
    @Transactional
    ComputeResource create(ComputeResource computeResource);

    /**
     * Retrieve multiple {@link ComputeResource} entries from the database.
     * <p>
     * The amount of entries is based on the given {@link Pageable} parameter. If the {@link Pageable} is unpaged a
     * {@link Page} with all entries is queried.
     *
     * @param pageable The page information, namely page size and page number, of the page we want to retrieve
     * @return The page of queried {@link ComputeResource} entries
     */
    Page<ComputeResource> findAll(Pageable pageable);

    /**
     * Find a database entry of a {@link ComputeResource} that is already saved in the database. This search is based on
     * the ID the database has given the {@link ComputeResource} object when it was created and first saved to the
     * database.
     * <p>
     * If there is no entry found in the database this method will throw a {@link java.util.NoSuchElementException}.
     *
     * @param computeResourceId The ID of the {@link ComputeResource} we want to find
     * @return The {@link ComputeResource} with the given ID
     */
    ComputeResource findById(UUID computeResourceId);

    /**
     * Update an existing {@link ComputeResource} database entry by saving the updated {@link ComputeResource} object to
     * the the database.
     * <p>
     * The ID of the {@link ComputeResource} parameter has to be set to the ID of the database entry we want to update.
     * The validation for this ID to be set is done by the Controller layer, which will reject {@link ComputeResource}s
     * without a given ID in its update path. This ID will be used to query the existing {@link ComputeResource} entry
     * we want to update. If no {@link ComputeResource} entry with the given ID is found this method will throw a {@link
     * java.util.NoSuchElementException}.
     *
     * @param computeResource The {@link ComputeResource} we want to update with its updated properties
     * @return the updated {@link ComputeResource} object that represents the updated status of the database
     */
    @Transactional
    ComputeResource update(ComputeResource computeResource);

    /**
     * Delete an existing {@link ComputeResource} entry from the database. This deletion is based on the ID the database
     * has given the {@link ComputeResource} when it was created and first saved to the database.
     * <p>
     * When deleting an {@link ComputeResource} related {@link org.planqk.atlas.core.model.ComputeResourceProperty}s
     * will be deleted together with it.
     * <p>
     * Objects that can be related to multiple {@link ComputeResource}s will not be deleted. Only the reference to the
     * deleted {@link ComputeResource} will be removed from these objects. These include {@link SoftwarePlatform}s and
     * {@link CloudService}s.
     * <p>
     * If no entry with the given ID is found this method will throw a {@link java.util.NoSuchElementException}.
     * <p>
     * If the {@link ComputeResource} is still referenced by at least one {@link SoftwarePlatform} or {@link
     * CloudService} a {@link org.planqk.atlas.core.exceptions.EntityReferenceConstraintViolationException} will be
     * thrown.
     *
     * @param computeResourceId The ID of the {@link ComputeResource} we want to delete
     */
    @Transactional
    void delete(UUID computeResourceId);

    /**
     * Retrieve multiple {@link CloudService}s entries from the database of {@link CloudService}s that are linked to the
     * given {@link ComputeResource}. If no entries are found an empty page is returned.
     * <p>
     * The amount of entries is based on the given {@link Pageable} parameter. If the {@link Pageable} is unpaged a
     * {@link Page} with all entries is queried.
     * <p>
     * The given {@link ComputeResource} is identified through its ID given as a parameter. If no {@link
     * ComputeResource} with the given ID can be found a {@link java.util.NoSuchElementException} is thrown.
     *
     * @param computeResourceId The ID of the {@link ComputeResource} we want find linked {@link CloudService}s for
     * @param pageable          The page information, namely page size and page number, of the page we want to retrieve
     * @return The page of queried {@link CloudService} entries which are linked to the {@link ComputeResource}
     */
    Page<CloudService> findLinkedCloudServices(UUID computeResourceId, Pageable pageable);

    /**
     * Retrieve multiple {@link SoftwarePlatform}s entries from the database of {@link SoftwarePlatform}s that are
     * linked to the given {@link ComputeResource}. If no entries are found an empty page is returned.
     * <p>
     * The amount of entries is based on the given {@link Pageable} parameter. If the {@link Pageable} is unpaged a
     * {@link Page} with all entries is queried.
     * <p>
     * The given {@link ComputeResource} is identified through its ID given as a parameter. If no {@link
     * ComputeResource} with the given ID can be found a {@link java.util.NoSuchElementException} is thrown.
     *
     * @param computeResourceId The ID of the {@link ComputeResource} we want find linked {@link SoftwarePlatform}s for
     * @param pageable          The page information, namely page size and page number, of the page we want to retrieve
     * @return The page of queried {@link SoftwarePlatform} entries which are linked to the {@link ComputeResource}
     */
    Page<SoftwarePlatform> findLinkedSoftwarePlatforms(UUID computeResourceId, Pageable pageable);
}
