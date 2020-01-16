package com.neo.codecomplexityanalyzer.service.serviceimpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class JavaSyntaxChecker {
    private String filePath;

    public JavaSyntaxChecker(String filePath){
        this.filePath = filePath;
    }

    public List<String> check() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromStrings(Arrays.asList(filePath));

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();

        List<String> messages = new ArrayList<>();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            messages.add(diagnostic.getKind() + ":\t Line [" + diagnostic.getLineNumber() + "] \t Position [" + diagnostic.getPosition() + "]\t" + diagnostic.getMessage(Locale.ROOT) + "\n");
        }

        return messages;
    }
}