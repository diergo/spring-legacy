package diergo.spring.legacy;

import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;

public class TestMetadataReader implements MetadataReader {

    private final AnnotationMetadata metadata;

    public TestMetadataReader(Class<?> type) {
        this.metadata = new StandardAnnotationMetadata(type);
    }

    @Override
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
