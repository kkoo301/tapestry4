// Copyright 2004, 2005 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.services.impl;

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.ErrorLog;
import org.apache.tapestry.BaseComponentTestCase;
import org.apache.tapestry.IEngine;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.engine.ILink;
import org.apache.tapestry.engine.ServiceEncoder;
import org.apache.tapestry.engine.ServiceEncoding;
import org.apache.tapestry.engine.encoders.PageServiceEncoder;
import org.apache.tapestry.record.PropertyPersistenceStrategy;
import org.apache.tapestry.record.PropertyPersistenceStrategySource;
import org.apache.tapestry.services.LinkFactory;
import org.apache.tapestry.services.ServiceConstants;
import org.apache.tapestry.util.io.DataSqueezerUtil;
import org.apache.tapestry.web.WebRequest;
import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.expect;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Tests for {@link org.apache.tapestry.services.impl.LinkFactoryImpl}.
 * 
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
@Test
public class LinkFactoryTest extends BaseComponentTestCase
{
    private ErrorLog newErrorLog()
    {
        return newMock(ErrorLog.class);
    }

    private static class NoopEncoder implements ServiceEncoder
    {
        public void decode(ServiceEncoding encoding)
        {
            //
        }

        public void encode(ServiceEncoding encoding)
        {
            //
        }
    }

    private static class MockSource implements PropertyPersistenceStrategySource
    {

        public PropertyPersistenceStrategy getStrategy(String name)
        {
            return null;
        }

        public Collection getAllStoredChanges(String pageName)
        {
            return null;
        }

        public void discardAllStoredChanged(String pageName)
        {
        }

        public void addParametersForPersistentProperties(ServiceEncoding encoding, boolean post)
        {
            encoding.setParameterValue("foo", "bar");
        }

    }

    private ServiceEncoderContribution newContribution(String id, ServiceEncoder encoder)
    {
        ServiceEncoderContribution result = new ServiceEncoderContribution();

        result.setId(id);
        result.setEncoder(encoder);

        return result;
    }

    private IEngine newEngine()
    {
        return newMock(IEngine.class);
    }

    private void trainGetOutputEncoding(IEngine engine, String outputEncoding)
    {
        expect(engine.getOutputEncoding()).andReturn(outputEncoding);
    }

    protected WebRequest newRequest(String contextPath)
    {
        WebRequest request = newRequest();
        checkOrder(request, false);
        
        expect(request.getContextPath()).andReturn(contextPath).anyTimes();

        return request;
    }

    public void test_No_Encoders()
    {
        ErrorLog log = newErrorLog();
        WebRequest request = newRequest("/context");
        IEngine engine = newEngine();
        IRequestCycle cycle = newCycle();
        IEngineService service = newService("myservice");

        trainGetEngine(cycle, engine);
        trainGetOutputEncoding(engine, "utf-8");
        
        replay();

        LinkFactoryImpl lf = new LinkFactoryImpl();

        lf.setContributions(Collections.EMPTY_LIST);
        lf.setErrorLog(log);
        lf.setServletPath("/app");
        lf.setRequest(request);
        lf.setRequestCycle(cycle);

        lf.initializeService();

        Map parameters = new HashMap();

        ILink link = lf.constructLink(service, false, parameters, false);

        assertEquals("/context/app?service=myservice", link.getURL());
        
        verify();
    }

    private IEngineService newService(String name)
    {
        IEngineService service = newMock(IEngineService.class);

        expect(service.getName()).andReturn(name);

        return service;
    }

    public void test_Stateful_Request()
    {
        ErrorLog log = newErrorLog();
        WebRequest request = newRequest("/context");
        IEngine engine = newEngine();
        IEngineService service = newService("myservice");
        IRequestCycle cycle = newCycle();

        trainGetEngine(cycle, engine);
        trainGetOutputEncoding(engine, "utf-8");
        trainEncodeURL(cycle, "/context/app?foo=bar&service=myservice", "/context/app?foo=bar&service=myservice&jsessionid=124");
        
        replay();

        LinkFactoryImpl lf = new LinkFactoryImpl();

        lf.setContributions(Collections.EMPTY_LIST);
        lf.setErrorLog(log);
        lf.setServletPath("/app");
        lf.setRequest(request);
        lf.setPersistenceStrategySource(new MockSource());
        lf.setRequestCycle(cycle);
        
        lf.initializeService();
        
        Map parameters = new HashMap();
        
        ILink link = lf.constructLink(service, false, parameters, true);
        
        assertEquals("/context/app?foo=bar&service=myservice&jsessionid=124", link.getURL());

        verify();
    }

