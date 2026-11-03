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
		assertThat(result).isEqualTo("\"a<EOL>b<EOL>c\"");
		assertThat(result).contains("<EOL>").doesNotContain("\n").doesNotContain("\r");
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
		assertThat(LogFormatUtils.formatValue(value, -1, true)).isEqualTo("line1<EOL><EOL>line2");
	}

	@Test
	void formatValueReplacesControlCharacters() {
		String result = LogFormatUtils.formatValue("a\u001b[31mb\tc\u0000d\u007fe\u0085f", -1, true);
		assertThat(result).isEqualTo("\"a?[31mb?c?d?e?f\"");
		assertThat(result).doesNotContain("\u001b").doesNotContain("\t")
				.doesNotContain("\u0000").doesNotContain("\u007f").doesNotContain("\u0085");
	}

	@Test
	void formatValueKeepsControlCharactersWhenNotReplacing() {
		String value = "a\u001b[31mb\tc\u0000d";
		assertThat(LogFormatUtils.formatValue(value, -1, false)).isEqualTo("\"" + value + "\"");
	}

	@Test
	void formatValueReplacesControlCharactersInNonCharSequence() {
		Object value = new Object() {
			@Override
			public String toString() {
				return "status\u001b[2K\u0008\u0008ok";
			}
		};
		assertThat(LogFormatUtils.formatValue(value, -1, true)).isEqualTo("status?[2K??ok");
	}

	@Test
	void formatValueWithLimitLengthReplacesControlCharacters() {
		String forged = "user\u001b[1A\u001b[2K2021-10-01 INFO forged log entry";
		String result = LogFormatUtils.formatValue(forged, true);
		assertThat(result).isEqualTo("\"user?[1A?[2K2021-10-01 INFO forged log entry\"");
		assertThat(result).doesNotContain("\u001b");
	}

	@Test
	void formatValueWithoutLimitLengthKeepsControlCharacters() {
		String value = "user\u001b[1A\tentry";
		assertThat(LogFormatUtils.formatValue(value, false)).isEqualTo("\"" + value + "\"");
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
		assertThat(result).isEqualTo("\"user<EOL><EOL>2021-10-01 INFO forged log entry\"");
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
