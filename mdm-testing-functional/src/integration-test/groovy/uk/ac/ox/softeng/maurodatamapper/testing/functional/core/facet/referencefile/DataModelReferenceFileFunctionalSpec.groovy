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
package uk.ac.ox.softeng.maurodatamapper.testing.functional.core.facet.referencefile

import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.testing.functional.facet.CatalogueItemReferenceFileFunctionalSpec

import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j

/**
 * <pre>
 * Controller: referenceFile
 *  |  POST    | /api/${catalogueItemDomainType}/${catalogueItemId}/referenceFiles        | Action: save
 *  |  GET     | /api/${catalogueItemDomainType}/${catalogueItemId}/referenceFiles        | Action: index
 *  |  DELETE  | /api/${catalogueItemDomainType}/${catalogueItemId}/referenceFiles/${id}  | Action: delete
 *  |  PUT     | /api/${catalogueItemDomainType}/${catalogueItemId}/referenceFiles/${id}  | Action: update
 *  |  GET     | /api/${catalogueItemDomainType}/${catalogueItemId}/referenceFiles/${id}  | Action: show
 * </pre>
 * @see uk.ac.ox.softeng.maurodatamapper.core.facet.ReferenceFileController
 */
@Integration
@Slf4j
class DataModelReferenceFileFunctionalSpec extends CatalogueItemReferenceFileFunctionalSpec {

    @Transactional
    String getComplexDataModelId() {
        DataModel.findByLabel('Complex Test DataModel').id.toString()
    }

    @Override
    String getModelId() {
        getComplexDataModelId()
    }

    @Override
    String getCatalogueItemDomainType() {
        'dataModels'
    }

    @Override
    String getCatalogueItemId() {
        getComplexDataModelId()
    }

    @Override
    String getEditorIndexJson() {
        '''{
  "count": 0,
  "items": []
}'''
    }
}