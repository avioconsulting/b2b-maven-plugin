package com.avioconsulting.b2b.filenaming

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.junit.Before
import org.junit.Test

import static junit.framework.TestCase.fail
import static org.hamcrest.Matchers.hasItems
import static org.hamcrest.core.IsNot.not
import static org.junit.Assert.assertThat

class RenamerTest {
    def directory = new File('build/tmp/sampleB2BFiles')
    def renamer = new Renamer({ str -> println "From class: ${str}" })

    @Before
    void setupFreshData() {
        if (directory.exists()) {
            directory.deleteDir()
        }
    }

    @Test
    void noIdCollisions() {
        // arrange
        FileUtils.copyDirectory new File('src/test/resources/sampleB2BFiles'), directory

        // act
        renamer.fixNames directory

        // assert
        def results = new FileNameFinder().getFileNames(directory.absolutePath, 'tp*.xml')
                .collect { entry -> FilenameUtils.getName(entry) }
        assertThat results,
                   not(hasItems('tp_luZRYV-7072537905454701067.xml',
                                'tp_XnfTl1318734747197746601.xml',
                                'tpa_aSr1674752484585300652.xml',
                                'tpa_bK-4961045773283993226.xml',
                                'tpa_Gsf8462216507708172471.xml',
                                'tpa_vkKRnu-1313236717328621662.xml',
                                'tpa_yzmn6926690204703390084.xml'))
    }

    @Test
    void idCollisions() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
