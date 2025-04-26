package cn.cidea.framework.strategy.core.filter;

import cn.cidea.framework.strategy.core.annotation.StrategyMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * @author: CIdea
 */
public class StrategyAPITypeFilter implements TypeFilter {

    private Logger log = LoggerFactory.getLogger(StrategyAPITypeFilter.class);

    private final Class<? extends Annotation> annotationType;

    public StrategyAPITypeFilter(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        // 有StrategyAPI的
        boolean api = hasAnnotation(metadataReader.getAnnotationMetadata(), annotationType);
        if (api) {
            return true;
        }
        // 有StrategyMaster且上面没有StrategyAPI的
        boolean master = hasAnnotation(metadataReader.getAnnotationMetadata(), StrategyMaster.class);
        if (master) {
            ClassMetadata metadata = metadataReader.getClassMetadata();
            if (hasAnnotation(metadata.getSuperClassName(), annotationType)) {
                return false;
            }
            for (String interfaceName : metadata.getInterfaceNames()) {
                if (hasAnnotation(interfaceName, annotationType)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean hasAnnotation(AnnotationMetadata metadata, Class<? extends Annotation> annotationType) {
        return metadata.hasAnnotation(annotationType.getName()) || metadata.hasMetaAnnotation(annotationType.getName());
    }

    private boolean hasAnnotation(String typeName, Class<? extends Annotation> annotationType) {
        if (Object.class.getName().equals(typeName)) {
            return false;
        }
        if (typeName.startsWith("java")) {
            return false;
        }
        if (annotationType.getName().startsWith("java")) {
            return false;
        }
        try {
            Class<?> clazz = ClassUtils.forName(typeName, getClass().getClassLoader());
            return AnnotationUtils.findAnnotation(clazz, annotationType) != null;
        } catch (Throwable ex) {
            // Class not regularly loadable - can't determine a match that way.
            log.error("classForName error", ex);
        }
        return false;
    }

}
