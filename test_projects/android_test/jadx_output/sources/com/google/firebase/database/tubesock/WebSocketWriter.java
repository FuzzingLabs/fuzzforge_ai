package com.google.firebase.database.tubesock;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* loaded from: classes.dex */
class WebSocketWriter {
    private WritableByteChannel channel;
    private BlockingQueue<ByteBuffer> pendingBuffers;
    private WebSocket websocket;
    private final Random random = new Random();
    private volatile boolean stop = false;
    private boolean closeSent = false;
    private final Thread innerThread = WebSocket.getThreadFactory().newThread(new Runnable() { // from class: com.google.firebase.database.tubesock.WebSocketWriter.1
        @Override // java.lang.Runnable
        public void run() {
            WebSocketWriter.this.runWriter();
        }
    });

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebSocketWriter(WebSocket websocket, String threadBaseName, int clientId) {
        WebSocket.getIntializer().setName(getInnerThread(), threadBaseName + "Writer-" + clientId);
        this.websocket = websocket;
        this.pendingBuffers = new LinkedBlockingQueue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setOutput(OutputStream output) {
        this.channel = Channels.newChannel(output);
    }

    private ByteBuffer frameInBuffer(byte opcode, boolean masking, byte[] data) throws IOException {
        int headerLength = 2;
        if (masking) {
            headerLength = 2 + 4;
        }
        int length = data.length;
        if (length >= 126) {
            if (length <= 65535) {
                headerLength += 2;
            } else {
                headerLength += 8;
            }
        }
        ByteBuffer frame = ByteBuffer.allocate(data.length + headerLength);
        byte startByte = (byte) (Byte.MIN_VALUE | opcode);
        frame.put(startByte);
        if (length < 126) {
            if (masking) {
                length |= 128;
            }
            frame.put((byte) length);
        } else if (length <= 65535) {
            int length_field = 126;
            if (masking) {
                length_field = 126 | 128;
            }
            frame.put((byte) length_field);
            frame.putShort((short) length);
        } else {
            int length_field2 = 127;
            if (masking) {
                length_field2 = 127 | 128;
            }
            frame.put((byte) length_field2);
            frame.putInt(0);
            frame.putInt(length);
        }
        if (masking) {
            byte[] mask = generateMask();
            frame.put(mask);
            for (int i = 0; i < data.length; i++) {
                frame.put((byte) (data[i] ^ mask[i % 4]));
            }
        }
        frame.flip();
        return frame;
    }

    private byte[] generateMask() {
        byte[] mask = new byte[4];
        this.random.nextBytes(mask);
        return mask;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x001c, code lost:
    
        r3.closeSent = true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public synchronized void send(byte r4, boolean r5, byte[] r6) throws java.io.IOException {
        /*
            r3 = this;
            monitor-enter(r3)
            java.nio.ByteBuffer r0 = r3.frameInBuffer(r4, r5, r6)     // Catch: java.lang.Throwable -> L26
            boolean r1 = r3.stop     // Catch: java.lang.Throwable -> L26
            r2 = 8
            if (r1 == 0) goto L1a
            boolean r1 = r3.closeSent     // Catch: java.lang.Throwable -> L26
            if (r1 != 0) goto L12
            if (r4 != r2) goto L12
            goto L1a
        L12:
            com.google.firebase.database.tubesock.WebSocketException r1 = new com.google.firebase.database.tubesock.WebSocketException     // Catch: java.lang.Throwable -> L26
            java.lang.String r2 = "Shouldn't be sending"
            r1.<init>(r2)     // Catch: java.lang.Throwable -> L26
            throw r1     // Catch: java.lang.Throwable -> L26
        L1a:
            if (r4 != r2) goto L1f
            r1 = 1
            r3.closeSent = r1     // Catch: java.lang.Throwable -> L26
        L1f:
            java.util.concurrent.BlockingQueue<java.nio.ByteBuffer> r1 = r3.pendingBuffers     // Catch: java.lang.Throwable -> L26
            r1.add(r0)     // Catch: java.lang.Throwable -> L26
            monitor-exit(r3)
            return
        L26:
            r4 = move-exception
            monitor-exit(r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.database.tubesock.WebSocketWriter.send(byte, boolean, byte[]):void");
    }

    private void writeMessage() throws InterruptedException, IOException {
        ByteBuffer msg = this.pendingBuffers.take();
        this.channel.write(msg);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stopIt() {
        this.stop = true;
    }

    private void handleError(WebSocketException e) {
        this.websocket.handleReceiverError(e);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runWriter() {
        while (!this.stop && !Thread.interrupted()) {
            try {
                writeMessage();
            } catch (IOException e) {
                handleError(new WebSocketException("IO Exception", e));
                return;
            } catch (InterruptedException e2) {
                return;
            }
        }
        for (int i = 0; i < this.pendingBuffers.size(); i++) {
            writeMessage();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Thread getInnerThread() {
        return this.innerThread;
    }
}
