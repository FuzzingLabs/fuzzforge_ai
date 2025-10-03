package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.IAccountAccessor;
import java.util.ArrayList;
import java.util.Set;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zaaq extends zaau {
    private final /* synthetic */ zaak zafz;
    private final ArrayList<Api.Client> zags;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public zaaq(zaak zaakVar, ArrayList<Api.Client> arrayList) {
        super(zaakVar, null);
        this.zafz = zaakVar;
        this.zags = arrayList;
    }

    @Override // com.google.android.gms.common.api.internal.zaau
    public final void zaal() {
        zabe zabeVar;
        Set<Scope> zaar;
        IAccountAccessor iAccountAccessor;
        zabe zabeVar2;
        zabeVar = this.zafz.zafv;
        zaaw zaawVar = zabeVar.zaeh;
        zaar = this.zafz.zaar();
        zaawVar.zahe = zaar;
        ArrayList<Api.Client> arrayList = this.zags;
        int size = arrayList.size();
        int i = 0;
        while (i < size) {
            Api.Client client = arrayList.get(i);
            i++;
            iAccountAccessor = this.zafz.zagj;
            zabeVar2 = this.zafz.zafv;
            client.getRemoteService(iAccountAccessor, zabeVar2.zaeh.zahe);
        }
    }
}
