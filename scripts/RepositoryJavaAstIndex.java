import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Compatibility redirect. The reusable parser is Dora-owned at dora/tools/java-ast-index.
public class RepositoryJavaAstIndex {
    public static void main(String[] args) throws Exception {
        Path tool = Paths.get("dora/tools/java-ast-index/RepositoryJavaAstIndex.java").toAbsolutePath();
        if (!tool.toFile().isFile()) throw new IllegalStateException("Dora Java AST index is missing: " + tool);
        String sourceRoot = args.length == 1 ? args[0] : "apps/themuffinman/src/main/java";
        List<String> command = new ArrayList<>(List.of("java", tool.toString(), "--project-root", ".", "--source-root", sourceRoot));
        Process process = new ProcessBuilder(command).inheritIO().start();
        System.exit(process.waitFor());
    }
}
