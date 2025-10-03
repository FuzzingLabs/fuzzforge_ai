package com.google.firebase.database.tubesock;

import com.google.common.base.Ascii;
import com.google.firebase.database.tubesock.MessageBuilderFactory;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

/* loaded from: classes.dex */
class WebSocketReceiver {
    private MessageBuilderFactory.Builder pendingBuilder;
    private WebSocket websocket;
    private DataInputStream input = null;
    private WebSocketEventHandler eventHandler = null;
    private byte[] inputHeader = new byte[112];
    private volatile boolean stop = false;

    WebSocketReceiver(WebSocket websocket) {
        this.websocket = null;
        this.websocket = websocket;
    }

    void setInput(DataInputStream input) {
        this.input = input;
    }

    void run() {
        int offset;
        byte[] bArr;
        boolean fin;
        boolean rsv;
        this.eventHandler = this.websocket.getEventHandler();
        while (!this.stop) {
            try {
                offset = 0 + read(this.inputHeader, 0, 1);
                bArr = this.inputHeader;
                fin = (bArr[0] & 128) != 0;
                rsv = (bArr[0] & 112) != 0;
            } catch (WebSocketException e) {
                handleError(e);
            } catch (SocketTimeoutException e2) {
            } catch (IOException ioe) {
                handleError(new WebSocketException("IO Error", ioe));
            }
            if (rsv) {
                throw new WebSocketException("Invalid frame received");
            }
            byte opcode = (byte) (bArr[0] & Ascii.f166SI);
            int offset2 = offset + read(bArr, offset, 1);
            byte[] bArr2 = this.inputHeader;
            byte length = bArr2[1];
            long payload_length = 0;
            if (length < 126) {
                payload_length = length;
            } else if (length == 126) {
                int read = offset2 + read(bArr2, offset2, 2);
                byte[] bArr3 = this.inputHeader;
                payload_length = ((bArr3[2] & 255) << 8) | (bArr3[3] & 255);
            } else if (length == Byte.MAX_VALUE) {
                payload_length = parseLong(this.inputHeader, (offset2 + read(bArr2, offset2, 8)) - 8);
            }
            byte[] payload = new byte[(int) payload_length];
            read(payload, 0, (int) payload_length);
            if (opcode == 8) {
                this.websocket.onCloseOpReceived();
            } else if (opcode != 10) {
                if (opcode != 1 && opcode != 2 && opcode != 9 && opcode != 0) {
                    throw new WebSocketException("Unsupported opcode: " + ((int) opcode));
                }
                appendBytes(fin, opcode, payload);
            }
        }
    }

    private void appendBytes(boolean fin, byte opcode, byte[] data) {
        if (opcode == 9) {
            if (fin) {
                handlePing(data);
                return;
            }
            throw new WebSocketException("PING must not fragment across frames");
        }
        MessageBuilderFactory.Builder builder = this.pendingBuilder;
        if (builder != null && opcode != 0) {
            throw new WebSocketException("Failed to continue outstanding frame");
        }
        if (builder == null && opcode == 0) {
            throw new WebSocketException("Received continuing frame, but there's nothing to continue");
        }
        if (builder == null) {
            this.pendingBuilder = MessageBuilderFactory.builder(opcode);
        }
        if (!this.pendingBuilder.appendBytes(data)) {
            throw new WebSocketException("Failed to decode frame");
        }
        if (fin) {
            WebSocketMessage message = this.pendingBuilder.toMessage();
            this.pendingBuilder = null;
            if (message == null) {
                throw new WebSocketException("Failed to decode whole message");
            }
            this.eventHandler.onMessage(message);
        }
    }

    private void handlePing(byte[] payload) {
        if (payload.length <= 125) {
            this.websocket.pong(payload);
            return;
        }
        throw new WebSocketException("PING frame too long");
    }

    private long parseLong(byte[] buffer, int offset) {
        return (buffer[offset + 0] << 56) + ((buffer[offset + 1] & 255) << 48) + ((buffer[offset + 2] & 255) << 40) + ((buffer[offset + 3] & 255) << 32) + ((buffer[offset + 4] & 255) << 24) + ((buffer[offset + 5] & 255) << 16) + ((buffer[offset + 6] & 255) << 8) + ((buffer[offset + 7] & 255) << 0);
    }

    private int read(byte[] buffer, int offset, int length) throws IOException {
        this.input.readFully(buffer, offset, length);
        return length;
    }

    void stopit() {
        this.stop = true;
    }

    boolean isRunning() {
        return !this.stop;
    }

    private void handleError(WebSocketException e) {
        stopit();
        this.websocket.handleReceiverError(e);
    }
}
