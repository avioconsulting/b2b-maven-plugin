package com.avioconsulting.b2b.util

import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class ListeningChannelFetcherTest {
    @Test
    void fetchActiveListeningChannels() {
        // arrange

        // act
        def channels = new ListeningChannelFetcher().fetchActiveListeningChannels(new File('src/test/resources'))

        // assert
        assertThat channels,
                   is(equalTo(['BETTERMMA', 'BETTERMMA2']))
    }
}
