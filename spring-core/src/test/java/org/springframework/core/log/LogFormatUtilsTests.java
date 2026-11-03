/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LogFormatUtils}.
 *
 * @author Seal Security
 */
class LogFormatUtilsTests {

	@Test
	void formatValueWithNull() {
		assertThat(LogFormatUtils.formatValue(null, false)).isEmpty();
		assertThat(LogFormatUtils.formatValue(null, true)).isEmpty();
		assertThat(LogFormatUtils.formatValue(null, 5, true)).isEmpty();
	}

	@Test
	void formatValueQuotesCharSequence() {
		assertThat(LogFormatUtils.formatValue("foo", -1, false)).isEqualTo("\"foo\"");
		assertThat(LogFormatUtils.formatValue(new StringBuilder("foo"), -1, false)).isEqualTo("\"foo\"");
		assertThat(LogFormatUtils.formatValue(42, -1, false)).isEqualTo("42");
	}

	@Test
	void formatValueTruncatesAtMaxLength() {
		assertThat(LogFormatUtils.formatValue("foobar", 4, false)).isEqualTo("\"foo (truncated)...");
		assertThat(LogFormatUtils.formatValue("foo", 5, false)).isEqualTo("\"foo\"");
	}

	@Test
	void formatValueWithoutMaxLength() {
		String longString = repeat('a', 500);
		assertThat(LogFormatUtils.formatValue(longString, -1, false)).isEqualTo("\"" + longString + "\"");
	}

	@Test
	void formatValueReplacesNewlines() {
		String result = LogFormatUtils.formatValue("a\nb\rc", -1, true);
		assertThat(result).isEqualTo("\"a<LF>b<CR>c\"");
		assertThat(result).contains("<LF>").contains("<CR>").doesNotContain("\n").doesNotContain("\r");
	}

	@Test
	void formatValueKeepsNewlinesWhenNotReplacing() {
		assertThat(LogFormatUtils.formatValue("a\nb\rc", -1, false)).isEqualTo("\"a\nb\rc\"");
	}

	@Test
	void formatValueReplacesNewlinesInNonCharSequence() {
		Object value = new Object() {
			@Override
			public String toString() {
				return "line1\r\nline2";
			}
		};
		assertThat(LogFormatUtils.formatValue(value, -1, true)).isEqualTo("line1<CR><LF>line2");
	}

	@Test
	void formatValueWithLimitLength() {
		String longString = repeat('a', 200);
		assertThat(LogFormatUtils.formatValue(longString, false)).isEqualTo("\"" + longString + "\"");
		assertThat(LogFormatUtils.formatValue(longString, true))
				.isEqualTo("\"" + repeat('a', 99) + " (truncated)...");
	}

	@Test
	void formatValueWithLimitLengthPreventsLogForging() {
		String forged = "user\r\n2021-10-01 INFO forged log entry";
		String result = LogFormatUtils.formatValue(forged, true);
		assertThat(result).isEqualTo("\"user<CR><LF>2021-10-01 INFO forged log entry\"");
		assertThat(result).doesNotContain("\n").doesNotContain("\r");
	}

	private static String repeat(char c, int count) {
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

}
