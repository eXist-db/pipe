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
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SourceChannel;

import org.exist.security.Permission;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class BytesPipe extends APipe {
    
    final Pipe pipe;

    public BytesPipe(Permission perms) throws IOException {
        super(perms);

        pipe = Pipe.open();
    }
    
    public void write(byte[] bytes) throws IOException {
        validate(Permission.WRITE);

        Pipe.SinkChannel sinkChannel = pipe.sink();
        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(bytes);

        buf.flip();

        while (buf.hasRemaining()) {
            sinkChannel.write(buf);
        }
    }
    
    public SourceChannel getSource(String name) throws IOException {
        validate(Permission.READ);

        return pipe.source();
    }
}
