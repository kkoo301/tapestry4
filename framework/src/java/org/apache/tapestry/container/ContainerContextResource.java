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

package org.apache.tapestry.container;

import java.net.URL;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hivemind.Resource;
import org.apache.hivemind.util.AbstractResource;
import org.apache.hivemind.util.LocalizedResource;

/**
 * Implementation of {@link org.apache.hivemind.Resource}for resources found within a
 * {@link org.apache.tapestry.container.ContainerContext}.
 * 
 * @author Howard Lewis Ship
 * @since 3.1
 */

public class ContainerContextResource extends AbstractResource
{
    private static final Log LOG = LogFactory.getLog(ContainerContextResource.class);

    private ContainerContext _context;

    public ContainerContextResource(ContainerContext context, String path)
    {
        this(context, path, null);
    }

    public ContainerContextResource(ContainerContext context, String path, Locale locale)
    {
        super(path, locale);

        _context = context;
    }

    /**
     * Locates the resource using {@link LocalizedContainerContextResourceFinder}and
     * {@link ServletContext#getResource(java.lang.String)}.
     */

    public Resource getLocalization(Locale locale)
    {
        LocalizedContainerContextResourceFinder finder = new LocalizedContainerContextResourceFinder(
                _context);

        String path = getPath();
        LocalizedResource localizedResource = finder.resolve(path, locale);

        if (localizedResource == null)
            return null;

        String localizedPath = localizedResource.getResourcePath();
        Locale pathLocale = localizedResource.getResourceLocale();

        if (localizedPath == null)
            return null;

        if (path.equals(localizedPath))
            return this;

        return new ContainerContextResource(_context, localizedPath, pathLocale);
    }

    public URL getResourceURL()
    {
        return _context.getResource(getPath());
    }

    public String toString()
    {
        return "context:" + getPath();
    }

    public int hashCode()
    {
        return 2387 & getPath().hashCode();
    }

    protected Resource newResource(String path)
    {
        return new ContainerContextResource(_context, path);
    }

}