package com.airbnb.lottie.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* loaded from: classes.dex */
public class KeyPath {
    private final List<String> keys;
    private KeyPathElement resolvedElement;

    public KeyPath(String... keys) {
        this.keys = Arrays.asList(keys);
    }

    private KeyPath(KeyPath keyPath) {
        this.keys = new ArrayList(keyPath.keys);
        this.resolvedElement = keyPath.resolvedElement;
    }

    public KeyPath addKey(String key) {
        KeyPath newKeyPath = new KeyPath(this);
        newKeyPath.keys.add(key);
        return newKeyPath;
    }

    public KeyPath resolve(KeyPathElement element) {
        KeyPath keyPath = new KeyPath(this);
        keyPath.resolvedElement = element;
        return keyPath;
    }

    public KeyPathElement getResolvedElement() {
        return this.resolvedElement;
    }

    public boolean matches(String key, int depth) {
        if (isContainer(key)) {
            return true;
        }
        if (depth >= this.keys.size()) {
            return false;
        }
        return this.keys.get(depth).equals(key) || this.keys.get(depth).equals("**") || this.keys.get(depth).equals("*");
    }

    public int incrementDepthBy(String key, int depth) {
        if (isContainer(key)) {
            return 0;
        }
        if (this.keys.get(depth).equals("**")) {
            return (depth != this.keys.size() - 1 && this.keys.get(depth + 1).equals(key)) ? 2 : 0;
        }
        return 1;
    }

    public boolean fullyResolvesTo(String key, int depth) {
        if (depth >= this.keys.size()) {
            return false;
        }
        boolean isLastDepth = depth == this.keys.size() - 1;
        String keyAtDepth = this.keys.get(depth);
        boolean isGlobstar = keyAtDepth.equals("**");
        if (!isGlobstar) {
            boolean matches = keyAtDepth.equals(key) || keyAtDepth.equals("*");
            return (isLastDepth || (depth == this.keys.size() + (-2) && endsWithGlobstar())) && matches;
        }
        boolean isGlobstarButNextKeyMatches = !isLastDepth && this.keys.get(depth + 1).equals(key);
        if (isGlobstarButNextKeyMatches) {
            return depth == this.keys.size() + (-2) || (depth == this.keys.size() + (-3) && endsWithGlobstar());
        }
        if (isLastDepth) {
            return true;
        }
        if (depth + 1 < this.keys.size() - 1) {
            return false;
        }
        return this.keys.get(depth + 1).equals(key);
    }

    public boolean propagateToChildren(String key, int depth) {
        return "__container".equals(key) || depth < this.keys.size() - 1 || this.keys.get(depth).equals("**");
    }

    private boolean isContainer(String key) {
        return "__container".equals(key);
    }

    private boolean endsWithGlobstar() {
        return this.keys.get(r0.size() - 1).equals("**");
    }

    public String keysToString() {
        return this.keys.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("KeyPath{keys=");
        sb.append(this.keys);
        sb.append(",resolved=");
        sb.append(this.resolvedElement != null);
        sb.append('}');
        return sb.toString();
    }
}
