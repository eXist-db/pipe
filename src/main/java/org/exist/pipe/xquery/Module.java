/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2013 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.exist.pipe.xquery;

import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Module extends AbstractInternalModule {
    public final static String NAMESPACE_URI = "http://exist-db.org/pipe";

    public final static String PREFIX = "pipe";
    public final static String RELEASED_IN_VERSION = "eXist-2.0";

    private final static FunctionDef[] functions = {
        new FunctionDef(Make.signatures[0], Make.class),

        new FunctionDef(Write.signatures[0], Write.class),
        new FunctionDef(Read.signatures[0], Read.class),
        
        new FunctionDef(Close.signatures[0], Close.class),
    };
    
    public Module(Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);

    }

    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    @Override
    public String getDefaultPrefix() {
        return PREFIX;
    }

    @Override
    public String getDescription() {
        return "Named pipe is inter-process communication patternt.";
    }

    @Override
    public String getReleaseVersion() {
        return RELEASED_IN_VERSION;
    }
}