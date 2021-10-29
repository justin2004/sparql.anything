/*
 * Copyright (c) 2021 Enrico Daga @ http://www.enridaga.net
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.sparqlanything.markdown;

import com.github.sparqlanything.testutils.AbstractTriplifierTester;
import org.junit.Test;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class MARKDOWNTriplifierTest extends AbstractTriplifierTester {
	public MARKDOWNTriplifierTest() {
		super(new MARKDOWNTriplifier(), new Properties());
	}

	@Test
	public void testDocument() {
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testDocument_2() {
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testHeading(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testParagraph(){
		assertResultIsIsomorphicWithExpected();
	}

//
	@Test
	public void testCode(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testLink(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testLink_2(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testBlockQuote(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testText(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testEmphasis(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testStrongEmphasis(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testIndentedCodeBlock(){
		assertResultIsIsomorphicWithExpected();
	}


	@Test
	public void testListItem(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testBulletList(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testOrderedList(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testOrderedList_2(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testFencedCodeBlock(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testHardLineBreak(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testSoftLineBreak(){
		assertResultIsIsomorphicWithExpected();
	}

//	@Test
//	public void testThematicBreak(){
//		logger.debug("Test: {}", name.getMethodName());
//		assertTrue(this.result.isIsomorphicWith(expected));
//	}

	@Test
	public void testHtmlInline(){
		assertResultIsIsomorphicWithExpected();
	}

	@Test
	public void testHtmlBlock(){
		assertResultIsIsomorphicWithExpected();
	}
//
//	@Test
//	public void testIndentedCodeBlock(){}
//
//	@Test
//	public void testImage(){}
//
//
//	@Test
//	public void testLinkReferenceDefinition(){}
//
//
//	@Test
//	public void testCustomBlock(){}
//
//	@Test
//	public void testCustomNode(){}

}