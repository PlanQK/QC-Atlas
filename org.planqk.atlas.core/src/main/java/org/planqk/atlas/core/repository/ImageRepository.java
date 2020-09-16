package org.planqk.atlas.core.repository;

import java.util.UUID;

import org.planqk.atlas.core.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository to access {@link Image}s.
 */
@RepositoryRestResource(exported = false)
public interface ImageRepository extends JpaRepository<Image, UUID> {

    Image findImageBySketchId(UUID sketchId);
}