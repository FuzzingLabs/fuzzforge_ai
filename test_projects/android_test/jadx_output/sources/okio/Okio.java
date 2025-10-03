package okio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import kotlin.Metadata;

@Metadata(m236bv = {1, 0, 3}, m237d1 = {"okio/Okio__JvmOkioKt", "okio/Okio__OkioKt"}, m239k = 4, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class Okio {
    public static final Sink appendingSink(File $this$appendingSink) throws FileNotFoundException {
        return Okio__JvmOkioKt.appendingSink($this$appendingSink);
    }

    public static final Sink blackhole() {
        return Okio__OkioKt.blackhole();
    }

    public static final BufferedSink buffer(Sink $this$buffer) {
        return Okio__OkioKt.buffer($this$buffer);
    }

    public static final BufferedSource buffer(Source $this$buffer) {
        return Okio__OkioKt.buffer($this$buffer);
    }

    public static final boolean isAndroidGetsocknameError(AssertionError $this$isAndroidGetsocknameError) {
        return Okio__JvmOkioKt.isAndroidGetsocknameError($this$isAndroidGetsocknameError);
    }

    public static final Sink sink(File file) throws FileNotFoundException {
        return Okio__JvmOkioKt.sink$default(file, false, 1, null);
    }

    public static final Sink sink(File $this$sink, boolean append) throws FileNotFoundException {
        return Okio__JvmOkioKt.sink($this$sink, append);
    }

    public static final Sink sink(OutputStream $this$sink) {
        return Okio__JvmOkioKt.sink($this$sink);
    }

    public static final Sink sink(Socket $this$sink) throws IOException {
        return Okio__JvmOkioKt.sink($this$sink);
    }

    public static final Sink sink(Path $this$sink, OpenOption... options) throws IOException {
        return Okio__JvmOkioKt.sink($this$sink, options);
    }

    public static final Source source(File $this$source) throws FileNotFoundException {
        return Okio__JvmOkioKt.source($this$source);
    }

    public static final Source source(InputStream $this$source) {
        return Okio__JvmOkioKt.source($this$source);
    }

    public static final Source source(Socket $this$source) throws IOException {
        return Okio__JvmOkioKt.source($this$source);
    }

    public static final Source source(Path $this$source, OpenOption... options) throws IOException {
        return Okio__JvmOkioKt.source($this$source, options);
    }
}
