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
package org.exist.pipe;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.exist.EXistException;
import org.exist.config.Configuration;
import org.exist.plugin.Plug;
import org.exist.plugin.PluginsManager;
import org.exist.security.PermissionFactory;
import org.exist.storage.DBBroker;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Pipes implements Plug {
    
    private final static int DEFAULT_PERM = 0600;

    public static Pipes _;
    
    //XXX: cleanup from time to time ...
    private static Map<String, NamedPipe> pipes = new ConcurrentHashMap<String, NamedPipe>();

    private PluginsManager pm;
    
    public Pipes(PluginsManager pm) {
        this.pm = pm;
        
        _ = this;
    }
    
    public NamedPipe make() throws IOException {
        return make(DEFAULT_PERM);
    }
    
    public NamedPipe make(int mode) throws IOException {
        
        final NamedPipe pipe = new ObjectPipe<String>(PermissionFactory.getPermission(mode));
        
        pipes.put(pipe.name(), pipe);

        return pipe;
    }
    
    public NamedPipe pipe(final String name) throws IOException {
        final NamedPipe pipe = pipes.get(name);

        if (pipe == null) {
            throw new IOException("Pipe '"+name+"' does not exist.");
        }
        
        return pipe;
    }

    protected void close(final NamedPipe pipe) throws IOException {
        if (pipe == null) {
            throw new IOException("Pipe can be NULL.");
        }
        
//        pipe.close();
        pipes.remove(pipe.name());
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public void start(DBBroker broker) throws EXistException {
    }
    
    //one hour
    protected long TIMEOUT = 60 * 60 * 1000;

    @Override
    public void sync(DBBroker broker) throws EXistException {
        final long ouTime = System.currentTimeMillis() - TIMEOUT;
        
        final Iterator<Entry<String, NamedPipe>> it = pipes.entrySet().iterator();
        Entry<String, NamedPipe> entry;
        while (it.hasNext()) {
            entry = it.next();
            if (entry.getValue().lastUse() < ouTime) {
                it.remove();
            }
        }
    }

    @Override
    public void stop(DBBroker broker) throws EXistException {
    }
}