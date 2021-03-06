package hu.advancedweb.scott.runtime.report;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.Description;

import hu.advancedweb.scott.runtime.report.javasource.MethodSource;
import hu.advancedweb.scott.runtime.report.javasource.SourcePathResolver;
import hu.advancedweb.scott.runtime.track.StateData;
import hu.advancedweb.scott.runtime.track.StateRegistry;

/**
 * Renders the pretty-printed report optimized for terminals.
 * 
 * @author David Csakvari
 */
public class FailureRenderer {

	private static SourcePathResolver sourcePathResolver = new SourcePathResolver();

	public static String render(Description description, Throwable throwable) {
		final ScottReport scottReport = new ScottReport();
		MethodSource methodSource = getTestMethodSource(description);
		
		if (methodSource != null) {
			fillSource(scottReport, methodSource);
		}
		
		fillTrackedData(scottReport);
		fillException(scottReport, methodSource, throwable);

		return renderPlain(scottReport);
	}
	
	private static MethodSource getTestMethodSource(Description description) {
		try {
			String testClassName = description.getTestClass().getCanonicalName();
			String testSourcePath = sourcePathResolver.getSourcePath(testClassName);
			String testMethodName = description.getMethodName();
			return new MethodSource(testSourcePath, testClassName, testMethodName);
		} catch (Exception e) {
			try {
				// As a fallback, look for the currently tracked method, and try to take its source.
				String testClassName = StateRegistry.getTestClassType().replace("/", ".");
				String testSourcePath = sourcePathResolver.getSourcePath(testClassName);
				String testMethodName = StateRegistry.getTestMethodName();
				return new MethodSource(testSourcePath, testClassName, testMethodName);
			} catch (Exception e2) {
				// Ignore, we simply don't fill the test source for the report.
				// It's better than crashing the test run.
				return null;
			}
		}
	}
	
	private static void fillSource(ScottReport scottReport, MethodSource methodSource) {
		scottReport.setBeginLine(methodSource.getBeginLine());
		for (String line : methodSource.getReportLines()) {
			scottReport.addLine(line);
		}
	}

	private static void fillTrackedData(ScottReport scottReport) {
		Map<String, String> trackedValue = new HashMap<>();
		
		for (StateData event : StateRegistry.getLocalVariableStates()) {
			String lastValue = trackedValue.get(event.key);
			if (!event.value.equals(lastValue)) {
				if (event.lineNumber == 0) {
					scottReport.addInitialSnapshot(getInitLine(event), StateRegistry.getLocalVariableName(event.key, event.lineNumber), event.value);
				} else {
					scottReport.addSnapshot(event.lineNumber, StateRegistry.getLocalVariableName(event.key, event.lineNumber), event.value);
				}
				trackedValue.put(event.key, event.value);
			}
		}
		
		trackedValue = new HashMap<>();
		
		for (StateData event : StateRegistry.getFieldStates()) {
			String lastValue = trackedValue.get(event.key);
			if (!event.value.equals(lastValue)) {
				if (event.lineNumber == 0) {
					scottReport.addInitialSnapshot(0, event.key, event.value);
				} else {
					scottReport.addSnapshot(event.lineNumber, event.key, event.value);
				}
			}
			trackedValue.put(event.key, event.value);
		}
	}

	private static int getInitLine(StateData event) {
		int initLine = 0;
		for (Map.Entry<String, Integer> methodStart: StateRegistry.getMethodStartLine().entrySet()) {
			if (event.key.endsWith(methodStart.getKey())) {
				initLine = methodStart.getValue();
			}
		}
		return initLine;
	}
	
	private static void fillException(ScottReport scottReport, MethodSource methodSource, Throwable throwable) {
		Integer lineNumber = scottReport.getBeginLineNumber();
		
		if (methodSource != null) {
			for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
				if (methodSource.getClassName().equals(stackTraceElement.getClassName()) &&
						methodSource.getMethodName().equals(stackTraceElement.getMethodName())) {
					lineNumber = stackTraceElement.getLineNumber();
					break;
				}
			}
		}
		
