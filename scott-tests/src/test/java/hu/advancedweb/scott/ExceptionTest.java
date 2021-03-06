package hu.advancedweb.scott;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import hu.advancedweb.scott.helper.TestHelper;

public class ExceptionTest {
	
	@SuppressWarnings("null")
	@Test
	public void recordExceptions() throws Exception {
		String o = null;
		
		try {
			o.length();
		} catch (Exception e) {
			o = "fallback";
			assertThat(TestHelper.getLastRecordedStateForVariable("e"), equalTo(e.toString()));
			assertThat(TestHelper.getLastRecordedStateForVariable("o"), equalTo(o));
		}
	}
	
	@SuppressWarnings("null")
	@Test
	public void recordExceptionsWithVariablesInTheTryScope() {
		String o = null;
		
		try {
			String inner = "inner";
			assertThat(TestHelper.getLastRecordedStateForVariable("inner"), equalTo(inner));
			o.length();
		} catch (Exception e) {
			o = "fallback";
			assertThat(TestHelper.getLastRecordedStateForVariable("e"), equalTo(e.toString()));
			assertThat(TestHelper.getLastRecordedStateForVariable("o"), equalTo(o));
		}	
	}
	
	@SuppressWarnings("null")
	@Test
	public void moreVariablesInTryBlockThanInCatchBlock() {
		String o = null;
		try {
			String inner = "inner";
			assertThat(TestHelper.getLastRecordedStateForVariable("inner"), equalTo(inner));
			String inner2 = "inner2";
			assertThat(TestHelper.getLastRecordedStateForVariable("inner2"), equalTo(inner2));
			o.length();
		} catch (Exception e) {
			o = "fallback";
			assertThat(TestHelper.getLastRecordedStateForVariable("e"), equalTo(e.toString()));
			assertThat(TestHelper.getLastRecordedStateForVariable("o"), equalTo(o));
		}
	}
	
	@SuppressWarnings("null")
	@Test
	public void nestedTryCatchBlocks() {
		String o = null;
		try {
			String inner = "inner";
			assertThat(TestHelper.getLastRecordedStateForVariable("inner"), equalTo(inner));
			String inner2 = "inner2";
			assertThat(TestHelper.getLastRecordedStateForVariable("inner2"), equalTo(inner2));
			o.length();
			String o_2 = null;
			try {
				String inner_2 = "inner";
				assertThat(TestHelper.getLastRecordedStateForVariable("inner_2"), equalTo(inner_2));
				String inner2_2 = "inner2";
				assertThat(TestHelper.getLastRecordedStateForVariable("inner2_2"), equalTo(inner2_2));
				o.length();
			} catch (Exception e_2) {
				o_2 = "fallback";
				assertThat(TestHelper.getLastRecordedStateForVariable("e_2"), equalTo(e_2.toString()));
				assertThat(TestHelper.getLastRecordedStateForVariable("o_2"), equalTo(o_2));
			}
			
		} catch (Exception e) {
			o = "fallback";
			assertThat(TestHelper.getLastRecordedStateForVariable("e"), equalTo(e.toString()));
			assertThat(TestHelper.getLastRecordedStateForVariable("o"), equalTo(o));
		}
	}
	
	@SuppressWarnings("null")
	@Test
	public void nestedTryCatchBlocks_2() {
		String o = null;
		try {
			String inner = "inner";
			assertThat(TestHelper.getLastRecordedStateForVariable("inner"), equalTo(inner));
			String inner2 = "inner2";
			assertThat(TestHelper.getLastRecordedStateForVariable("inner2"), equalTo(inner2));
			o.length();
		} catch (Exception e) {
			o = "fallback";
			assertThat(TestHelper.getLastRecordedStateForVariable("e"), equalTo(e.toString()));
			assertThat(TestHelper.getLastRecordedStateForVariable("o"), equalTo(o));
			
			String o_2 = null;
			try {
				String inner_2 = "inner";
				assertThat(TestHelper.getLastRecordedStateForVariable("inner_2"), equalTo(inner_2));
				String inner2_2 = "inner2";
				assertThat(TestHelper.getLastRecordedStateForVariable("inner2_2"), equalTo(inner2_2));
				o.length();
			} catch (Exception e_2) {
				o_2 = "fallback";
				assertThat(TestHelper.getLastRecordedStateForVariable("e_2"), equalTo(e_2.toString()));
				assertThat(TestHelper.getLastRecordedStateForVariable("o_2"), equalTo(o_2));
			}
		}
	}
	
}
