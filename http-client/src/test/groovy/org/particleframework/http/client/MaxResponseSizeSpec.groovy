/*
 * Copyright 2018 original authors
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
package org.particleframework.http.client

import org.particleframework.context.ApplicationContext
import org.particleframework.http.HttpRequest
import org.particleframework.http.MediaType
import org.particleframework.http.annotation.Controller
import org.particleframework.http.client.exceptions.ContentLengthExceededException
import org.particleframework.http.client.exceptions.ReadTimeoutException
import org.particleframework.runtime.server.EmbeddedServer
import org.particleframework.web.router.annotation.Get
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class MaxResponseSizeSpec extends Specification {
    @Shared @AutoCleanup ApplicationContext context = ApplicationContext.run(
            "particle.http.client.maxContentLength":'1kb'
    )
    @Shared EmbeddedServer embeddedServer = context.getBean(EmbeddedServer).start()
    @Shared @AutoCleanup HttpClient client = context.createBean(HttpClient, [url:embeddedServer.getURL()])

    void "test max content length setting"() {
        when:
        client.toBlocking().retrieve(HttpRequest.GET('/max'), String)

        then:
        def e = thrown(ContentLengthExceededException)
        e.message == 'The received length exceeds the maximum content length [1024]'
    }

    @Controller("/max")
    static class GetController {

        @Get(uri = "/", produces = MediaType.TEXT_PLAIN)
        String index() {
            return ("success" * 1000)
        }
    }
}
