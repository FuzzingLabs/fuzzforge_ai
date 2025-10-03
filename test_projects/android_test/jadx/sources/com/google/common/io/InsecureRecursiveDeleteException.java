package com.google.common.io;

import java.nio.file.FileSystemException;

/* loaded from: classes.dex */
public final class InsecureRecursiveDeleteException extends FileSystemException {
    public InsecureRecursiveDeleteException(String file) {
        super(file, null, "unable to guarantee security of recursive delete");
    }
}
