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
package uk.ac.ox.softeng.maurodatamapper.core.model

abstract class ModelItemService<K extends ModelItem> extends CatalogueItemService<K> {

    @Override
    Class<K> getCatalogueItemClass() {
        return getModelItemClass()
    }

    abstract Class<K> getModelItemClass()

   /**
    * After saving a new modelItem whose index is set, update the indices of its siblings.
    */
    K save(Map args, K modelItem) {
        K mi = super.save(args, modelItem)
        if (mi.idx < Integer.MAX_VALUE) {
            mi.updateIndices(mi.idx)
        }
        mi
    }
}