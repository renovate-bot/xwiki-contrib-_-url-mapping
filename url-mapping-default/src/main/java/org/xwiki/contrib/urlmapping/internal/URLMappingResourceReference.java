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

import org.xwiki.resource.AbstractResourceReference;
import org.xwiki.resource.ResourceType;
import org.xwiki.stability.Unstable;

/**
 * Represents a reference to a mapped URL.
 *
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
public class URLMappingResourceReference extends AbstractResourceReference
{
    private final String path;
    private final String name;

    /**
     * @param name the role hint of the handler handling this path
     * @param prefix the prefix of the path
     * @param path the mapped path
     * @since 0.0.1
     */
    public URLMappingResourceReference(String prefix, String name, String path)
    {
        this.name = name;
        this.path = path == null ? "" : path;
        setType(new ResourceType(prefix));
    }

    /**
     * @return the mapped path
     * @since 0.0.1
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @return the mapped path
     * @since 0.0.1
     */
    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof URLMappingResourceReference
            && ((URLMappingResourceReference) object).getPath().equals(this.path)
            && ((URLMappingResourceReference) object).getName().equals(this.name);
    }

    @Override
    public int hashCode()
    {
        return this.path.hashCode();
    }
}
