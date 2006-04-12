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

// Appease Javadoc

import com.primix.tapestry.util.*;

/**
 *  Used by a {@link PropertySelection} to provide labels for options.
 *
 *  <p>The component requires three different representations
 *  of each option:
 *  <ul>
 *  <li>The option value, a Java object that will eventually be assigned to
 *  a property
 *  <li>The label, a String which is incorprated into the HTML to identify the
 *  option to the user
 *  <li>The value, a String which is used to represent the option as the value
 *  of the &lt;option&gt; or &lt;input type=radio&gt; generated by the
 *  {@link PropertySelection}.
 *  </ul>
 *
 *  <p>The option is usually either an {@link Enum} (see {@link EnumPropertySelectionModel})
 *  or some kind of business object.  The label is often a property of the
 *  option object (for example, for a list of customers, it could be the customer name).
 *
 *  <p>It should be easy to convert between the value and the option.  It may simply
 *  be an index into an array.  For business objects, it is often the primary key
 *  of the object, expressed as a String.
 *
 *  @version $Id$
 *  @author Howard Ship
 *
 */

public interface IPropertySelectionModel
{
	/**
	 *  Returns the number of possible options.
	 *
	 */

	public int getOptionCount();

	/**
	 *  Returns one possible option.
	 *
	 */

	public Object getOption(int index);

	/**
	 *  Returns the label for an option.  It is the responsibility of the
	 *  adaptor to make this value localized.
	 *
	 */

	public String getLabel(int index);

	/**
	 *  Returns a String used to represent the option in the HTML (as the
	 *  value of an &lt;option&gt; or &lt;input type=radio&gt;.
	 *
	 */

	public String getValue(int index);

	/**
	 *  Returns the option corresponding to a value.  This is used when
	 *  interpreting submitted form parameters.
	 *
	 */

	public Object translateValue(String value);
}