/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Tapestry" 
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" 
 *    or "Tapestry", nor may "Apache" or "Tapestry" appear in their 
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE TAPESTRY CONTRIBUTOR COMMUNITY
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.tapestry.vlib.pages;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;

import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.event.PageRenderListener;
import org.apache.tapestry.vlib.Protected;
import org.apache.tapestry.vlib.VirtualLibraryEngine;
import org.apache.tapestry.vlib.Visit;
import org.apache.tapestry.vlib.ejb.IOperations;

/**
 *  Collects information for a new book.  
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 * 
 **/

public abstract class NewBook extends Protected implements PageRenderListener
{

    public abstract Map getAttributes();

    public abstract void setAttributes(Map attributes);

    public abstract String getPublisherName();

    public void addBook(IRequestCycle cycle)
    {
        Map attributes = getAttributes();

        Integer publisherPK = (Integer) attributes.get("publisherPK");
        String publisherName = getPublisherName();

        if (publisherPK == null && Tapestry.isNull(publisherName))
        {
            setErrorField("inputPublisherName", getString("need-publisher-name"));
            return;
        }

        if (publisherPK != null && !Tapestry.isNull(publisherName))
        {
            setErrorField("inputPublisherName", getString("leave-publisher-name-empty"));
            return;
        }

        if (isInError())
            return;

        Visit visit = (Visit) getVisit();
        Integer userPK = visit.getUserPK();
        VirtualLibraryEngine vengine = visit.getEngine();

        attributes.put("ownerPK", userPK);
        attributes.put("holderPK", userPK);

        int i = 0;
        while (true)
        {
            try
            {

                IOperations operations = vengine.getOperations();

                if (publisherPK != null)
                    operations.addBook(attributes);
                else
                {
                    operations.addBook(attributes, publisherName);

                    // Clear the app's cache of info; in this case, known publishers.

                    visit.clearCache();
                }

                break;
            }
            catch (CreateException ex)
            {
                setError("Error adding book: " + ex.getMessage());
                return;
            }
            catch (RemoteException ex)
            {
                vengine.rmiFailure("Remote exception adding new book.", ex, i++);
            }
        }

        // Success.  First, update the message property of the return page.

        MyLibrary myLibrary = (MyLibrary) cycle.getPage("MyLibrary");

        myLibrary.setMessage(formatString("added-book", attributes.get("title")));

		myLibrary.activate(cycle);
    }

    public void pageBeginRender(PageEvent event)
    {
        if (getAttributes() == null)
        {
            Map attributes = new HashMap();

            // Setup defaults for the new book.

            attributes.put("lendable", Boolean.TRUE);

            setAttributes(attributes);
        }
    }

    public void pageEndRender(PageEvent event)
    {
    }

}