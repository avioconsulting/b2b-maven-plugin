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
    def fixer = new FixDesignData({ str -> println "From class: ${str}" })

    @Before
    void setupFreshData() {
        if (directory.exists()) {
            directory.deleteDir()
        }
        FileUtils.copyDirectory new File('src/test/resources/sampleB2BFiles'), directory
    }

    @Test
    void tradingPartner_fileRenamed() {
        // arrange
        def inputFilename = new File(directory, 'tp_luZRYV-7072537905454701067.xml')

        // act
        def result = fixer.fix inputFilename

        // assert
        assertThat result,
                   is(equalTo('tp_BradyInc'))
        assertThat inputFilename.exists(),
                   is(equalTo(false))
        def expectedFile = new File(directory, 'tp_BradyInc.xml')
        assertThat expectedFile.exists(),
                   is(equalTo(true))
    }

    @Test
    void tradingPartner_IdAttributeChanged() {
        // arrange
        def inputFilename = new File(directory, 'tp_luZRYV-7072537905454701067.xml')

        // act
        fixer.fix inputFilename
        def expectedFile = new File(directory, 'tp_BradyInc.xml')
        def actualId = new XmlParser().parse(expectedFile).@id

        // assert
        assertThat actualId as String,
                   is(equalTo('tp_BradyInc'))
    }

    @Test
    void tradingPartner_othersChanged() {
        // arrange
        def inputFilename = new File(directory, 'tp_luZRYV-7072537905454701067.xml')

        // act
        fixer.fix inputFilename
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

    @Test
    void tradingPartnerAgreement_fileRenamed() {
        // arrange
        def inputFilename = new File(directory, 'tpa_aSr1674752484585300652.xml')

        // act
        def result = fixer.fix inputFilename

        // assert
        assertThat result,
                   is(equalTo('tpa_A_PARTNER_999Agreement'))
        assertThat inputFilename.exists(),
                   is(equalTo(false))
        def expectedFile = new File(directory, 'tpa_A_PARTNER_999Agreement.xml')
        assertThat expectedFile.exists(),
                   is(equalTo(true))
    }

    @Test
    void tradingPartnerAgreement_IdAttributeChanged() {
        // arrange
        def inputFilename = new File(directory, 'tpa_aSr1674752484585300652.xml')

        // act
        fixer.fix inputFilename
        def expectedFile = new File(directory, 'tpa_A_PARTNER_999Agreement.xml')
        def actualId = new XmlParser().parse(expectedFile).@id

        // assert
        assertThat actualId as String,
                   is(equalTo('tpa_A_PARTNER_999Agreement'))
    }
}
