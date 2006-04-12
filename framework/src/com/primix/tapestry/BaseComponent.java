/*
 * Tapestry Web Application Framework
 * Copyright (c) 2000-2001 by Howard Lewis Ship
 *
 * Howard Lewis Ship
 * http://sf.net/projects/tapestry
 * mailto:hship@users.sf.net
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
 * but WITHOUT ANY WARRANTY; without even the implied waranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 */

package com.primix.tapestry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;

import com.primix.tapestry.parse.ComponentTemplate;
import com.primix.tapestry.parse.TemplateToken;
import com.primix.tapestry.parse.TokenType;
import com.primix.tapestry.spec.ComponentSpecification;

/**
 * Base implementation for most components that use an HTML template.
 *
 * @author Howard Ship
 * @version $Id$
 */

public class BaseComponent extends AbstractComponent
{
	protected static final int OUTER_INIT_SIZE = 5;
	protected int outerCount = 0;
	protected IRender[] outer;

	private static final Category CAT = Category.getInstance(BaseComponent.class);

	/**
	 *  Adds an element as an outer element for the receiver.  Outer
	 *  elements are elements that should be directly rendered by the
	 *  receiver's <code>render()</code> method.  That is, they are
	 *  top-level elements on the HTML template.
	 *
	 */

	protected void addOuter(IRender element)
	
	{
		if (outer == null)
		{
			outer = new IRender[OUTER_INIT_SIZE];
			outer[0] = element;

			outerCount = 1;
			return;
		}

		// No more room?  Make the array bigger.

		if (outerCount == outer.length)
		{
			IRender[] newOuter;

			newOuter = new IRender[outer.length * 2];

			System.arraycopy(outer, 0, newOuter, 0, outerCount);

			outer = newOuter;
		}

		outer[outerCount++] = element;
	}

	/**
	 *
	 * Reads the receiver's template and figures out which elements wrap which
	 * other elements.
	 *
	 * <P>This is coded as a single, big, ugly method for efficiency.
	 */

	protected void readTemplate(IPageLoader loader) throws PageLoaderException
	{
		IComponent component;
		ComponentTemplate componentTemplate;
		Set seenIds = new HashSet();
		IPageSource pageSource = loader.getEngine().getPageSource();

		if (CAT.isDebugEnabled())
			CAT.debug(this +" reading template");

		try
		{
			ITemplateSource source = loader.getTemplateSource();
			componentTemplate = source.getTemplate(this);
		}
		catch (ResourceUnavailableException ex)
		{
			throw new PageLoaderException(
				Tapestry.getString("BaseComponent.unable-to-obtain-template"),
				this,
				ex);
		}

		int count = componentTemplate.getTokenCount();

		// The stack can never be as large as the number of tokens, so this is safe.

		IComponent[] componentStack = new IComponent[count];
		IComponent activeComponent = null;
		int stackx = 0;
		boolean check = true;

		for (int i = 0; i < count; i++)
		{
			TemplateToken token = componentTemplate.getToken(i);
			TokenType type = token.getType();

			if (type == TokenType.TEXT)
			{
				// Get a render for the token.  This allows the token and the render
				// to be shared across sessions.

				IRender element = token.getRender();

				if (activeComponent == null)
					addOuter(element);
				else
				{
					if (check)
					{
						check = false;

						if (!activeComponent.getSpecification().getAllowBody())
							throw new BodylessComponentException(activeComponent);
					}

					activeComponent.addWrapped(element);
				}

				continue;
			}

			// On an OPEN, we get the name

			if (type == TokenType.OPEN)
			{
				String id = token.getId();

				try
				{
					component = getComponent(id);

					check = true;
				}
				catch (NoSuchComponentException ex)
				{
					throw new PageLoaderException(
						Tapestry.getString(
							"BaseComponent.undefined-embedded-component",
							getExtendedId(),
							id),
						this,
						ex);
				}

				// Make sure the template contains each component only once.

				if (seenIds.contains(id))
					throw new PageLoaderException(
						Tapestry.getString(
							"BaseComponent.multiple-component-references",
							getExtendedId(),
							id),
						this);

				seenIds.add(id);

				if (activeComponent == null)
					addOuter(component);
				else
				{
					if (check)
					{
						check = false;

						// If you use a <jwc> tag in the template, you can get here.
						// If you use a normal tag and a jwcid attribute, the
						// body is automatically editted out.

						if (!activeComponent.getSpecification().getAllowBody())
							throw new BodylessComponentException(activeComponent);
					}

					activeComponent.addWrapped(component);
				}

				addStaticBindings(component, token.getAttributes(), pageSource);

				componentStack[stackx++] = activeComponent;

				activeComponent = component;

				continue;
			}

			if (type == TokenType.CLOSE)
			{
				try
				{
					activeComponent = componentStack[--stackx];

					check = true;
				}
				catch (IndexOutOfBoundsException e)
				{
					// This is now almost impossible to reach, because the
					// TemplateParser does a great job of checking for most of these cases.

					throw new PageLoaderException(
						Tapestry.getString("BaseComponent.unbalanced-close-tags"),
						this);
				}
			}
		}

		// This is also pretty much unreachable, and the message is kind of out
		// of date, too.

		if (stackx != 0)
			throw new PageLoaderException(
				Tapestry.getString("BaseComponent.unbalance-open-tags"),
				this);

		checkAllComponentsReferenced(seenIds);

		if (CAT.isDebugEnabled())
			CAT.debug(this +" finished reading template");
	}

