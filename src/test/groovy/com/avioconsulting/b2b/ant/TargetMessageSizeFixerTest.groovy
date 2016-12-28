package com.avioconsulting.b2b.ant

import com.avioconsulting.b2b.filenaming.Renamer
import groovy.test.GroovyAssert
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.junit.Before
import org.junit.Test

import static junit.framework.TestCase.fail
import static org.hamcrest.Matchers.*
import static org.hamcrest.core.IsNot.not
import static org.junit.Assert.assertThat

class TargetMessageSizeFixerTest {
    def directory = new File('build/tmp/antFix')
    def fixer = new TargetMessageSizeFixer({ str -> println "From class: ${str}" })

    @Before
    void setupFreshData() {
        if (directory.exists()) {
            directory.deleteDir()
        }
    }

    @Test
    void notThere() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void there_Matches() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void there_DifferentSetting() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
