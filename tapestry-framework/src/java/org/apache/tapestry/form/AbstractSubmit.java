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

package org.apache.tapestry.form;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.hivemind.util.Defense;
import org.apache.tapestry.IActionListener;
import org.apache.tapestry.IForm;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.IScript;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.TapestryUtils;
import org.apache.tapestry.listener.ListenerInvoker;
import org.apache.tapestry.util.ScriptUtils;

/**
 * Superclass for components submitting their form.
 * 
 * @author Richard Lewis-Shell
 * @since 4.0
 */

abstract class AbstractSubmit extends AbstractFormComponent
{   
    /**
     * Determine if this submit component was clicked.
     * 
     * @param cycle
     * @param name
     * @return true if this submit was clicked
     */
    protected abstract boolean isClicked(IRequestCycle cycle, String name);

    /**
     * @see org.apache.tapestry.form.AbstractFormComponent#rewindFormComponent(org.apache.tapestry.IMarkupWriter, org.apache.tapestry.IRequestCycle)
     */
    protected void rewindFormComponent(IMarkupWriter writer, IRequestCycle cycle)
    {
        if (isClicked(cycle, getName()))
            handleClick(cycle, getForm());
    }

    void handleClick(final IRequestCycle cycle, IForm form)
    {
        if (isParameterBound("selected"))
            setSelected(getTag());

        final IActionListener listener = getListener();
        final IActionListener action = getAction();

        if (listener == null && action == null)
            return;

        final ListenerInvoker listenerInvoker = getListenerInvoker();

        Object parameters = getParameters();
        if (parameters != null)
        {
            if (parameters instanceof Collection)
            {
                cycle.setListenerParameters(((Collection) parameters).toArray());
            }
            else
            {
                cycle.setListenerParameters(new Object[]
                { parameters });
            }
        }
        
        // Invoke 'listener' now, but defer 'action' for later
        if (listener != null)
            listenerInvoker.invokeListener(listener, AbstractSubmit.this, cycle);
        
        if (action != null) {
            Runnable notify = new Runnable()
            {
                public void run()
                {
                    listenerInvoker.invokeListener(action, AbstractSubmit.this, cycle);
                }
            };

            form.addDeferredRunnable(notify);
        }
    }

    protected void renderSubmitType(IMarkupWriter writer, IRequestCycle cycle)
    {
        String type = getSubmitType();
        
        Defense.notNull(type, "submitType");
        
        if (type.equals(FormConstants.SUBMIT_NORMAL))
            return;
        
        if (!isParameterBound("onClick")) {
            
            writer.attribute("onClick", 
                    "tapestry.form." + type + "('" + getForm().getClientId() 
                    + "', '" + getName() + "')");
        } else {
            
            PageRenderSupport prs = TapestryUtils.getPageRenderSupport(cycle, this);
            
            Map parms = new HashMap();
            
            parms.put("submit", this);
            parms.put("type", type);
            parms.put("key", ScriptUtils.functionHash(type + this.hashCode()));
            
            getSubmitTypeScript().execute(this, cycle, prs, parms);
        }
    }
    
    /** parameter. */
    public abstract IActionListener getListener();
    
    /** parameter. */
    public abstract IActionListener getAction();

    /** parameter. */
    public abstract Object getTag();

    /** parameter. */
    public abstract void setSelected(Object tag);

    /** parameter. */
    public abstract boolean getDefer();

    /** parameter. */
    public abstract Object getParameters();

    /** The type of submission, normal/cancel/refresh. */
    public abstract String getSubmitType();
    
    /** Injected. */
    public abstract ListenerInvoker getListenerInvoker();
    
    /** Injected. */
    public abstract IScript getSubmitTypeScript();
}
