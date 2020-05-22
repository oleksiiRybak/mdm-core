/*
 * Copyright 2020 University of Oxford
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.core.container

import uk.ac.ox.softeng.maurodatamapper.core.container.Classifier
import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItemService
import uk.ac.ox.softeng.maurodatamapper.core.model.ContainerService
import uk.ac.ox.softeng.maurodatamapper.security.User
import uk.ac.ox.softeng.maurodatamapper.security.UserSecurityPolicyManager
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Slf4j
class ClassifierService implements ContainerService<Classifier> {

    @Autowired(required = false)
    List<CatalogueItemService> catalogueItemServices

    SessionFactory sessionFactory

    @Override
    boolean handles(Class clazz) {
        clazz == Classifier
    }

    @Override
    boolean handles(String domainType) {
        domainType == Classifier.simpleName
    }

    @Override
    boolean isContainerVirtual() {
        true
    }

    @Override
    String getContainerPropertyNameInModel() {
        'classifiers'
    }

    @Override
    List<Classifier> getAll(Collection<UUID> containerIds) {
        Classifier.getAll(containerIds)
    }

    Classifier get(Serializable id) {
        Classifier.get(id)
    }

    List<Classifier> list(Map pagination = [:]) {
        Classifier.list(pagination)
    }

    @Override
    List<Classifier> findAllContainersInside(UUID containerId) {
        Classifier.findAllContainedInClassifierId(containerId)
    }

    @Override
    List<Classifier> findAllReadableContainersBySearchTerm(UserSecurityPolicyManager userSecurityPolicyManager, String searchTerm) {
        log.debug('Searching readable classifiers for search term in label')
        List<UUID> readableIds = userSecurityPolicyManager.listReadableSecuredResourceIds(Classifier)
        Classifier.luceneTreeLabelSearch(readableIds.collect {it.toString()}, searchTerm)
    }

    Long count() {
        Classifier.count()
    }

    Classifier save(Classifier classifier) {
        classifier.save()
    }

    def saveAll(Collection<Classifier> classifiers) {

        Collection<Classifier> alreadySaved = classifiers.findAll {it.ident() && it.isDirty()}
        Collection<Classifier> notSaved = classifiers.findAll {!it.ident()}

        if (alreadySaved) {
            log.debug('Straight saving {} classifiers', alreadySaved.size())
            Classifier.saveAll(alreadySaved)
        }

        if (notSaved) {
            log.debug('Batch saving {} classifiers', notSaved.size())
            List batch = []
            int count = 0

            notSaved.each {de ->

                batch += de
                count++
                if (count % Classifier.BATCH_SIZE == 0) {
                    batchSave(batch)
                    batch.clear()
                }
            }
            batchSave(batch)
            batch.clear()
        }
    }

    void batchSave(List<Classifier> classifiers) {
        long start = System.currentTimeMillis()
        log.trace('Batch saving {} classifiers', classifiers.size())

        Classifier.saveAll(classifiers)

        sessionFactory.currentSession.flush()
        sessionFactory.currentSession.clear()

        log.trace('Batch save took {}', Utils.getTimeString(System.currentTimeMillis() - start))
    }

    void delete(Serializable id) {
        delete(get(id))
    }

    void delete(Classifier classifier, boolean root = true) {
        if (!classifier) {
            log.warn('Attempted to delete Classifier which doesnt exist')
            return
        }
        cleanoutClassifier(classifier)
        if (root) classifier.delete(flush: true)
    }

    Classifier findOrCreateByLabel(String label) {
        Classifier.findOrCreateByLabel(label)
    }

    Classifier findOrCreateByLabel(String label, User createdBy) {
        Classifier classifier = Classifier.findByLabel(label)
        if (classifier) return classifier
        new Classifier(label: label, createdBy: createdBy).addToEdits(createdBy: createdBy, description: "Classifier ${label} created")
    }

    Set<Classifier> findOrCreateAllByLabels(Collection<String> labels, User catalogueUser) {
        labels.collect {
            findOrCreateByLabel(it, catalogueUser)
        } as Set
    }

    Classifier findOrCreateClassifier(User catalogueUser, Classifier classifier) {
        Classifier exists = Classifier.findByLabel(classifier.label)
        if (exists) return exists
        classifier.createdBy = catalogueUser
        classifier
    }

    /**
     * Find all resources by the defined user security policy manager. If none provided then assume no security policy in place in which case
     * everything is public.
     * @param userSecurityPolicyManager
     * @param pagination
     * @return
     */
    List<Classifier> findAllByUser(UserSecurityPolicyManager userSecurityPolicyManager, Map pagination = [:]) {
        List<UUID> ids = userSecurityPolicyManager.listReadableSecuredResourceIds(Classifier)
        ids ? Classifier.findAllByIdInList(ids, pagination) : []
    }

    List<Classifier> findAllByCatalogueItemId(UserSecurityPolicyManager userSecurityPolicyManager, UUID catalogueItemId, Map pagination = [:]) {
        CatalogueItem catalogueItem

        for (CatalogueItemService service : catalogueItemServices) {
            if (catalogueItem) break
            catalogueItem = service.findByIdJoinClassifiers(catalogueItemId)
        }

        if (!catalogueItem || !catalogueItem.classifiers) return []

        // Filter out all the classifiers which the user can't read
        Collection<Classifier> allClassifiersInItem = catalogueItem.classifiers
        List<UUID> readableIds = userSecurityPolicyManager.listReadableSecuredResourceIds(Classifier)
        allClassifiersInItem.findAll {it.id in readableIds}.toList()
    }

    Classifier editInformation(Classifier classifier, String label, String description) {
        classifier.label = label
        classifier.description = description
        classifier.validate()
        classifier
    }

    def <C extends CatalogueItem> Classifier addClassifierToCatalogueItem(Class<C> catalogueItemClass, UUID catalogueItemId, Classifier classifier) {
        CatalogueItemService service = catalogueItemServices.find {it.handles(catalogueItemClass)}
        service.addClassifierToCatalogueItem(catalogueItemId, classifier)
        classifier
    }

    def <C extends CatalogueItem> void removeClassifierFromCatalogueItem(Class<C> catalogueItemClass, UUID catalogueItemId, Classifier classifier) {
        CatalogueItemService service = catalogueItemServices.find {it.handles(catalogueItemClass)}
        service.removeClassifierFromCatalogueItem(catalogueItemId, classifier)
        classifier
    }

    void checkClassifiers(User catalogueUser, def classifiedItem) {
        Set<Classifier> classifiers = [] as HashSet
        classifiers.addAll(classifiedItem.classifiers ?: [])

        classifiedItem.classifiers?.clear()

        classifiers.each {
            classifiedItem.addToClassifiers(findOrCreateClassifier(catalogueUser, it as Classifier))
        }
    }

    List<CatalogueItem> findAllReadableCatalogueItemsByClassifierId(UserSecurityPolicyManager userSecurityPolicyManager,
                                                                    UUID classifierId, Map pagination = [:]) {
        Classifier classifier = get(classifierId)
        catalogueItemServices.collect {service ->
            service.findAllReadableByClassifier(userSecurityPolicyManager, classifier)
        }.findAll().flatten()
    }

    private void cleanoutClassifier(Classifier classifier) {
        classifier.childClassifiers.each {cleanoutClassifier(it)}
        catalogueItemServices.each {it.removeAllFromClassifier(classifier)}
    }
}