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
