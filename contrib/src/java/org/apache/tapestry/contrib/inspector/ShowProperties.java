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

package org.apache.tapestry.contrib.inspector;

import java.util.Collections;
import java.util.List;

import org.apache.hivemind.service.ClassFabUtils;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IPage;
import org.apache.tapestry.engine.IPageRecorder;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.event.PageRenderListener;
import org.apache.tapestry.record.IPageChange;

/**
 * Component of the {@link Inspector}page used to display the persisent properties of the page.
 * 
 * @author Howard Lewis Ship
 */

public class ShowProperties extends BaseComponent implements PageRenderListener
{
    private List _properties;

    private IPageChange _change;

    private IPage _inspectedPage;

    /**
     * Does nothing.
     * 
     * @since 1.0.5
     */

    public void pageBeginRender(PageEvent event)
    {
    }

    /**
     * @since 1.0.5
     */

    public void pageEndRender(PageEvent event)
    {
        _properties = null;
        _change = null;
        _inspectedPage = null;
    }

    private void buildProperties()
    {
        Inspector inspector = (Inspector) getPage();

        _inspectedPage = inspector.getInspectedPage();

        //  IEngine engine = getPage().getEngine();
        IPageRecorder recorder = null;

        // TODO: This is going to blow up with UnsupportedOperationException
        // engine.getPageRecorder(_inspectedPage.getPageName(), inspector.getRequestCycle());

        // No page recorder? No properties.

        if (recorder == null)
        {
            _properties = Collections.EMPTY_LIST;
            return;
        }

        _properties = Collections.EMPTY_LIST;

        // The getChanges() method was removed
        // from IPageRecorder in release 3.1
        // new ArrayList(recorder.getChanges());
    }

    /**
     * Returns a {@link List}of {@link IPageChange}objects.
     * <p>
     * Sort order is not defined.
     */

    public List getProperties()
    {
        if (_properties == null)
            buildProperties();

        return _properties;
    }

    public void setChange(IPageChange value)
    {
        _change = value;
    }

    public IPageChange getChange()
    {
        return _change;
    }

    /**
     * Returns the name of the value's class, if the value is non-null.
     */

    public String getValueClassName()
    {
        Object value;

        value = _change.getNewValue();

        if (value == null)
            return "<null>";

        return ClassFabUtils.getJavaClassName(value.getClass());
    }
}