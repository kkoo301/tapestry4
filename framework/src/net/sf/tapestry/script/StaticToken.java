//
// Tapestry Web Application Framework
// Copyright (c) 2000-2002 by Howard Lewis Ship
//
// Howard Lewis Ship
// http://sf.net/projects/tapestry
// mailto:hship@users.sf.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE, as well as License.html. If the license is not
// included with this distribution, you may find a copy at the FSF web
// site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
// Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package net.sf.tapestry.script;

import net.sf.tapestry.ScriptException;
import net.sf.tapestry.ScriptSession;

/**
 *  A token for static portions of the template.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *
 **/

class StaticToken implements IScriptToken
{
    private String text;

    StaticToken(String text)
    {
        this.text = text;
    }

    /**
     *  Writes the text to the writer.
     *
     **/

    public void write(StringBuffer buffer, ScriptSession session)
        throws ScriptException
    {
        buffer.append(text);
    }

    public void addToken(IScriptToken token)
    {
        // Should never be invoked.
    }
}