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
package uk.ac.ox.softeng.maurodatamapper.core.traits.service


import org.grails.orm.hibernate.proxy.HibernateProxyHandler

trait DomainService<K> {

    final static HibernateProxyHandler HIBERNATE_PROXY_HANDLER = new HibernateProxyHandler()

    abstract K get(Serializable id)

    abstract List<K> list(Map args)

    abstract Long count()

    abstract void delete(K domain)

    K save(K domain) {
        // Default behaviours for save in GormEntity
        save(flush: false, validate: true, domain)
    }

    K save(Map args, K domain) {
        domain.save(args)
    }

    K unwrapIfProxy(def ge) {
        HIBERNATE_PROXY_HANDLER.unwrapIfProxy(ge) as K
    }
}