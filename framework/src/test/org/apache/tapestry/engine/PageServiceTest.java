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

package org.apache.tapestry.engine;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.services.LinkFactory;
import org.apache.tapestry.services.ResponseRenderer;
import org.apache.tapestry.services.ServiceConstants;

/**
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
public class PageServiceTest extends ServiceTestCase
{
    public void testGetLink()
    {
        LinkFactory lf = newLinkFactory();
        ILink link = newLink();

        Map parameters = new HashMap();
        parameters.put(ServiceConstants.PAGE, "TargetPage");

        PageService ps = new PageService();
        ps.setLinkFactory(lf);

        trainConstructLink(lf, ps, false, parameters, true, link);

        replayControls();

        assertSame(link, ps.getLink(false, "TargetPage"));

        verifyControls();
    }

    public void testService() throws Exception
    {
        IRequestCycle cycle = newCycle();
        ResponseRenderer rr = newResponseRenderer();

        trainGetParameter(cycle, ServiceConstants.PAGE, "TargetPage");

        cycle.activate("TargetPage");

        rr.renderResponse(cycle);

        replayControls();

        PageService ps = new PageService();
        ps.setResponseRenderer(rr);

        ps.service(cycle);

        verifyControls();
    }
}
