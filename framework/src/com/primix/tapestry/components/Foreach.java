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

package com.primix.tapestry.components;

import com.primix.tapestry.util.*;
import java.util.*;
import com.primix.tapestry.*;

/**
 *  Repeatedly renders its wrapped contents while iterating through
 *  a list of values.
 *
 *
 * <table border=1>
 * <tr> 
 *    <th>Parameter</th>
 *    <th>Type</th>
 *    <th>Read / Write</th>
 *    <th>Required</th> 
 *    <th>Default</th>
 *    <th>Description</th>
 * </tr>
 *
 * <tr>
 *  <td>source</td>
 *  <td>{@link Iterator}
 *		<br>{@link Collection}
 *      <br>java.lang.Object[]
 *  </td>
 *  <td>R</th>
 *  <td>no</td>
 *  <td>&nbsp;</td>
 *  <td>The source of objects to be iterated, which may be a Collection,
 *  an Iterator or an array of Objects.  If unbound, or the binding
 *  is null, or bound to an unusable value (one not in the list), 
 *  then no iteration takes place.</td>
 * </tr>
 *
 * <tr>
 *  <td>value</td>
 *  <td>java.lang.Object</td>
 *  <td>W</td>
 *  <td>no</td>
 *  <td>&nbsp;</td>
 *  <td>Used to update the current value on each iteration.
 *  <p>Alternate, wrapped components may access the value via
 *  the {@link #getValue() value property}.</td>
 *  </tr>
 *
 * <tr>
 *	<td>index</td>
 * 	<td>int</td>
 *	<td>W</td>
 *	<td>no</td>
 *	<td>&nbsp;</td>
 *	<td>Used to store the index of the current value within the stream of
 * elements provided by the source parameter.  The index parameter is
 * explicitly updated <em>before</em> the value parameter.
 *
 *	</td> 
 *  </tr>
 *
 *  <tr>
 *		<td>element</td>
 *		<td>{@link String}</td>
 *		<td>R</td>
 *		<td>no</td>
 *		<td>&nbsp;</td>
 *		<td>If specified, then the component acts like an {@link Any}, emitting an open
 *		and close tag before and after each iteration. 
 *		Most often, the element is "tr" when the Foreach is part of an HTML
 *		table.  Any informal parameters
 *		are applied to the tag.  If no element is specified, informal parameters
 *		are ignored.
 *		</td>
 * </tr>
 *
 * </table>
 *
 * <p>Informal parameters are allowed.  A body is allowed (and expected).
 *
 *  @author Howard Ship
 *  @version $Id$
 */

public class Foreach extends AbstractComponent
{
	private IBinding sourceBinding;
	private IBinding valueBinding;
	private IBinding indexBinding;
	private IBinding elementBinding;
	private String staticElement;

	private Object value;
	private boolean rendering;

	public IBinding getSourceBinding()
	{
		return sourceBinding;
	}

	public IBinding getIndexBinding()
	{
		return indexBinding;
	}

	public void setIndexBinding(IBinding value)
	{
		indexBinding = value;
	}

	/** @since 1.0.4 **/

	public void setElementBinding(IBinding value)
	{
		elementBinding = value;

		if (value.isStatic())
			staticElement = value.getString();
	}

	/** @since 1.0.4 **/

	public IBinding getElementBinding()
	{
		return elementBinding;
	}

	/**
	 *  Gets the source binding and returns an {@link Iterator}
	 *  representing
	 *  the values identified by the source.  Returns an empty {@link Iterator}
	 *  if the binding, or the binding value, is null.
	 *
	 *  <p>Invokes {@link Tapestry#coerceToIterator(Object)} to perform
	 *  the actual conversion.
	 *
	 */

	protected Iterator getSourceData() throws RequestCycleException
	{

		if (sourceBinding == null)
			return null;

		Object rawValue = sourceBinding.getObject();

		return Tapestry.coerceToIterator(rawValue);
	}

	public IBinding getValueBinding()
	{
		return valueBinding;
	}

	/**
	 *  Gets the source binding and iterates through
	 *  its values.  For each, it updates the value binding and render's its wrapped elements.
	 *
	 */

	public void render(IResponseWriter writer, IRequestCycle cycle)
		throws RequestCycleException
	{
		Iterator dataSource = getSourceData();

		// The dataSource was either not convertable, or was empty.

		if (dataSource == null)
			return;

		String element = null;

		if (elementBinding != null && !cycle.isRewinding())
		{
			if (staticElement == null)
				element = (String) elementBinding.getObject("element", String.class);
			else
				element = staticElement;
		}

		try
		
			{
			rendering = true;
			value = null;
			int i = 0;

			boolean hasNext = dataSource.hasNext();

			while (hasNext)
			{
				value = dataSource.next();
				hasNext = dataSource.hasNext();

				if (indexBinding != null)
					indexBinding.setInt(i);

				if (valueBinding != null)
					valueBinding.setObject(value);

				if (element != null)
				{
					writer.begin(element);
					generateAttributes(writer, cycle);
				}

				renderWrapped(writer, cycle);

				if (element != null)
					writer.end();

				i++;
			}
		}
		finally
		{
			value = null;
			rendering = false;
		}
	}

	public void setSourceBinding(IBinding value)
	{
		sourceBinding = value;
	}

	public void setValueBinding(IBinding value)
	{
		valueBinding = value;
	}

	/**
	 *  Returns the most recent value extracted from the source parameter.
	 *
	 *  @throws RenderOnlyPropertyException is the Foreach is not currently rendering.
	 *
	 */

	public Object getValue()
	{
		if (!rendering)
			throw new RenderOnlyPropertyException(this, "value");

		return value;
	}

}