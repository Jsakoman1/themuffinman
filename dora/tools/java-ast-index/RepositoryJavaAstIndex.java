import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RepositoryJavaAstIndex {
    private static String json(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }

    private static List<Path> sourceFiles(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream.filter(path -> path.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        }
    }

    private static void fail(String message) {
        throw new IllegalArgumentException(message);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Path projectRoot = null;
        List<String> configuredRoots = new ArrayList<>();
        for (int index = 0; index < args.length; index++) {
            if ("--project-root".equals(args[index]) && index + 1 < args.length) {
                projectRoot = Paths.get(args[++index]).toAbsolutePath().normalize();
            } else if ("--source-root".equals(args[index]) && index + 1 < args.length) {
                configuredRoots.add(args[++index]);
            } else {
                fail("usage: java RepositoryJavaAstIndex.java --project-root <path> --source-root <path> [...]");
            }
        }
        if (projectRoot == null || configuredRoots.isEmpty()) fail("project root and at least one source root are required");

        List<Path> paths = new ArrayList<>();
        List<String> sourceRoots = new ArrayList<>();
        for (String configuredRoot : configuredRoots) {
            Path sourceRoot = projectRoot.resolve(configuredRoot).normalize();
            if (!sourceRoot.startsWith(projectRoot) || !Files.isDirectory(sourceRoot)) fail("source root is invalid or missing: " + configuredRoot);
            sourceRoots.add(projectRoot.relativize(sourceRoot).toString());
            paths.addAll(sourceFiles(sourceRoot));
        }
        paths = paths.stream().distinct().sorted().toList();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("JDK compiler is unavailable");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<Map<String, Object>> files = new ArrayList<>();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            Iterable<? extends JavaFileObject> sources = manager.getJavaFileObjectsFromFiles(paths.stream().map(Path::toFile).toList());
            JavacTask task = (JavacTask) compiler.getTask(null, manager, diagnostics, List.of("-proc:none"), null, sources);
            for (CompilationUnitTree unit : task.parse()) {
                Path sourcePath = Paths.get(unit.getSourceFile().toUri()).toAbsolutePath();
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("path", projectRoot.relativize(sourcePath).toString());
                row.put("package", unit.getPackageName() == null ? "" : unit.getPackageName().toString());
                row.put("imports", unit.getImports().stream().map(ImportTree::getQualifiedIdentifier).map(Object::toString).sorted().toList());
                List<Map<String, String>> symbols = new ArrayList<>();
                new TreePathScanner<Void, Void>() {
                    @Override public Void visitClass(ClassTree node, Void unused) {
                        symbols.add(Map.of("kind", "class", "name", node.getSimpleName().toString()));
                        return super.visitClass(node, unused);
                    }
                    @Override public Void visitMethod(MethodTree node, Void unused) {
                        symbols.add(Map.of("kind", "method", "name", node.getName().toString()));
                        return super.visitMethod(node, unused);
                    }
                }.scan(unit, null);
                row.put("symbols", symbols);
                files.add(row);
            }
        }

        long errors = diagnostics.getDiagnostics().stream().filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR).count();
        StringBuilder output = new StringBuilder("{\"version\":1,\"kind\":\"java_ast_index\",\"sourceRoots\":[");
        for (int index = 0; index < sourceRoots.size(); index++) { if (index > 0) output.append(','); output.append(json(sourceRoots.get(index))); }
        output.append("],\"files\":[");
        for (int index = 0; index < files.size(); index++) {
            if (index > 0) output.append(',');
            Map<String, Object> row = files.get(index);
            output.append("{\"path\":").append(json((String) row.get("path"))).append(",\"package\":").append(json((String) row.get("package"))).append(",\"imports\":[");
            List<String> imports = (List<String>) row.get("imports");
            for (int importIndex = 0; importIndex < imports.size(); importIndex++) { if (importIndex > 0) output.append(','); output.append(json(imports.get(importIndex))); }
            output.append("],\"symbols\":[");
            List<Map<String, String>> symbols = (List<Map<String, String>>) row.get("symbols");
            for (int symbolIndex = 0; symbolIndex < symbols.size(); symbolIndex++) {
                if (symbolIndex > 0) output.append(',');
                output.append("{\"kind\":").append(json(symbols.get(symbolIndex).get("kind"))).append(",\"name\":").append(json(symbols.get(symbolIndex).get("name"))).append('}');
            }
            output.append("]}");
        }
        output.append("],\"summary\":{\"files\":").append(files.size()).append(",\"symbols\":").append(files.stream().mapToInt(row -> ((List<?>) row.get("symbols")).size()).sum()).append(",\"parseErrors\":").append(errors).append("}}\n");
        System.out.print(output);
        if (errors > 0) System.exit(1);
    }
}
