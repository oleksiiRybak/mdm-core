/*
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.profile

import uk.ac.ox.softeng.maurodatamapper.core.authority.Authority
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.bootstrap.BootstrapModels
import uk.ac.ox.softeng.maurodatamapper.test.functional.BaseFunctionalSpec

import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import grails.testing.spock.RunOnce
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Tag
import spock.lang.Shared

import static uk.ac.ox.softeng.maurodatamapper.core.bootstrap.StandardEmailAddress.FUNCTIONAL_TEST

import static io.micronaut.http.HttpStatus.OK

/**
 * @since 12/04/2022
 */
@Tag('non-parallel')
@Integration
@Slf4j
class SearchFunctionalSpec extends BaseFunctionalSpec {

    @Shared
    Folder folder

    @Shared
    UUID complexDataModelId

    @Shared
    UUID simpleDataModelId

    ProfileSpecificationFieldProfileService profileSpecificationFieldProfileService

    @Transactional
    Authority getTestAuthority() {
        Authority.findByDefaultAuthority(true)
    }

    @RunOnce
    @Transactional
    def setup() {
        log.debug('Check and setup test data')
        folder = new Folder(label: 'Functional Test Folder', createdBy: FUNCTIONAL_TEST)
        checkAndSave(folder)
        folder.addToMetadata(new Metadata(namespace: 'test.namespace', key: 'propertyKey', value: 'propertyValue', createdBy: FUNCTIONAL_TEST))
        checkAndSave(folder)

        DataModel dataModel = BootstrapModels.buildAndSaveComplexDataModel(messageSource, folder, testAuthority)
        complexDataModelId = dataModel.id

        dataModel.allDataElements.sort().eachWithIndex {de, i ->
            profileSpecificationFieldProfileService.storeFieldInEntity(de, "value $i", 'metadataPropertyName', FUNCTIONAL_TEST)
            if (de.label == 'ele1') profileSpecificationFieldProfileService.storeFieldInEntity(de, "value type $i", 'metadataPropertyName', FUNCTIONAL_TEST)
            de.save()
        }
        sessionFactory.currentSession.flush()
    }

    @Transactional
    def cleanupSpec() {
        log.debug('CleanupSpec DataFlowFunctionalSpec')
        cleanUpResources(DataModel, Folder)
    }

    @Override
    String getResourcePath() {
        ''
    }

    void 'S01 : test searching using profile filter for single result'() {
        when:
        POST("dataModels/${complexDataModelId}/search", [
            searchTerm   : "child",
            domainTypes  : ["DataElement"],
            labelOnly    : true,
            profileFields: [
                [
                    metadataNamespace   : profileSpecificationFieldProfileService.metadataNamespace,
                    metadataPropertyName: 'metadataPropertyName',
                    filterTerm          : 'value'
                ],
            ]
        ])

        then:
        verifyResponse(OK, response)
        responseBody().count == 1
        responseBody().items.first().label == 'child'

        when:
        POST("dataModels/${complexDataModelId}/search", [
            searchTerm   : "child",
            domainTypes  : ["DataElement"],
            labelOnly    : true,
            profileFields: [
                [
                    metadataNamespace   : profileSpecificationFieldProfileService.metadataNamespace,
                    metadataPropertyName: 'metadataPropertyName',
                    filterTerm          : 'blob'
                ],
            ]
        ])

        then:
        verifyResponse(OK, response)
        responseBody().count == 0
    }

    void 'S02 : test searching using profile filter for multiple results'() {
        when:
        POST("dataModels/${complexDataModelId}/search", [
            searchTerm   : "ele*",
            domainTypes  : ["DataElement"],
            labelOnly    : true,
            profileFields: [
                [
                    metadataNamespace   : profileSpecificationFieldProfileService.metadataNamespace,
                    metadataPropertyName: 'metadataPropertyName',
                    filterTerm          : 'value'
                ],
            ]
        ])

        then:
        verifyResponse(OK, response)
        responseBody().count == 2
        responseBody().items.any {it.label == 'ele1'}
        responseBody().items.any {it.label == 'element2'}

        when:
        POST("dataModels/${complexDataModelId}/search", [
            searchTerm   : "ele*",
            domainTypes  : ["DataElement"],
            labelOnly    : true,
            profileFields: [
                [
                    metadataNamespace   : profileSpecificationFieldProfileService.metadataNamespace,
                    metadataPropertyName: 'metadataPropertyName',
                    filterTerm          : 'value type'
                ],
            ]
        ])

        then:
        verifyResponse(OK, response)
        responseBody().count == 1
        responseBody().items.any {it.label == 'ele1'}
    }

