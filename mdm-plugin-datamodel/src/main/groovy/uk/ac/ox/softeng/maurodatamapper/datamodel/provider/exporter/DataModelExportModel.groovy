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
package uk.ac.ox.softeng.maurodatamapper.datamodel.provider.exporter

import uk.ac.ox.softeng.maurodatamapper.core.provider.exporter.ExportMetadata
import uk.ac.ox.softeng.maurodatamapper.core.provider.exporter.ExportModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel

import groovy.xml.Namespace

/**
 * @since 01/04/2020
 */
class DataModelExportModel extends ExportModel {

    public static String getCurrentVersion(boolean isXml) {
        isXml ? new DataModelXmlExporterService().version : new DataModelJsonExporterService().version
    }

    DataModelExportModel(DataModel dataModel, ExportMetadata exportMetadata, boolean isXml) {
        super(getCurrentVersion(isXml))
        exportModelType = 'dataModel'
        modelExportTemplatePath = isXml ? '/dataModel/export.gml' : '/dataModel/export'
        modelExportMap = [export: dataModel, dataModel: dataModel]
        this.exportMetadata = exportMetadata
        modelXmlNamespace = new Namespace("http://maurodatamapper.com/dataModel/${getCurrentVersion(isXml)}", 'xmlns:mdm')
    }
}
