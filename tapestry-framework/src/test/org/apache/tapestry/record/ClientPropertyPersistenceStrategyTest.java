// Copyright 2005 The Apache Software Foundation
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

package org.apache.tapestry.record;

import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry.BaseComponentTestCase;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.engine.ServiceEncoding;
import org.apache.tapestry.web.WebRequest;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.apache.tapestry.record.ClientPropertyPersistenceStrategy}.
 * 
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
@Test
public class ClientPropertyPersistenceStrategyTest extends BaseComponentTestCase
{
    private PersistentPropertyDataEncoder newEncoder()
    {
        PersistentPropertyDataEncoderImpl encoder = new PersistentPropertyDataEncoderImpl();
        encoder.setClassResolver(getClassResolver());

        return encoder;
    }

    private ClientPropertyPersistenceScope newScope()
    {
        return newMock(ClientPropertyPersistenceScope.class);
    }

    public void testAddParametersForPersistentProperties()
    {
        WebRequest request = newRequest();

        ServiceEncoding encoding = newMock(ServiceEncoding.class);

        trainGetParameterNames(request, new String[]
        { "bar", "appstate:MyPage" });

        trainGetParameterValue(request, "appstate:MyPage", "ENCODED");

        encoding.setParameterValue("appstate:MyPage", "ENCODED");

        replay();

        ClientPropertyPersistenceStrategy strategy = new ClientPropertyPersistenceStrategy();
        strategy.setRequest(request);
        strategy.setScope(new AppClientPropertyPersistenceScope());
        strategy.setEncoder(newEncoder());

        strategy.initializeService();

        strategy.addParametersForPersistentProperties(encoding, false);

        verify();
    }

    public void testGetChangesUnknownPage()
    {
        ClientPropertyPersistenceStrategy strategy = new ClientPropertyPersistenceStrategy();

        assertTrue(strategy.getStoredChanges("UnknownPage").isEmpty());
    }

    public void testInitialize()
    {
        WebRequest request = newRequest();
        ClientPropertyPersistenceScope scope = newScope();
        PersistentPropertyDataEncoder encoder = newMock(PersistentPropertyDataEncoder.class);

        trainGetParameterNames(request, new String[]
        { "foo", "state:MyPage" });

        trainIsParameterForScope(scope, "foo", false);
        trainIsParameterForScope(scope, "state:MyPage", true);

        trainExtractPageName(scope, "state:MyPage", "MyPage");

        trainGetParameterValue(request, "state:MyPage", "ENCODED");

        List changes = Collections.singletonList(new PropertyChangeImpl("foo", "bar", "baz"));

        trainDecodePageChanges(encoder, "ENCODED", changes);

        replay();

        ClientPropertyPersistenceStrategy strategy = new ClientPropertyPersistenceStrategy();
        strategy.setRequest(request);
        strategy.setScope(scope);
        strategy.setEncoder(encoder);

        strategy.initializeService();

        assertSame(changes, strategy.getStoredChanges("MyPage"));

        verify();
    }

    public void testPageScope()
    {
        WebRequest request = newRequest();
        IRequestCycle cycle = newCycle();
        IPage page = newPage();

        ServiceEncoding encoding = newMock(ServiceEncoding.class);
        
        trainGetParameterNames(request, new String[] { "foo", "state:MyPage", "state:OtherPage" });
        
        trainGetParameterValue(request, "state:MyPage", "ENCODED1");
        trainGetParameterValue(request, "state:OtherPage", "ENCODED2");
       
        trainGetPage(cycle, page);
        trainGetPageName(page, "MyPage");
        
        encoding.setParameterValue("state:MyPage", "ENCODED1");

        trainGetPage(cycle, page);
        trainGetPageName(page, "MyPage");


        replay();

        PageClientPropertyPersistenceScope scope = new PageClientPropertyPersistenceScope();
        scope.setRequestCycle(cycle);

        ClientPropertyPersistenceStrategy strategy = new ClientPropertyPersistenceStrategy();
        strategy.setRequest(request);
        strategy.setScope(scope);
        strategy.setEncoder(newEncoder());

        strategy.initializeService();

        strategy.addParametersForPersistentProperties(encoding, false);

        verify();

    }

    public void testStoreAndRetrieve()
    {
        PropertyChange pc = new PropertyChangeImpl("foo", "bar", "baz");

        ClientPropertyPersistenceStrategy strategy = new ClientPropertyPersistenceStrategy();
        strategy.setEncoder(newEncoder());

        strategy.store("MyPage", "foo", "bar", "baz");

        assertEquals(Collections.singletonList(pc), strategy.getStoredChanges("MyPage"));

        strategy.discardStoredChanges("MyPage");

        assertEquals(Collections.EMPTY_LIST, strategy.getStoredChanges("MyPage"));
    }

    private void trainDecodePageChanges(PersistentPropertyDataEncoder encoder, String encoded,
            List changes)
    {
        expect(encoder.decodePageChanges(encoded)).andReturn(changes);
    }

    private void trainExtractPageName(ClientPropertyPersistenceScope scope, String parameterName,
            String pageName)
    {
        expect(scope.extractPageName(parameterName)).andReturn(pageName);
    }

    private void trainGetPage(IRequestCycle cycle, IPage page)
    {
        expect(cycle.getPage()).andReturn(page);
    }

    private void trainGetParameterNames(WebRequest request, String[] names)
    {
        expect(request.getParameterNames()).andReturn(Arrays.asList(names));
    }

    private void trainGetParameterValue(WebRequest request, String parameterName, String value)
    {
        expect(request.getParameterValue(parameterName)).andReturn(value);
    }

    private void trainIsParameterForScope(ClientPropertyPersistenceScope scope,
            String parameterName, boolean result)
    {
        expect(scope.isParameterForScope(parameterName)).andReturn(result);
    }
}