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

import org.exist.security.Permission;
import org.exist.security.PermissionFactory;
import org.exist.security.Subject;

import com.eaio.uuid.UUID;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public abstract class APipe implements NamedPipe {
    
    private String name;
    
    private Permission perms;
    
    private long lastUse;
    
    public APipe(Permission perms) {
        this.perms = perms;
        
        name = (new UUID()).toString();
        
        use();
    }
    
    protected void use() {
        lastUse = System.currentTimeMillis();
    }
    
    @Override
    public String name() {
        return name;
    }
    
    public void close() throws IOException {
        validate(Permission.WRITE);
        
        Pipes._.close(this);
    }
    
    public long lastUse() {
        return lastUse;
    }

    private Subject getSubject() {
        return PermissionFactory.sm.getCurrentSubject();
    }

    protected void validate(int mode) throws IOException {
        if (!perms.validate(getSubject(), mode)) {
            if (mode == Permission.WRITE) {
                throw new IOException("no write permission for pipe.");
            } else if (mode == Permission.READ) {
                throw new IOException("no read permission for pipe.");
            } else {
                throw new IOException("unkwon permission request for pipe.");
            }
        }
    }
}