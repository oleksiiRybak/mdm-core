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
package uk.ac.ox.softeng.maurodatamapper.core.admin

import uk.ac.ox.softeng.maurodatamapper.core.hibernate.search.LuceneIndexingService
import uk.ac.ox.softeng.maurodatamapper.core.rest.transport.LuceneIndexParameters
import uk.ac.ox.softeng.maurodatamapper.core.session.SessionService
import uk.ac.ox.softeng.maurodatamapper.core.traits.controller.ResourcelessMdmController
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import grails.databinding.DataBindingSource
import grails.web.databinding.DataBindingUtils

import java.sql.DriverManager

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.OK

class AdminController implements ResourcelessMdmController {

    AdminService adminService
    LuceneIndexingService luceneIndexingService
    SessionService sessionService

    static responseFormats = ['json', 'xml']

    static allowedMethods = [editApiProperties: ['POST', 'PUT'], editApiProperty: ['POST', 'PUT'],
                             apiProperties    : 'GET', apiVersion: 'GET', coreVersion: 'GET', isTestMode: 'GET', activeSessions: 'GET']

    def editApiProperties() {
        DataBindingSource dataBindingSource = DataBindingUtils.createDataBindingSource(getGrailsApplication(), HashMap, request)
        Map<String, String> newValues = [:]
        dataBindingSource.propertyNames.each {n ->
            newValues[n] = dataBindingSource.getPropertyValue(n)
        }

        try {
            respond view: 'apiProperties', [apiPropertyList: adminService.getAndUpdateApiProperties(currentUser, newValues)]
        } catch (Exception ex) {
            log.error("AC01 - Could not update ApiProperties due to exception", ex)
            respond view: '/error', status: INTERNAL_SERVER_ERROR, [message: ex.message, errorCode: 'AC01']
        }
    }

    def apiProperties() {
        respond adminService.getApiProperties()
    }

    def rebuildLuceneIndexes(LuceneIndexParameters indexParameters) {
        long start = System.currentTimeMillis()
        luceneIndexingService.rebuildLuceneIndexes(indexParameters)
        long end = System.currentTimeMillis()

        Map info = [
            user                 : currentUser.emailAddress,
            indexed              : true,
            timeTakenMilliseconds: end - start,
            timeTaken            : Utils.getTimeString(end - start)
        ]

        respond info, status: OK

    }

    def status() {

        List<Map<String, Serializable>> databaseDrivers = []

        DriverManager.getDrivers().each {driver ->

            databaseDrivers.add([
                class  : driver.getClass().canonicalName,
                version: "${driver.majorVersion}.${driver.minorVersion}",

            ])
        }

        respond([
                    'Mauro Data Mapper Version'       : grailsApplication.config.info.app.version,
                    'Grails Version'                  : grailsApplication.config.info.app.grailsVersion,
                    'Java Version'                    : System.getProperty('java.version'),
                    'Java Vendor'                     : System.getProperty('java.vendor'),
                    'OS Name'                         : System.getProperty('os.name'),
                    'OS Version'                      : System.getProperty('os.version'),
                    'OS Architecture'                 : System.getProperty('os.arch'),
                    'Driver Manager Drivers Available': databaseDrivers
                ], status: OK)
    }
}
