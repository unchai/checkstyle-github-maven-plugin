package com.github.unchai.maven.checkstyle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationLoader.class})
class CheckstyleExecutorTest {
    @Mock
    private Checker checker;

    private CheckstyleExecutor executor;

    public CheckstyleExecutorTest() {
    }

    @Before
    public void setup() {
        executor = new CheckstyleExecutor();
        Whitebox.setInternalState(executor, "checker", checker);
    }

    @Test
    public void testExecute() throws Exception {
        mockStatic(ConfigurationLoader.class);
        when(ConfigurationLoader.loadConfiguration(anyString(), any())).thenReturn(null);
        when(checker.process(any())).thenReturn(100);
        executor.execute(anyString(), any());
    }
}