	/**
	 * Adds static bindings for any attrributes specified in the HTML
	 * template, skipping any that are reserved (explicitly, or
	 * because they match a formal parameter name).
	 *
	 */

	private void addStaticBindings(
		IComponent component,
		Map attributes,
		IPageSource pageSource)
	{
		if (attributes == null || attributes.isEmpty())
			return;

		ComponentSpecification spec = component.getSpecification();

		boolean rejectInformal = !spec.getAllowInformalParameters();

		Iterator i = attributes.entrySet().iterator();

		while (i.hasNext())
		{
			Map.Entry e = (Map.Entry) i.next();

			String name = (String) e.getKey();

			// If matches a formal parameter name, allow it to be set
			// unless there's already a binding.

			boolean isFormal = (spec.getParameter(name) != null);

			if (isFormal)
			{
				if (component.getBinding(name) != null)
					continue;
			}
			else
			{
				// Skip informal parameters if the component doesn't allow them.

				if (rejectInformal)
					continue;

				// If the name is reserved (matches a formal parameter
				// or reserved name, caselessly), then skip it.

				if (spec.isReservedParameterName(name))
					continue;
			}

			String value = (String) e.getValue();

			IBinding binding = pageSource.getStaticBinding(value);

			component.setBinding(name, binding);
		}
	}

	private void checkAllComponentsReferenced(Set seenIds)
		throws PageLoaderException
	{
		Set ids = null;

		// First, contruct a modifiable copy of the ids of all expected components
		// (that is, components declared in the specification).

		Map components = getComponents();

		// Occasionally, a component will have a template but no embedded components.

		if (components == null)
			ids = Collections.EMPTY_SET;
		else
			ids = components.keySet();

		// If the seen ids ... ids referenced in the template, matches
		// all the ids in the specification then we're fine.

		if (seenIds.containsAll(ids))
			return;

		// Create a modifiable copy.  Remove the ids that are referenced in
		// the template.  The remainder are worthy of note.

		ids = new HashSet(ids);
		ids.removeAll(seenIds);

		int count = ids.size();

		String key =
			(count == 1)
				? "BaseComponent.missing-component-spec-single"
				: "BaseComponent.missing-component-spec-multi";

		StringBuffer buffer =
			new StringBuffer(Tapestry.getString(key, getExtendedId()));

		Iterator i = ids.iterator();
		int j = 1;

		while (i.hasNext())
		{
			if (j == 1)
				buffer.append(' ');
			else if (j == count)
			{
				buffer.append(' ');
				buffer.append(Tapestry.getString("BaseComponent.and"));
				buffer.append(' ');
			}
			else
				buffer.append(", ");

			buffer.append(i.next());

			j++;
		}

		buffer.append('.');

		throw new PageLoaderException(buffer.toString(), this);
	}

	/**
	 *  Renders the top level components contained by the receiver.
	 *
	 */

	public void render(IResponseWriter writer, IRequestCycle cycle)
		throws RequestCycleException
	{
		if (CAT.isDebugEnabled())
			CAT.debug("Begin render " + getExtendedId());

		for (int i = 0; i < outerCount; i++)
			outer[i].render(writer, cycle);

		if (CAT.isDebugEnabled())
			CAT.debug("End render " + getExtendedId());
	}

	/**
	 *  Loads the template for the component, and invokes
	 *  {@link #finishLoad()}.  Subclasses must invoke this method first,
	 *  before adding any additional behavior.
	 *
	 */

	public void finishLoad(
		IPageLoader loader,
		ComponentSpecification specification)
		throws PageLoaderException
	{
		readTemplate(loader);

		finishLoad();
	}
}