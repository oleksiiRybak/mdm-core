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
package uk.ac.ox.softeng.maurodatamapper.core.container.provider.importer

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiBadRequestException
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.container.provider.importer.parameter.FolderFileImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.core.traits.provider.importer.JsonImportMapping
import uk.ac.ox.softeng.maurodatamapper.security.User

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class FolderJsonImporterService extends DataBindFolderImporterProviderService<FolderFileImporterProviderServiceParameters> implements JsonImportMapping {

    @Override
    String getDisplayName() {
        'JSON Folder Importer'
    }

    @Override
    String getVersion() {
        '1.0'
    }

    @Override
    Boolean canImportMultipleDomains() {
        false
    }

    @Override
    Folder importFolder(User currentUser, byte[] content) {
        null
    }

    @Override
    List<Folder> importFolders(User currentUser, byte[] content) {
        throw new ApiBadRequestException('FBIP04', "${name} cannot import multiple Folders")
    }
}
