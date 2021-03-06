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

package org.apache.tapestry.resolver;

import org.apache.commons.logging.Log;
import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.Location;
import org.apache.hivemind.Resource;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.engine.ISpecificationSource;
import org.apache.tapestry.services.ClassFinder;
import org.apache.tapestry.spec.IComponentSpecification;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

import java.net.URL;

/**
 * Tests for {@link org.apache.tapestry.resolver.ComponentSpecificationResolverImpl}.
 * 
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
@Test
public class TestComponentSpecificationResolver extends AbstractSpecificationResolverTestCase
{
    private void trainIsDeprecated(IComponentSpecification spec,
            boolean isDeprecated)
    {
        expect(spec.isDeprecated()).andReturn(isDeprecated);
    }

    protected ISpecificationSource newSource(Resource resource, IComponentSpecification spec)
    {
        ISpecificationSource source = newMock(ISpecificationSource.class);

        expect(source.getComponentSpecification(resource)).andReturn(spec);

        return source;
    }

    protected ISpecificationSource newSource(INamespace framework)
    {
        ISpecificationSource source = newMock(ISpecificationSource.class);

        expect(source.getFrameworkNamespace()).andReturn(framework);

        return source;
    }

    private ISpecificationResolverDelegate newDelegate(IRequestCycle cycle, INamespace namespace,
            String type, IComponentSpecification spec)
    {
        ISpecificationResolverDelegate delegate = newMock(ISpecificationResolverDelegate.class);
        checkOrder(delegate, false);
        
        expect(delegate.findComponentSpecification(cycle, namespace, type)).andReturn(spec).anyTimes();

        return delegate;
    }
    
    public void test_Not_Found_In_Any_Namespace()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();
        
        INamespace namespace = newMock(INamespace.class);
        
        ApplicationRuntimeException exception = new ApplicationRuntimeException("");

        expect(namespace.getChildNamespace("invalid")).andThrow(exception);
        
        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();

        try
        {
            resolver.resolve(cycle, namespace, "invalid:MyComponent", l);
            unreachable();
        }
        catch (ApplicationRuntimeException ex)
        {
            assertSame(ex.getCause(), exception);
        }

        verify();
    }    

    public void test_Found_In_Namespace()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();
        
        IComponentSpecification spec = newSpec();
        INamespace namespace = newMock(INamespace.class);

        expect(namespace.containsComponentType("MyComponent")).andReturn(true);

        expect(namespace.getComponentSpecification("MyComponent")).andReturn(spec);

        trainIsDeprecated(spec, false);
        
        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();

        resolver.resolve(cycle, namespace, "MyComponent", l);

        assertSame(spec, resolver.getSpecification());
        assertSame(namespace, resolver.getNamespace());

        verify();
    }

    public void test_Deprecated()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();
        
        IComponentSpecification spec = newSpec();
        INamespace namespace = newMock(INamespace.class);
        
        expect(namespace.containsComponentType("MyComponent")).andReturn(true);
        
        expect(namespace.getComponentSpecification("MyComponent")).andReturn(spec);
        
        trainIsDeprecated(spec, true);

        Log log = newMock(Log.class);
        
        log.warn(startsWith("Component 'MyComponent' ("));
        // at classpath:/org/apache/tapestry/resolver/TestComponentSpecificationResolver, line 1) is deprecated, and will likely
        // be removed in a later release. Consult its documentation to find a replacement component.");
        
        replay();
        
        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setLog(log);
        
        resolver.resolve(cycle, namespace, "MyComponent", l);
        
        assertSame(spec, resolver.getSpecification());
        assertSame(namespace, resolver.getNamespace());

        verify();
    }

    public void test_Found_In_Child_Namespace()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();

        IComponentSpecification spec = newSpec();
        INamespace namespace = newMock(INamespace.class);
        INamespace library = newMock(INamespace.class);

        expect(namespace.getChildNamespace("lib")).andReturn(library);

        expect(library.containsComponentType("MyComponent")).andReturn(true);
        
        expect(library.getComponentSpecification("MyComponent")).andReturn(spec);

        trainIsDeprecated(spec, false);

        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();

        resolver.resolve(cycle, namespace, "lib:MyComponent", l);

        assertSame(spec, resolver.getSpecification());
        assertSame(library, resolver.getNamespace());

        verify();
    }

    public void test_Search_Found_Relative()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();

        IComponentSpecification spec = newSpec();
        INamespace namespace = newMock(INamespace.class);
        
        Log log = newMock(Log.class);

        Resource namespaceLocation = newResource("LibraryStandin.library");
        Resource specLocation = namespaceLocation.getRelativeResource("MyComponent.jwc");

        expect(namespace.containsComponentType("MyComponent")).andReturn(false);

        train(log, ResolverMessages.resolvingComponent("MyComponent", namespace));

        expect(namespace.getSpecificationLocation()).andReturn(namespaceLocation);

        train(log, ResolverMessages.checkingResource(specLocation));
        
        ISpecificationSource source = newSource(specLocation, spec);
        
        train(log, ResolverMessages.installingComponent("MyComponent", namespace, spec));

        namespace.installComponentSpecification("MyComponent", spec);

        trainIsDeprecated(spec, false);

        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setLog(log);
        resolver.setSpecificationSource(source);

        resolver.resolve(cycle, namespace, "MyComponent", l);

        assertSame(spec, resolver.getSpecification());
        assertSame(namespace, resolver.getNamespace());

        verify();
    }

    public void test_Found_In_Framework_Namespace()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();

        IComponentSpecification spec = newSpec();
        INamespace namespace = newMock(INamespace.class);
        ISpecificationResolverDelegate delegate = newMock(ISpecificationResolverDelegate.class);
        
        Log log = newMock(Log.class);
        INamespace framework = newMock(INamespace.class);

        Resource namespaceLocation = newResource("LibraryStandin.library");

        expect(namespace.containsComponentType("FrameworkComponent")).andReturn(false);

        train(log, ResolverMessages.resolvingComponent("FrameworkComponent", namespace));

        expect(namespace.getSpecificationLocation()).andReturn(namespaceLocation);

        train(log, ResolverMessages.checkingResource(namespaceLocation
                .getRelativeResource("FrameworkComponent.jwc")));
        
        expect(namespace.isApplicationNamespace()).andReturn(false);
        
        expect(delegate.findComponentSpecification(cycle, namespace, "FrameworkComponent")).andReturn(null);
        
        trainGetPackages(namespace, "org.foo");
        
        ClassFinder finder = newClassFinder("org.foo", "FrameworkComponent", null);
        
        ISpecificationSource source = newSource(framework);

        expect(framework.containsComponentType("FrameworkComponent")).andReturn(true);

        expect(framework.getComponentSpecification("FrameworkComponent")).andReturn(spec);
        
        train(log, ResolverMessages
                .installingComponent("FrameworkComponent", namespace, spec));
        namespace.installComponentSpecification("FrameworkComponent", spec);

        trainIsDeprecated(spec, false);

        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setLog(log);
        resolver.setSpecificationSource(source);
        resolver.setClassFinder(finder);
        resolver.setDelegate(delegate);
        
        resolver.resolve(cycle, namespace, "FrameworkComponent", l);

        assertSame(spec, resolver.getSpecification());
        assertSame(namespace, resolver.getNamespace());

        verify();
    }

    public void test_Provided_By_Delegate()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();

        IComponentSpecification spec = newSpec();
        INamespace namespace = newMock(INamespace.class);
        
        Log log = newMock(Log.class);
        //INamespace framework = newMock(INamespace.class);
        
        ISpecificationResolverDelegate delegate = newDelegate(
                cycle,
                namespace,
                "DelegateComponent",
                spec);
        
        expect(namespace.containsComponentType("DelegateComponent")).andReturn(false);

        train(log, ResolverMessages.resolvingComponent("DelegateComponent", namespace));

        Resource namespaceLocation = newResource("LibraryStandin.library");
        
        expect(namespace.getSpecificationLocation()).andReturn(namespaceLocation);

        train(log, ResolverMessages.checkingResource(namespaceLocation
                .getRelativeResource("DelegateComponent.jwc")));

        expect(namespace.isApplicationNamespace()).andReturn(false);
        
        expect(log.isDebugEnabled()).andReturn(false);
        
        namespace.installComponentSpecification("DelegateComponent", spec);
        
        trainIsDeprecated(spec, false);
        
        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setLog(log);
        resolver.setDelegate(delegate);
        
        resolver.resolve(cycle, namespace, "DelegateComponent", l);
        
        assertSame(spec, resolver.getSpecification());
        assertSame(namespace, resolver.getNamespace());
        
        verify();
    }

    private void trainGetPackages(INamespace namespace, String packages)
    {
        expect(namespace.getPropertyValue("org.apache.tapestry.component-class-packages"))
        .andReturn(packages);
    }

    private ClassFinder newClassFinder(String packages, String className, Class result)
    {
        ClassFinder finder = newMock(ClassFinder.class);

        expect(finder.findClass(packages, className)).andReturn(result);

        return finder;
    }

    public void test_Not_Found()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();
        
        Log log = newMock(Log.class);
        
        INamespace namespace = newMock(INamespace.class);
        INamespace framework = newMock(INamespace.class);
        
        ISpecificationResolverDelegate delegate = newDelegate(
                cycle,
                namespace,
                "NotFoundComponent",
                null);
        
        Resource namespaceLocation = newResource("LibraryStandin.library");
        
        expect(namespace.containsComponentType("NotFoundComponent")).andReturn(false);

        train(log, ResolverMessages.resolvingComponent("NotFoundComponent", namespace));

        expect(namespace.getSpecificationLocation()).andReturn(namespaceLocation);

        train(log, ResolverMessages.checkingResource(namespaceLocation
                .getRelativeResource("NotFoundComponent.jwc")));

        expect(namespace.isApplicationNamespace()).andReturn(false);

        ISpecificationSource source = newSource(framework);

        expect(framework.containsComponentType("NotFoundComponent")).andReturn(false);

        ClassFinder finder = newClassFinder("org.foo", "NotFoundComponent", null);
        trainGetPackages(namespace, "org.foo");

        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setLog(log);
        resolver.setSpecificationSource(source);
        resolver.setDelegate(delegate);
        resolver.setClassFinder(finder);

        try
        {
            resolver.resolve(cycle, namespace, "NotFoundComponent", l);
            unreachable();
        }
        catch (ApplicationRuntimeException ex)
        {
            assertEquals(
                    "Component 'NotFoundComponent' not found in EasyMock for interface org.apache.tapestry.INamespace.",
                    ex.getMessage());
            assertSame(l, ex.getLocation());
        }

        verify();
    }

    /**
     * Test for checking inside the WEB-INF/app folder (app is the application id, i.e., the servlet
     * name).
     */

    public void test_Found_In_App_Folder()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();
        
        IComponentSpecification spec = newSpec();
        Log log = newLog();

        Resource contextRoot = newResource("context/");
        
        INamespace namespace = newMock(INamespace.class);
        
        Resource namespaceLocation = newResource("LibraryStandin.library");
        Resource specLocation = contextRoot.getRelativeResource("WEB-INF/myapp/MyAppComponent.jwc");
        
        expect(namespace.containsComponentType("MyAppComponent")).andReturn(false);

        train(log, ResolverMessages.resolvingComponent("MyAppComponent", namespace));

        expect(namespace.getSpecificationLocation()).andReturn(namespaceLocation);

        train(log, ResolverMessages.checkingResource(namespaceLocation
                .getRelativeResource("MyAppComponent.jwc")));
        
        expect(namespace.isApplicationNamespace()).andReturn(true);

        train(log, ResolverMessages.checkingResource(specLocation));
        
        ISpecificationSource source = newSource(specLocation, spec);
        
        train(log, ResolverMessages.installingComponent("MyAppComponent", namespace, spec));
        
        namespace.installComponentSpecification("MyAppComponent", spec);

        trainIsDeprecated(spec, false);

        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setLog(log);
        resolver.setSpecificationSource(source);
        resolver.setContextRoot(contextRoot);
        resolver.setApplicationId("myapp");
        resolver.initializeService();

        resolver.resolve(cycle, namespace, "MyAppComponent", l);

        assertSame(spec, resolver.getSpecification());
        assertSame(namespace, resolver.getNamespace());

        verify();
    }

    public void test_Found_In_Web_Inf_Folder()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();

        IComponentSpecification spec = newSpec();
        Log log = newLog();

        Resource contextRoot = newResource("context/");

        INamespace namespace = newMock(INamespace.class);

        Resource namespaceLocation = newResource("LibraryStandin.library");
        Resource specLocation = contextRoot.getRelativeResource("WEB-INF/MyWebInfComponent.jwc");
        
        expect(namespace.containsComponentType("MyWebInfComponent")).andReturn(false);
        
        train(log, ResolverMessages.resolvingComponent("MyWebInfComponent", namespace));

        expect(namespace.getSpecificationLocation()).andReturn(namespaceLocation);

        train(log, ResolverMessages.checkingResource(namespaceLocation
                .getRelativeResource("MyWebInfComponent.jwc")));

        expect(namespace.isApplicationNamespace()).andReturn(true);

        train(log, ResolverMessages.checkingResource(contextRoot
                .getRelativeResource("WEB-INF/myapp/MyWebInfComponent.jwc")));
        train(log, ResolverMessages.checkingResource(specLocation));
        
        ISpecificationSource source = newSource(specLocation, spec);
        
        train(log, ResolverMessages.installingComponent("MyWebInfComponent", namespace, spec));

        namespace.installComponentSpecification("MyWebInfComponent", spec);
        
        trainIsDeprecated(spec, false);
        
        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setLog(log);
        resolver.setSpecificationSource(source);
        resolver.setContextRoot(contextRoot);
        resolver.setApplicationId("myapp");
        resolver.initializeService();

        resolver.resolve(cycle, namespace, "MyWebInfComponent", l);

        assertSame(spec, resolver.getSpecification());
        assertSame(namespace, resolver.getNamespace());

        verify();
    }

    public void test_Found_In_Context_Root()
    {
        IRequestCycle cycle = newCycle();
        Location l = newLocation();

        IComponentSpecification spec = newSpec();
        Log log = newLog();

        Resource contextRoot = newResource("context/");
        
        INamespace namespace = newMock(INamespace.class);
        
        Resource namespaceLocation = newResource("LibraryStandin.library");
        Resource specLocation = contextRoot.getRelativeResource("ContextRootComponent.jwc");
        
        

        expect(namespace.containsComponentType("ContextRootComponent")).andReturn(false);

        train(log, ResolverMessages.resolvingComponent("ContextRootComponent", namespace));
        
        expect(namespace.getSpecificationLocation()).andReturn(namespaceLocation);

        train(log, ResolverMessages.checkingResource(namespaceLocation
                .getRelativeResource("ContextRootComponent.jwc")));
        
        expect(namespace.isApplicationNamespace()).andReturn(true);

        train(log, ResolverMessages.checkingResource(contextRoot
                .getRelativeResource("WEB-INF/myapp/ContextRootComponent.jwc")));
        train(log, ResolverMessages.checkingResource(contextRoot
                .getRelativeResource("WEB-INF/ContextRootComponent.jwc")));
        
        train(log, ResolverMessages.checkingResource(specLocation));
        
        ISpecificationSource source = newSource(specLocation, spec);
        
        train(log, ResolverMessages.installingComponent(
                "ContextRootComponent",
                namespace,
                spec));

        namespace.installComponentSpecification("ContextRootComponent", spec);

        trainIsDeprecated(spec, false);
        
        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setLog(log);
        resolver.setSpecificationSource(source);
        resolver.setContextRoot(contextRoot);
        resolver.setApplicationId("myapp");
        resolver.initializeService();

        resolver.resolve(cycle, namespace, "ContextRootComponent", l);

        assertSame(spec, resolver.getSpecification());
        assertSame(namespace, resolver.getNamespace());

        verify();
    }

    public void test_Found_Component_Class()
    {   
        INamespace namespace = newMock(INamespace.class);

        trainGetPackages(namespace, "org.foo");
        ClassFinder finder = newClassFinder("org.foo", "folder.MyComponent", BaseComponent.class);

        Resource componentResource = newResource();
        Resource namespaceResource = newResource("folder/MyComponent.jwc", componentResource);
        
        trainGetResource(namespace, namespaceResource);

        URL componentUrl = this.getClass().getResource("MyComponent.jwc");
        expect(componentResource.getResourceURL()).andReturn(componentUrl);

        replay();

        ComponentSpecificationResolverImpl resolver = new ComponentSpecificationResolverImpl();
        resolver.setClassFinder(finder);

        IComponentSpecification spec = resolver.searchForComponentClass(namespace, "folder/MyComponent");

        assertEquals(BaseComponent.class.getName(), spec.getComponentClassName());
        assertSame(componentResource, spec.getSpecificationLocation());
        assertSame(componentResource, spec.getLocation().getResource());

        verify();
    }

    private void trainGetResource(INamespace namespace, Resource resource)
    {
        expect(namespace.getSpecificationLocation()).andReturn(resource);
    }

    private Resource newResource(String relativePath, Resource relativeResource)
    {
        Resource resource = newMock(Resource.class);
        checkOrder(resource, false);
        
        expect(resource.getRelativeResource(relativePath)).andReturn(relativeResource);

        return resource;
    }
}