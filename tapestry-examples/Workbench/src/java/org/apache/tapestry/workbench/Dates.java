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
package org.apache.tapestry.workbench;

import java.util.Date;

import org.apache.tapestry.annotations.EventListener;
import org.apache.tapestry.html.BasePage;
import org.apache.tapestry.services.ResponseBuilder;
import org.apache.tapestry.valid.IValidationDelegate;


/**
 * Sample page.
 */
public abstract class Dates extends BasePage
{
    public abstract ResponseBuilder getBuilder();
    
    public abstract void setEndDate(Date date);
    
    public abstract Date getStartDate();
    
    public abstract IValidationDelegate getDelegate();
    
    @EventListener(targets = "startDate", events = {"onchange"}, submitForm = "dateForm", focus = false)
    public void startChanged()
    {
        if (getStartDate() == null)
            return;
        
        getDelegate().clearErrors();
        
        setEndDate(new Date(getStartDate().getTime() + (1000 * 60 * 60 * 24 * 2)));
        getBuilder().updateComponent("endDate");
    }
}