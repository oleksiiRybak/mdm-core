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
package uk.ac.ox.softeng.maurodatamapper.profile.rest.transport

/**
 * Bind to a request body like
 * {
 *   "count": 2,
 *   "profilesProvided": [
 *       {
 *           "profile": {
 *               "sections": [
 *                   etc...
 *               ],
 *               "id": "97757635-d1da-4c03-86d8-ba90efbd5670",
 *               "label": "ElementInChild",
 *               "domainType": "DataElement"
 *            },
 *            "profileProviderService": {
 *                "name": "Service1",
 *                "namespace": "uk.ac.ox.softeng.maurodatamapper.profile.provider",
 *                "version": "main"
 *            }
 *       },
 *       {
 *           "profile": {
 *               "sections": [
 *                   etc...
 *               ],
 *               "id": "97757635-d1da-4c03-86d8-ba90efbd5670",
 *               "label": "ElementInChild",
 *               "domainType": "DataElement"
 *            },
 *            "profileProviderService": {
 *                "name": "Service1",
 *                "namespace": "uk.ac.ox.softeng.maurodatamapper.profile.provider",
 *                "version": "main"
 *            }
 *       }
 *    ]
 * }
 *
 * profile is initially bound to a Map. This Map can later be bound to an instance returned by
 * the ProfileProviderService specified.
 */
class ProfileProvidedCollection implements grails.validation.Validateable {
    List<ProfileProvidedDataBinding> profilesProvided
}
