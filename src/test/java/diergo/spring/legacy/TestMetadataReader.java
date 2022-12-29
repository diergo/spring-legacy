package diergo.spring.legacy;

import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.lang.Nullable;

public class TestMetadataReader implements MetadataReader {

    private final AnnotationMetadata metadata;

    public TestMetadataReader(Class<?> type) {
        this.metadata = AnnotationMetadata.introspect(type);
    }

    @Override
    @Nullable
    public Resource getResource() {
        return null;
    }

    @Override
    public ClassMetadata getClassMetadata() {
        return metadata;
    }

    @Override
    public AnnotationMetadata getAnnotationMetadata() {
        return metadata;
    }
}
