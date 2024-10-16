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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletOutputStream;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheManager;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.container.servlet.ServletResponse;
import org.xwiki.contrib.urlmapping.internal.DefaultURLMappingRedirector;
import org.xwiki.contrib.urlmapping.internal.URLMappingResourceReferenceHandler;
import org.xwiki.contrib.urlmapping.internal.URLMappingResourceReferenceResolver;
import org.xwiki.contrib.urlmapping.internal.URLPrefixHandlerRegistrationListener;
import org.xwiki.job.event.status.JobProgressManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceHandlerChain;
import org.xwiki.resource.ResourceReferenceResolver;
import org.xwiki.resource.ResourceType;
import org.xwiki.resource.internal.MainResourceReferenceHandlerManager;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.template.TemplateManager;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.test.page.PageComponentList;
import org.xwiki.url.ExtendedURL;
import org.xwiki.url.URLConfiguration;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.XWikiPluginManager;
import com.xpn.xwiki.test.junit5.mockito.OldcoreTest;
import com.xpn.xwiki.test.reference.ReferenceComponentList;

import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for URL Mapping.
 *
 * @version $Id: 8c8b784b7b02d7bb40c99a062275b56e0b6983c2 $
 */
@OldcoreTest
@ComponentList({
    DefaultURLMappingRedirector.class,
    TestURLMappingPrefixHandler.class,
    TestDelayedURLMappingPrefixHandler.class,
    TestCustomIntroURLMappingPrefixHandler.class,
    TestCustomRedirectStatusURLMappingPrefixHandler.class,
    TestCustomRuntimeConfigIntroHandlerMapping.class,
    URLPrefixHandlerRegistrationListener.class,
    URLMappingResourceReferenceHandler.class
})
@ReferenceComponentList
@PageComponentList
class URLMappingTest
{
    private static final String HTTP_HEADER_LOCATION = "Location";

    @MockComponent
    ConfigurationSource configurationSource;

    @MockComponent
    CacheManager cacheManager;

    @MockComponent
    JobProgressManager jobProgressManager;

    @MockComponent
    Container container;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @InjectMockComponents
    private URLMappingResourceReferenceHandler urlMappingResourceReferenceHandler;

    @InjectMockComponents
    private MainResourceReferenceHandlerManager mainResourceReferenceHandlerManager;

    @MockComponent
    ContextualAuthorizationManager contextualAuthorizationManager;

    @MockComponent
    URLConfiguration urlConfiguration;

    @MockComponent
    WikiDescriptorManager wikiDescriptorManager;

    @MockComponent
    AuthorizationManager authorizationManager;

    @MockComponent
    XWikiContext context;

    @Inject
    private TemplateManager templateManager;

    private XWikiResponse response;

    private XWikiRequest request;

    private static int nextChainCalled = 0;

    private static final ResourceReferenceHandlerChain dummyChain = reference -> nextChainCalled++;

    private final AtomicReference<String> html = new AtomicReference<>();

    private void conf(String key, String value)
    {
        when(configurationSource.getProperty(key)).thenReturn(value);
        when(configurationSource.getProperty(eq(key), any())).thenReturn(value);
        when(configurationSource.getProperty(eq(key), anyString())).thenReturn(value);
        when(configurationSource.getProperty(key, (String) null)).thenReturn(value);
    }

    private void conf(String key, int value)
    {
        when(configurationSource.getProperty(key)).thenReturn(value);
        when(configurationSource.getProperty(eq(key), any())).thenReturn(value);
        when(configurationSource.getProperty(eq(key), anyInt())).thenReturn(value);
        when(configurationSource.getProperty(key, (Integer) null)).thenReturn(value);
    }

    private void conf(String key, boolean value)
    {
        when(configurationSource.getProperty(key)).thenReturn(value);
        when(configurationSource.getProperty(eq(key), any())).thenReturn(value);
        when(configurationSource.getProperty(eq(key), anyBoolean())).thenReturn(value);
    }

