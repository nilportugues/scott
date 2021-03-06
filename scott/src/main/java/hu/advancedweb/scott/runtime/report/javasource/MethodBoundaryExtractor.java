package hu.advancedweb.scott.runtime.report.javasource;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import hu.advancedweb.scott.runtime.report.javasource.MethodBoundaryExtractor.Bounderies;

/**
 * Extracts the begin and the end line for a method.
 * 
 * @author David Csakvari
 */
class MethodBoundaryExtractor extends VoidVisitorAdapter<Bounderies> {

	private final String methodName;
	
	
	public MethodBoundaryExtractor(String methodName) {
		this.methodName = methodName;
	}

	public void visit(MethodDeclaration methodDeclaration, Bounderies boundaries) {
		if (methodDeclaration.getName().equals(methodName)) {
			boundaries.beginLine = methodDeclaration.getRange().begin.line;
			boundaries.endLine = methodDeclaration.getRange().end.line;
		}
	}
	
	public final static class Bounderies {
		int beginLine;
		int endLine;
	}
	
}
