package com.google.firebase.database.tubesock;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
class MessageBuilderFactory {

    interface Builder {
        boolean appendBytes(byte[] bArr);

        WebSocketMessage toMessage();
    }

    MessageBuilderFactory() {
    }

    static class BinaryBuilder implements Builder {
        private int pendingByteCount = 0;
        private List<byte[]> pendingBytes = new ArrayList();

        BinaryBuilder() {
        }

        @Override // com.google.firebase.database.tubesock.MessageBuilderFactory.Builder
        public boolean appendBytes(byte[] bytes) {
            this.pendingBytes.add(bytes);
            this.pendingByteCount += bytes.length;
            return true;
        }

        @Override // com.google.firebase.database.tubesock.MessageBuilderFactory.Builder
        public WebSocketMessage toMessage() {
            byte[] payload = new byte[this.pendingByteCount];
            int offset = 0;
            for (int i = 0; i < this.pendingBytes.size(); i++) {
                byte[] segment = this.pendingBytes.get(i);
                System.arraycopy(segment, 0, payload, offset, segment.length);
                offset += segment.length;
            }
            return new WebSocketMessage(payload);
        }
    }

    static class TextBuilder implements Builder {
        private static ThreadLocal<CharsetDecoder> localDecoder = new ThreadLocal<CharsetDecoder>() { // from class: com.google.firebase.database.tubesock.MessageBuilderFactory.TextBuilder.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // java.lang.ThreadLocal
            public CharsetDecoder initialValue() {
                Charset utf8 = Charset.forName("UTF8");
                CharsetDecoder decoder = utf8.newDecoder();
                decoder.onMalformedInput(CodingErrorAction.REPORT);
                decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                return decoder;
            }
        };
        private static ThreadLocal<CharsetEncoder> localEncoder = new ThreadLocal<CharsetEncoder>() { // from class: com.google.firebase.database.tubesock.MessageBuilderFactory.TextBuilder.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // java.lang.ThreadLocal
            public CharsetEncoder initialValue() {
                Charset utf8 = Charset.forName("UTF8");
                CharsetEncoder encoder = utf8.newEncoder();
                encoder.onMalformedInput(CodingErrorAction.REPORT);
                encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                return encoder;
            }
        };
        private StringBuilder builder = new StringBuilder();
        private ByteBuffer carryOver;

        TextBuilder() {
        }

        @Override // com.google.firebase.database.tubesock.MessageBuilderFactory.Builder
        public boolean appendBytes(byte[] bytes) {
            String nextFrame = decodeString(bytes);
            if (nextFrame != null) {
                this.builder.append(nextFrame);
                return true;
            }
            return false;
        }

        @Override // com.google.firebase.database.tubesock.MessageBuilderFactory.Builder
        public WebSocketMessage toMessage() {
            if (this.carryOver != null) {
                return null;
            }
            return new WebSocketMessage(this.builder.toString());
        }

