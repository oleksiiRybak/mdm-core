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
package uk.ac.ox.softeng.maurodatamapper.core.container.provider.exporter

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiBadRequestException
import uk.ac.ox.softeng.maurodatamapper.core.bootstrap.StandardEmailAddress
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.container.test.provider.BaseFolderExporterServiceSpec
import uk.ac.ox.softeng.maurodatamapper.test.json.JsonComparer

import com.google.common.base.CaseFormat
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Assert

import java.nio.file.Files
import java.nio.file.Path

@Integration
@Rollback
class FolderJsonExporterServiceSpec extends BaseFolderExporterServiceSpec<FolderJsonExporterService> implements JsonComparer {

    FolderJsonExporterService folderJsonExporterService

    @Override
    FolderJsonExporterService getExporterService() {
        folderJsonExporterService
    }

    @Override
    String getImportType() {
        'json'
    }

    @Override
    Object setup() {
        setupData()
    }

    Object cleanup() {
        folderService.delete(folderService.get(folderId), true)
    }

    @Override
    void validateExportedFolder(String testName, String exportedFolder) {
        assert exportedFolder, 'There must be an exported Folder string'

        Path expectedPath = resourcesPath.resolve("${CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, testName)}.${importType}")
        if (!Files.exists(expectedPath)) {
            Files.write(expectedPath, exportedFolder.bytes)
            Assert.fail("Expected export file ${expectedPath} does not exist")
        }

        String expectedJson = replaceContentWithMatchers(Files.readString(expectedPath))
        verifyJson(expectedJson, exportedFolder.replace(/Mauro Data Mapper/, 'Test Authority'))
    }

    void 'test export null Folder'() {
        when:
        exportFolder(null)

        then:
        ApiBadRequestException exception = thrown(ApiBadRequestException)
        exception.errorCode == NO_FOLDER_IDS_TO_EXPORT_CODE
    }

    void 'test export empty Folder'() {
        expect:
        validateExportedFolder('emptyFolder', exportFolder(folderId))
    }

    void 'test export Folder with description'() {
        when:
        folderService.get(folderId).tap {
            description = 'Test Folder description'
            checkAndSave(it)
        }

        then:
        validateExportedFolder('folderIncDescription', exportFolder(folderId))
    }

    void 'test export Folder with metadata'() {
        when:
        Folder folder = folderService.get(folderId)
        List<Map<String, String>> metadata = [
            [namespace: 'test.com', key: 'mdk1', value: 'mdv1'],
            [namespace: 'test.com/simple', key: 'mdk1', value: 'mdv1'],
            [namespace: 'test.com/simple', key: 'mdk2', value: 'mdv2']
        ]
        metadata.each {
            folder.addToMetadata(*: it, createdBy: StandardEmailAddress.INTEGRATION_TEST)
        }
        checkAndSave(folder)

        then:
        validateExportedFolder('folderIncMetadata', exportFolder(folderId))
    }

    void 'test export Folder with annotations'() {
        when:
        Folder folder = folderService.get(folderId)
        List<Map<String, String>> annotations = [
            [label: 'Test Annotation 1', description: 'Test Annotation 1 description'],
            [label: 'Test Annotation 2', description: 'Test Annotation 2 description']
        ]
        annotations.each {
            folder.addToAnnotations(*: it, createdBy: StandardEmailAddress.INTEGRATION_TEST)
        }
        checkAndSave(folder)

        then:
        validateExportedFolder('folderIncAnnotations', exportFolder(folderId))
    }

    void 'test export Folder with rules'() {
        when:
        Folder folder = folderService.get(folderId)
        List<Map<String, String>> rules = [
            [name: 'Test Rule 1', description: 'Test Rule 1 description'],
            [name: 'Test Rule 2', description: 'Test Rule 2 description']
        ]
        rules.each {
            folder.addToRules(*: it, createdBy: StandardEmailAddress.INTEGRATION_TEST)
        }
        checkAndSave(folder)

        then:
        validateExportedFolder('folderIncRules', exportFolder(folderId))
    }
}
