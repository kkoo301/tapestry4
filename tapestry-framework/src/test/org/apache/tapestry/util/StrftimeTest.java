// Copyright Jun 10, 2006 The Apache Software Foundation
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
package org.apache.tapestry.util;

import org.apache.tapestry.BaseComponentTestCase;
import org.testng.annotations.Test;


/**
 * Tests functionality of {@link Strftime}.
 * 
 * @see "http://www.opengroup.org/onlinepubs/007908799/xsh/strftime.html"
 * @author jkuhnert
 */
@Test
public class StrftimeTest extends BaseComponentTestCase
{

    public void testPosixFormat()
    {
        assertEquals("%m/%d/%Y", Strftime.convertToPosixFormat("MM/dd/yyyy"));
        assertEquals("%H:%M:%S at %p", Strftime.convertToPosixFormat("HH:mm:ss at a"));
    }
}
