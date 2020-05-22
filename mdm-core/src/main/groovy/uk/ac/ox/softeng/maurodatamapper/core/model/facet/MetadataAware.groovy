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
package uk.ac.ox.softeng.maurodatamapper.core.model.facet

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.security.User

import groovy.transform.CompileStatic
import groovy.transform.SelfType

/**
 * @since 30/01/2020
 */
@SelfType(CatalogueItem)
@CompileStatic
trait MetadataAware {

    abstract Set<Metadata> getMetadata()

    Set<Metadata> findMetadataByNamespace(String namespace) {
        metadata?.findAll {it.namespace == namespace} ?: [] as HashSet
    }

    Metadata findMetadataByNamespaceAndKey(String namespace, String key) {
        metadata?.find {it.namespace == namespace && it.key == key}
    }

    CatalogueItem addToMetadata(Metadata add) {
        Metadata existing = findMetadataByNamespaceAndKey(add.namespace, add.key)
        if (existing) {
            existing.value = add.value
            markDirty('metadata', existing)
            this as CatalogueItem
        } else {
            add.setCatalogueItem(this as CatalogueItem)
            addTo('metadata', add)
        }
    }

    CatalogueItem addToMetadata(Map args) {
        addToMetadata(new Metadata(args))
    }

    CatalogueItem addToMetadata(String namespace, String key, String value, User createdBy) {
        addToMetadata(namespace, key, value, createdBy.emailAddress)
    }

    CatalogueItem addToMetadata(String namespace, String key, String value, String createdBy) {
        addToMetadata(new Metadata(namespace: namespace, key: key, value: value, createdBy: createdBy))
    }

    CatalogueItem addToMetadata(String namespace, String key, String value) {
        addToMetadata(namespace: namespace, key: key, value: value)
    }

    CatalogueItem removeFromMetadata(Metadata metadata) {
        removeFrom('metadata', metadata)
    }
}