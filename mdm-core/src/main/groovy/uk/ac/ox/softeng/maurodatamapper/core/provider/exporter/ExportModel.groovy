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
package uk.ac.ox.softeng.maurodatamapper.core.provider.exporter

import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem

import groovy.xml.Namespace

class ExportModel {

    Map<String, Object> modelExportMap
    String exportModelType
    String modelExportTemplatePath
    ExportMetadata exportMetadata
    Namespace xmlNamespace
    Namespace modelXmlNamespace

    ExportModel(CatalogueItem model, String modelType, String version, ExportMetadata exportMetadata) {
        this(model, modelType, version, version, '', exportMetadata)
    }

    ExportModel(List<CatalogueItem> models, String modelType, String multiModelType, String version, ExportMetadata exportMetadata) {
        this(models, modelType, multiModelType, version, version, '', exportMetadata)
    }

    ExportModel(CatalogueItem model, String modelType, String version, String templatePathFileExtension, ExportMetadata exportMetadata) {
        this(model, modelType, version, version, templatePathFileExtension, exportMetadata)
    }

    ExportModel(CatalogueItem model, String modelType, String version, String modelVersion, String templatePathFileExtension, ExportMetadata exportMetadata) {
        modelExportMap = [export: model]
        exportModelType = modelType
        modelExportTemplatePath = "/${modelType}/export${templatePathFileExtension ? ".$templatePathFileExtension" : ''}"
        this.exportMetadata = exportMetadata
        xmlNamespace = new Namespace("http://maurodatamapper.com/export/${version}", 'xmlns:exp')
        modelXmlNamespace = new Namespace("http://maurodatamapper.com/${modelType}/${modelVersion}", 'xmlns:mdm')
    }

    ExportModel(List<CatalogueItem> models, String modelType, String multiModelType, String version, String modelVersion, String templatePathFileExtension,
                ExportMetadata exportMetadata) {
        modelExportMap = [export: models]
        exportModelType = multiModelType
        modelExportTemplatePath = "/${modelType}/export${templatePathFileExtension ? ".$templatePathFileExtension" : ''}"
        this.exportMetadata = exportMetadata
        xmlNamespace = new Namespace("http://maurodatamapper.com/export/${version}", 'xmlns:exp')
        modelXmlNamespace = new Namespace("http://maurodatamapper.com/${modelType}/${modelVersion}", 'xmlns:mdm')
    }

    Map<String, String> getXmlNamespaces() {
        [
            (xmlNamespace.prefix)     : xmlNamespace.uri,
            (modelXmlNamespace.prefix): modelXmlNamespace.uri
        ]
    }
}
