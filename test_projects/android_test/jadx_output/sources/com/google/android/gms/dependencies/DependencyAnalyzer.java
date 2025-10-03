package com.google.android.gms.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class DependencyAnalyzer {
    private Logger logger = LoggerFactory.getLogger(DependencyAnalyzer.class);
    private ArtifactDependencyManager dependencyManager = new ArtifactDependencyManager();

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void registerDependency(@Nonnull Dependency dependency) {
        this.dependencyManager.addDependency(dependency);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Nonnull
    public synchronized Collection<Dependency> getActiveDependencies(Collection<ArtifactVersion> versionedArtifacts) {
        ArrayList<Dependency> dependencies;
        HashSet<Artifact> artifacts = new HashSet<>();
        HashSet<ArtifactVersion> artifactVersions = new HashSet<>();
        for (ArtifactVersion version : versionedArtifacts) {
            artifacts.add(version.getArtifact());
            artifactVersions.add(version);
        }
        dependencies = new ArrayList<>();
        Iterator<Artifact> it = artifacts.iterator();
        while (it.hasNext()) {
            Artifact artifact = it.next();
            for (Dependency dep : this.dependencyManager.getDependencies(artifact)) {
                if (artifactVersions.contains(dep.getFromArtifactVersion()) && artifacts.contains(dep.getToArtifact())) {
                    dependencies.add(dep);
                }
            }
        }
        return dependencies;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized Collection<Node> getPaths(Artifact artifact) {
        ArrayList<Node> pathsToReturn;
        pathsToReturn = new ArrayList<>();
        Collection<Dependency> deps = this.dependencyManager.getDependencies(artifact);
        for (Dependency dep : deps) {
            getNode(pathsToReturn, new Node(null, dep), dep.getFromArtifactVersion());
        }
        return pathsToReturn;
    }

    private synchronized void getNode(ArrayList<Node> terminalPathList, Node n, ArtifactVersion artifactVersion) {
        Collection<Dependency> deps = this.dependencyManager.getDependencies(artifactVersion.getArtifact());
        if (deps.size() < 1) {
            terminalPathList.add(n);
            return;
        }
        for (Dependency dep : deps) {
            if (dep.isVersionCompatible(artifactVersion.getVersion())) {
                getNode(terminalPathList, new Node(n, dep), dep.getFromArtifactVersion());
            }
        }
    }
}
