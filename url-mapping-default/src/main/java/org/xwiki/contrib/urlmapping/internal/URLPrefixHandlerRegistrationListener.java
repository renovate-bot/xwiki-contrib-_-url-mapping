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
package org.xwiki.contrib.urlmapping.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.contrib.urlmapping.URLMappingPrefixHandler;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.resource.ResourceReferenceHandler;
import org.xwiki.resource.ResourceType;

/**
 * Manages newly added URL prefix handlers to register resolvers.
 * @since 0.0.1
 * @version $Id$
 */
@Component
@Singleton
@Named(URLPrefixHandlerRegistrationListener.HINT)
public class URLPrefixHandlerRegistrationListener implements EventListener
{
    static final String HINT = "urlprefixhandler";

    @Inject
    private Provider<ComponentManager> rootComponentManagerProvider;

    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Inject
    private Logger logger;

    @Override
    public String getName()
    {
        return HINT;
    }

    @Override
    public List<Event> getEvents()
    {
        return List.of(
            new ApplicationStartedEvent(),
            new ComponentDescriptorAddedEvent(URLMappingPrefixHandler.class),
            new ComponentDescriptorRemovedEvent(URLMappingPrefixHandler.class)
        );
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof ApplicationStartedEvent) {
            // register resolvers for already registered handlers manager since no
            // ComponentDescriptorAddedEvent will be sent for them.
            //  see https://jira.xwiki.org/browse/XWIKI-18563. When this is fixed, we won't need anymore to listen to
            // ApplicationStartedEvent.
            logger.debug("Received application started event, will register all prefix handlers");
            registerAllHandlers();
        } else if (event instanceof ComponentDescriptorAddedEvent) {
            ComponentManager componentManager = (ComponentManager) source;
            ComponentDescriptor<?> descriptor = (ComponentDescriptor<?>) data;

            String name = descriptor.getRoleHint();

            logger.debug("ComponentDescriptorAddedEvent for a URLMappingPrefixHandler with hint [{}]", name);

            try {
                URLMappingPrefixHandler handler = componentManager.getInstance(URLMappingPrefixHandler.class, name);
                registerHandler(handler, name);
            } catch (ComponentLookupException e) {
                logger.error("Could not register a URL Mapping prefix handler for [{}])", name, e);
            }
        } else if (event instanceof ComponentDescriptorRemovedEvent) {
            ComponentDescriptor<?> descriptor = (ComponentDescriptor<?>) data;
            String name = descriptor.getRoleHint();
            logger.debug("ComponentDescriptorRemovedEvent for a URLMappingPrefixHandler with hint [{}]", name);
            unregisterHandler(name);
        }

        updateSupportedTypes();
    }

    private void updateSupportedTypes()
    {
        try {
            URLMappingResourceReferenceHandler urlMappingHandler = rootComponentManagerProvider.get().getInstance(
                    new DefaultParameterizedType(
                        null,
                        ResourceReferenceHandler.class,
                        ResourceType.class
                    ), "urlmapping");
            Map<String, URLMappingPrefixHandler> handlers =
                contextComponentManagerProvider.get().getInstanceMap(URLMappingPrefixHandler.class);
            Collection<String> types = new ArrayList<>(handlers.size());
            for (Map.Entry<String, URLMappingPrefixHandler> entry : handlers.entrySet()) {
                String type = StringUtils.strip(entry.getValue().getPrefix(), "/");
                if (StringUtils.isEmpty(type)) {
                    logger.warn("URL Mapping handler with hint [{}] has no prefix, it won't be used", entry.getKey());
                } else {
                    types.add(type);
                    logger.debug("URL Mapping handler with hint [{}] has prefix [{}]", entry.getKey(), type);
                }
            }
            logger.debug("Updating supported types: [{}]", types);
            urlMappingHandler.setSupportedTypes(types);
        } catch (ComponentLookupException e) {
            logger.error("Failed to update the supported URL prefixes", e);
        }
    }

    private void registerHandler(URLMappingPrefixHandler handler, String name)
    {
        String prefix = handler.getPrefix();
        if (StringUtils.isEmpty(prefix)) {
            logger.debug("URL prefix handler [{}], name [{}] doesn't have a prefix, it will not be registered.",
                handler.getClass().getName(), name);
            return;
        }
        logger.debug("Registering URL prefix handler [{}], name [{}] with prefix [{}].", handler.getClass().getName(),
            name, prefix);
        URLMappingResourceReferenceResolver.register(name, prefix, rootComponentManagerProvider.get());
    }

    private void registerAllHandlers()
    {
        try {
            ComponentManager componentManager = contextComponentManagerProvider.get();
            Map<String, URLMappingPrefixHandler> handlers =
                componentManager.getInstanceMap(URLMappingPrefixHandler.class);
            for (Map.Entry<String, URLMappingPrefixHandler> entry : handlers.entrySet()) {
                registerHandler(entry.getValue(), entry.getKey());
            }
        } catch (ComponentLookupException e) {
            logger.error("Could not register URL Mapping resolvers", e);
        }
    }

    private void unregisterHandler(String name)
    {
        logger.debug("Unregistering URL prefix handler with name [{}]", name);
        rootComponentManagerProvider.get().unregisterComponent(URLMappingResourceReferenceResolver.class, name);
    }
}
