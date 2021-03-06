package hu.advancedweb.scott.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.advancedweb.scott.runtime.track.StateData;
import hu.advancedweb.scott.runtime.track.StateRegistry;

public class TestHelper {
	
	public static String getLastRecordedStateForVariable(String variableName) {
		List<StateData> states = new ArrayList<StateData>(StateRegistry.getLocalVariableStates());
		Collections.reverse(states);
		
		for (StateData localVariableState : states) {
			String nameOfLocalVariableState = StateRegistry.getLocalVariableName(localVariableState.key, localVariableState.lineNumber);
			if (nameOfLocalVariableState.equals(variableName)) {
				return localVariableState.value;
			}
		}
		
		return null;
	}
	
	public static String getLastRecordedStateForField(String fieldName) {
		List<StateData> states = new ArrayList<StateData>(StateRegistry.getFieldStates());
		Collections.reverse(states);
		
		for (StateData localVariableState : states) {
			if (localVariableState.key.equals(fieldName)) {
				return localVariableState.value;
			}
		}
		
		return null;
	}

}
