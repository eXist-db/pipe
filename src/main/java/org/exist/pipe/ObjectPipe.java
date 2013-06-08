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
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import org.exist.security.Permission;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class ObjectPipe<T> extends APipe {
    
    private TransferQueue<T> pipe = new LinkedTransferQueue<T>();
    
    private long timeout = 10;
    private TimeUnit unit = TimeUnit.SECONDS;
    
    public ObjectPipe(Permission perms) {
        super(perms);
    }
    
    public void write(T obj) throws IOException {
        validate(Permission.WRITE);
        
        try {
            pipe.put(obj);
        } catch (InterruptedException e) {
            throw new IOException("can't write to pipe", e);
        }
    }
    
    public T read() throws IOException {
        validate(Permission.READ);
        
        try {
            return pipe.poll(timeout, unit);
        } catch (InterruptedException e) {
            throw new IOException("can't read from pipe", e);
        }
    }
}
