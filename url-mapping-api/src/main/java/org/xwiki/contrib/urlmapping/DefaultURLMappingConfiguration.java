/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.urlmapping;

import java.util.EnumMap;
import java.util.Map;

import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.stability.Unstable;

/**
 * The default redirection configuration of a URLMappingPrefixHandler, or the root handler.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
public class DefaultURLMappingConfiguration implements URLMappingConfiguration
{
    private static final String CONFIGURATION_PREFIX = "urlmapping.";
    private static final String HANDLERS_PREFIX = CONFIGURATION_PREFIX + "prefixhandlers.";

    private final String urlHandlerName;

    private final Map<Key, Object> defaults;
    private final Map<Key, Object> conf;

    /**
     * The possible configuration keys.
     */
    @Unstable
    public enum Key
    {
        /**
         * Prefix.
         */
        PREFIX("prefix", false),

        /**
         * Delay.
         */
        DELAY("delay", true),

        /**
         * Redirect screen template.
         */
        REDIRECT_SCREEN_TEMPLATE("redirectScreenTemplate", false),

        /**
         * Not found screen template.
         */
        NOT_FOUND_SCREEN_TEMPLATE("notFoundScreenTemplate", false),

        /**
         * Intro message.
         */
        INTRO_MESSAGE("introMessage", false),

        /**
         * Not found message.
         */
        NOT_FOUND_INTRO_MESSAGE("notFoundIntroMessage", false),

        /**
         * title.
         */
        TITLE("title", false),

        /**
         * Redirect HTTP status.
         */
        REDIRECT_HTTP_STATUS("redirectHttpStatus", true);

        private final String name;
        private final boolean isInt;

        Key(String name, boolean isInt)
        {
            this.name = name;
            this.isInt = isInt;
        }

        @Override
        public String toString()
        {
            return this.name;
        }

        private boolean isInt()
        {
            return isInt;
        }
    }

    /**
     * @param configurationSource the configuration source to use to get the configuration
     * @param urlHandlerName the name of the handler for which to get the configuration, null for the root configuration
     */
    public DefaultURLMappingConfiguration(ConfigurationSource configurationSource, String urlHandlerName)
    {
        this.urlHandlerName = urlHandlerName;
        this.defaults = new EnumMap<>(Key.class);
        this.defaults.put(Key.DELAY, 0);
        this.defaults.put(Key.REDIRECT_SCREEN_TEMPLATE, "url-mapping/redirectscreen.vm");
        this.defaults.put(Key.NOT_FOUND_SCREEN_TEMPLATE, "url-mapping/notfoundscreen.vm");
        this.conf = new EnumMap<>(Key.class);

        for (Key key : Key.values()) {
            Object v = key.isInt() ? getInt(key, configurationSource) : getString(key, configurationSource);
            if (v != null) {
                this.conf.put(key, v);
            }

            v = key.isInt()
                ? getDefault(key, configurationSource, Integer.class, 0)
                : getDefault(key, configurationSource, String.class, null);

            if (v != null) {
                this.defaults.put(key, v);
            }
        }
    }

    @Override
    public int getDelay()
    {
        return getInt(Key.DELAY);
    }

    @Override
    public String getPrefix()
    {
        if (this.urlHandlerName == null) {
            return "";
        }

        return (String) this.conf.getOrDefault(Key.PREFIX, "");
    }

    @Override
    public String getRedirectScreenTemplateName()
    {
        return getString(Key.REDIRECT_SCREEN_TEMPLATE);
    }

    @Override
    public String getNotFoundScreenTemplateName()
    {
        return getString(Key.NOT_FOUND_SCREEN_TEMPLATE);
    }

    @Override
    public String getIntroMessage()
    {
        return getString(Key.INTRO_MESSAGE);
    }

    @Override
    public String getNotFoundIntroMessage()
    {
        return getString(Key.NOT_FOUND_INTRO_MESSAGE);
    }

    @Override
    public int getRedirectHTTPStatus()
    {
        return getInt(Key.REDIRECT_HTTP_STATUS);
    }

    @Override
    public String getTitle()
    {
        return getString(Key.TITLE);
    }

    private String getString(Key property)
    {
        Object v = getConf(property);

        if (v instanceof String) {
            return (String) v;
        }

        return null;
    }

    private Object getConf(Key property)
    {
        Object v = this.conf.get(property);
        if (v == null) {
            v = this.defaults.get(property);
        }
        return v;
    }

    private int getInt(Key property)
    {
        Object v = getConf(property);

        if (v instanceof Integer) {
            return (int) v;
        }

        return 0;
    }

    private String getString(Key property, ConfigurationSource configurationSource)
    {
        return getSpecific(property, configurationSource, String.class);
    }

    private Integer getInt(Key property, ConfigurationSource configurationSource)
    {
        return getSpecific(property, configurationSource, Integer.class);
    }

    private <T> T getSpecific(Key property, ConfigurationSource configurationSource, Class<T> clazz)
    {
        if (this.urlHandlerName == null) {
            T d = null;
            try {
                d = clazz.cast(this.defaults.get(property));
            } catch (Exception ignored) { }
            return getDefault(property, configurationSource, clazz, d);
        }
        return configurationSource.getProperty(HANDLERS_PREFIX + this.urlHandlerName + '.' + property, clazz);
    }

    private <T> T getDefault(Key property, ConfigurationSource configurationSource, Class<T> clazz, T defaultValue)
    {
        T value = configurationSource.getProperty(CONFIGURATION_PREFIX + "default." + property, clazz, defaultValue);
        if (value == null || (value instanceof String && ((String) value).isEmpty())) {
            return defaultValue;
        }
        return value;
    }

    /**
     * @param property the property to set the default for
     * @param value the default to use if not set in the wiki configuration
     */
    public void setDefault(Key property, Object value)
    {
        this.defaults.put(property, value);
    }
}
