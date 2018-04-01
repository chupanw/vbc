////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package edu.cmu.cs.vbc.prog.checkstyle;

import edu.cmu.cs.vbc.prog.checkstyle.api.Context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A default implementation of the Context interface.
 * @author lkuehne
 */
public final class DefaultContext implements Context
{
    /** stores the context entries */
    private final Map<String, Object> entries = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public Object get(String key)
    {
        return entries.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public LinkedList<String> getAttributeNames()
    {
        LinkedList<String> list = new LinkedList<>();
        for (String e : entries.keySet()) {
            list.add(e);
        }
        return list;
    }

    /**
     * Adds a context entry.
     * @param key the context key
     * @param value the value for key
     */
    public void add(String key, Object value)
    {
        entries.put(key, value);
    }
}