    void 'S03 : test searching to include profile fields in results'() {
        when:
        POST("dataModels/${complexDataModelId}/profiles/${profileSpecificationFieldProfileService.namespace}/${profileSpecificationFieldProfileService.name}/search", [
            searchTerm   : "ele*",
            domainTypes  : ["DataElement"],
            labelOnly    : true,
            profileFields: [
                [
                    metadataNamespace   : profileSpecificationFieldProfileService.metadataNamespace,
                    metadataPropertyName: 'metadataPropertyName',
                    filterTerm          : 'value'
                ],
            ]
        ], STRING_ARG)

        then:
        verifyJsonResponse(OK, '''{
  "count": 2,
  "items": [
    {
      "id": "${json-unit.matches:id}",
      "domainType": "DataElement",
      "label": "ele1",
      "model": "${json-unit.matches:id}",
      "breadcrumbs": [
        {
          "id": "${json-unit.matches:id}",
          "label": "Complex Test DataModel",
          "domainType": "DataModel",
          "finalised": false
        },
        {
          "id": "${json-unit.matches:id}",
          "label": "content",
          "domainType": "DataClass"
        }
      ],
      "profileFields": [
        {
          "fieldName": "Metadata Property Name",
          "metadataPropertyName": "metadataPropertyName",
          "currentValue": "value type 1",
          "dataType": "string"
        },
        {
          "fieldName": "Default Value",
          "metadataPropertyName": "defaultValue",
          "currentValue": "",
          "dataType": "string"
        },
        {
          "fieldName": "Regular expression",
          "metadataPropertyName": "regularExpression",
          "currentValue": "",
          "dataType": "string"
        },
        {
          "fieldName": "May be edited after finalisation",
          "metadataPropertyName": "editableAfterFinalisation",
          "currentValue": "true",
          "dataType": "boolean"
        }
      ]
    },
    {
      "id": "${json-unit.matches:id}",
      "domainType": "DataElement",
      "label": "element2",
      "model": "${json-unit.matches:id}",
      "breadcrumbs": [
        {
          "id": "${json-unit.matches:id}",
          "label": "Complex Test DataModel",
          "domainType": "DataModel",
          "finalised": false
        },
        {
          "id": "${json-unit.matches:id}",
          "label": "content",
          "domainType": "DataClass"
        }
      ],
      "profileFields": [
        {
          "fieldName": "Metadata Property Name",
          "metadataPropertyName": "metadataPropertyName",
          "currentValue": "value 2",
          "dataType": "string"
        },
        {
          "fieldName": "Default Value",
          "metadataPropertyName": "defaultValue",
          "currentValue": "",
          "dataType": "string"
        },
        {
          "fieldName": "Regular expression",
          "metadataPropertyName": "regularExpression",
          "currentValue": "",
          "dataType": "string"
        },
        {
          "fieldName": "May be edited after finalisation",
          "metadataPropertyName": "editableAfterFinalisation",
          "currentValue": "true",
          "dataType": "boolean"
        }
      ]
    }
  ]
}''')

        when:
        POST("dataModels/${complexDataModelId}/profiles/${profileSpecificationFieldProfileService.namespace}/${profileSpecificationFieldProfileService.name}/search", [
            searchTerm   : "ele*",
            domainTypes  : ["DataElement"],
            labelOnly    : true,
            profileFields: [
                [
                    metadataNamespace   : profileSpecificationFieldProfileService.metadataNamespace,
                    metadataPropertyName: 'metadataPropertyName',
                    filterTerm          : 'value type'
                ],
            ]
        ], STRING_ARG)

        then:
        verifyJsonResponse(OK, '''{
  "count": 1,
  "items": [
    {
      "id": "${json-unit.matches:id}",
      "domainType": "DataElement",
      "label": "ele1",
      "model": "${json-unit.matches:id}",
      "breadcrumbs": [
        {
          "id": "${json-unit.matches:id}",
          "label": "Complex Test DataModel",
          "domainType": "DataModel",
          "finalised": false
        },
        {
          "id": "${json-unit.matches:id}",
          "label": "content",
          "domainType": "DataClass"
        }
      ],
      "profileFields": [
        {
          "fieldName": "Metadata Property Name",
          "metadataPropertyName": "metadataPropertyName",
          "dataType": "string",
          "currentValue": "value type 1"
        },
        {
          "fieldName": "Default Value",
          "metadataPropertyName": "defaultValue",
          "dataType": "string",
          "currentValue": ""
        },
        {
          "fieldName": "Regular expression",
          "metadataPropertyName": "regularExpression",
          "dataType": "string",
          "currentValue": ""
        },
        {
          "fieldName": "May be edited after finalisation",
          "metadataPropertyName": "editableAfterFinalisation",
          "dataType": "boolean",
          "currentValue": "true"
        }
      ]
    }
  ]
}''')

    }
}
