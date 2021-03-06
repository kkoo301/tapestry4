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

import org.apache.tapestry.BaseComponentTestCase;
import static org.easymock.EasyMock.expect;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link org.apache.tapestry.services.impl.CookieSourceImpl}. 
 */
@Test
public class CookieSourceTest extends BaseComponentTestCase
{
    // In seconds

    private static final int ONE_WEEK = 7 * 24 * 60 * 60;

    private static class ComparableCookie extends Cookie
    {
        public ComparableCookie(String name, String value, int maxAge)
        {
            super(name, value);
            setMaxAge(maxAge);
        }

        public boolean equals(Object obj)
        {
            Cookie c = (Cookie) obj;

            return equals(getName(), c.getName()) && equals(getValue(), c.getValue())
                    && equals(getPath(), c.getPath()) && getMaxAge() == c.getMaxAge();
        }

        private boolean equals(Object value, Object other)
        {
            return value == other || (value != null && value.equals(other));
        }
    }

    private HttpServletRequest newRequest(String[] nameValues)
    {
        Cookie[] cookies = null;

        if (nameValues != null)
        {

            List l = new ArrayList();

            for (int i = 0; i < nameValues.length; i += 2)
            {
                String name = nameValues[i];
                String value = nameValues[i + 1];

                Cookie c = new Cookie(name, value);

                l.add(c);
            }

            cookies = (Cookie[]) l.toArray(new Cookie[l.size()]);
        }

        HttpServletRequest request = newHttpRequest();

        expect(request.getCookies()).andReturn(cookies);

        return request;
    }

    protected HttpServletRequest newHttpRequest()
    {
        return newMock(HttpServletRequest.class);
    }

    private void attempt(String name, String expected, String[] nameValues)
    {
        HttpServletRequest request = newRequest(nameValues);

        CookieSourceImpl cs = new CookieSourceImpl();

        cs.setRequest(request);

        replay();

        String actual = cs.readCookieValue(name);

        assertEquals(actual, expected);

        verify();
    }

    public void test_No_Cookies()
    {
        attempt("foo", null, null);
    }

    public void test_Match()
    {
        attempt("fred", "flintstone", 
                new String[] { "barney", "rubble", "fred", "flintstone" });
    }

    public void test_No_Match()
    {
        attempt("foo", null, new String[] { "bar", "baz" });
    }

    public void test_Write_Cookie_Domain()
    {
        HttpServletRequest request = newHttpRequest();
        HttpServletResponse response = newResponse();

        // Training

        trainGetContextPath(request, "/context");
        
        Cookie cookie = new ComparableCookie("foo", "bar", ONE_WEEK);
        cookie.setPath("/context/");
        cookie.setDomain("fobar.com");
        
        response.addCookie(cookie);
        
        replay();
        
        CookieSourceImpl cs = new CookieSourceImpl();
        cs.setRequest(request);
        cs.setResponse(response);
        cs.setDefaultMaxAge(ONE_WEEK);
        
        cs.writeDomainCookieValue("foo", "bar", "fobar.com", ONE_WEEK);

        verify();
    }

    public void test_Write_Cookie_With_Max_Age()
    {
        HttpServletRequest request = newHttpRequest();
        HttpServletResponse response = newResponse();

        // Training

        trainGetContextPath(request, "/ctx");

        Cookie cookie = new ComparableCookie("foo", "bar", -1);
        cookie.setPath("/ctx/");

        response.addCookie(cookie);

        replay();

        CookieSourceImpl cs = new CookieSourceImpl();
        cs.setRequest(request);
        cs.setResponse(response);
        cs.setDefaultMaxAge(ONE_WEEK);

        cs.writeCookieValue("foo", "bar", -1);

        verify();
    }

    public void test_Write_Cookie()
    {
        HttpServletRequest request = newHttpRequest();
        HttpServletResponse response = newResponse();

        // Training
        
        trainGetContextPath(request, "/context");
        
        Cookie cookie = new ComparableCookie("foo", "bar", ONE_WEEK);
        cookie.setPath("/context/");

        response.addCookie(cookie);

        replay();

        CookieSourceImpl cs = new CookieSourceImpl();
        cs.setRequest(request);
        cs.setResponse(response);
        cs.setDefaultMaxAge(ONE_WEEK);

        cs.writeCookieValue("foo", "bar");

        verify();
    }
    
    private void trainGetContextPath(HttpServletRequest request, String contextPath)
    {
        expect(request.getContextPath()).andReturn(contextPath);
    }

    private HttpServletResponse newResponse()
    {
        return newMock(HttpServletResponse.class);
    }

    public void test_Remove_Cookie()
    {
        HttpServletRequest request = newHttpRequest();
        HttpServletResponse response = newResponse();

        // Training

        trainGetContextPath(request, "/context");

        Cookie cookie = new ComparableCookie("foo", null, 0);
        cookie.setPath("/context/");

        response.addCookie(cookie);

        replay();

        CookieSourceImpl cs = new CookieSourceImpl();
        cs.setRequest(request);
        cs.setResponse(response);

        cs.removeCookieValue("foo");

        verify();
    }
}