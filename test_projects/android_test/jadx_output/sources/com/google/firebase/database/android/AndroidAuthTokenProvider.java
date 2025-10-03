package com.google.firebase.database.android;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.internal.IdTokenListener;
import com.google.firebase.auth.internal.InternalAuthProvider;
import com.google.firebase.database.core.TokenProvider;
import com.google.firebase.inject.Deferred;
import com.google.firebase.inject.Provider;
import com.google.firebase.internal.InternalTokenResult;
import com.google.firebase.internal.api.FirebaseNoSignedInUserException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public class AndroidAuthTokenProvider implements TokenProvider {
    private final Deferred<InternalAuthProvider> deferredAuthProvider;
    private final AtomicReference<InternalAuthProvider> internalAuth = new AtomicReference<>();

    public AndroidAuthTokenProvider(Deferred<InternalAuthProvider> deferredAuthProvider) {
        this.deferredAuthProvider = deferredAuthProvider;
        deferredAuthProvider.whenAvailable(new Deferred.DeferredHandler() { // from class: com.google.firebase.database.android.AndroidAuthTokenProvider$$ExternalSyntheticLambda3
            @Override // com.google.firebase.inject.Deferred.DeferredHandler
            public final void handle(Provider provider) {
                AndroidAuthTokenProvider.this.m226x1833a710(provider);
            }
        });
    }

    /* renamed from: lambda$new$0$com-google-firebase-database-android-AndroidAuthTokenProvider */
    public /* synthetic */ void m226x1833a710(Provider authProvider) {
        this.internalAuth.set((InternalAuthProvider) authProvider.get());
    }

    @Override // com.google.firebase.database.core.TokenProvider
    public void getToken(boolean forceRefresh, final TokenProvider.GetTokenCompletionListener listener) {
        InternalAuthProvider authProvider = this.internalAuth.get();
        if (authProvider != null) {
            Task<GetTokenResult> getTokenResult = authProvider.getAccessToken(forceRefresh);
            getTokenResult.addOnSuccessListener(new OnSuccessListener() { // from class: com.google.firebase.database.android.AndroidAuthTokenProvider$$ExternalSyntheticLambda1
                @Override // com.google.android.gms.tasks.OnSuccessListener
                public final void onSuccess(Object obj) {
                    TokenProvider.GetTokenCompletionListener.this.onSuccess(((GetTokenResult) obj).getToken());
                }
            }).addOnFailureListener(new OnFailureListener() { // from class: com.google.firebase.database.android.AndroidAuthTokenProvider$$ExternalSyntheticLambda0
                @Override // com.google.android.gms.tasks.OnFailureListener
                public final void onFailure(Exception exc) {
                    AndroidAuthTokenProvider.lambda$getToken$2(TokenProvider.GetTokenCompletionListener.this, exc);
                }
            });
        } else {
            listener.onSuccess(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$getToken$2(TokenProvider.GetTokenCompletionListener listener, Exception e) {
        if (isUnauthenticatedUsage(e)) {
            listener.onSuccess(null);
        } else {
            listener.onError(e.getMessage());
        }
    }

    @Override // com.google.firebase.database.core.TokenProvider
    public void addTokenChangeListener(final ExecutorService executorService, final TokenProvider.TokenChangeListener tokenListener) {
        this.deferredAuthProvider.whenAvailable(new Deferred.DeferredHandler() { // from class: com.google.firebase.database.android.AndroidAuthTokenProvider$$ExternalSyntheticLambda4
            @Override // com.google.firebase.inject.Deferred.DeferredHandler
            public final void handle(Provider provider) {
                ((InternalAuthProvider) provider.get()).addIdTokenListener(new IdTokenListener() { // from class: com.google.firebase.database.android.AndroidAuthTokenProvider$$ExternalSyntheticLambda2
                    @Override // com.google.firebase.auth.internal.IdTokenListener
                    public final void onIdTokenChanged(InternalTokenResult internalTokenResult) {
                        r1.execute(new Runnable() { // from class: com.google.firebase.database.android.AndroidAuthTokenProvider$$ExternalSyntheticLambda5
                            @Override // java.lang.Runnable
                            public final void run() {
                                TokenProvider.TokenChangeListener.this.onTokenChange(internalTokenResult.getToken());
                            }
                        });
                    }
                });
            }
        });
    }

    @Override // com.google.firebase.database.core.TokenProvider
    public void removeTokenChangeListener(TokenProvider.TokenChangeListener tokenListener) {
    }

    private static boolean isUnauthenticatedUsage(Exception e) {
        return (e instanceof FirebaseApiNotAvailableException) || (e instanceof FirebaseNoSignedInUserException);
    }
}
