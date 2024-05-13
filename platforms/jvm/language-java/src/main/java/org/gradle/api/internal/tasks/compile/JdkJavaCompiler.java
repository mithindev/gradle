/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.tasks.compile;

import com.sun.tools.javac.util.Context;
import org.gradle.api.JavaVersion;
import org.gradle.api.internal.tasks.compile.processing.AnnotationProcessorDeclaration;
import org.gradle.api.internal.tasks.compile.reflect.GradleStandardJavaFileManager;
import org.gradle.api.problems.internal.InternalProblems;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.Factory;
import org.gradle.internal.classpath.DefaultClassPath;
import org.gradle.language.base.internal.compile.Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JdkJavaCompiler implements Compiler<JavaCompileSpec>, Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdkJavaCompiler.class);

    private final Context context;
    private final Factory<ContextAwareJavaCompiler> compilerFactory;
    private final DiagnosticToProblemListener diagnosticToProblemListener;

    @Inject
    public JdkJavaCompiler(
        Factory<ContextAwareJavaCompiler> compilerFactory,
        InternalProblems problemsService
    ) {
        this.context = new Context();
        this.compilerFactory = compilerFactory;
        this.diagnosticToProblemListener = new DiagnosticToProblemListener(problemsService.getInternalReporter(), context);
    }

    @Override
    public WorkResult execute(JavaCompileSpec spec) {
        LOGGER.info("Compiling with JDK Java compiler API.");

        ApiCompilerResult result = new ApiCompilerResult();
        JavaCompiler.CompilationTask task = createCompileTask(spec, result);
        boolean success = task.call();
        if (!success) {
            throw new CompilationFailedException(result);
        }
        return result;
    }

    @SuppressWarnings("DefaultCharset")
    private JavaCompiler.CompilationTask createCompileTask(JavaCompileSpec spec, ApiCompilerResult result) {
        List<String> options = new JavaCompilerArgumentsBuilder(spec).build();
        ContextAwareJavaCompiler compiler = compilerFactory.create();

        MinimalJavaCompileOptions compileOptions = spec.getCompileOptions();
        Charset charset = Optional.ofNullable(compileOptions.getEncoding())
            .map(Charset::forName)
            .orElse(null);
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticToProblemListener, null, charset);

        Iterable<? extends JavaFileObject> compilationUnits = standardFileManager.getJavaFileObjectsFromFiles(spec.getSourceFiles());
        boolean hasEmptySourcepaths = JavaVersion.current().isJava9Compatible() && emptySourcepathIn(options);
        JavaFileManager fileManager = GradleStandardJavaFileManager.wrap(standardFileManager, DefaultClassPath.of(spec.getAnnotationProcessorPath()), hasEmptySourcepaths);

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticToProblemListener, options, spec.getClassesToProcess(), compilationUnits, context);
        if (compiler instanceof IncrementalCompilationAwareJavaCompiler) {
            task = ((IncrementalCompilationAwareJavaCompiler) compiler).makeIncremental(
                task,
                result.getSourceClassesMapping(),
                result.getConstantsAnalysisResult(),
                new CompilationSourceDirs(spec),
                new CompilationClassBackupService(spec, result)
            );
        }
        Set<AnnotationProcessorDeclaration> annotationProcessors = spec.getEffectiveAnnotationProcessors();
        task = new AnnotationProcessingCompileTask(task, annotationProcessors, spec.getAnnotationProcessorPath(), result.getAnnotationProcessingResult());
        task = new ResourceCleaningCompilationTask(task, fileManager);
        return task;
    }

    private static boolean emptySourcepathIn(List<String> options) {
        Iterator<String> optionsIter = options.iterator();
        while (optionsIter.hasNext()) {
            String current = optionsIter.next();
            if (current.equals("-sourcepath") || current.equals("--source-path")) {
                return optionsIter.next().isEmpty();
            }
        }
        return false;
    }

}
