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

package org.apache.tapestry.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.hivemind.test.HiveMindTestCase;

/**
 * Tests for {@link org.apache.tapestry.container.ContainerUtils}.
 * 
 * @author Howard M. Lewis Ship
 * @since 3.1
 */
public class TestContainerUtils extends HiveMindTestCase
{
    public void testToSortedList()
    {
        List l = new ArrayList();
        l.add("Fred");
        l.add("Barney");
        Enumeration e = Collections.enumeration(l);

        l = ContainerUtils.toSortedList(e);

        assertEquals(2, l.size());
        assertEquals("Barney", l.get(0));
        assertEquals("Fred", l.get(1));

        // Make sure its not modifiable.

        try
        {
            l.add("Wilma");
            unreachable();
        }
        catch (UnsupportedOperationException ex)
        {
            // That's expected.
        }
    }

    public void testToSortedListEmpty()
    {
        List l = new ArrayList();
        Enumeration e = Collections.enumeration(l);

        List sorted = ContainerUtils.toSortedList(e);

        assertSame(Collections.EMPTY_LIST, sorted);
    }
}