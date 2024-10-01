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

import org.xwiki.stability.Unstable;

/**
 * Configuration for a URL redirection.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
public interface URLMappingConfiguration
{
    /**
     * @return the duration of the interstitial screen. 0 for no delay (the screen won't be shown and the template
     * won't be loaded), -1 to disable automatic redirection.
     */
    int getDelay();

    /**
     * @return the handled prefix
     */
    String getPrefix();

    /**
     * @return the template to use for the redirection screen
     */
    String getRedirectScreenTemplateName();

    /**
     * @return the template to use for the not found screen
     */
    String getNotFoundScreenTemplateName();

    /**
     * @return the message to use as an introduction for the redirection screen.
     */
    String getIntroMessage();

    /**
     * @return the message to use as an introduction for the not found screen.
     */
    String getNotFoundIntroMessage();

    /**
     * @return the title to use for the redirection screen.
     */
    String getTitle();
}
