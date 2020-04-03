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

package org.planqk.atlas.core.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

/**
 * Entity representing an implementation of a certain quantum {@link Algorithm}.
 */
@Entity
public class Implementation extends AlgorOrImpl {

    @Getter
    @Setter
    private ProgrammingLanguage programmingLanguage;

    @Getter
    @Setter
    private String selectionRule;

    @Getter
    @Setter
    private URL fileLocation;

    @Getter
    @Setter
    @ManyToOne
    private Algorithm implementedAlgorithm;

    @Getter
    @Setter
    @ManyToOne
    private Sdk sdk;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "implementation_tag",
            joinColumns = @JoinColumn(name = "implementation_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Setter
    private List<Tag> tags;

    public Implementation() {
        super();
    }

    @NonNull
    public List<Tag> getTags() {
        if (Objects.isNull(tags)) {
            return new ArrayList<>();
        }
        return tags;
    }
}