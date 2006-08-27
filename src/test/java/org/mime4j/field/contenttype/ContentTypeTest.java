/*
 *  Copyright 2004 the mime4j project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mime4j.field.contenttype;

import org.mime4j.field.contenttype.parser.ContentTypeParser;
import org.mime4j.field.contenttype.parser.ParseException;

import java.io.StringReader;

import junit.framework.TestCase;

public class ContentTypeTest extends TestCase {

    public void testContentType() throws ParseException {
        test("one/two; three          =  four", "one", "two");
        test("one/(foo)two; three          =  \"four\"", "one", "two");
        test("one(foo)/two; three          =  (foo) four", "one", "two");
        test("one/two; three          =  four", "one", "two");

        // TODO: add more tests
    }

    private void test(String val, String expectedType, String expectedSubtype) throws ParseException {
        ContentTypeParser parser = new ContentTypeParser(new StringReader(val));
        parser.parseAll();

        String type = parser.getType();
        String subtype = parser.getSubType();

        assertEquals(expectedType, type);
        assertEquals(expectedSubtype, subtype);
    }

}