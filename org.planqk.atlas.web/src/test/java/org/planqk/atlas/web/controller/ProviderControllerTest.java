/********************************************************************************
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.planqk.atlas.core.model.Provider;
import org.planqk.atlas.core.services.ProviderService;
import org.planqk.atlas.web.Constants;
import org.planqk.atlas.web.controller.util.ObjectMapperUtils;
import org.planqk.atlas.web.dtos.ProviderDto;
import org.planqk.atlas.web.linkassembler.ProviderAssembler;
import org.planqk.atlas.web.utils.HateoasUtils;
import org.planqk.atlas.web.utils.ModelMapperUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
public class ProviderControllerTest {

    @MockBean
    private ProviderService providerService;
    @MockBean
    private PagedResourcesAssembler<ProviderDto> paginationAssembler;
    @MockBean
    private ProviderAssembler providerAssembler;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper;

    private final int page = 0;
    private final int size = 2;
    private final Pageable pageable = PageRequest.of(page, size);

    @BeforeEach
    public void initialize() {
        this.mapper = ObjectMapperUtils.newTestMapper();
    }

    @Test
    public void getProviders_withoutPagination() throws Exception {
        when(providerService.findAll(Pageable.unpaged())).thenReturn(Page.empty());
        when(paginationAssembler.toModel(ArgumentMatchers.any()))
                .thenReturn(HateoasUtils.generatePagedModel(Page.empty()));
        doNothing().when(providerAssembler).addLinks(ArgumentMatchers.<Collection<EntityModel<ProviderDto>>>any());

        mockMvc.perform(get("/" + Constants.PROVIDERS + "/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getProviders_withEmptyProviderList() throws Exception {
        when(providerService.findAll(pageable)).thenReturn(Page.empty());
        when(paginationAssembler.toModel(ArgumentMatchers.any()))
                .thenReturn(HateoasUtils.generatePagedModel(Page.empty()));
        doNothing().when(providerAssembler).addLinks(ArgumentMatchers.<Collection<EntityModel<ProviderDto>>>any());

        MvcResult result = mockMvc
                .perform(get("/" + Constants.PROVIDERS + "/").queryParam(Constants.PAGE, Integer.toString(page))
                        .queryParam(Constants.SIZE, Integer.toString(size)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        var resultList = ObjectMapperUtils.mapResponseToList(
                result.getResponse().getContentAsString(),
                "providerDtoes",
                ProviderDto.class
        );
        assertEquals(0, resultList.size());
    }

    @Test
    public void getProviders_withOneProvider() throws Exception {
        List<Provider> providerList = new ArrayList<>();

        UUID provId = UUID.randomUUID();

        Provider provider = new Provider();
        provider.setId(provId);
        providerList.add(provider);

        Page<Provider> pageEntity = new PageImpl<Provider>(providerList, pageable, providerList.size());
        Page<ProviderDto> pageDto = ModelMapperUtils.convertPage(pageEntity, ProviderDto.class);

        when(providerService.findAll(pageable)).thenReturn(pageEntity);
        when(paginationAssembler.toModel(ArgumentMatchers.any()))
                .thenReturn(HateoasUtils.generatePagedModel(pageDto));
        doNothing().when(providerAssembler).addLinks(ArgumentMatchers.<Collection<EntityModel<ProviderDto>>>any());

        MvcResult result = mockMvc
                .perform(get("/" + Constants.PROVIDERS + "/").queryParam(Constants.PAGE, Integer.toString(page))
                        .queryParam(Constants.SIZE, Integer.toString(size)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        var resultList = ObjectMapperUtils.mapResponseToList(
                result.getResponse().getContentAsString(),
                "providerDtoes",
                ProviderDto.class
        );

        assertEquals(1, resultList.size());
        assertEquals(provId, resultList.get(0).getId());
    }

    @Test
    public void getProvider_returnNotFound() throws Exception {
        when(providerService.findById(any(UUID.class))).thenThrow(new NoSuchElementException());
        mockMvc.perform(get("/" + Constants.PROVIDERS + "/" + UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getProvider_returnProvider() throws Exception {
        UUID provId = UUID.randomUUID();
        Provider provider = new Provider();
        provider.setId(provId);

        when(providerService.findById(provId)).thenReturn(provider);
        doNothing().when(providerAssembler).addLinks(ArgumentMatchers.<EntityModel<ProviderDto>>any());

        MvcResult result = mockMvc
                .perform(get("/" + Constants.PROVIDERS + "/" + provId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        EntityModel<ProviderDto> response = mapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<ProviderDto>>() {
                });
        assertEquals(response.getContent().getId(), provId);
    }

    @Test
    public void createProvider_returnBadRequest() throws Exception {
        ProviderDto providerDto = new ProviderDto();
        providerDto.setName("IBM");

        mockMvc.perform(
                post("/" + Constants.PROVIDERS + "/").content(mapper.writeValueAsString(providerDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createProvider_returnProvider() throws Exception {
        ProviderDto providerDto = new ProviderDto();
        providerDto.setName("IBM");
        providerDto.setAccessKey("123");
        providerDto.setSecretKey("456");
        Provider provider = ModelMapperUtils.convert(providerDto, Provider.class);

        when(providerService.save(provider)).thenReturn(provider);
        doNothing().when(providerAssembler).addLinks(ArgumentMatchers.<EntityModel<ProviderDto>>any());

        MvcResult result = mockMvc
                .perform(post("/" + Constants.PROVIDERS + "/")
                        .content(mapper.writeValueAsString(providerDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();

        EntityModel<ProviderDto> response = mapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<ProviderDto>>() {
                });
        assertEquals(response.getContent().getName(), providerDto.getName());
    }
}
