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
package uk.ac.ox.softeng.maurodatamapper.referencedata.provider.importer

import uk.ac.ox.softeng.maurodatamapper.core.container.ClassifierService
import uk.ac.ox.softeng.maurodatamapper.core.provider.ProviderType
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.ImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.referencedata.ReferenceDataModelService
import uk.ac.ox.softeng.maurodatamapper.referencedata.ReferenceDataModel
import uk.ac.ox.softeng.maurodatamapper.referencedata.provider.importer.parameter.ReferenceDataModelImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.security.User

import org.springframework.beans.factory.annotation.Autowired

/**
 * @since 07/03/2018
 */
abstract class ReferenceDataModelImporterProviderService<T extends ReferenceDataModelImporterProviderServiceParameters>
    implements ImporterProviderService<ReferenceDataModel, T> {

    @Autowired
    ReferenceDataModelService referenceDataModelService

    @Autowired
    ClassifierService classifierService

    @Override
    ReferenceDataModel importDomain(User currentUser, T params) {
        ReferenceDataModel referenceDataModel = importReferenceDataModel(currentUser, params)
        if (!referenceDataModel) return null
        if (params.modelName) referenceDataModel.label = params.modelName
        checkImport(currentUser, referenceDataModel, params.finalised, params.importAsNewDocumentationVersion)
    }

    @Override
    List<ReferenceDataModel> importDomains(User currentUser, T params) {
        List<ReferenceDataModel> referenceDataModels = importReferenceDataModels(currentUser, params)
        referenceDataModels?.collect { checkImport(currentUser, it, params.finalised, params.importAsNewDocumentationVersion) }
    }

    abstract ReferenceDataModel importReferenceDataModel(User currentUser, T params)

    abstract List<ReferenceDataModel> importReferenceDataModels(User currentUser, T params)

    @Override
    String getProviderType() {
        "ReferenceDataModel${ProviderType.IMPORTER.name}"
    }

    private ReferenceDataModel checkImport(User currentUser, ReferenceDataModel referenceDataModel, boolean finalised, boolean importAsNewDocumentationVersion) {
        referenceDataModelService.checkfinaliseModel(referenceDataModel, finalised)
        referenceDataModelService.checkDocumentationVersion(referenceDataModel, importAsNewDocumentationVersion, currentUser)
        classifierService.checkClassifiers(currentUser, referenceDataModel)

        referenceDataModel.referenceDataElements.each { de ->
            classifierService.checkClassifiers(currentUser, de)
        }

        referenceDataModel.referenceDataTypes.each { dt ->
            classifierService.checkClassifiers(currentUser, dt)
        }
        referenceDataModel
    }
}
