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

package com.primix.vlib.ejb;

import javax.ejb.*;
import java.rmi.*;
import java.util.*;

/** 
 *  Defines the remove interface for an entity which can download and upload
 *  a subset of its properties as a {@link Map}.
 *
 *  @version $Id$
 *  @author Howard Ship
 *
 */

public interface IEntityBean extends EJBObject
{
	/**
	 *  Returns the simple attributes of the entity as a Map.
	 *
	 */

	public Map getEntityAttributes() throws RemoteException;

	/**
	 *  Updates some or all of the properties of the entity.
	 *
	 */

	public void updateEntityAttributes(Map data) throws RemoteException;
}