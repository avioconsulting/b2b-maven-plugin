package com.avioconsulting.b2b.filenaming

import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.core.IsNot.not
import static org.junit.Assert.assertThat

class FixDesignDataTest {
    def directory = new File('build/tmp/sampleB2BFiles')
    def logger = new FixDesignData({ str -> println "From class: ${str}" })

    @Before
    void setupFreshData() {
        if (directory.exists()) {
            directory.deleteDir()
        }
        FileUtils.copyDirectory new File('src/test/resources/sampleB2BFiles'), directory
    }

    @Test
    void references_fileRenamed() {
        // arrange
        def inputFilename = 'tp_luZRYV-7072537905454701067.xml'

        // act
        def result = logger.fix inputFilename, directory

        // assert
        assertThat result,
                   is(equalTo('BradyInc'))
        assertThat new File(directory, inputFilename).exists(),
                   is(equalTo(false))
        def expectedFile = new File(directory, 'tp_BradyInc.xml')
        assertThat expectedFile.exists(),
                   is(equalTo(true))
    }

    @Test
    void references_IdAttributeChanged() {
        // arrange
        def inputFilename = 'tp_luZRYV-7072537905454701067.xml'

        // act
        logger.fix inputFilename, directory
        def expectedFile = new File(directory, 'tp_BradyInc.xml')
        def actualId = new XmlParser().parse(expectedFile).@id

        // assert
        assertThat actualId as String,
                   is(equalTo('tp_BradyInc'))
    }

    @Test
    void references_othersChanged() {
        // arrange
        def inputFilename = 'tp_luZRYV-7072537905454701067.xml'

        // act
        logger.fix inputFilename, directory
        def expectedRefFiles = [
                'tpa_Gsf8462216507708172471.xml',
                'tpa_vkKRnu-1313236717328621662.xml'
        ]

        // assert
        expectedRefFiles.each { file ->
            println "Checking file ${file}..."
            def lines = new File(directory, file).readLines()
            lines.each { line ->
                assertThat line,
                           not(containsString('tp_luZRYV-7072537905454701067'))
            }
        }
    }
}
