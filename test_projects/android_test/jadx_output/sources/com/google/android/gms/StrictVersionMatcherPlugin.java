package com.google.android.gms;

import com.google.android.gms.dependencies.DependencyAnalyzer;
import com.google.android.gms.dependencies.DependencyInspector;
import javax.annotation.Nonnull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/* loaded from: classes.dex */
public class StrictVersionMatcherPlugin implements Plugin<Project> {
    private static DependencyAnalyzer globalDependencies = new DependencyAnalyzer();

    public void apply(@Nonnull Project project) {
        project.getGradle().addListener(new DependencyInspector(globalDependencies, project.getName(), "This error message came from the strict-version-matcher-plugin Gradle plugin, report issues at https://github.com/google/play-services-plugins and disable by removing the reference to the plugin (\"apply 'strict-version-matcher-plugin'\") from build.gradle."));
    }
}
