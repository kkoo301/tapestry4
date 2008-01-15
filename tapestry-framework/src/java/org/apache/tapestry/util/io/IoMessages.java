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

package org.apache.tapestry.util.io;

import org.apache.hivemind.impl.MessageFormatter;

/**
 * 
 * @author hls
 */
final class IoMessages
{
    private static final MessageFormatter _formatter = new MessageFormatter(IoMessages.class);

    /* defeat instantiation */
    private IoMessages() { }
    
    static String encodeFailure(Object object, Throwable cause)
    {
        return _formatter.format("encode-failure", object, cause);
    }

    static String decodeFailure(Throwable cause)
    {
        return _formatter.format("decode-failure", cause);
    }

    static String unknownPrefix(String prefix)
    {
        return _formatter.format("unknown-prefix", prefix);
    }
}