    @BeforeComponent
    void setupOnce() throws Exception
    {
        Utils.setComponentManager(componentManager);
        componentManager.registerComponent(ComponentManager.class, "context", componentManager);
        componentManager.registerComponent(ConfigurationSource.class, "all", configurationSource);
        componentManager.registerComponent(ConfigurationSource.class, "xwikicfg", configurationSource);
        Cache<Object> cache = mock(Cache.class);
        when(cacheManager.createNewCache(any())).thenReturn(cache);

        when(configurationSource.getProperty(any(), (String) any())).thenAnswer(c -> c.getArgument(1));
        when(configurationSource.getProperty(any(), (Integer) any())).thenAnswer(c -> c.getArgument(1));
        conf("urlmapping.prefixhandlers.test.prefix", "myprefix");
        conf("urlmapping.prefixhandlers.test.delay", 0);
        conf("urlmapping.prefixhandlers.testdelayed.prefix", "delayedprefix");
        conf("urlmapping.prefixhandlers.testdelayed.delay", 2);
        conf("urlmapping.prefixhandlers.testcustomintro.prefix", "customintro");
        conf("urlmapping.prefixhandlers.testcustomintro.title", "Custom title");
        conf("urlmapping.prefixhandlers.testcustomintro.introMessage", "<Redirecting>. Please update your bookmarks.");
        conf("urlmapping.prefixhandlers.testcustomredirect.prefix", "customredirect");
        conf("urlmapping.prefixhandlers.testcustomredirect.redirectHttpStatus", 307);
        conf("urlmapping.prefixhandlers.customruntimeconfig.prefix", "customruntimeconfig");
        conf("logging.deprecated.enabled", true);
    }

    @BeforeEach
    void setup() throws Exception
    {
        request = mock(XWikiRequest.class);
        response = mock(XWikiResponse.class);
        ServletRequest servletRequest = mock(ServletRequest.class);
        ServletResponse servletResponse = mock(ServletResponse.class);
        when(servletRequest.getHttpServletRequest()).thenReturn(request);
        when(servletResponse.getHttpServletResponse()).thenReturn(response);
        when(this.container.getRequest()).thenReturn(servletRequest);
        when(this.container.getResponse()).thenReturn(servletResponse);

        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
        doAnswer(invocationOnMock -> {
            html.set(invocationOnMock.getArgument(0));
            return null;
        }).when(outputStream).print(anyString());
        doAnswer(invocationOnMock -> {
            html.set(new String(invocationOnMock.getArgument(0), StandardCharsets.UTF_8));
            return null;
        }).when(outputStream).write(any());
        Provider<XWikiContext> xcontextProvider = componentManager.getInstance(new DefaultParameterizedType(null,
            Provider.class,
            XWikiContext.class));
        XWikiContext xcontext = xcontextProvider.get();
        XWiki wiki = mock(XWiki.class);
        XWikiPluginManager xwikiPluginManager = mock(XWikiPluginManager.class);
        when(xwikiPluginManager.endParsing(any(), any())).thenAnswer(c -> c.getArgument(0));
        when(wiki.getEncoding()).thenReturn(StandardCharsets.UTF_8.toString());
        when(wiki.getPluginManager()).thenReturn(xwikiPluginManager);
        when(wiki.evaluateTemplate(any(), any())).thenAnswer(c -> templateManager.render(c.getArgument(0)));
        when(wiki.getURL((EntityReference) any(), eq("view"), eq(xcontext))).then(invocationOnMock -> {
                EntityReference expectedRef = new DocumentReference("mywiki", "MySpace", "MyDoc");
                return invocationOnMock.getArgument(0).equals(expectedRef)
                    ? "/hello"
                    : "/bye";
            }
        );
        xcontext.setWiki(wiki);
        xcontext.setResponse(response);
        xcontext.setRequest(request);
        nextChainCalled = 0;
        URLPrefixHandlerRegistrationListener l = componentManager.getInstance(EventListener.class, "urlprefixhandler");
        l.onEvent(new ApplicationStartedEvent(), null, null);
    }

    private void httpGet(String path) throws Exception
    {
        when(request.getMethod()).thenReturn("get");
        mainResourceReferenceHandlerManager.handle(getResourceReference(path));
    }

    private ResourceReference getResourceReference(String path) throws ComponentLookupException
    {
        String[] segments = StringUtils.stripStart(path, "/").split("/");
        ExtendedURL eURL = new ExtendedURL(Arrays.stream(segments).skip(1).collect(Collectors.toList()));
        String prefix = segments[0];
        URLMappingResourceReferenceResolver resolver =
            componentManager.getInstance(new DefaultParameterizedType(null,
                ResourceReferenceResolver.class,
                ExtendedURL.class), prefix);
        if (resolver == null) {
            return null;
        }
        return resolver.resolve(eURL, null, null);
    }

    private void httpGetDirect(String path) throws Exception
    {
        when(request.getMethod()).thenReturn("get");
        ResourceReference ref = getResourceReference(path);
        if (ref != null) {
            urlMappingResourceReferenceHandler.handle(ref, dummyChain);
        }
    }

