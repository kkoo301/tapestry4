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

package org.apache.tapestry.describe;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry.IMarkupWriter;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.apache.tapestry.describe.HTMLDescriptionReceiver}and
 * {@link org.apache.tapestry.describe.HTMLDescriberImpl}.
 * 
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
@Test(sequential = true)
public class HTMLDescriptionReceiverTest extends BaseDescribeTestCase
{
    protected DescribableStrategy newStrategy()
    {
        return newMock(DescribableStrategy.class);
    }

    private void trainForTitle(IMarkupWriter writer, String title)
    {
        writer.begin("div");
        writer.attribute("class", "described-object-title");
        writer.print(title);
        writer.end();
        writer.println();

        writer.begin("table");
        writer.attribute("class", "described-object");
        writer.println();
    }

    private void trainForSection(IMarkupWriter writer, String section)
    {
        writer.begin("tr");
        writer.attribute("class", "section");
        writer.begin("th");
        writer.attribute("colspan", 2);
        writer.print(section);
        writer.end("tr");
        writer.println();
    }

    private void trainForKeyValue(IMarkupWriter writer, String key, String value, boolean even)
    {
        writer.begin("tr");
        writer.attribute("class", even ? "even" : "odd");
        writer.begin("th");
        if (key != null)
            writer.print(key);
        writer.end();
        writer.begin("td");
        writer.print(value);
        writer.end("tr");
        writer.println();
    }

    private void trainForNestedKeyValue(IMarkupWriter writer, String key, String value, boolean even)
    {
        writer.begin("tr");
        writer.attribute("class", even ? "even" : "odd");
        writer.begin("th");
        if (key != null)
            writer.print(key);
        writer.end();
        writer.begin("td");
        writer.print(value);
        writer.println();
        writer.end("tr");
        writer.println();
    }

    public void test_Set_Title_Twice_Fails()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = newStrategy();

        replay();

        DescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        dr.title("First Title");

        try
        {
            dr.title("Second Title");
            unreachable();

        }
        catch (IllegalStateException ex)
        {
            assertEquals(DescribeMessages.setTitleOnce(), ex.getMessage());
        }

