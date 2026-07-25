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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RepositoryJavaAstIndex {
    private static String json(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private static List<Path> sourceFiles(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream.filter(path -> path.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args.length > 0 ? args[0] : "apps/themuffinman/src/main/java").toAbsolutePath();
        Path repositoryRoot = root.getParent().getParent().getParent().getParent().getParent();
        List<Path> paths = sourceFiles(root);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("JDK compiler is unavailable");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<Map<String, Object>> files = new ArrayList<>();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            Iterable<? extends JavaFileObject> sources = manager.getJavaFileObjectsFromFiles(paths.stream().map(Path::toFile).toList());
            JavacTask task = (JavacTask) compiler.getTask(null, manager, diagnostics, List.of("-proc:none"), null, sources);
            for (CompilationUnitTree unit : task.parse()) {
                Path sourcePath = Paths.get(unit.getSourceFile().toUri()).toAbsolutePath();
                Map<String, Object> row = new HashMap<>();
                row.put("path", repositoryRoot.relativize(sourcePath).toString());
                row.put("package", unit.getPackageName() == null ? "" : unit.getPackageName().toString());
                List<String> imports = unit.getImports().stream().map(ImportTree::getQualifiedIdentifier).map(Object::toString).sorted().toList();
                row.put("imports", imports);
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
        StringBuilder output = new StringBuilder("{\"version\":1,\"kind\":\"java_ast_index\",");
        output.append("\"sourceRoot\":").append(json(root.toString())).append(",\"files\":[");
        for (int i = 0; i < files.size(); i++) {
            if (i > 0) output.append(',');
            Map<String, Object> row = files.get(i);
            output.append("{\"path\":").append(json((String) row.get("path")))
                .append(",\"package\":").append(json((String) row.get("package"))).append(",\"imports\":[");
            List<String> imports = (List<String>) row.get("imports");
            for (int j = 0; j < imports.size(); j++) { if (j > 0) output.append(','); output.append(json(imports.get(j))); }
            output.append("],\"symbols\":[");
            List<Map<String, String>> symbols = (List<Map<String, String>>) row.get("symbols");
            for (int j = 0; j < symbols.size(); j++) {
                if (j > 0) output.append(',');
                output.append("{\"kind\":").append(json(symbols.get(j).get("kind"))).append(",\"name\":").append(json(symbols.get(j).get("name"))).append('}');
            }
            output.append("]}");
        }
        output.append("],\"summary\":{\"files\":").append(files.size())
            .append(",\"symbols\":").append(files.stream().mapToInt(row -> ((List<?>) row.get("symbols")).size()).sum())
            .append(",\"parseErrors\":").append(errors).append("}}\n");
        System.out.print(output);
        if (errors > 0) System.exit(1);
    }
}