    private void httpPost(String path) throws Exception
    {
        when(request.getMethod()).thenReturn("post");
        ResourceReference ref = getResourceReference(path);
        if (ref != null) {
            mainResourceReferenceHandlerManager.handle(ref);
        }
    }

    @Test
    void testNominal() throws Exception
    {
        httpGet("/myprefix/expectedmatch");
        verify(this.response).sendRedirect("/hello");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/myprefix/expectedmatch",
        "/myprefix/unexpectedmatch"
    })
    void testNextHandlerCalled(String path) throws Exception
    {
        httpGetDirect(path);
        assertEquals(1, nextChainCalled);
    }

    @Test
    void testShouldPresentNotFoundScreenWithSuggestions() throws Exception
    {
        httpGet("/myprefix/unexpectedmatch");
        verify(this.response, times(0)).sendRedirect(any());
        assertThat(html.get(), containsString("url-mapping-suggestions"));
    }

    @Test
    void testStringURL() throws Exception
    {
        httpGet("/myprefix/0GETURL");
        verify(this.response).sendRedirect("/transformedUrl");
    }

    @Test
    void testStringURLAndURLMappingSpecificationConstructor() throws Exception
    {
        httpGet("/myprefix/1GETURL/HELLO");
        verify(this.response).sendRedirect("/HELLO");
    }

    @Test
    void testCustomConfigurationForMissingConversion() throws Exception
    {
        httpGet("/myprefix/01missinglink");
        verify(this.response).setStatus(301);
        verify(this.response).setHeader(HTTP_HEADER_LOCATION, "/mycustomnotfound");
    }

    @Test
    void testDelayedRedirect() throws Exception
    {
        httpGet("/delayedprefix/hello");
        assertThat(html.get(), containsString("<meta http-equiv=\"refresh\" content=\"2; url=/delayed\" />"));
        assertThat(html.get(), containsString(
            "$escapetool.xml($services.localization.render('urlmapping.template.redirectionscreen.redirected'))"));
    }

    @Test
    void testDelayedRedirectWithCustomIntro() throws Exception
    {
        httpGet("/customintro/hello");
        assertThat(html.get(), containsString("&#60;Redirecting&#62;. Please update your bookmarks."));
        assertThat(html.get(), containsString("Custom title"));
        assertThat(html.get(), containsString("<meta http-equiv=\"refresh\" content=\"3; url=/bye\" />"));
        verify(this.response).setStatus(429);
    }

    @Test
    void testWrongMethod() throws Exception
    {
        httpPost("/customintro/hello");
        verify(this.response).sendRedirect("/bye");
    }

    @Test
    void testCustomRedirectStatusCodeConfiguration() throws Exception
    {
        httpPost("/customredirect/hello");
        verify(this.response).setStatus(307);
        verify(this.response).setHeader(HTTP_HEADER_LOCATION, "/bye");
    }

    @Test
    void testNotFoundCustomRedirectStatusCodeConfiguration() throws Exception
    {
        httpPost("/customredirect/hello2");
        verify(this.response).setStatus(404);
    }

    @Test
    void testUnmatchedPathRuntimeIntroConfig() throws Exception
    {
        httpPost("/customruntimeconfig/hello");
        assertThat(html.get(), containsString("Custom intro"));
    }

    @Test
    void testAddedRemovedPrefixHandler() throws Exception
    {
        URLPrefixHandlerRegistrationListener l = componentManager.getInstance(EventListener.class, "urlprefixhandler");
        assertThat(getSupportedTypes(), hasItem("myprefix"));
        ComponentDescriptor<Object> descriptor = componentManager.getComponentDescriptor(URLMappingPrefixHandler.class,
            "test");
        componentManager.unregisterComponent(URLMappingPrefixHandler.class, "test");
        l.onEvent(new ComponentDescriptorRemovedEvent(), componentManager, descriptor);
        assertThat(getSupportedTypes(), not(hasItem("myprefix")));
        componentManager.registerComponent(TestURLMappingPrefixHandler.class);
        l.onEvent(new ComponentDescriptorAddedEvent(), componentManager, descriptor);
        assertThat(getSupportedTypes(), hasItem("myprefix"));
    }

    private List<String> getSupportedTypes()
    {
        return urlMappingResourceReferenceHandler.getSupportedResourceReferences()
            .stream().map(ResourceType::toString).collect(Collectors.toList());
    }
}
