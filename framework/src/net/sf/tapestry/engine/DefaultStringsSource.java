package net.sf.tapestry.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.IComponent;
import net.sf.tapestry.IComponentStrings;
import net.sf.tapestry.IComponentStringsSource;
import net.sf.tapestry.IResourceResolver;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.util.MultiKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Global object (stored in the servlet context) that accesses
 *  localized properties for a component.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.0.4
 *
 **/

public class DefaultStringsSource implements IComponentStringsSource
{
    private static final Log LOG = LogFactory.getLog(DefaultStringsSource.class);

    private IResourceResolver resolver;

    private Properties emptyProperties = new Properties();

    /**
     *  Map of {@link Properties}, keyed on a {@link MultiKey} of
     *  component specification path and locale.
     * 
     **/

    private Map cache = new HashMap();

    public DefaultStringsSource(IResourceResolver resolver)
    {
        this.resolver = resolver;
    }

    /**
     *  Returns an instance of {@link Properties} containing
     *  the properly localized messages for the component,
     *  in the {@link Locale} identified by the component's
     *  containing page.
     * 
     **/

    protected synchronized Properties getLocalizedProperties(IComponent component)
    {
        if (component == null)
            throw new IllegalArgumentException(Tapestry.getString("invalid-null-parameter", "component"));

        String specificationPath = component.getSpecification().getSpecificationResourcePath();
        Locale locale = component.getPage().getLocale();

        // Check to see if already in the cache

        MultiKey key = buildKey(specificationPath, locale);

        Properties result = (Properties) cache.get(key);

        if (result != null)
            return result;

        // Not found, create it now.

        result = assembleProperties(specificationPath, locale);

        cache.put(key, result);

        return result;
    }

    private static final String SUFFIX = ".properties";

    private Properties assembleProperties(String path, Locale locale)
    {
        boolean debug = LOG.isDebugEnabled();
        if (debug)
            LOG.debug("Assembling properties for " + path + " " + locale);

        int dotx = path.indexOf('.');
        String basePath = path.substring(0, dotx);

        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();

        Properties parent = (Properties) cache.get(path);

        if (parent == null)
        {
            parent = readProperties(basePath, null, null);

            if (parent == null)
                parent = emptyProperties;

            cache.put(path, parent);
        }

        Properties result = parent;

        if (!Tapestry.isNull(language))
        {
            Locale l = new Locale(language, "");
            MultiKey key = buildKey(path, l);

            result = (Properties) cache.get(key);

            if (result == null)
                result = readProperties(basePath, l, parent);

            cache.put(key, result);

            parent = result;
        }
        else
            language = "";

        if (!Tapestry.isNull(country))
        {
            Locale l = new Locale(language, country);
            MultiKey key = buildKey(path, l);

            result = (Properties) cache.get(key);

            if (result == null)
                result = readProperties(basePath, l, parent);

            cache.put(key, result);

            parent = result;
        }
        else
            country = "";

        if (!Tapestry.isNull(variant))
        {
            Locale l = new Locale(language, country, variant);
            MultiKey key = buildKey(path, l);

            result = (Properties) cache.get(key);

            if (result == null)
                result = readProperties(basePath, l, parent);

            cache.put(key, result);
        }

        return result;
    }

    private MultiKey buildKey(String path, Locale locale)
    {
        return new MultiKey(new Object[] { path, locale.toString()}, false);
    }

    private Properties readProperties(String basePath, Locale locale, Properties parent)
    {
        StringBuffer buffer = new StringBuffer(basePath);

        if (locale != null)
        {
            buffer.append('_');
            buffer.append(locale.toString());
        }

        buffer.append(SUFFIX);

        String path = buffer.toString();

        URL propertiesURL = resolver.getResource(path);

        if (propertiesURL == null)
            return parent;

        Properties result = null;

        if (parent == null)
            result = new Properties();
        else
            result = new Properties(parent);

        try
        {
            InputStream input = propertiesURL.openStream();

            result.load(input);
        }
        catch (IOException ex)
        {
            throw new ApplicationRuntimeException(
                Tapestry.getString("ComponentPropertiesStore.unable-to-read-input", propertiesURL),
                ex);
        }

        return result;
    }

    /**
     *  Clears the cache of read properties files.
     * 
     **/

    public void reset()
    {
        cache.clear();
    }

    public IComponentStrings getStrings(IComponent component)
    {
        return new ComponentStrings(getLocalizedProperties(component));
    }

}