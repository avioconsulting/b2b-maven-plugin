package com.avioconsulting.b2b.maven

import groovy.test.GroovyAssert
import org.apache.maven.model.Build
import org.apache.maven.project.MavenProject
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class B2BExportMojoTest implements MojoAnt {
    B2bExportMojo mojo

    @Before
    void setup() {
        mojo = new B2bExportMojo()
        mojo.metaClass.getBaseDir = { -> baseDirectory }
        def build = [outputDirectory: new File(new File(baseDirectory, 'target'), 'classes')] as Build
        mojo.metaClass.project = [build: build] as MavenProject
    }

    @Test
    void disabled() {
        // arrange
        mojo.doExport = false
        def run = false
        mojo.metaClass.createAntProject = { ->
            run = true
        }

        // act
        mojo.execute()

        // assert
        assertThat run,
                   is(equalTo(false))
    }

    @Test
    void runs_CorrectTarget() {
        // arrange
        mojo.doExport = true
        mojo.b2BArtifactType = B2BArtifactTypes.DocumentDefinitions
        simpleFileStub()

        // act
        mojo.execute()

        // assert
        assertThat targetsRun,
                   is(equalTo(['b2bexport']))
    }

    @Test
    void runs_CorrectProperties() {
        // arrange
        mojo.doExport = true
        mojo.b2BArtifactType = B2BArtifactTypes.DocumentDefinitions
        simpleFileStub()

        // act
        mojo.execute()

        // assert
        assertThat propsUsed,
                   is(equalTo([exportfile: zipFile.absolutePath]))
    }

    @Test
    void runs_FileStructure_DocumentDef() {
        // arrange
        mojo.doExport = true
        mojo.b2BArtifactType = B2BArtifactTypes.DocumentDefinitions
        simpleFileStub()

        // act
        mojo.execute()
        def files = new FileNameFinder().getFileNames(baseDirectory.absolutePath, '**/*')
                .collect { f -> f.replace(new File(baseDirectory, 'src/main/resources/b2b').absolutePath + '/', '') }

        // assert
        assertThat files,
                   is(equalTo([
                           'EDI_X12/v5010/837/837Default/X12-5010-837.xsd',
                           'doc_HL7.xml'
                   ]))
    }

    @Test
    void runs_FileStructure_TradingPartners_NoList() {
        // arrange
        mojo.doExport = true
        mojo.b2BArtifactType = B2BArtifactTypes.PartnersAndAgreements
        simpleFileStub()

        // act
        def exception = GroovyAssert.shouldFail {
            mojo.execute()
        }

        // assert
        assertThat exception.message,
                   is(equalTo('If b2b.artifact.type/PartnersAndAgreements is used, must supply b2b.partners/agreements!'))
    }

    @Test
    void runs_FileStructure_TradingPartners() {
        // arrange
        mojo.doExport = true
        mojo.b2BArtifactType = B2BArtifactTypes.PartnersAndAgreements
        mojo.partners = ['partner1']
        mojo.agreements = ['agree1']
        simpleFileStub()

        // act
        mojo.execute()
        def files = new FileNameFinder().getFileNames(baseDirectory.absolutePath, '**/*')
                .collect { f -> f.replace(new File(baseDirectory, 'src/main/resources/b2b').absolutePath + '/', '') }

        // assert
        assertThat files,
                   is(equalTo([
                           'tp_partner1.xml',
                           'tpa_agree1.xml'
                   ]))
        assertThat new File(baseDirectory, 'src/main/resources/b2b/EDI_X12').exists(),
                   is(equalTo(false))
    }
}
