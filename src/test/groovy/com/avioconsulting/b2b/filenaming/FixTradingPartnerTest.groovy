package com.avioconsulting.b2b.filenaming

import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test

import static junit.framework.TestCase.fail

class FixTradingPartnerTest {
    def directory = new File('build/tmp/sampleB2BFiles')

    @Before
    void setupFreshData() {
        if (directory.exists()) {
            directory.deleteDir()
        }
        FileUtils.copyDirectory new File('src/test/resources/sampleB2BFiles'), directory
    }

    @Test
    void stuff() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