    public void test_Noop_Encoders()
    {
        WebRequest request = newRequest("/context");
        IRequestCycle cycle = newCycle();
        ErrorLog log = newErrorLog();
        IEngine engine = newEngine();
        IEngineService service = newService("myservice");

        trainGetEngine(cycle, engine);
        trainGetOutputEncoding(engine, "utf-8");
        
        replay();

        List l = new ArrayList();
        l.add(newContribution("fred", new NoopEncoder()));
        l.add(newContribution("barney", new NoopEncoder()));

        LinkFactoryImpl lf = new LinkFactoryImpl();

        lf.setContributions(l);
        lf.setErrorLog(log);
        lf.setServletPath("/app");
        lf.setRequest(request);
        lf.setRequestCycle(cycle);

        lf.initializeService();

        Map parameters = new HashMap();

        ILink link = lf.constructLink(service, false, parameters, false);

        assertEquals("/context/app?service=myservice", link.getURL());
        
        verify();
    }

    public void test_Active_Encoder()
    {
        WebRequest request = newRequest("/context");
        IRequestCycle cycle = newCycle();
        ErrorLog log = newErrorLog();
        IEngineService service = newService("page");
        IEngine engine = newEngine();

        trainGetEngine(cycle, engine);
        trainGetOutputEncoding(engine, "utf-8");

        replay();

        PageServiceEncoder e = new PageServiceEncoder();
        e.setServiceName("page");
        e.setExtension("html");

        List l = Collections.singletonList(newContribution("encoder", e));

        LinkFactoryImpl lf = new LinkFactoryImpl();

        lf.setContributions(l);
        lf.setErrorLog(log);
        lf.setServletPath("/app");
        lf.setRequest(request);
        lf.setRequestCycle(cycle);

        lf.initializeService();

        Map parameters = new HashMap();
        parameters.put(ServiceConstants.PAGE, "Barney");

        ILink link = lf.constructLink(service, false, parameters, false);

        assertEquals("/context/Barney.html", link.getURL());

        verify();
    }

    public void test_Service_Name_Is_Null()
    {
        IEngineService service = newService(null);

        Map parameters = new HashMap();

        replay();

        LinkFactory lf = new LinkFactoryImpl();

        try
        {
            lf.constructLink(service, false, parameters, true);
            unreachable();
        }
        catch (ApplicationRuntimeException ex)
        {
            assertEquals(ImplMessages.serviceNameIsNull(), ex.getMessage());
        }

        verify();
    }

    public void test_With_Service_Parameters()
    {
        WebRequest request = newRequest("/context");
        IRequestCycle cycle = newCycle();
        ErrorLog log = newErrorLog();
        IEngineService service = newService("external");
        IEngine engine = newEngine();

        trainGetEngine(cycle, engine);
        trainGetOutputEncoding(engine, "utf-8");

        replay();

        PageServiceEncoder e = new PageServiceEncoder();
        e.setServiceName("external");
        e.setExtension("ext");

        List l = Collections.singletonList(newContribution("encoder", e));

        LinkFactoryImpl lf = new LinkFactoryImpl();

        lf.setContributions(l);
        lf.setErrorLog(log);
        lf.setServletPath("/app");
        lf.setDataSqueezer(DataSqueezerUtil.createUnitTestSqueezer());
        lf.setRequest(request);
        lf.setRequestCycle(cycle);

        lf.initializeService();

        Map parameters = new HashMap();
        parameters.put(ServiceConstants.PAGE, "Barney");
        parameters.put(ServiceConstants.PARAMETER, new Object[]
        { Boolean.TRUE });

        ILink link = lf.constructLink(service, false, parameters, false);

        assertEquals("/context/Barney.ext?sp=T", link.getURL());

        verify();
    }
}