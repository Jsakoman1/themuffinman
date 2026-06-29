import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CodexJavaAstContext {
    private CodexJavaAstContext() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: CodexJavaAstContext <repoRoot> <relativePath> [changedLinesCsv]");
            System.exit(1);
        }

        String repoRoot = args[0];
        String relativePath = args[1];
        Set<Long> changedLines = parseChangedLines(args.length >= 3 ? args[2] : "");
        Path filePath = Path.of(repoRoot, relativePath);
        String source = Files.readString(filePath, StandardCharsets.UTF_8);
        List<String> sourceLines = Arrays.asList(source.split("\n", -1));

        List<Span> spans = parseSpans(filePath, relativePath, sourceLines, changedLines);
        System.out.println(toJson(spans));
    }

    private static Set<Long> parseChangedLines(String csv) {
      if (csv == null || csv.isBlank()) {
          return Collections.emptySet();
      }
      Set<Long> lines = new HashSet<>();
      for (String token : csv.split(",")) {
          if (!token.isBlank()) {
              lines.add(Long.parseLong(token.trim()));
          }
      }
      return lines;
    }

    private static List<Span> parseSpans(Path filePath, String relativePath, List<String> sourceLines, Set<Long> changedLines) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("System Java compiler is not available.");
        }
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(filePath.toFile());
            JavacTask task = (JavacTask) compiler.getTask(null, fileManager, null, List.of("-proc:none"), null, compilationUnits);
            Iterable<? extends CompilationUnitTree> parsedUnits = task.parse();
            task.analyze();
            Trees trees = Trees.instance(task);
            List<Span> spans = new ArrayList<>();
            for (CompilationUnitTree unit : parsedUnits) {
                SourcePositions positions = trees.getSourcePositions();
                new TreePathScanner<Void, Void>() {
                    private final Deque<String> typeStack = new ArrayDeque<>();

                    @Override
                    public Void visitClass(ClassTree node, Void unused) {
                        String symbol = node.getSimpleName().toString();
                        Span span = buildSpan("java-type", symbol, relativePath, sourceLines, changedLines, unit, positions, node);
                        if (span != null) {
                            spans.add(span);
                        }
                        typeStack.push(symbol);
                        super.visitClass(node, unused);
                        typeStack.pop();
                        return null;
                    }

                    @Override
                    public Void visitMethod(MethodTree node, Void unused) {
                        String owner = typeStack.peek();
                        String name = node.getName().toString();
                        String symbol = owner == null || owner.isBlank() ? name : owner + "." + name;
                        Span span = buildSpan("java-method", symbol, relativePath, sourceLines, changedLines, unit, positions, node);
                        if (span != null) {
                            spans.add(span);
                        }
                        return super.visitMethod(node, unused);
                    }

                    @Override
                    public Void visitVariable(VariableTree node, Void unused) {
                        Tree leaf = getCurrentPath() == null ? null : getCurrentPath().getParentPath() == null ? null : getCurrentPath().getParentPath().getLeaf();
                        if (leaf instanceof ClassTree) {
                            String owner = typeStack.peek();
                            String symbol = owner == null || owner.isBlank() ? node.getName().toString() : owner + "." + node.getName();
                            Span span = buildSpan("java-field", symbol, relativePath, sourceLines, changedLines, unit, positions, node);
                            if (span != null) {
                                spans.add(span);
                            }
                        }
                        return super.visitVariable(node, unused);
                    }
                }.scan(unit, null);
            }
            return spans;
        }
    }

    private static Span buildSpan(
        String type,
        String symbol,
        String relativePath,
        List<String> sourceLines,
        Set<Long> changedLines,
        CompilationUnitTree unit,
        SourcePositions positions,
        Tree node
    ) {
        long startPosition = positions.getStartPosition(unit, node);
        long endPosition = positions.getEndPosition(unit, node);
        if (startPosition < 0 || endPosition < 0) {
            return null;
        }
        long startLine = unit.getLineMap().getLineNumber(startPosition);
        long endLine = unit.getLineMap().getLineNumber(Math.max(startPosition, endPosition - 1));
        if (!overlaps(changedLines, startLine, endLine)) {
            return null;
        }
        String snippet = compactSnippet(sourceLines, (int) startLine, (int) endLine);
        return new Span(type, symbol, relativePath, startLine, endLine, snippet);
    }

    private static boolean overlaps(Set<Long> changedLines, long startLine, long endLine) {
        if (changedLines.isEmpty()) {
            return true;
        }
        for (long line = startLine; line <= endLine; line += 1) {
            if (changedLines.contains(line)) {
                return true;
            }
        }
        return false;
    }

    private static String compactSnippet(List<String> sourceLines, int startLine, int endLine) {
        int from = Math.max(startLine - 1, 0);
        int to = Math.min(endLine, sourceLines.size());
        List<String> trimmed = new ArrayList<>();
        for (int index = from; index < to && trimmed.size() < 20; index += 1) {
            String line = sourceLines.get(index).replaceAll("\\s+$", "");
            if (!line.trim().isEmpty()) {
                trimmed.add(line);
            }
        }
        String snippet = String.join("\n", trimmed);
        return snippet.length() > 1200 ? snippet.substring(0, 1197) + "..." : snippet;
    }

    private static String toJson(List<Span> spans) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int index = 0; index < spans.size(); index += 1) {
            if (index > 0) {
                builder.append(",");
            }
            Span span = spans.get(index);
            builder.append("{")
                .append("\"type\":\"").append(escape(span.type)).append("\",")
                .append("\"symbol\":\"").append(escape(span.symbol)).append("\",")
                .append("\"file\":\"").append(escape(span.file)).append("\",")
                .append("\"lineStart\":").append(span.lineStart).append(",")
                .append("\"lineEnd\":").append(span.lineEnd).append(",")
                .append("\"snippet\":\"").append(escape(span.snippet)).append("\"")
                .append("}");
        }
        builder.append("]");
        return builder.toString();
    }

    private static String escape(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    private record Span(String type, String symbol, String file, long lineStart, long lineEnd, String snippet) {
    }
}
