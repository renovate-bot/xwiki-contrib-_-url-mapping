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

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * A converter specifies which requests it can handle, and converts these requests.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
@Role
public interface URLMapper
{
    /**
     * @return the specification requests should match to be managed by this mapper.
     */
    URLMappingSpecification getSpecification();

    /**
     * @return the result of a conversion, or null if no such conversion is possible, in which case the
     * request will be considered not handled by the converter.
     * @param match the result of the matched regex
     */
    URLMappingResult convert(URLMappingMatch match);
}

