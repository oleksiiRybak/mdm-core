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
package uk.ac.ox.softeng.maurodatamapper.core

class UrlMappings {

    static mappings = {
        final List<String> DEFAULT_EXCLUDES = ['patch', 'create', 'edit']
        final List<String> DEFAULT_EXCLUDES_AND_UPDATING = ['patch', 'create', 'edit', 'update']
        final List<String> INDEX_ONLY = ['index']
        final List<String> READ_ONLY_INCLUDES = ['index', 'show']

        '500'(view: '/error')
        '404'(view: '/notFound')
        '410'(view: '/gone')
        '501'(view: '/notImplemented')
        '401'(view: '/unauthorised')
        '400'(view: '/badRequest')

        group '/api', {

            group '/admin', {
                get '/modules'(controller: 'mauroDataMapperProvider', action: 'modules')
                get '/properties'(controller: 'admin', action: 'apiProperties')
                post '/rebuildLuceneIndexes'(controller: 'admin', action: 'rebuildLuceneIndexes')
                post '/editProperties'(controller: 'admin', action: 'editApiProperties')
                get '/status'(controller: 'admin', action: 'status')
                get '/activeSessions'(controller: 'session', action: 'activeSessions')
                '/emails'(resources: 'email', includes: INDEX_ONLY)

                group "/tree/$containerDomainType/$modelDomainType", {
                    get '/documentationSuperseded'(controller: 'treeItem', action: 'documentationSupersededModels') // New URL
                    get '/modelSuperseded'(controller: 'treeItem', action: 'modelSupersededModels') // New URL
                    get '/deleted'(controller: 'treeItem', action: 'deletedModels') // New URL
                }

                group '/providers', {
                    get '/importers'(controller: 'mauroDataMapperServiceProvider', action: 'importerProviders')
                    get '/dataLoaders'(controller: 'mauroDataMapperServiceProvider', action: 'dataLoaderProviders')
                    get '/emailers'(controller: 'mauroDataMapperServiceProvider', action: 'emailProviders')
                    get '/exporters'(controller: 'mauroDataMapperServiceProvider', action: 'exporterProviders')
                }
            }

            // Open access url
            get '/session/isAuthenticated'(controller: 'session', action: 'isAuthenticatedSession') // New Url

            group '/importer', {
                get "/parameters/$ns?/$name?/$version?"(controller: 'importer', action: 'parameters')
            }

            '/folders'(resources: 'folder', excludes: DEFAULT_EXCLUDES) {
                '/folders'(resources: 'folder', excludes: DEFAULT_EXCLUDES)
            }

            '/classifiers'(resources: 'classifier', excludes: DEFAULT_EXCLUDES) {
                '/classifiers'(resources: 'classifier', excludes: DEFAULT_EXCLUDES)
                '/catalogueItems'(controller: 'classifier', action: 'catalogueItems') // New URL
            }

            /*
            Catalogue Item accessible resources
             */
            group "/$catalogueItemDomainType/$catalogueItemId", {
                /*
                Classifiers
                 */
                '/classifiers'(resources: 'classifier', excludes: DEFAULT_EXCLUDES_AND_UPDATING)

                /*
                Metadata
                */
                '/metadata'(resources: 'metadata', excludes: DEFAULT_EXCLUDES)

                /*
                Annotations
                 */
                '/annotations'(resources: 'annotation', excludes: DEFAULT_EXCLUDES_AND_UPDATING) {
                    '/annotations'(resources: 'annotation', excludes: DEFAULT_EXCLUDES_AND_UPDATING)
                }

                /*
                Semantic Links
                 */
                '/semanticLinks'(resources: 'semanticLink', excludes: DEFAULT_EXCLUDES)

                /*
                Reference Files
                 */
                '/referenceFiles'(resources: 'referenceFiles', excludes: DEFAULT_EXCLUDES)
            }

            /*
            Edits
             */
            get "/$resourceDomainType/$resourceId/edits"(controller: 'edit', action: 'index')

            /*
            Tree
            */
            group "/tree/$containerDomainType", {
                get '/'(controller: 'treeItem', action: 'index')
                get "/${catalogueItemDomainType}/$catalogueItemId"(controller: 'treeItem', action: 'show')
                get "/search/$search"(controller: 'treeItem', action: 'search')
            }

            /*
            Metadata
             */
            get "/metadata/namespaces/$id?"(controller: 'metadata', action: 'namespaces')

            /*
            Version Links
             */
            "/$modelDomainType/$modelId/versionLinks"(resources: 'versionLink', excludes: DEFAULT_EXCLUDES)

            /*
            User Images
             */
            get "/userImageFiles/$id"(controller: 'userImageFile', action: 'show')
        }
    }
}
