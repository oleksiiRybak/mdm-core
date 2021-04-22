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
package uk.ac.ox.softeng.maurodatamapper.core.rest.transport.model


import grails.validation.Validateable

/**
 * @since 07/02/2018
 */
class MergeFieldDiffData<T, K> implements Validateable {

    String fieldName
    T value
    Collection<MergeItemData> created
    Collection<MergeItemData> deleted
    Collection<MergeObjectDiffData> modified

    MergeFieldDiffData() {
        created = []
        deleted = []
        modified = []
    }

    boolean hasDiffs() {
        value || !created.isEmpty() || !deleted.isEmpty() || modified.any {it.hasDiffs()}
    }


    boolean isFieldChange() {
        value
    }

    boolean isMetadataChange() {
        fieldName == 'metadata'
    }

    String getSummary() {
        String prefix = "Merge Summary on field [${fieldName}]"
        if (isFieldChange()) return "${prefix}: Changing value"
        "${prefix}: Creating ${created.size()} Deleting ${deleted.size()} Modifying ${modified.size()}"
    }

    String toString() {
        "Merge on field [${fieldName}]"
    }
}
