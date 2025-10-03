package com.google.android.gms.dependencies;

import com.google.android.gms.dependencies.ArtifactVersion;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class DependencyInspector implements DependencyResolutionListener {
    private static final String GRADLE_PROJECT = "gradle.project";
    private static Logger logger = LoggerFactory.getLogger(DependencyInspector.class);
    private final DependencyAnalyzer dependencyAnalyzer;
    private final String exceptionMessageAddendum;
    private final String projectName;

    public DependencyInspector(@Nonnull DependencyAnalyzer dependencyAnalyzer, @Nonnull String projectName, @Nullable String exceptionMessageAddendum) {
        this.dependencyAnalyzer = dependencyAnalyzer;
        this.exceptionMessageAddendum = exceptionMessageAddendum;
        this.projectName = projectName;
    }

    private static String simplifyKnownGroupIds(@Nonnull String inputString) {
        return inputString.replace("com.google.android.gms", "c.g.a.g").replace("com.google.firebase", "c.g.f");
    }

    private static void printNode(int depth, @Nonnull Node n) {
        StringBuilder prefix = new StringBuilder();
        for (int z = 0; z < depth; z++) {
            prefix.append("--");
        }
        prefix.append(" ");
        Dependency dep = n.getDependency();
        if (GRADLE_PROJECT.equals(n.getDependency().getFromArtifactVersion().getGroupId())) {
            String fromRef = dep.getFromArtifactVersion().getGradleRef().replace(GRADLE_PROJECT, "");
            String toRef = simplifyKnownGroupIds(dep.getToArtifact().getGradleRef());
            Logger logger2 = logger;
            String sb = prefix.toString();
            String toArtifactVersionString = dep.getToArtifactVersionString();
            StringBuilder sb2 = new StringBuilder(String.valueOf(sb).length() + 21 + String.valueOf(fromRef).length() + String.valueOf(toRef).length() + String.valueOf(toArtifactVersionString).length());
            sb2.append(sb);
            sb2.append(fromRef);
            sb2.append(" task/module dep -> ");
            sb2.append(toRef);
            sb2.append("@");
            sb2.append(toArtifactVersionString);
            logger2.info(sb2.toString());
        } else {
            String fromRef2 = simplifyKnownGroupIds(dep.getFromArtifactVersion().getGradleRef());
            String toRef2 = simplifyKnownGroupIds(dep.getToArtifact().getGradleRef());
            Logger logger3 = logger;
            String sb3 = prefix.toString();
            String toArtifactVersionString2 = dep.getToArtifactVersionString();
            StringBuilder sb4 = new StringBuilder(String.valueOf(sb3).length() + 21 + String.valueOf(fromRef2).length() + String.valueOf(toRef2).length() + String.valueOf(toArtifactVersionString2).length());
            sb4.append(sb3);
            sb4.append(fromRef2);
            sb4.append(" library depends -> ");
            sb4.append(toRef2);
            sb4.append("@");
            sb4.append(toArtifactVersionString2);
            logger3.info(sb4.toString());
        }
        if (n.getChild() != null) {
            printNode(depth + 1, n.getChild());
        }
    }

    private void registerDependencies(@Nonnull ResolvableDependencies resolvableDependencies, @Nonnull String projectName, @Nonnull String taskName) {
        ArtifactVersion fromDep;
        ResolutionResult resolutionResult = resolvableDependencies.getResolutionResult();
        for (DependencyResult depResult : resolutionResult.getAllDependencies()) {
            if (depResult.getFrom() == null || "".equals(depResult.getFrom().getId().getDisplayName()) || "project :".equals(depResult.getFrom().getId().getDisplayName())) {
                ArtifactVersion.Companion companion = ArtifactVersion.INSTANCE;
                StringBuilder sb = new StringBuilder(String.valueOf(projectName).length() + 22 + String.valueOf(taskName).length());
                sb.append("gradle.project:");
                sb.append(projectName);
                sb.append("-");
                sb.append(taskName);
                sb.append(":0.0.0");
                fromDep = companion.fromGradleRef(sb.toString());
            } else {
                String valueOf = String.valueOf(depResult.getFrom().getId().getDisplayName());
                String depFromString = valueOf.length() != 0 ? "".concat(valueOf) : new String("");
                if (depFromString.startsWith("project ")) {
                    String[] splitDepName = depFromString.split(":");
                    String depName = splitDepName.length > 1 ? splitDepName[1] : "module";
                    ArtifactVersion.Companion companion2 = ArtifactVersion.INSTANCE;
                    StringBuilder sb2 = new StringBuilder(String.valueOf(projectName).length() + 23 + String.valueOf(taskName).length() + String.valueOf(depName).length());
                    sb2.append("gradle.project:");
                    sb2.append(projectName);
                    sb2.append("-");
                    sb2.append(taskName);
                    sb2.append("-");
                    sb2.append(depName);
                    sb2.append(":0.0.0");
                    fromDep = companion2.fromGradleRef(sb2.toString());
                } else {
                    try {
                        fromDep = ArtifactVersion.INSTANCE.fromGradleRef(depFromString);
                    } catch (IllegalArgumentException e) {
                        Logger logger2 = logger;
                        String valueOf2 = String.valueOf(depFromString);
                        logger2.info(valueOf2.length() != 0 ? "Skipping misunderstood FROM dep string: ".concat(valueOf2) : new String("Skipping misunderstood FROM dep string: "));
                    }
                }
            }
            if (depResult.getRequested() != null) {
                String valueOf3 = String.valueOf(depResult.getRequested());
                StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf3).length());
                sb3.append(valueOf3);
                String toDepString = sb3.toString();
                try {
                    ArtifactVersion toDep = ArtifactVersion.INSTANCE.fromGradleRef(toDepString);
                    this.dependencyAnalyzer.registerDependency(Dependency.INSTANCE.fromArtifactVersions(fromDep, toDep));
                } catch (IllegalArgumentException e2) {
                    Logger logger3 = logger;
                    String valueOf4 = String.valueOf(toDepString);
                    logger3.info(valueOf4.length() != 0 ? "Skipping misunderstood TO dep string: ".concat(valueOf4) : new String("Skipping misunderstood TO dep string: "));
                }
            }
        }
    }

    public void beforeResolve(ResolvableDependencies resolvableDependencies) {
    }

    public void afterResolve(ResolvableDependencies resolvableDependencies) {
        String taskName = resolvableDependencies.getName();
        if (!taskName.contains("ompile")) {
            return;
        }
        Logger logger2 = logger;
        String str = this.projectName;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 31 + String.valueOf(taskName).length());
        sb.append("Registered task dependencies: ");
        sb.append(str);
        sb.append(":");
        sb.append(taskName);
        logger2.info(sb.toString());
        if (resolvableDependencies.getResolutionResult() != null && resolvableDependencies.getResolutionResult().getAllDependencies() != null) {
            registerDependencies(resolvableDependencies, this.projectName, taskName);
        }
        logger.info("Starting dependency analysis");
        ResolutionResult resolutionResult = resolvableDependencies.getResolutionResult();
        HashMap<Artifact, ArtifactVersion> resolvedVersions = new HashMap<>();
        for (ResolvedComponentResult resolvedComponentResult : resolutionResult.getAllComponents()) {
            ArtifactVersion version = ArtifactVersion.INSTANCE.fromGradleRefOrNull(resolvedComponentResult.getId().toString());
            if (version != null) {
                resolvedVersions.put(version.getArtifact(), version);
            }
        }
        if (resolvedVersions.size() < 1) {
            return;
        }
        Collection<Dependency> activeDeps = this.dependencyAnalyzer.getActiveDependencies(resolvedVersions.values());
        for (Dependency dep : activeDeps) {
            ArtifactVersion resolvedVersion = resolvedVersions.get(dep.getToArtifact());
            if (!dep.isVersionCompatible(resolvedVersion.getVersion())) {
                Logger logger3 = logger;
                String valueOf = String.valueOf(dep);
                StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf).length() + 48);
                sb2.append("Dependency resolved to an incompatible version: ");
                sb2.append(valueOf);
                logger3.warn(sb2.toString());
                Collection<Node> depsPaths = this.dependencyAnalyzer.getPaths(resolvedVersion.getArtifact());
                Logger logger4 = logger;
                String valueOf2 = String.valueOf(dep.getToArtifact());
                StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf2).length() + 99);
                sb3.append("Dependency Resolution Help: Displaying all currently known paths to any version of the dependency: ");
                sb3.append(valueOf2);
                logger4.info(sb3.toString());
                logger.info("NOTE: com.google.android.gms translated to c.g.a.g for brevity. Same for com.google.firebase -> c.g.f");
                for (Node n : depsPaths) {
                    printNode(1, n);
                }
                throw new GradleException(getErrorMessage(dep, resolvedVersion, depsPaths));
            }
        }
    }

    private String getErrorMessage(@Nonnull Dependency dep, @Nonnull ArtifactVersion resolvedVersion, @Nonnull Collection<Node> depPaths) {
        StringBuilder sb = new StringBuilder("In project '");
        sb.append(this.projectName);
        sb.append("' a resolved Google Play services library dependency depends on another at an exact version (e.g. \"");
        sb.append(dep.getToArtifactVersionString());
        sb.append("\", but isn't being resolved to that version. Behavior exhibited by the library will be unknown.");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Dependency failing: ");
        sb.append(dep.getDisplayString());
        sb.append(", but ");
        sb.append(dep.getToArtifact().getArtifactId());
        sb.append(" version was ");
        sb.append(resolvedVersion.getVersion());
        sb.append(".");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        StringBuilder errorMessage = sb.append("The following dependencies are project dependencies that are direct or have transitive dependencies that lead to the artifact with the issue.");
        HashSet<String> directDependencyStrings = new HashSet<>();
        StringBuilder currentString = new StringBuilder();
        for (Node node : depPaths) {
            String[] projectNameParts = node.getDependency().getFromArtifactVersion().getArtifactId().split("-");
            if (projectNameParts[0].equals(projectNameParts[2])) {
                currentString.append("-- Project '");
                currentString.append(projectNameParts[0]);
                currentString.append("' depends onto ");
            } else {
                currentString.append("-- Project '");
                currentString.append(projectNameParts[0]);
                currentString.append("' depends on project '");
                currentString.append(projectNameParts[2]);
                currentString.append("' which depends onto ");
            }
            currentString.append(node.getDependency().getToArtifact().getGroupId());
            currentString.append(":");
            currentString.append(node.getDependency().getToArtifact().getArtifactId());
            currentString.append("@");
            currentString.append(node.getDependency().getToArtifactVersionString());
            directDependencyStrings.add(currentString.toString());
            currentString.delete(0, currentString.length());
        }
        Iterator<String> it = directDependencyStrings.iterator();
        while (it.hasNext()) {
            String d = it.next();
            errorMessage.append(System.lineSeparator());
            errorMessage.append(d);
        }
        errorMessage.append(System.lineSeparator());
        errorMessage.append(System.lineSeparator());
        errorMessage.append("For extended debugging info execute Gradle from the command line with ");
        errorMessage.append("./gradlew --info :");
        errorMessage.append(this.projectName);
        errorMessage.append(":assembleDebug to see the dependency paths to the artifact. ");
        String str = this.exceptionMessageAddendum;
        if (str != null && !"".equals(str.trim())) {
            errorMessage.append(this.exceptionMessageAddendum);
        }
        String sb2 = errorMessage.toString();
        String valueOf = String.valueOf(System.lineSeparator());
        return sb2.replaceAll(".{120}(?=.)", valueOf.length() != 0 ? "$0".concat(valueOf) : new String("$0"));
    }
}
