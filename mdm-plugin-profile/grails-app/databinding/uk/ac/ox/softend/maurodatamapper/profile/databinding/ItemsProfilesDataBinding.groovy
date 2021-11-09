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
package uk.ac.ox.softend.maurodatamapper.profile.databinding

/**
 * Bind to a request body like
 * {
 *     "multiFacetAwareItems": [
 *         {
 *             "multiFacetAwareItemDomainType": "dataElement",
 *             "multiFacetAwareItemId": "97757635-d1da-4c03-86d8-ba90efbd5670"
 *         },
 *         {
 *             "multiFacetAwareItemDomainType": "dataElement",
 *             "multiFacetAwareItemId": "0a8c9022-c5d9-4d0f-b426-86d70aee5c74"
 *         }
 *     ],
 *     "profileProviderServices": [
 *         {
 *             "name": "ExampleProfileProvider",
 *             "namespace": "uk.ac.ox.softeng.maurodatamapper.profile.provider"
 *         }
 *     ]
 * }
 */
class ItemsProfilesDataBinding {
    List<MultiFacetAwareItemDataBinding> multiFacetAwareItems
    List<ProfileProviderServiceDataBinding> profileProviderServices
}
