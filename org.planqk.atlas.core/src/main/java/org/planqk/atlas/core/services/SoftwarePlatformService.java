package org.planqk.atlas.core.services;

import org.planqk.atlas.core.model.SoftwarePlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SoftwarePlatformService {

    SoftwarePlatform save(SoftwarePlatform softwarePlatform);

    Page<SoftwarePlatform> findAll(Pageable pageable);

    SoftwarePlatform findById(UUID platformId);

    void delete(UUID platformId);
}