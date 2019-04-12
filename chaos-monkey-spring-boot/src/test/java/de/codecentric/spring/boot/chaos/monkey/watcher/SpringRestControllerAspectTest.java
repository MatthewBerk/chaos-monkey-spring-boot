/*
 * Copyright 2018 the original author or authors.
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
 */

package de.codecentric.spring.boot.chaos.monkey.watcher;

import de.codecentric.spring.boot.chaos.monkey.component.ChaosMonkeyRequestScope;
import de.codecentric.spring.boot.chaos.monkey.component.MetricEventPublisher;
import de.codecentric.spring.boot.chaos.monkey.component.MetricType;
import de.codecentric.spring.boot.demo.chaos.monkey.restcontroller.DemoRestController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.mockito.Mockito.*;

/**
 * @author Benjamin Wilms
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringRestControllerAspectTest {

    @Mock
    private ChaosMonkeyRequestScope chaosMonkeyRequestScopeMock;

    @Mock
    private MetricEventPublisher metricsMock;

    private String pointcutName = "execution.DemoRestController.sayHello";
    private String simpleName = "de.codecentric.spring.boot.demo.chaos.monkey.restcontroller.DemoRestController.sayHello";


    @Test
    public void chaosMonkeyIsCalled() {
        DemoRestController target = new DemoRestController();

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        SpringRestControllerAspect serviceAspect = new SpringRestControllerAspect(chaosMonkeyRequestScopeMock, metricsMock);
        factory.addAspect(serviceAspect);

        DemoRestController proxy = factory.getProxy();
        proxy.sayHello();

        verify(chaosMonkeyRequestScopeMock, times(1)).callChaosMonkey(simpleName);
        verify(metricsMock, times(1)).publishMetricEvent(pointcutName, MetricType.RESTCONTROLLER);
        verifyNoMoreInteractions(chaosMonkeyRequestScopeMock, metricsMock);

    }

    @Test
    public void chaosMonkeyIsNotCalled() {
        DemoRestController target = new DemoRestController();

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        SpringControllerAspect controllerAspect = new SpringControllerAspect(chaosMonkeyRequestScopeMock, metricsMock);
        SpringServiceAspect serviceAspect = new SpringServiceAspect(chaosMonkeyRequestScopeMock, metricsMock);
        SpringRepositoryAspect repositoryAspect = new SpringRepositoryAspect(chaosMonkeyRequestScopeMock, metricsMock);
        factory.addAspect(controllerAspect);
        factory.addAspect(serviceAspect);
        factory.addAspect(repositoryAspect);


        DemoRestController proxy = factory.getProxy();
        proxy.sayHello();

        verify(chaosMonkeyRequestScopeMock, times(0)).callChaosMonkey(simpleName);
        verify(metricsMock, times(0)).publishMetricEvent(pointcutName,MetricType.RESTCONTROLLER);
        verifyNoMoreInteractions(chaosMonkeyRequestScopeMock, metricsMock);

    }
}