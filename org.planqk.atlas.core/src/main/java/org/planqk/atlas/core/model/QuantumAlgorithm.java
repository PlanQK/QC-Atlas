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

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Entity
public class QuantumAlgorithm extends Algorithm {

    private boolean nisqReady;

    private QuantumComputationModel quantumComputationModel;

    private String speedUp;

//    @OneToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
//    @JoinTable(name = "algorithm_implementation",
//            joinColumns = @JoinColumn(name = "algorithm_id"),
//            inverseJoinColumns = @JoinColumn(name = "implementation_id"))
//    @EqualsAndHashCode.Exclude
//    private Set<QuantumImplementation> implementations;
}
