/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.dict;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

//import org.apache.commons.lang.StringUtils;

/**
 * A dictionary for date string (date only, no time).
 * 
 * Dates are numbered from 0000-1-1 -- 0 for "0000-1-1", 1 for "0000-1-2", 2 for "0000-1-3" and
 * up to 3652426 for "9999-12-31".
 * 
 * Note the implementation is not thread-safe.
 * 
 * @author yangli9
 */
public class DateStrDictionary extends Dictionary<String> {

    static final int ID_9999_12_31 = 3652426; // assume 0 based

    // ============================================================================

    private String pattern;
    private int baseId;
    private int maxId;

    public DateStrDictionary() {
        init(DateFormat.DEFAULT_DATE_PATTERN, 0);
    }

    public DateStrDictionary(String datePattern, int baseId) {
        init(datePattern, baseId);
    }

    private void init(String datePattern, int baseId) {
        this.pattern = datePattern;
        this.baseId = baseId;
        this.maxId = baseId + ID_9999_12_31;
    }

    @Override
    public int getMinId() {
        return baseId;
    }

    @Override
    public int getMaxId() {
        return maxId;
    }

    @Override
    public int getSizeOfId() {
        return 3;
    }

    @Override
    public int getSizeOfValue() {
        return pattern.length();
    }

    @Override
    protected boolean isNullByteForm(byte[] value, int offset, int len) {
        return value == null || len == 0;
    }

    @Override
    final protected int getIdFromValueImpl(String value, int roundFlag) {
        Date date = DateFormat.stringToDate(value, pattern);
        int id = calcIdFromSeqNo(getNumOfDaysSince0000(date));
        if (id < baseId || id > maxId)
            throw new IllegalArgumentException("'" + value + "' encodes to '" + id + "' which is out of range [" + baseId + "," + maxId + "]");

        return id;
    }

    @Override
    final protected String getValueFromIdImpl(int id) {
        if (id < baseId || id > maxId)
            throw new IllegalArgumentException("ID '" + id + "' is out of range [" + baseId + "," + maxId + "]");
        Date d = getDateFromNumOfDaysSince0000(calcSeqNoFromId(id));
        return DateFormat.dateToString(d, pattern);
    }

    private int getNumOfDaysSince0000(Date d) {
        // 86400000 = 1000 * 60 * 60 * 24
        // -719530 is offset of 0000-01-01
        return (int) (d.getTime() / 86400000 + 719530);
    }

    private Date getDateFromNumOfDaysSince0000(int n) {
        long millis = ((long) n - 719530) * 86400000;
        return new Date(millis);
    }

    @Override
    final protected int getIdFromValueBytesImpl(byte[] value, int offset, int len, int roundingFlag) {
        try {
            return getIdFromValue(new String(value, offset, len, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // never happen
        }
    }

    @Override
    final protected int getValueBytesFromIdImpl(int id, byte[] returnValue, int offset) {
        String date = getValueFromId(id);
        byte bytes[];
        try {
            bytes = date.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // never happen
        }
        System.arraycopy(bytes, 0, returnValue, offset, bytes.length);
        return bytes.length;
    }

    private int calcIdFromSeqNo(int seq) {
        return seq < 0 ? seq : baseId + seq;
    }

    private int calcSeqNoFromId(int id) {
        return id - baseId;
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(pattern);
        out.writeInt(baseId);
    }

    public void readFields(DataInput in) throws IOException {
        String pattern = in.readUTF();
        int baseId = in.readInt();
        init(pattern, baseId);
    }

    @Override
    public int hashCode() {
        return 31 * baseId + pattern.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof DateStrDictionary) == false)
            return false;
        DateStrDictionary that = (DateStrDictionary) o;
        return this.pattern == that.pattern && this.baseId == that.baseId;
    }

    @Override
    public void dump(PrintStream out) {
        out.println(this.toString());
    }

    @Override
    public String toString() {
        return "DateStrDictionary [pattern=" + pattern + ", baseId=" + baseId + "]";
    }

}
