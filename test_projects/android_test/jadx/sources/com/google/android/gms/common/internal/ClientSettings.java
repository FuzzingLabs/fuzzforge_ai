package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.content.Context;
import android.view.View;
import androidx.collection.ArraySet;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.signin.SignInOptions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class ClientSettings {
    public static final String KEY_CLIENT_SESSION_ID = "com.google.android.gms.common.internal.ClientSettings.sessionId";
    private final Account account;
    private final Set<Scope> zaof;
    private final Set<Scope> zaog;
    private final Map<Api<?>, OptionalApiSettings> zaoh;
    private final int zaoi;
    private final View zaoj;
    private final String zaok;
    private final String zaol;
    private final SignInOptions zaom;
    private final boolean zaon;
    private Integer zaoo;

    /* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
    public static final class OptionalApiSettings {
        public final Set<Scope> mScopes;

        public OptionalApiSettings(Set<Scope> set) {
            Preconditions.checkNotNull(set);
            this.mScopes = Collections.unmodifiableSet(set);
        }
    }

    public static ClientSettings createDefault(Context context) {
        return new GoogleApiClient.Builder(context).buildClientSettings();
    }

    public ClientSettings(Account account, Set<Scope> set, Map<Api<?>, OptionalApiSettings> map, int i, View view, String str, String str2, SignInOptions signInOptions) {
        this(account, set, map, i, view, str, str2, signInOptions, false);
    }

    /* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
    public static final class Builder {
        private Account account;
        private Map<Api<?>, OptionalApiSettings> zaoh;
        private View zaoj;
        private String zaok;
        private String zaol;
        private ArraySet<Scope> zaop;
        private boolean zaoq;
        private int zaoi = 0;
        private SignInOptions zaom = SignInOptions.DEFAULT;

        public final Builder setAccount(Account account) {
            this.account = account;
            return this;
        }

        public final Builder addRequiredScope(Scope scope) {
            if (this.zaop == null) {
                this.zaop = new ArraySet<>();
            }
            this.zaop.add(scope);
            return this;
        }

        public final Builder addAllRequiredScopes(Collection<Scope> collection) {
            if (this.zaop == null) {
                this.zaop = new ArraySet<>();
            }
            this.zaop.addAll(collection);
            return this;
        }

        public final Builder setOptionalApiSettingsMap(Map<Api<?>, OptionalApiSettings> map) {
            this.zaoh = map;
            return this;
        }

        public final Builder setGravityForPopups(int i) {
            this.zaoi = i;
            return this;
        }

        public final Builder setViewForPopups(View view) {
            this.zaoj = view;
            return this;
        }

        public final Builder setRealClientPackageName(String str) {
            this.zaok = str;
            return this;
        }

        public final Builder setRealClientClassName(String str) {
            this.zaol = str;
            return this;
        }

        public final Builder setSignInOptions(SignInOptions signInOptions) {
            this.zaom = signInOptions;
            return this;
        }

        public final Builder enableSignInClientDisconnectFix() {
            this.zaoq = true;
            return this;
        }

        public final ClientSettings build() {
            return new ClientSettings(this.account, this.zaop, this.zaoh, this.zaoi, this.zaoj, this.zaok, this.zaol, this.zaom, this.zaoq);
        }
    }

    public ClientSettings(Account account, Set<Scope> set, Map<Api<?>, OptionalApiSettings> map, int i, View view, String str, String str2, SignInOptions signInOptions, boolean z) {
        this.account = account;
        Set<Scope> emptySet = set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
        this.zaof = emptySet;
        map = map == null ? Collections.emptyMap() : map;
        this.zaoh = map;
        this.zaoj = view;
        this.zaoi = i;
        this.zaok = str;
        this.zaol = str2;
        this.zaom = signInOptions;
        this.zaon = z;
        HashSet hashSet = new HashSet(emptySet);
        Iterator<OptionalApiSettings> it = map.values().iterator();
        while (it.hasNext()) {
            hashSet.addAll(it.next().mScopes);
        }
        this.zaog = Collections.unmodifiableSet(hashSet);
    }

    @Nullable
    @Deprecated
    public final String getAccountName() {
        Account account = this.account;
        if (account != null) {
            return account.name;
        }
        return null;
    }

    @Nullable
    public final Account getAccount() {
        return this.account;
    }

    public final Account getAccountOrDefault() {
        Account account = this.account;
        if (account != null) {
            return account;
        }
        return new Account("<<default account>>", AccountType.GOOGLE);
    }

    public final int getGravityForPopups() {
        return this.zaoi;
    }

    public final Set<Scope> getRequiredScopes() {
        return this.zaof;
    }

    public final Set<Scope> getAllRequestedScopes() {
        return this.zaog;
    }

    public final Map<Api<?>, OptionalApiSettings> getOptionalApiSettings() {
        return this.zaoh;
    }

    @Nullable
    public final String getRealClientPackageName() {
        return this.zaok;
    }

    @Nullable
    public final String getRealClientClassName() {
        return this.zaol;
    }

    @Nullable
    public final View getViewForPopups() {
        return this.zaoj;
    }

    @Nullable
    public final SignInOptions getSignInOptions() {
        return this.zaom;
    }

    @Nullable
    public final Integer getClientSessionId() {
        return this.zaoo;
    }

    public final void setClientSessionId(Integer num) {
        this.zaoo = num;
    }

    public final Set<Scope> getApplicableScopes(Api<?> api) {
        OptionalApiSettings optionalApiSettings = this.zaoh.get(api);
        if (optionalApiSettings == null || optionalApiSettings.mScopes.isEmpty()) {
            return this.zaof;
        }
        HashSet hashSet = new HashSet(this.zaof);
        hashSet.addAll(optionalApiSettings.mScopes);
        return hashSet;
    }

    public final boolean isSignInClientDisconnectFixEnabled() {
        return this.zaon;
    }
}
