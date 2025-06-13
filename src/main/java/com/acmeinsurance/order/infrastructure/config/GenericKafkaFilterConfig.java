package com.acmeinsurance.order.infrastructure.config;

import ch.qos.logback.core.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.ClassUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;


@Configuration
@RequiredArgsConstructor
public class GenericKafkaFilterConfig implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(GenericKafkaFilterConfig.class);

    private static final String AVRO_BASE_PACKAGE = "com.acmeinsurance.order.avro";

    private static final String EVENT_TYPE_HEADER = "__TypeId__";

    private final Set<String> allowedEventTypes = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        final String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                ClassUtils.convertClassNameToResourcePath(AVRO_BASE_PACKAGE) + "/**/*.class";

        final Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                String className = metadataReader.getClassMetadata().getClassName();

                try {

                    final Class<?> clazz = Class.forName(className);

                    if (className.startsWith(AVRO_BASE_PACKAGE + ".")) {
                        allowedEventTypes.add(className);
                    }

                } catch (ClassNotFoundException e) {
                    log.warn("Não foi possível carregar a classe {} durante a varredura: {}", className, e.getMessage());
                }
            }
        }

        if (allowedEventTypes.isEmpty()) {
            log.warn("Nenhuma classe Avro detectada no pacote " + AVRO_BASE_PACKAGE + ". Verifique a configuração.");
        } else {
            log.info("Classes Avro detectadas automaticamente para este microsserviço: {}", allowedEventTypes);
        }
    }

    @Bean
    public RecordFilterStrategy<String, Object> genericSchemaFilterStrategy() {
        return consumerRecord -> {

            Object recordValue = consumerRecord.value();

            Map<String, Object> headerMap = new HashMap<>();
            consumerRecord.headers().forEach(header ->
                    headerMap.put(header.key(), new String(header.value(), StandardCharsets.UTF_8)));
            MessageHeaders headers = new MessageHeaders(headerMap);

            if (ofNullable(recordValue).isEmpty()) {
                System.out.println("DEBUG FILTER: Mensagem descartada: payload nulo.");
                return true;
            }

            final String eventTypeFromHeader = headers.get(EVENT_TYPE_HEADER, String.class);

            if (StringUtil.isNullOrEmpty(eventTypeFromHeader)) {
                return true;
            }

            final boolean isAllowedType = allowedEventTypes.contains(eventTypeFromHeader);

            final boolean isActualTypeMatchingHeader = recordValue.getClass().getName().equals(eventTypeFromHeader);

            return !(isAllowedType && isActualTypeMatchingHeader);
        };
    }
}