package com.ft.platform.dropwizard;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CheckResultDataTest {

	private static final CheckResultData okResult = new CheckResultData(null, AdvancedResult.healthy());
	private static final CheckResultData warnResult = new ErrorCheckResultData(null, AdvancedResult.warn(null, "This is a warning"));
	private static final CheckResultData errorResult = new ErrorCheckResultData(null, AdvancedResult.error(null, "This is an error"));
	
	@Test
	public void list_of_check_result_data_with_ok_first_should_have_error_first_when_sorted() {
		List<CheckResultData> checkResults = Arrays.asList(okResult, warnResult, errorResult);
		Collections.sort(checkResults);
		List<CheckResultData> expectedResults = Arrays.asList(errorResult, warnResult, okResult);
		assertListSorted(expectedResults, checkResults);
	}
	
	@Test
	public void list_of_check_result_data_with_warn_first_should_have_error_first_when_sorted() {
		List<CheckResultData> checkResults = Arrays.asList(warnResult, errorResult, okResult);
		Collections.sort(checkResults);
		List<CheckResultData> expectedResults = Arrays.asList(errorResult, warnResult, okResult);
		assertListSorted(expectedResults, checkResults);
	}
	
	@Test
	public void list_of_check_result_data_with_error_first_should_have_error_first_when_sorted() {
		List<CheckResultData> checkResults = Arrays.asList(errorResult, okResult, warnResult);
		Collections.sort(checkResults);
		List<CheckResultData> expectedResults = Arrays.asList(errorResult, warnResult, okResult);
		assertListSorted(expectedResults, checkResults);
	}
	
	private void assertListSorted(List<CheckResultData> expected, List<CheckResultData> actual) {
		assertThat(expected.size(), is(actual.size()));
		
		for (int i=0; i<expected.size(); i++) {
			assertThat(expected.get(i), is(actual.get(i)));
		}
	}
	
}
