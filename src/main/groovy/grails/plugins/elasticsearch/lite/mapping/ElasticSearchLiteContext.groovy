package grails.plugins.elasticsearch.lite.mapping

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.GrailsDomainClass
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler

import javax.annotation.PostConstruct

/**
 * Created by marcoscarceles on 08/02/2017.
 */
@CompileStatic
class ElasticSearchLiteContext {

    GrailsApplication grailsApplication

    private static Map<Class<?>, ElasticSearchType> ELASTIC_TYPES
    private static Map<Class<?>, ElasticSearchMarshaller<?>> MARSHALLERS

    /**
     * A Set containing all the indices that were regenerated during migration
     */
    Set<String> indexesRebuiltOnMigration = [] as Set

    @PostConstruct
    Map<Class, ElasticSearchMarshaller<?>> init() {
        ELASTIC_TYPES = [:]
        MARSHALLERS = [:]
        for (GrailsClass grailsClazz : grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)) {
            GrailsDomainClass domainClass = (GrailsDomainClass) grailsClazz
            Class clazz = domainClass.clazz
            if(clazz.isAnnotationPresent(Mapping)) {
                Mapping mapping = clazz.getAnnotation(Mapping)
                MARSHALLERS.put(clazz, mapping.value().newInstance())
            }
            if(clazz.isAnnotationPresent(Searchable)) {
                assert MARSHALLERS.containsKey(clazz), 'All @Searchable classes require a mapping'
                Searchable searchable = clazz.getAnnotation(Searchable)
                ELASTIC_TYPES.put(clazz, new ElasticSearchType(index: searchable.index(), type: searchable.type(), marshaller: MARSHALLERS[clazz]))
            }
        }
        ELASTIC_TYPES = Collections.unmodifiableMap(ELASTIC_TYPES)
        MARSHALLERS = Collections.unmodifiableMap(MARSHALLERS)
    }

    Map<Class<?>, ElasticSearchType> getElasticSearchTypes() {
        ELASTIC_TYPES
    }

    String getType(Class clazz) {
        ELASTIC_TYPES[clazz]
    }
}
