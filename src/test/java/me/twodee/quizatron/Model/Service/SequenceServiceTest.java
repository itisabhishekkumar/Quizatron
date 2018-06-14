package me.twodee.quizatron.Model.Service;

import me.twodee.quizatron.Model.Entity.Configuration.Configuration;
import me.twodee.quizatron.Model.Entity.QuizData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class SequenceServiceTest
{
    @Mock
    private QuizData quizData;
    @Mock
    private Configuration configuration;

    //private SequenceMapper sequenceMapper;

    @Before
    public void beforeEachTest() throws URISyntaxException, MalformedURLException
    {
        MockitoAnnotations.initMocks(this);
        when(quizData.getConfiguration()).thenReturn(configuration);
        when(configuration.getSequence())
                .thenReturn(this.getClass().getResource("/sequence.csv").toURI().toURL().toExternalForm());
    }
    @Test
    public void sequenceLoadTest() throws IOException
    {
        //sequenceMapper = new SequenceMapper();
        //SequenceService sequence = new SequenceService(quizData, sequenceMapper);
        //sequence.load();
        //assertThat(sequence.isLoaded(), is(true));
    }
}
