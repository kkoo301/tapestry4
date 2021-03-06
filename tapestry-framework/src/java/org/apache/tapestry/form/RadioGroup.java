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

package org.apache.tapestry.form;

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.tapestry.*;
import org.apache.tapestry.valid.ValidatorException;

import java.util.Map;
import java.util.HashMap;

/**
 * A special type of form component that is used to contain {@link Radio}components. The Radio and
 * {@link Radio}group components work together to update a property of some other object, much like
 * a more flexible version of a {@link PropertySelection}. [ <a
 * href="../../../../../components/form/radiogroup.html">Component Reference </a>]
 * <p>
 * As of 4.0, this component can be validated.
 *
 * @author Howard Lewis Ship
 * @author Paul Ferraro
 */
public abstract class RadioGroup extends AbstractFormComponent implements ValidatableField
{
    /**
     * A <code>RadioGroup</code> places itself into the {@link IRequestCycle}as an attribute, so
     * that its wrapped {@link Radio}components can identify thier state.
     */

    static final String ATTRIBUTE_NAME = "org.apache.tapestry.active.RadioGroup";

    // Cached copy of the value from the selectedBinding
    Object _selection;

    // The value from the HTTP request indicating which
    // Radio was selected by the user.
    int _selectedOption;

    boolean _rewinding;

    boolean _rendering;

    private int _nextOptionId;

    /** A script providing a method onChange to be called whenever one of the enclosed radio-buttons is
     * clicked 
     */
    public abstract IScript getScript();

    public static RadioGroup get(IRequestCycle cycle)
    {
        return (RadioGroup) cycle.getAttribute(ATTRIBUTE_NAME);
    }

    public int getNextOptionId()
    {
        if (!_rendering)
            throw Tapestry.createRenderOnlyPropertyException(this, "nextOptionId");

        return _nextOptionId++;
    }

    public boolean isRewinding()
    {
        if (!_rendering)
            throw Tapestry.createRenderOnlyPropertyException(this, "rewinding");

        return _rewinding;
    }

    /**
     * Returns true if the value is equal to the current selection for the group. This is invoked by
     * a {@link Radio}during rendering to determine if it should be marked 'checked'.
     */

    public boolean isSelection(Object value)
    {
        if (!_rendering)
            throw Tapestry.createRenderOnlyPropertyException(this, "selection");

        if (_selection == value)
            return true;

        if (_selection == null || value == null)
            return false;

        return _selection.equals(value);
    }

    /**
     * Invoked by the {@link Radio}which is selected to update the property bound to the selected
     * parameter.
     */

    public void updateSelection(Object value)
    {
        getBinding("selected").setObject(value);

        _selection = value;
    }

    /**
     * Used by {@link Radio}components when rewinding to see if their value was submitted.
     */

    public boolean isSelected(int option)
    {
        return _selectedOption == option;
    }

    /**
     * @see org.apache.tapestry.AbstractComponent#prepareForRender(org.apache.tapestry.IRequestCycle)
     */
    protected void prepareForRender(IRequestCycle cycle)
    {
        super.prepareForRender(cycle);

        if (cycle.getAttribute(ATTRIBUTE_NAME) != null)
            throw new ApplicationRuntimeException(Tapestry.getMessage("RadioGroup.may-not-nest"),
                                                  this, null, null);

        cycle.setAttribute(ATTRIBUTE_NAME, this);

        _rendering = true;
        _nextOptionId = 0;
    }

    /**
     * @see org.apache.tapestry.AbstractComponent#cleanupAfterRender(org.apache.tapestry.IRequestCycle)
     */
    protected void cleanupAfterRender(IRequestCycle cycle)
    {
        super.cleanupAfterRender(cycle);

        _rendering = false;
        _selection = null;

        cycle.removeAttribute(ATTRIBUTE_NAME);
    }

    /**
     * @see org.apache.tapestry.form.AbstractFormComponent#renderFormComponent(org.apache.tapestry.IMarkupWriter,
     *      org.apache.tapestry.IRequestCycle)
     */
    protected void renderFormComponent(IMarkupWriter writer, IRequestCycle cycle)
    {
        _rewinding = false;

        // render script generating the onChange method
        PageRenderSupport pageRenderSupport = TapestryUtils.getPageRenderSupport(cycle, this);
        Map symbols = new HashMap();
        symbols.put( "id", getClientId() );
        getScript().execute(this, cycle, pageRenderSupport, symbols);

        // For rendering, the Radio components need to know what the current
        // selection is, so that the correct one can mark itself 'checked'.
        _selection = getBinding("selected").getObject();

        renderDelegatePrefix(writer, cycle);

        writer.begin(getTemplateTagName());

        renderInformalParameters(writer, cycle);
        
        if (getId() != null && !isParameterBound("id"))
                renderIdAttribute(writer, cycle);

        renderDelegateAttributes(writer, cycle);

        renderBody(writer, cycle);

        writer.end();

        renderDelegateSuffix(writer, cycle);

        getValidatableFieldSupport().renderContributions(this, writer, cycle);
    }

    /**
     * @see org.apache.tapestry.form.AbstractFormComponent#rewindFormComponent(org.apache.tapestry.IMarkupWriter,
     *      org.apache.tapestry.IRequestCycle)
     */
    protected void rewindFormComponent(IMarkupWriter writer, IRequestCycle cycle)
    {
        String value = cycle.getParameter(getName());

        if (value == null)
            _selectedOption = -1;
        else
            _selectedOption = Integer.parseInt(value);

        _rewinding = true;

        renderBody(writer, cycle);

        try
        {
            getValidatableFieldSupport().validate(this, writer, cycle, _selection);
        }
        catch (ValidatorException e)
        {
            getForm().getDelegate().record(e);
        }
    }

    /**
     * Injected.
     */
    public abstract ValidatableFieldSupport getValidatableFieldSupport();

    /**
     * @see org.apache.tapestry.form.AbstractFormComponent#isRequired()
     */
    public boolean isRequired()
    {
        return getValidatableFieldSupport().isRequired(this);
    }

    /**
     * This component can not take focus.
     */
    protected boolean getCanTakeFocus()
    {
        return false;
    }
}
