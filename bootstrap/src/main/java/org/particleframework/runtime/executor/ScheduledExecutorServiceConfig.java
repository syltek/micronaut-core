/*
 * Copyright 2017 original authors
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
package org.particleframework.runtime.executor;

import org.particleframework.context.annotation.Bean;
import org.particleframework.context.annotation.Factory;
import org.particleframework.context.annotation.Requires;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A default executor service for scheduling adhoc tasks via {@link java.util.concurrent.ScheduledExecutorService}
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Requires(missingProperty = "particle.server.executors.scheduled")
@Factory
public class ScheduledExecutorServiceConfig {
    /**
     * The name of the default IO executor service
     */
    public static final String NAME = "scheduled";

    @Singleton
    @Bean
    @Named(ScheduledExecutorServiceConfig.NAME)
    ExecutorConfiguration configuration() {
        return UserExecutorConfiguration.of(ExecutorType.SCHEDULED);
    }
}