		scottReport.setException(lineNumber, throwable.getClass().getSimpleName(), throwable.getMessage());
	}
	
	private static String renderPlain(ScottReport scottReport) {
		boolean firstLineWithBraketAppended = false;
		boolean initialReportAppended = false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		
		for (Map.Entry<Integer, String> line : scottReport.getSourceLines().entrySet()) {
			int lineNumber = line.getKey();
			String lineText = line.getValue().replaceAll("\t", "    ");
			boolean initialAdded = false;
			
			if (firstLineWithBraketAppended && initialReportAppended == false) {
				initialReportAppended = true;
				if (!scottReport.getInitialSnapshots(0).isEmpty()) {
					String blankLine = lineText.replaceFirst("[^\\s].*$", "");
					for (Snapshot snapshot : scottReport.getInitialSnapshots(0)) {
						sb.append("    ");
						sb.append("|  ");
						sb.append(blankLine);
						sb.append("//    => ");
						sb.append(snapshot.name + "=" + snapshot.value.trim());
						sb.append("\n");
						initialAdded = true;
					}
				}
			}
			
			if (!scottReport.getInitialSnapshots(lineNumber).isEmpty()) {
				String blankLine = lineText.replaceFirst("[^\\s].*$", "");
				for (Snapshot snapshot : scottReport.getInitialSnapshots(lineNumber)) {
					sb.append("    ");
					sb.append("|  ");
					sb.append(blankLine);
					sb.append("//    => ");
					sb.append(snapshot.name + "=" + snapshot.value.trim());
					sb.append("\n");
					initialAdded = true;
				}
			}
			
			if (initialAdded) {
				String blankLine = lineText.replaceFirst("[^\\s].*$", "");
				sb.append("    ");
				sb.append("|  ");
				sb.append(blankLine);
				sb.append("\n");
			}
			
			sb.append(String.format("%1$4s", lineNumber));
			if (scottReport.getExceptionLineNumber() == lineNumber) {
				sb.append("|* ");
			} else {
				sb.append("|  ");
			}
			
			sb.append(lineText);
			
			boolean isFirstCommentInThisLine = true;
			for (Snapshot variableSnapshot : scottReport.getVariableSnapshots(lineNumber)) {
				String[] variableSnapshotTextLines = getVariableSnapshotComment(variableSnapshot);
				
				for (String comment : variableSnapshotTextLines) {
					renderComment(sb, lineText, comment, isFirstCommentInThisLine);
					isFirstCommentInThisLine = false;
				}
			}
			
			if (scottReport.getExceptionLineNumber() == lineNumber) {
				String[] exceptionMessageLines = getExceptionComment(scottReport);
				
				for (String comment : exceptionMessageLines) {
					renderComment(sb, lineText, comment, isFirstCommentInThisLine);
					isFirstCommentInThisLine = false;
				}
			}
			sb.append("\n");
			
			firstLineWithBraketAppended = firstLineWithBraketAppended || lineText.contains("{");
		}
		
		return sb.toString();
	}

	private static String[] getExceptionComment(ScottReport scottReport) {
		final String exceptionMessage;
		if (scottReport.getExceptionMessage() != null) {
			exceptionMessage = scottReport.getExceptionClassName() + ": " + scottReport.getExceptionMessage().trim();
		} else {
			exceptionMessage = scottReport.getExceptionClassName();
		}
		
		String[] exceptionMessageLines = exceptionMessage.split("\\n");
		return exceptionMessageLines;
	}

	private static String[] getVariableSnapshotComment(Snapshot variableSnapshot) {
		final String variableSnapshotText;
		if (variableSnapshot.name != null) {
			variableSnapshotText = variableSnapshot.name + "=" + variableSnapshot.value.trim();
		} else {
			variableSnapshotText = variableSnapshot.value.trim();
		}
		String[] variableSnapshotTextLines = variableSnapshotText.split("\\n");
		return variableSnapshotTextLines;
	}
	
	private static void renderComment(StringBuilder sb, String lineText, String comment, boolean isFirstCommentInThisLine) {
		if (!isFirstCommentInThisLine) {
			addBlankLine(sb, lineText);
		}
		
		sb.append("  // ");
		sb.append(comment);
		isFirstCommentInThisLine = false;
	}

	private static void addBlankLine(StringBuilder sb, String lineText) {
		sb.append("\n");
		sb.append("    ");
		sb.append("|  ");
		sb.append(lineText.replaceAll(".", " "));
	}

}
