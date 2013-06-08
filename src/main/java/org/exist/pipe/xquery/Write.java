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

import java.io.IOException;

import org.exist.dom.QName;
import org.exist.pipe.ObjectPipe;
import org.exist.pipe.Pipes;
import org.exist.xquery.*;
import org.exist.xquery.value.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Write extends BasicFunction {

	public final static FunctionSignature signatures[] = { 
		new FunctionSignature(
			new QName("write", Module.NAMESPACE_URI, Module.PREFIX), 
			"",
            new SequenceType[] {
                new FunctionParameterSequenceType("pipe-name", Type.STRING, Cardinality.EXACTLY_ONE, "The pipe name."),
                new FunctionParameterSequenceType("data", Type.STRING, Cardinality.EXACTLY_ONE, "The data to write.")
			},
			new SequenceType(Type.EMPTY, Cardinality.ZERO)
		)
	};

	public Write(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	@Override
	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
	    try {
            ObjectPipe<String> pipe = (ObjectPipe<String>) Pipes._.pipe(args[0].getStringValue());

            pipe.write(args[1].getStringValue());
            
            return Sequence.EMPTY_SEQUENCE;

	    } catch (IOException e) {
            throw new XPathException(this, e);
        }
	}
}