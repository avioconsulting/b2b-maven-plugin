package com.avioconsulting.b2b.filenaming

import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

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
    void references_fileRenamed() {
        // arrange
        def inputFilename = 'tp_luZRYV-7072537905454701067.xml'

        // act
        def result = FixTradingPartner.fix inputFilename, directory

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
        FixTradingPartner.fix inputFilename, directory
        def expectedFile = new File(directory, 'tp_BradyInc.xml')
        def actualId = new XmlParser().parse(expectedFile).@id

        // assert
        assertThat actualId as String,
                   is(equalTo('tp_BradyInc'))
    }
}
