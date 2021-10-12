/*
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.datamodel.provider.exporter

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiInternalException
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.DataModelXmlImporterService
import uk.ac.ox.softeng.maurodatamapper.datamodel.test.provider.DataBindImportAndDefaultExporterServiceSpec
import uk.ac.ox.softeng.maurodatamapper.test.xml.XmlValidator

import com.google.common.base.CaseFormat
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import org.junit.Assert
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

import static org.junit.Assert.assertTrue

/**
 * @since 18/11/2017
 */
@Integration
@Rollback
@Slf4j
class DataModelXmlExporterServiceSpec extends DataBindImportAndDefaultExporterServiceSpec<DataModelXmlImporterService, DataModelXmlExporterService>
    implements XmlValidator {

    private static final String NO_DATAMODEL_IDS_TO_EXPORT_CODE = 'DMEP01'
    private static final String SIMPLE_DATAMODEL_FILENAME = 'simpleDataModel'
    private static final String SIMPLE_AND_COMPLEX_DATAMODELS_FILENAME = 'simpleAndComplexDataModels'

    DataModelXmlImporterService dataModelXmlImporterService
    DataModelXmlExporterService dataModelXmlExporterService

    @Override
    DataModelXmlImporterService getImporterService() {
        dataModelXmlImporterService
    }

    @Override
    DataModelXmlExporterService getExporterService() {
        dataModelXmlExporterService
    }

    @Override
    String getImportType() {
        'xml'
    }

    @Override
    void validateExportedModel(String testName, String exportedModel) {
        assert exportedModel, 'There must be an exported model string'

        Path expectedPath = resourcesPath.resolve("${CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, testName)}.xml")
        if (!Files.exists(expectedPath)) {
            Files.writeString(expectedPath, (prettyPrint(exportedModel)))
            Assert.fail("Expected export file ${expectedPath} does not exist")
        }
        validateAndCompareXml(Files.readString(expectedPath), exportedModel, 'export', exporterService.version)
    }

    @Unroll
    void 'test "#testName" xml files are valid'() {
        given:
        setupData()

        when:
        Path xmlPath = resourcesPath.resolve("${CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, testName)}.xml")
        if (!Files.exists(xmlPath)) {
            Assert.fail("Expected export file ${xmlPath} does not exist")
        }

        def xmlIsValid = validateXml('export', dataModelXmlExporterService.version, Files.readString(xmlPath))

        then:
        assertTrue failureReason, xmlIsValid

        where:
        testName << [
            'Simple',
            'IncAliases',
            'IncMetadata',
            'IncAnnotation',
            'IncSinglePrimitiveType',
            'IncSinglePrimitiveTypeAndMetadata',
            'IncSinglePrimitiveTypeAndAnnotation',
            'IncSingleEnumerationType',
            'IncSingleEnumerationTypeAndMetadata',
            'IncEmptyDataClass',
            'IncEmptyDataClassAndMetadata',
            'IncEmptyDataClassAndAnnotation',
            'IncDataClassWithChild',
            'IncDataClassWithDataElement',
            'IncDataClassWithChildAndSingleReferenceDataType',
            'SimpleDataModel',
            'ComplexDataModel',
            'SimpleAndComplexDataModels'
        ]
    }

    void 'test multi-export invalid DataModels'() {
        expect:
        exporterService.canExportMultipleDomains()

        when: 'given null'
        exportModels(null)

        then:
        ApiInternalException exception = thrown(ApiInternalException)
        exception.errorCode == NO_DATAMODEL_IDS_TO_EXPORT_CODE

        when: 'given an empty list'
        exportModels([])

        then:
        exception = thrown(ApiInternalException)
        exception.errorCode == NO_DATAMODEL_IDS_TO_EXPORT_CODE

        when: 'given a null model'
        String exported = exportModels([null])

        then:
        exception = thrown(ApiInternalException)
        exception.errorCode == NO_DATAMODEL_IDS_TO_EXPORT_CODE

        when: 'given a single invalid model'
        exported = exportModels([UUID.randomUUID()])

        then:
        exception = thrown(ApiInternalException)
        exception.errorCode == NO_DATAMODEL_IDS_TO_EXPORT_CODE

        when: 'given multiple invalid models'
        exported = exportModels([UUID.randomUUID(), UUID.randomUUID()])

        then:
        exception = thrown(ApiInternalException)
        exception.errorCode == NO_DATAMODEL_IDS_TO_EXPORT_CODE
    }

    void 'test multi-export single DataModel'() {
        given:
        setupData()
        DataModel.count() == 2

        expect:
        exporterService.canExportMultipleDomains()

        when:
        String exported = exportModels([simpleDataModelId])

        then:
        validateExportedModels(SIMPLE_DATAMODEL_FILENAME, replaceWithTestAuthority(exported))
    }

    void 'test multi-export multiple DataModels'() {
        given:
        setupData()
        DataModel.count() == 2

        expect:
        exporterService.canExportMultipleDomains()

        when:
        String exported = exportModels([simpleDataModelId, complexDataModelId])

        then:
        validateExportedModels(SIMPLE_AND_COMPLEX_DATAMODELS_FILENAME, replaceWithTestAuthority(exported))
    }

    void 'test multi-export DataModels with invalid models'() {
        given:
        setupData()
        DataModel.count() == 2

        expect:
        exporterService.canExportMultipleDomains()

        when:
        String exported = exportModels([UUID.randomUUID(), simpleDataModelId])

        then:
        validateExportedModels(SIMPLE_DATAMODEL_FILENAME, replaceWithTestAuthority(exported))

        when:
        exported = exportModels([UUID.randomUUID(), simpleDataModelId, UUID.randomUUID(), complexDataModelId])

        then:
        validateExportedModels(SIMPLE_AND_COMPLEX_DATAMODELS_FILENAME, replaceWithTestAuthority(exported))
    }

    void 'test multi-export DataModels with duplicates'() {
        given:
        setupData()
        DataModel.count() == 2

        expect:
        exporterService.canExportMultipleDomains()

        when:
        String exported = exportModels([simpleDataModelId, simpleDataModelId])

        then:
        validateExportedModels(SIMPLE_DATAMODEL_FILENAME, replaceWithTestAuthority(exported))

        when:
        exported = exportModels([simpleDataModelId, complexDataModelId, complexDataModelId, simpleDataModelId])

        then:
        validateExportedModels(SIMPLE_AND_COMPLEX_DATAMODELS_FILENAME, replaceWithTestAuthority(exported))
    }
}
