/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.hal;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.hateoas.core.EvoInflectorRelProvider;

/**
 * Unit tests for {@link HalEmbeddedBuilder}.
 * 
 * @author Oliver Gierke
 * @author Dietrich Schulten
 */
public class HalEmbeddedBuilderUnitTest {

	RelProvider provider;

	@Before
	public void setUp() {
		provider = new EvoInflectorRelProvider();
	}

	@Test
	public void rendersSingleElementsWithSingleEntityRel() {

		Map<String, Object> map = setUpBuilder("foo", 1L);

		assertThat(map.get("string"), is((Object) "foo"));
		assertThat(map.get("long"), is((Object) 1L));
	}

	@Test
	public void rendersMultipleElementsWithCollectionResourceRel() {

		Map<String, Object> map = setUpBuilder("foo", "bar", 1L);

		assertThat(map.containsKey("string"), is(false));
		assertThat(map.get("long"), is((Object) 1L));
		assertHasValues(map, "strings", "foo", "bar");
	}

	/**
	 * @see #110
	 */
	@Test
	public void correctlyPilesUpResourcesInCollectionRel() {

		Map<String, Object> map = setUpBuilder("foo", "bar", "foobar", 1L);

		assertThat(map.containsKey("string"), is(false));
		assertHasValues(map, "strings", "foo", "bar", "foobar");
		assertThat(map.get("long"), is((Object) 1L));
	}

	/**
	 * @see #135
	 */
	@Test
	public void forcesCollectionRelToBeUsedIfConfigured() {

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider, true);
		builder.add("Sample");

		assertThat(builder.asMap().get("string"), is(nullValue()));
		assertHasValues(builder.asMap(), "strings", "Sample");
	}

	/**
	 * @see #195
	 */
	@Test
	public void doesNotPreferCollectionsIfRelAwareWasAdded() {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider, true);
		builder.add(wrappers.wrap("MyValue", "foo"));

		assertThat(builder.asMap().get("foo"), is(instanceOf(String.class)));
	}

	/**
	 * @see #195
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullRelProvider() {
		new HalEmbeddedBuilder(null, false);
	}

	private static void assertHasValues(Map<String, Object> source, String rel, Object... values) {

		Object value = source.get(rel);

		assertThat(value, is(instanceOf(List.class)));
		assertThat((List<Object>) value, Matchers.<List<Object>> allOf(hasSize(values.length), hasItems(values)));
	}

	private Map<String, Object> setUpBuilder(Object... values) {

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider, false);

		for (Object value : values) {
			builder.add(value);
		}

		return builder.asMap();
	}
}