        private String decodeString(byte[] bytes) {
            try {
                ByteBuffer input = ByteBuffer.wrap(bytes);
                CharBuffer buf = localDecoder.get().decode(input);
                String text = buf.toString();
                return text;
            } catch (CharacterCodingException e) {
                return null;
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:18:0x003b, code lost:
        
            if (r1.remaining() <= 0) goto L12;
         */
        /* JADX WARN: Code restructure failed: missing block: B:19:0x003d, code lost:
        
            r6.carryOver = r1;
         */
        /* JADX WARN: Code restructure failed: missing block: B:20:0x003f, code lost:
        
            r4 = java.nio.CharBuffer.wrap(r3);
            com.google.firebase.database.tubesock.MessageBuilderFactory.TextBuilder.localEncoder.get().encode(r4);
            r3.flip();
            r0 = r3.toString();
         */
        /* JADX WARN: Code restructure failed: missing block: B:21:0x0055, code lost:
        
            return r0;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        private java.lang.String decodeStringStreaming(byte[] r7) {
            /*
                r6 = this;
                r0 = 0
                java.nio.ByteBuffer r1 = r6.getBuffer(r7)     // Catch: java.nio.charset.CharacterCodingException -> L6d
                int r2 = r1.remaining()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                float r2 = (float) r2     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.lang.ThreadLocal<java.nio.charset.CharsetDecoder> r3 = com.google.firebase.database.tubesock.MessageBuilderFactory.TextBuilder.localDecoder     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.lang.Object r3 = r3.get()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.nio.charset.CharsetDecoder r3 = (java.nio.charset.CharsetDecoder) r3     // Catch: java.nio.charset.CharacterCodingException -> L6d
                float r3 = r3.averageCharsPerByte()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                float r2 = r2 * r3
                int r2 = (int) r2     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.nio.CharBuffer r3 = java.nio.CharBuffer.allocate(r2)     // Catch: java.nio.charset.CharacterCodingException -> L6d
            L1c:
                java.lang.ThreadLocal<java.nio.charset.CharsetDecoder> r4 = com.google.firebase.database.tubesock.MessageBuilderFactory.TextBuilder.localDecoder     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.lang.Object r4 = r4.get()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.nio.charset.CharsetDecoder r4 = (java.nio.charset.CharsetDecoder) r4     // Catch: java.nio.charset.CharacterCodingException -> L6d
                r5 = 0
                java.nio.charset.CoderResult r4 = r4.decode(r1, r3, r5)     // Catch: java.nio.charset.CharacterCodingException -> L6d
                boolean r5 = r4.isError()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                if (r5 == 0) goto L30
                return r0
            L30:
                boolean r5 = r4.isUnderflow()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                if (r5 == 0) goto L56
            L37:
                int r4 = r1.remaining()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                if (r4 <= 0) goto L3f
                r6.carryOver = r1     // Catch: java.nio.charset.CharacterCodingException -> L6d
            L3f:
                java.nio.CharBuffer r4 = java.nio.CharBuffer.wrap(r3)     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.lang.ThreadLocal<java.nio.charset.CharsetEncoder> r5 = com.google.firebase.database.tubesock.MessageBuilderFactory.TextBuilder.localEncoder     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.lang.Object r5 = r5.get()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.nio.charset.CharsetEncoder r5 = (java.nio.charset.CharsetEncoder) r5     // Catch: java.nio.charset.CharacterCodingException -> L6d
                r5.encode(r4)     // Catch: java.nio.charset.CharacterCodingException -> L6d
                r3.flip()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                java.lang.String r0 = r3.toString()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                return r0
            L56:
                boolean r5 = r4.isOverflow()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                if (r5 == 0) goto L6c
                int r5 = r2 * 2
                int r5 = r5 + 1
                java.nio.CharBuffer r2 = java.nio.CharBuffer.allocate(r5)     // Catch: java.nio.charset.CharacterCodingException -> L6d
                r3.flip()     // Catch: java.nio.charset.CharacterCodingException -> L6d
                r2.put(r3)     // Catch: java.nio.charset.CharacterCodingException -> L6d
                r3 = r2
                r2 = r5
            L6c:
                goto L1c
            L6d:
                r1 = move-exception
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.database.tubesock.MessageBuilderFactory.TextBuilder.decodeStringStreaming(byte[]):java.lang.String");
        }

        private ByteBuffer getBuffer(byte[] bytes) {
            ByteBuffer byteBuffer = this.carryOver;
            if (byteBuffer != null) {
                ByteBuffer buffer = ByteBuffer.allocate(bytes.length + byteBuffer.remaining());
                buffer.put(this.carryOver);
                this.carryOver = null;
                buffer.put(bytes);
                buffer.flip();
                return buffer;
            }
            return ByteBuffer.wrap(bytes);
        }
    }

    static Builder builder(byte opcode) {
        if (opcode == 2) {
            return new BinaryBuilder();
        }
        return new TextBuilder();
    }
}
