/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.message.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.field.AddressListField;
import org.apache.james.mime4j.field.ContentDispositionField;
import org.apache.james.mime4j.field.ContentTransferEncodingField;
import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.field.DateTimeField;
import org.apache.james.mime4j.field.FieldName;
import org.apache.james.mime4j.field.MailboxField;
import org.apache.james.mime4j.field.MailboxListField;
import org.apache.james.mime4j.field.UnstructuredField;
import org.apache.james.mime4j.field.address.Address;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.impl.ContentTransferEncodingFieldImpl;
import org.apache.james.mime4j.field.impl.ContentTypeFieldImpl;
import org.apache.james.mime4j.field.impl.Fields;
import org.apache.james.mime4j.message.Body;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.SingleBody;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.james.mime4j.parser.impl.MimeStreamParser;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * Represents a MIME message. The following code parses a stream into a
 * <code>Message</code> object.
 * 
 * <pre>
 * Message msg = new Message(new FileInputStream(&quot;mime.msg&quot;));
 * </pre>
 */
public class MessageImpl extends Message {

    /**
     * Creates a new empty <code>Message</code>.
     */
    public MessageImpl() {
    }

    /**
     * Creates a new <code>Message</code> from the specified
     * <code>Message</code>. The <code>Message</code> instance is
     * initialized with copies of header and body of the specified
     * <code>Message</code>. The parent entity of the new message is
     * <code>null</code>.
     * 
     * @param other
     *            message to copy.
     * @throws UnsupportedOperationException
     *             if <code>other</code> contains a {@link SingleBody} that
     *             does not support the {@link SingleBody#copy() copy()}
     *             operation.
     * @throws IllegalArgumentException
     *             if <code>other</code> contains a <code>Body</code> that
     *             is neither a {@link MessageImpl}, {@link Multipart} or
     *             {@link SingleBody}.
     */
    public MessageImpl(Message other) {
        if (other.getHeader() != null) {
            setHeader(new Header(other.getHeader()));
        }

        if (other.getBody() != null) {
            Body bodyCopy = BodyCopier.copy(other.getBody());
            setBody(bodyCopy);
        }
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance.
     * 
     * @param is
     *            the stream to parse.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public MessageImpl(InputStream is) throws IOException, MimeIOException {
        this(is, null, DefaultStorageProvider.getInstance());
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig}.
     * 
     * @param is
     *            the stream to parse.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public MessageImpl(InputStream is, MimeEntityConfig config) throws IOException,
            MimeIOException {
        this(is, config, DefaultStorageProvider.getInstance());
    }

    /**
     * Parses the specified MIME message stream into a <code>Message</code>
     * instance using given {@link MimeEntityConfig} and {@link StorageProvider}.
     * 
     * @param is
     *            the stream to parse.
     * @param config
     *            {@link MimeEntityConfig} to use.
     * @param storageProvider
     *            {@link StorageProvider} to use for storing text and binary
     *            message bodies.
     * @throws IOException
     *             on I/O errors.
     * @throws MimeIOException
     *             on MIME protocol violations.
     */
    public MessageImpl(InputStream is, MimeEntityConfig config,
            StorageProvider storageProvider) throws IOException,
            MimeIOException {
        try {
            MimeStreamParser parser = new MimeStreamParser(config);
            parser.setContentDecoding(true);
            parser.setContentHandler(new MessageBuilder(this, storageProvider));
            parser.parse(is);
        } catch (MimeException e) {
            throw new MimeIOException(e);
        }
    }

    /**
     * @see org.apache.james.mime4j.message.impl.Message#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream out) throws IOException {
        MessageWriter.DEFAULT.writeEntity(this, out);
    }

	@Override
	protected String newUniqueBoundary() {
		return MimeUtil.createUniqueBoundary();
	}

	protected UnstructuredField newMessageId(String hostname) {
		return Fields.messageId(hostname);
	}

	protected DateTimeField newDate(Date date, TimeZone zone) {
		return Fields.date(FieldName.DATE, date, zone);
	}

	protected MailboxField newMailbox(String fieldName, Mailbox mailbox) {
		return Fields.mailbox(fieldName, mailbox);
	}

	protected MailboxListField newMailboxList(String fieldName,
			Collection<Mailbox> mailboxes) {
		return Fields.mailboxList(fieldName, mailboxes);
	}

	protected AddressListField newAddressList(String fieldName,
			Collection<Address> addresses) {
		return Fields.addressList(fieldName, addresses);
	}

	protected UnstructuredField newSubject(String subject) {
		return Fields.subject(subject);
	}

    protected ContentDispositionField newContentDisposition(
            String dispositionType, String filename, long size,
            Date creationDate, Date modificationDate, Date readDate) {
        return Fields.contentDisposition(dispositionType, filename, size,
                creationDate, modificationDate, readDate);
    }

    protected ContentDispositionField newContentDisposition(
            String dispositionType, Map<String, String> parameters) {
        return Fields.contentDisposition(dispositionType, parameters);
    }

    protected ContentTypeField newContentType(String mimeType,
            Map<String, String> parameters) {
        return Fields.contentType(mimeType, parameters);
    }

    protected ContentTransferEncodingField newContentTransferEncoding(
            String contentTransferEncoding) {
        return Fields.contentTransferEncoding(contentTransferEncoding);
    }

    protected String calcTransferEncoding(ContentTransferEncodingField f) {
        return ContentTransferEncodingFieldImpl.getEncoding(f);
    }

    protected String calcMimeType(ContentTypeField child, ContentTypeField parent) {
        return ContentTypeFieldImpl.getMimeType(child, parent);
    }

    protected String calcCharset(ContentTypeField contentType) {
        return ContentTypeFieldImpl.getCharset(contentType); 
    }

}