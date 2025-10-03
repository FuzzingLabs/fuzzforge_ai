package com.google.firebase.database.core;

import com.google.firebase.database.core.view.QuerySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/* loaded from: classes.dex */
public class ZombieEventManager implements EventRegistrationZombieListener {
    private static ZombieEventManager defaultInstance = new ZombieEventManager();
    final HashMap<EventRegistration, List<EventRegistration>> globalEventRegistrations = new HashMap<>();

    private ZombieEventManager() {
    }

    public static ZombieEventManager getInstance() {
        return defaultInstance;
    }

    public void recordEventRegistration(EventRegistration registration) {
        synchronized (this.globalEventRegistrations) {
            List<EventRegistration> registrationList = this.globalEventRegistrations.get(registration);
            if (registrationList == null) {
                registrationList = new ArrayList();
                this.globalEventRegistrations.put(registration, registrationList);
            }
            registrationList.add(registration);
            if (!registration.getQuerySpec().isDefault()) {
                EventRegistration defaultRegistration = registration.clone(QuerySpec.defaultQueryAtPath(registration.getQuerySpec().getPath()));
                List<EventRegistration> registrationList2 = this.globalEventRegistrations.get(defaultRegistration);
                if (registrationList2 == null) {
                    registrationList2 = new ArrayList();
                    this.globalEventRegistrations.put(defaultRegistration, registrationList2);
                }
                registrationList2.add(registration);
            }
            registration.setIsUserInitiated(true);
            registration.setOnZombied(this);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:31:0x006a A[Catch: all -> 0x0084, TryCatch #0 {, blocks: (B:5:0x0004, B:8:0x000f, B:10:0x0015, B:14:0x001b, B:12:0x0020, B:15:0x0023, B:17:0x0029, B:20:0x0030, B:24:0x003a, B:26:0x0048, B:29:0x0064, B:31:0x006a, B:35:0x0070, B:33:0x0074, B:36:0x0077, B:38:0x007d, B:40:0x0082), top: B:4:0x0004 }] */
    /* JADX WARN: Removed duplicated region for block: B:38:0x007d A[Catch: all -> 0x0084, TryCatch #0 {, blocks: (B:5:0x0004, B:8:0x000f, B:10:0x0015, B:14:0x001b, B:12:0x0020, B:15:0x0023, B:17:0x0029, B:20:0x0030, B:24:0x003a, B:26:0x0048, B:29:0x0064, B:31:0x006a, B:35:0x0070, B:33:0x0074, B:36:0x0077, B:38:0x007d, B:40:0x0082), top: B:4:0x0004 }] */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0077 A[EDGE_INSN: B:39:0x0077->B:36:0x0077 BREAK  A[LOOP:1: B:29:0x0064->B:33:0x0074], SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void unRecordEventRegistration(com.google.firebase.database.core.EventRegistration r7) {
        /*
            r6 = this;
            java.util.HashMap<com.google.firebase.database.core.EventRegistration, java.util.List<com.google.firebase.database.core.EventRegistration>> r0 = r6.globalEventRegistrations
            monitor-enter(r0)
            r1 = 0
            java.util.HashMap<com.google.firebase.database.core.EventRegistration, java.util.List<com.google.firebase.database.core.EventRegistration>> r2 = r6.globalEventRegistrations     // Catch: java.lang.Throwable -> L84
            java.lang.Object r2 = r2.get(r7)     // Catch: java.lang.Throwable -> L84
            java.util.List r2 = (java.util.List) r2     // Catch: java.lang.Throwable -> L84
            if (r2 == 0) goto L2e
            r3 = 0
        Lf:
            int r4 = r2.size()     // Catch: java.lang.Throwable -> L84
            if (r3 >= r4) goto L23
            java.lang.Object r4 = r2.get(r3)     // Catch: java.lang.Throwable -> L84
            if (r4 != r7) goto L20
            r1 = 1
            r2.remove(r3)     // Catch: java.lang.Throwable -> L84
            goto L23
        L20:
            int r3 = r3 + 1
            goto Lf
        L23:
            boolean r3 = r2.isEmpty()     // Catch: java.lang.Throwable -> L84
            if (r3 == 0) goto L2e
            java.util.HashMap<com.google.firebase.database.core.EventRegistration, java.util.List<com.google.firebase.database.core.EventRegistration>> r3 = r6.globalEventRegistrations     // Catch: java.lang.Throwable -> L84
            r3.remove(r7)     // Catch: java.lang.Throwable -> L84
        L2e:
            if (r1 != 0) goto L39
            boolean r3 = r7.isUserInitiated()     // Catch: java.lang.Throwable -> L84
            if (r3 != 0) goto L37
            goto L39
        L37:
            r3 = 0
            goto L3a
        L39:
            r3 = 1
        L3a:
            com.google.firebase.database.core.utilities.Utilities.hardAssert(r3)     // Catch: java.lang.Throwable -> L84
            com.google.firebase.database.core.view.QuerySpec r3 = r7.getQuerySpec()     // Catch: java.lang.Throwable -> L84
            boolean r3 = r3.isDefault()     // Catch: java.lang.Throwable -> L84
            if (r3 != 0) goto L82
        L48:
            com.google.firebase.database.core.view.QuerySpec r3 = r7.getQuerySpec()     // Catch: java.lang.Throwable -> L84
            com.google.firebase.database.core.Path r3 = r3.getPath()     // Catch: java.lang.Throwable -> L84
            com.google.firebase.database.core.view.QuerySpec r3 = com.google.firebase.database.core.view.QuerySpec.defaultQueryAtPath(r3)     // Catch: java.lang.Throwable -> L84
            com.google.firebase.database.core.EventRegistration r3 = r7.clone(r3)     // Catch: java.lang.Throwable -> L84
            java.util.HashMap<com.google.firebase.database.core.EventRegistration, java.util.List<com.google.firebase.database.core.EventRegistration>> r4 = r6.globalEventRegistrations     // Catch: java.lang.Throwable -> L84
            java.lang.Object r4 = r4.get(r3)     // Catch: java.lang.Throwable -> L84
            java.util.List r4 = (java.util.List) r4     // Catch: java.lang.Throwable -> L84
            r2 = r4
            if (r2 == 0) goto L82
            r4 = 0
        L64:
            int r5 = r2.size()     // Catch: java.lang.Throwable -> L84
            if (r4 >= r5) goto L77
            java.lang.Object r5 = r2.get(r4)     // Catch: java.lang.Throwable -> L84
            if (r5 != r7) goto L74
            r2.remove(r4)     // Catch: java.lang.Throwable -> L84
            goto L77
        L74:
            int r4 = r4 + 1
            goto L64
        L77:
            boolean r4 = r2.isEmpty()     // Catch: java.lang.Throwable -> L84
            if (r4 == 0) goto L82
            java.util.HashMap<com.google.firebase.database.core.EventRegistration, java.util.List<com.google.firebase.database.core.EventRegistration>> r4 = r6.globalEventRegistrations     // Catch: java.lang.Throwable -> L84
            r4.remove(r3)     // Catch: java.lang.Throwable -> L84
        L82:
            monitor-exit(r0)     // Catch: java.lang.Throwable -> L84
            return
        L84:
            r1 = move-exception
            monitor-exit(r0)     // Catch: java.lang.Throwable -> L84
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.database.core.ZombieEventManager.unRecordEventRegistration(com.google.firebase.database.core.EventRegistration):void");
    }

    public void zombifyForRemove(EventRegistration registration) {
        synchronized (this.globalEventRegistrations) {
            List<EventRegistration> registrationList = this.globalEventRegistrations.get(registration);
            if (registrationList != null && !registrationList.isEmpty()) {
                if (registration.getQuerySpec().isDefault()) {
                    HashSet<QuerySpec> zombiedQueries = new HashSet<>();
                    for (int i = registrationList.size() - 1; i >= 0; i--) {
                        EventRegistration currentRegistration = registrationList.get(i);
                        if (!zombiedQueries.contains(currentRegistration.getQuerySpec())) {
                            zombiedQueries.add(currentRegistration.getQuerySpec());
                            currentRegistration.zombify();
                        }
                    }
                } else {
                    registrationList.get(0).zombify();
                }
            }
        }
    }

    @Override // com.google.firebase.database.core.EventRegistrationZombieListener
    public void onZombied(EventRegistration zombiedInstance) {
        unRecordEventRegistration(zombiedInstance);
    }
}
