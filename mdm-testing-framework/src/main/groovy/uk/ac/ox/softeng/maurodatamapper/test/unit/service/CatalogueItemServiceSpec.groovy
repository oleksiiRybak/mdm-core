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
package uk.ac.ox.softeng.maurodatamapper.test.unit.service

import uk.ac.ox.softeng.maurodatamapper.core.container.Classifier
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.facet.Annotation
import uk.ac.ox.softeng.maurodatamapper.core.facet.BreadcrumbTree
import uk.ac.ox.softeng.maurodatamapper.core.facet.Edit
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.facet.ReferenceFile
import uk.ac.ox.softeng.maurodatamapper.core.facet.SemanticLink
import uk.ac.ox.softeng.maurodatamapper.test.unit.BaseUnitSpec

import groovy.util.logging.Slf4j
import org.grails.datastore.gorm.GormEntity

/**
 * @since 03/04/2020
 */
@Slf4j
class CatalogueItemServiceSpec extends BaseUnitSpec {

    def setup() {
        log.debug('Setting up CatalogueItemServiceSpec unit')
        mockDomains(Classifier, Folder, Annotation, Edit, Metadata, ReferenceFile, SemanticLink, BreadcrumbTree)
        checkAndSave(new Folder(label: 'catalogue', createdBy: admin.emailAddress))
    }

    Folder getTestFolder() {
        Folder.findByLabel('catalogue')
    }

    @Override
    void checkAndSave(GormEntity domainObj) {
        super.checkAndSave(domainObj)
        // Second check and save to make sure breadcrumbs/facets are setup correctly
        super.checkAndSave(domainObj)
        currentSession.flush()
    }

    void verifyBreadcrumbTrees() {
        List<BreadcrumbTree> trees = BreadcrumbTree.list()
        trees.findAll {it.isDirty() && it.validate()}.each {it.save()}
        currentSession.flush()
        assert BreadcrumbTree.countByDomainIdIsNull() == 0
    }
}
