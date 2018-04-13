/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.discovery.propertyStore

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult
import com.amazonaws.services.simplesystemsmanagement.model.Parameter
import io.micronaut.configurations.aws.AWSConfiguration
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.context.env.EnvironmentPropertySource
import io.micronaut.context.env.PropertySource
import io.micronaut.core.io.socket.SocketUtils
import io.micronaut.discovery.aws.parameterStore.AWSParameterStoreConfigClient
import io.micronaut.discovery.aws.parameterStore.AWSParameterStoreConfiguration
import io.micronaut.discovery.aws.route53.Route53ClientDiscoveryConfiguration
import io.micronaut.discovery.config.ConfigurationClient
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.Flowable
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class AWSPropertyStoreMockConfigurationClientSpec extends Specification {
    @Shared
    int serverPort = SocketUtils.findAvailableTcpPort()

    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer,
            [

                    'aws.systemManager.parameterStore.enabled': true,
                    'aws.systemManager.parameterStore.useSecureParameters' : false,
                    'micronaut.application.name':'amazonTest'],
            Environment.AMAZON_EC2

    )

    @Shared
    AWSParameterStoreConfigClient client = embeddedServer.applicationContext.getBean(AWSParameterStoreConfigClient)


    def setup() {
        client.client = Mock(AWSSimpleSystemsManagement)
    }

    void "test discovery property sources from AWS Systems Manager Parameter Store - StringList"() {

        given:
        client.client.getParametersByPath(_) >> {  GetParametersByPathRequest getRequest->
            GetParametersByPathResult result = new GetParametersByPathResult()

            ArrayList<Parameter> parameters = new ArrayList<Parameter>()

            if (getRequest.getPath() == "/config/application") {

                Parameter parameter = new Parameter()
                parameter.name = "/config/application"
                parameter.value = "datasource.url=mysql://blah,datasource.driver=java.SomeDriver"
                parameter.type = "StringList"
                parameters.add(parameter)
            }
            if (getRequest.getPath() == "/config/application,test") {
                Parameter parameter1 = new Parameter()
                parameter1.name = "/config/application,test"
                parameter1.value = "foo=bar"
                parameter1.type = "StringList"
                parameters.add(parameter1)
            }

            result.setParameters(parameters)
            result
        }


        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['test'] as Set)
        List<PropertySource> propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()

        then: "verify property source characteristics"
        propertySources.size() == 2
        propertySources[0].order > EnvironmentPropertySource.POSITION
        propertySources[0].name == 'route53-application'
        propertySources[0].get('datasource.url') == "mysql://blah"
        propertySources[0].get('datasource.driver') == "java.SomeDriver"
        propertySources[0].toList().size() == 2
        propertySources[1].name == 'route53-application[test]'
        propertySources[1].get("foo") == "bar"
        propertySources[1].order > propertySources[0].order
        propertySources[1].toList().size() == 1
    }


    void "test discovery property sources from AWS Systems Manager Parameter Store - String"() {

        given:
        client.client.getParametersByPath(_) >> {  GetParametersByPathRequest getRequest->
            GetParametersByPathResult result = new GetParametersByPathResult()
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()
            Parameter parameter = new Parameter()
            parameter.name = "/config/application"
            parameter.value = "datasource.url=mysql://blah,datasource.driver=java.SomeDriver"
            parameter.type = "StringList"
            parameters.add(parameter)
            result.setParameters(parameters)
            result
        }


        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['test'] as Set)
        List<PropertySource> propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()

        then: "verify property source characteristics"
        propertySources.size() == 2
        propertySources[0].order > EnvironmentPropertySource.POSITION
        propertySources[0].name == 'amazonTest-application'
        propertySources[0].get('datasource.url') == "mysql://blah"
        propertySources[0].get('datasource.driver') == "java.SomeDriver"
        propertySources[0].toList().size() == 2
        propertySources[1].name == 'amazonTest-application[test]'
        propertySources[1].get("foo") == "bar"
        propertySources[1].order > propertySources[0].order
        propertySources[1].toList().size() == 1
    }


    void "test discovery property sources from AWS Systems Manager Parameter Store - SecureString"() {

        given:
        client.client.getParametersByPath(_) >> {  GetParametersByPathRequest getRequest->
            GetParametersByPathResult result = new GetParametersByPathResult()
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()

            Parameter parameter = new Parameter()
            parameter.name = "/config/application"
            parameter.value = "datasource.url=mysql://blah,datasource.driver=java.SomeDriver"
            parameter.type = "StringList"
            parameters.add(parameter)

            Parameter parameter1 = new Parameter()
            parameter1.name = "/config/application,test"
            parameter1.value = "datasource.url=mysql://blah,datasource.driver=java.SomeDriver"
            parameter1.type = "StringList"
            parameters.add(parameter1)

            result.setParameters(parameters)

            result
        }


        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['test'] as Set)
        List<PropertySource> propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()

        then: "verify property source characteristics"
        propertySources.size() == 2
        propertySources[0].order > EnvironmentPropertySource.POSITION
        propertySources[0].name == 'amazonTest-application'
        propertySources[0].get('datasource.url') == "mysql://blah"
        propertySources[0].get('datasource.driver') == "java.SomeDriver"
        propertySources[0].toList().size() == 2
        propertySources[1].name == 'amazonTest-application[test]'
        propertySources[1].get("foo") == "bar"
        propertySources[1].order > propertySources[0].order
        propertySources[1].toList().size() == 1
    }

}
