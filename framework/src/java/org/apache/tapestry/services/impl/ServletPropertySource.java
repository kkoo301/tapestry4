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

package org.apache.tapestry.services.impl;

import javax.servlet.ServletConfig;

import org.apache.tapestry.engine.IPropertySource;

/**
 * Obtains property values from {@link javax.servlet.ServletConfig} for
 * the application servlet.
 *
 * @author Howard Lewis Ship
 * @since 3.1
 */
public class ServletPropertySource implements IPropertySource
{
    private ServletConfig _servletConfig;

    public String getPropertyValue(String propertyName)
    {
        return _servletConfig.getInitParameter(propertyName);
    }

    public void setServletConfig(ServletConfig config)
    {
        _servletConfig = config;
    }
}