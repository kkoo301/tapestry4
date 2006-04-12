/*
 * Tapestry Web Application Framework
 * Copyright (c) 2000, 2001 by Howard Ship and Primix
 *
 * Primix
 * 311 Arsenal Street
 * Watertown, MA 02472
 * http://www.primix.com
 * mailto:hship@primix.com
 * 
 * This library is free software.
 * 
 * You may redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation.
 *
 * Version 2.1 of the license should be included with this distribution in
 * the file LICENSE, as well as License.html. If the license is not
 * included with this distribution, you may find a copy at the FSF web
 * site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
 * Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 */

package com.primix.tapestry.form;

import com.primix.tapestry.*;


/**
 *  Base class for implementing various types of text input fields.
 *  This includes {@link TextField} but also 
 *  forms the base for 
 *  {@link com.primix.tapestry.valid.AbstractValidatingTextField}.
 *
 *
 *  @author Howard Ship
 *  @version $Id$
 *  @since 1.0.2
 */


public abstract class AbstractTextField extends AbstractFormComponent
{
	private IBinding displayWidthBinding;
	
	private boolean staticDisplayWidth;
	private int displayWidthValue;
	
	private IBinding maximumLengthBinding;
	
	private boolean staticMaximumLength;
	private int maximumLengthValue;
	
	private IBinding hiddenBinding;
	
	private boolean staticHidden;
	private boolean hiddenValue;
	
	private IBinding disabledBinding;
	
	private String name;
	
	private static final String[] reservedNames =
	{ "type", "size", "maxlength", "value" };
	
	
	public String getName()
	{
		return name;
	}
	
	public IBinding getDisabledBinding()
	{
		return disabledBinding;
	}
	
	public IBinding getDisplayWidthBinding()
	{
		return displayWidthBinding;
	}
	
	public IBinding getHiddenBinding()
	{
		return hiddenBinding;
	}
	
	/**
	 *  Renders the form element, or responds when the form containing the element
	 *  is submitted (by checking {@link Form#isRewinding()}.
	 *
	 **/
	
	public void render(IResponseWriter writer, IRequestCycle cycle)
		throws RequestCycleException
	{
		boolean rewinding;
		IActionListener listener;
		String value;
		boolean disabled = false;
		int displayWidth;
		int maximumLength;
		boolean hidden = false;
		Form form;
		
		form = getForm(cycle);
		
		// It isn't enough to know whether the cycle in general is rewinding, need to know
		// specifically if the form which contains this component is rewinding.
		
		rewinding = form.isRewinding();
		
		// If the cycle is rewinding, but the form containing this field is not,
		// then there's no point in doing more work.
		
		if (!rewinding && cycle.isRewinding())
			return;
		
		// Used whether rewinding or not.
		
		name = form.getElementId(this);
		
		if (disabledBinding != null)
			disabled = disabledBinding.getBoolean();	
		
		if (rewinding)
		{
			if (!disabled)
			{
				value = cycle.getRequestContext().getParameter(name);
				
				updateValue(value);
			}
			
			return;
		}
		
		if (staticHidden)
			hidden = hiddenValue;
		else if (hiddenBinding != null)
			hidden = hiddenBinding.getBoolean();
		
		
		writer.beginEmpty("input");
		
		writer.attribute("type", hidden ? "password" : "text");
		
		if (disabled)
			writer.attribute("disabled");
		
		writer.attribute("name", name);
		
		if (displayWidthBinding != null)
		{
			if (staticDisplayWidth)
				displayWidth = displayWidthValue;
			else
				displayWidth = displayWidthBinding.getInt();
			
			writer.attribute("size", displayWidth);
		}
		
		if (maximumLengthBinding != null)
		{
			if (staticMaximumLength)
				maximumLength = maximumLengthValue;
			else
				maximumLength = maximumLengthBinding.getInt();
			
			writer.attribute("maxlength", maximumLength);
		}
		
		value = readValue();
		if (value != null)
			writer.attribute("value", value);
		
		generateAttributes(cycle, writer, reservedNames);
		
		writer.closeTag();
		
	}

	/**
	 *  Invoked by {@link #render(IResponseWriter writer, IRequestCycle cycle)}
	 *  when a value is obtained from the
	 *  {@link HttpServletRequest}.
	 *
	 */
	
	abstract protected void updateValue(String value);
	

	/**
	 *  Invoked by {@link #render(IResponseWriter writer, IRequestCycle cycle)}
	 *  when rendering a response.
	 *
	 *  @returns the current value for the field, as a String, or null.
	 */	
	
	abstract protected String readValue();
	
	public void setDisabledBinding(IBinding value)
	{
		disabledBinding = value;
	}
	
	public void setDisplayWidthBinding(IBinding value)
	{
		displayWidthBinding = value;
		
		staticDisplayWidth = value.isStatic();
		
		if (staticDisplayWidth)
			displayWidthValue = value.getInt();
	}
	
	public void setHiddenBinding(IBinding value)
	{
		hiddenBinding = value;
		
		staticHidden = value.isStatic();
		
		if (staticHidden)
			hiddenValue = value.getBoolean();
	}
	
	public void setMaximumLengthBinding(IBinding value)
	{
		maximumLengthBinding = value;
		
		staticMaximumLength = value.isStatic();
		
		if (staticMaximumLength)
			maximumLengthValue = value.getInt();
	}
	
	public IBinding getMaximumLengthBinding()
	{
		return maximumLengthBinding;
	}
}
