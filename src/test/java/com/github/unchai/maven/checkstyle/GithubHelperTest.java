/*
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
package com.github.unchai.maven.checkstyle;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GithubHelperTest {
    private GithubHelper githubHelper;

    @Before
    public void setUp() {
        githubHelper = new GithubHelper();
    }

    @Test
    public void parsePatchMODIFY() throws IOException {
        final Map<Integer, Integer> diffMap = githubHelper.parsePatch(readFile("/diff_modify.txt"));

        assertThat(diffMap.size(), is(3));
        assertThat(diffMap.get(27), is(5));
        assertThat(diffMap.get(35), is(13));
        assertThat(diffMap.get(36), is(14));
    }

    @Test
    public void parsePatchADD() throws IOException {
        final Map<Integer, Integer> diffMap = githubHelper.parsePatch(readFile("/diff_add.txt"));

        assertThat(diffMap.size(), is(3));
        assertThat(diffMap.get(1), is(1));
        assertThat(diffMap.get(2), is(2));
        assertThat(diffMap.get(3), is(3));
    }

    @Test
    public void parsePatchDELETE() throws IOException {
        final Map<Integer, Integer> diffMap = githubHelper.parsePatch(readFile("/diff_delete.txt"));

        assertThat(diffMap.isEmpty(), is(true));
    }

    private String readFile(String filename) throws IOException {
        try (InputStream in = this.getClass().getResourceAsStream(filename)) {
            final Scanner scanner = new Scanner(in).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
