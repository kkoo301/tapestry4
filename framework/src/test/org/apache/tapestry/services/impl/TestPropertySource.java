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

import java.util.Collections;

import org.apache.hivemind.ErrorLog;
import org.apache.hivemind.test.HiveMindTestCase;
import org.apache.tapestry.engine.IPropertySource;
import org.easymock.MockControl;

/**
 * Tests for {@link org.apache.tapestry.services.impl.PropertySourceImpl}.
 * 
 * @author Howard Lewis Ship
 * @since 3.1
 */
public class TestPropertySource extends HiveMindTestCase
{
    public void testSuccess()
    {
        MockControl sourceControl = newControl(IPropertySource.class);
        IPropertySource source = (IPropertySource) sourceControl.getMock();

        ErrorLog log = (ErrorLog) newMock(ErrorLog.class);

        // Training

        source.getPropertyValue("foo");
        sourceControl.setReturnValue("bar");

        replayControls();

        PropertySourceContribution c = new PropertySourceContribution();
        c.setName("psc");
        c.setSource(source);

        PropertySourceImpl ps = new PropertySourceImpl();

        ps.setContributions(Collections.singletonList(c));
        ps.setErrorLog(log);

        ps.initializeService();

        assertEquals("bar", ps.getPropertyValue("foo"));

        verifyControls();
    }

    public void testFailure()
    {
        ErrorLog log = (ErrorLog) newMock(ErrorLog.class);

        replayControls();

        PropertySourceImpl ps = new PropertySourceImpl();

        ps.setContributions(Collections.EMPTY_LIST);
        ps.setErrorLog(log);

        ps.initializeService();

        assertNull(ps.getPropertyValue("foo"));

        verifyControls();
    }
}