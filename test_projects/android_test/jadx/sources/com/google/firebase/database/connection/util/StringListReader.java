package com.google.firebase.database.connection.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class StringListReader extends Reader {
    private List<String> strings;
    private boolean closed = false;
    private int charPos;
    private int markedCharPos = this.charPos;
    private int stringListPos;
    private int markedStringListPos = this.stringListPos;
    private boolean frozen = false;

    public StringListReader() {
        this.strings = null;
        this.strings = new ArrayList();
    }

    public void addString(String string) {
        if (this.frozen) {
            throw new IllegalStateException("Trying to add string after reading");
        }
        if (string.length() > 0) {
            this.strings.add(string);
        }
    }

    public void freeze() {
        if (this.frozen) {
            throw new IllegalStateException("Trying to freeze frozen StringListReader");
        }
        this.frozen = true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String string : this.strings) {
            builder.append(string);
        }
        return builder.toString();
    }

    @Override // java.io.Reader
    public void reset() throws IOException {
        this.charPos = this.markedCharPos;
        this.stringListPos = this.markedStringListPos;
    }

    private String currentString() {
        if (this.stringListPos < this.strings.size()) {
            return this.strings.get(this.stringListPos);
        }
        return null;
    }

    private int currentStringRemainingChars() {
        String current = currentString();
        if (current == null) {
            return 0;
        }
        return current.length() - this.charPos;
    }

    private void checkState() throws IOException {
        if (this.closed) {
            throw new IOException("Stream already closed");
        }
        if (!this.frozen) {
            throw new IOException("Reader needs to be frozen before read operations can be called");
        }
    }

    private long advance(long numChars) {
        long advanced = 0;
        while (this.stringListPos < this.strings.size() && advanced < numChars) {
            int remainingStringChars = currentStringRemainingChars();
            long remainingChars = numChars - advanced;
            if (remainingChars < remainingStringChars) {
                this.charPos = (int) (this.charPos + remainingChars);
                advanced += remainingChars;
            } else {
                advanced += remainingStringChars;
                this.charPos = 0;
                this.stringListPos++;
            }
        }
        return advanced;
    }

    @Override // java.io.Reader, java.lang.Readable
    public int read(CharBuffer target) throws IOException {
        checkState();
        int remaining = target.remaining();
        int total = 0;
        String current = currentString();
        while (remaining > 0 && current != null) {
            int strLength = Math.min(current.length() - this.charPos, remaining);
            String str = this.strings.get(this.stringListPos);
            int i = this.charPos;
            target.put(str, i, i + strLength);
            remaining -= strLength;
            total += strLength;
            advance(strLength);
            current = currentString();
        }
        if (total > 0 || current != null) {
            return total;
        }
        return -1;
    }

    @Override // java.io.Reader
    public int read() throws IOException {
        checkState();
        String current = currentString();
        if (current == null) {
            return -1;
        }
        char c = current.charAt(this.charPos);
        advance(1L);
        return c;
    }

    @Override // java.io.Reader
    public long skip(long n) throws IOException {
        checkState();
        return advance(n);
    }

    @Override // java.io.Reader
    public boolean ready() throws IOException {
        checkState();
        return true;
    }

    @Override // java.io.Reader
    public boolean markSupported() {
        return true;
    }

    @Override // java.io.Reader
    public void mark(int readAheadLimit) throws IOException {
        checkState();
        this.markedCharPos = this.charPos;
        this.markedStringListPos = this.stringListPos;
    }

    @Override // java.io.Reader
    public int read(char[] cbuf, int off, int len) throws IOException {
        checkState();
        int charsCopied = 0;
        String current = currentString();
        while (current != null && charsCopied < len) {
            int copyLength = Math.min(currentStringRemainingChars(), len - charsCopied);
            int i = this.charPos;
            current.getChars(i, i + copyLength, cbuf, off + charsCopied);
            charsCopied += copyLength;
            advance(copyLength);
            current = currentString();
        }
        if (charsCopied > 0 || current != null) {
            return charsCopied;
        }
        return -1;
    }

    @Override // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        checkState();
        this.closed = true;
    }
}