        verify();
    }

    public void test_Set_Section_Before_Title_Fails()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = newStrategy();

        replay();

        DescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        try
        {
            dr.section("Section");
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertEquals(DescribeMessages.mustSetTitleBeforeSection(), ex.getMessage());
        }

        verify();
    }

    public void test_Int_Property()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = newStrategy();

        trainForTitle(writer, "Object Title");
        trainForSection(writer, "Section");
        trainForKeyValue(writer, "intProperty", "97", true);

        replay();

        DescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        dr.title("Object Title");
        dr.section("Section");
        dr.property("intProperty", 97);

        verify();
    }

    public void test_Properties_Without_Section()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = newStrategy();

        trainForTitle(writer, "Object Title");
        trainForKeyValue(writer, "first", "1", true);
        trainForKeyValue(writer, "second", "2", false);

        replay();

        DescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        dr.title("Object Title");
        dr.property("first", 1);
        dr.property("second", 2);

        verify();

        trainForSection(writer, "Section 1");
        trainForKeyValue(writer, "s1", "1", true);
        trainForKeyValue(writer, "s2", "2", false);

        replay();

        dr.section("Section 1");
        dr.property("s1", 1);
        dr.property("s2", 2);

        verify();
    }

    public void test_Finish_With_Properties()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = newStrategy();

        trainForTitle(writer, "Object Title");
        trainForKeyValue(writer, "first", "1", true);

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        dr.title("Object Title");
        dr.property("first", 1);

        verify();

        writer.end("table");
        writer.println();

        replay();

        dr.finishUp(null);

        verify();
    }

    public void test_Finish_No_Properties_No_Title()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = newStrategy();

        String object = "Fred";

        writer.print("Fred");
        writer.println();

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        dr.finishUp(object);

        verify();
    }

    public void test_Finish_No_Properties_With_Title()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = newStrategy();

        String object = "Fred";

        writer.print("Fred Title");
        writer.println();

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        dr.title("Fred Title");

        dr.finishUp(object);

        verify();
    }

    public void test_Array()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = new NoOpStrategy();

        Object[] array = new Object[]
        { "Fred", "Barney" };

        trainForTitle(writer, "Array");
        trainForNestedKeyValue(writer, "list", "Fred", true);
        trainForNestedKeyValue(writer, null, "Barney", false);

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        dr.title("Array");
        dr.array("list", array);

        verify();
    }

    public void test_Collection()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = new NoOpStrategy();

        Object[] array = new Object[]
        { "Fred", "Barney" };
        Collection collection = Arrays.asList(array);

        trainForTitle(writer, "Collection");
        trainForNestedKeyValue(writer, "list", "Fred", true);
        trainForNestedKeyValue(writer, null, "Barney", false);

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);

        dr.title("Collection");
        dr.collection("list", collection);

        verify();
    }

    public void test_Array_Null_And_Empty()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy strategy = newStrategy();

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, strategy);

        dr.title("Array");
        dr.array("null", null);
        dr.array("empty", new Object[0]);

        verify();
    }

    public void test_Collection_Null_And_Empty()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy strategy = newStrategy();

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, strategy);

        dr.title("Collection");
        dr.collection("null", null);
        dr.collection("empty", Collections.EMPTY_LIST);

        verify();
    }

    public void test_Collection_Concurrent_Modification()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy adapter = new NoOpStrategy();
        
        final List list = new ArrayList();
        list.add("Fred");
        list.add("Barney");
        
        trainForTitle(writer, "Collection");
        trainForNestedKeyValue(writer, "list", "Fred", true);
        trainForNestedKeyValue(writer, null, "Barney", false);
        
        replay();
        
        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, adapter);
        
        dr.title("Collection");
        dr.collection("list", list);
        
        verify();
    }
    
    public void test_Scalar_Properties()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy strategy = newStrategy();

        trainForTitle(writer, "Scalars");
        trainForKeyValue(writer, "boolean", "true", true);
        trainForKeyValue(writer, "byte", "22", false);
        trainForKeyValue(writer, "char", "*", true);
        trainForKeyValue(writer, "short", "-8", false);
        trainForKeyValue(writer, "int", "900", true);
        trainForKeyValue(writer, "long", "200020", false);
        trainForKeyValue(writer, "float", "3.14", true);
        trainForKeyValue(writer, "double", "-2.7", false);

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, strategy);

        dr.title("Scalars");
        dr.property("boolean", true);
        dr.property("byte", (byte) 22);
        dr.property("char", '*');
        dr.property("short", (short) -8);
        dr.property("int", 900);
        dr.property("long", 200020l);
        dr.property("float", (float) 3.14);
        dr.property("double", -2.7);

        verify();
    }

    public void test_Null_Root()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy strategy = newStrategy();

        writer.print(HTMLDescriptionReceiver.NULL_VALUE);

        replay();

        RootDescriptionReciever dr = new HTMLDescriptionReceiver(writer, strategy);

        dr.describe(null);

        verify();
    }

    public void test_Null_Property()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy strategy = newStrategy();

        trainForTitle(writer, "Null Property");
        trainForKeyValue(writer, "null", HTMLDescriptionReceiver.NULL_VALUE, true);

        replay();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, strategy);

        dr.title("Null Property");
        dr.property("null", null);

        verify();

    }

    public void test_HTML_Describer()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy strategy = new NoOpStrategy();

        RootDescriptionReceiverFactory factory = newReceiverFactory();

        trainGetReciever(factory, writer, new HTMLDescriptionReceiver(writer, strategy));

        String object = "Tapestry";

        writer.print("Tapestry");
        writer.println();

        replay();

        HTMLDescriberImpl d = new HTMLDescriberImpl();
        d.setReceiverFactory(factory);

        d.describeObject(object, writer);

        verify();
    }

    protected void trainGetReciever(RootDescriptionReceiverFactory factory, IMarkupWriter writer,
            RootDescriptionReciever receiver)
    {
        expect(factory.newRootDescriptionReceiver(writer)).andReturn(receiver);
    }

    protected RootDescriptionReceiverFactory newReceiverFactory()
    {
        return newMock(RootDescriptionReceiverFactory.class);
    }
    
    public void test_Describe_Alternate()
    {
        IMarkupWriter writer = newWriter();
        DescribableStrategy strategy = newStrategy();

        Object alternate = new Object();

        HTMLDescriptionReceiver dr = new HTMLDescriptionReceiver(writer, strategy);

        strategy.describeObject(alternate, dr);

        replay();

        dr.describeAlternate(alternate);

        verify();
    }
}