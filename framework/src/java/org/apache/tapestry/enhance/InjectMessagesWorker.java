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

package org.apache.tapestry.enhance;

import java.lang.reflect.Modifier;

import org.apache.hivemind.ErrorLog;
import org.apache.hivemind.Messages;
import org.apache.hivemind.service.MethodSignature;
import org.apache.tapestry.services.ComponentMessagesSource;
import org.apache.tapestry.spec.IComponentSpecification;

/**
 * Injects the read-only {@link org.apache.tapestry.IComponent#getMessages() messages}property into
 * all components.
 * 
 * @author Howard M. Lewis Ship
 * @since 3.1
 */
public class InjectMessagesWorker implements EnhancementWorker
{
    final String MESSAGES_PROPERTY = "messages";

    private ErrorLog _errorLog;

    private ComponentMessagesSource _componentMessagesSource;

    final String METHOD_CODE = "{\nif (_$messages == null)\n"
            + "  _$messages = _$componentMessagesSource.getMessages(this);\n"
            + "return _$messages;\n}\n";

    final MethodSignature METHOD_SIGNATURE = new MethodSignature(Messages.class, "getMessages",
            null, null);

    public void performEnhancement(EnhancementOperation op, IComponentSpecification spec)
    {
        try
        {
            op.claimProperty(MESSAGES_PROPERTY);

            op.addField(
                    "_$componentMessagesSource",
                    ComponentMessagesSource.class,
                    _componentMessagesSource);
            op.addField("_$messages", Messages.class);

            op.addMethod(Modifier.PUBLIC, METHOD_SIGNATURE, METHOD_CODE);
        }
        catch (Exception ex)
        {
            _errorLog.error(EnhanceMessages.errorAddingProperty(MESSAGES_PROPERTY, op
                    .getBaseClass(), ex), null, ex);
        }
    }

    public void setComponentMessagesSource(ComponentMessagesSource componentMessagesSource)
    {
        _componentMessagesSource = componentMessagesSource;
    }

    public void setErrorLog(ErrorLog errorLog)
    {
        _errorLog = errorLog;
    }
}