package com.avioconsulting.b2b.maven

import groovy.mock.interceptor.StubFor
import groovy.test.GroovyAssert
import org.apache.commons.io.FileUtils
import org.apache.tools.ant.Project
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class B2BExportMojoTest {
    B2bExportMojo mojo
    def targetsRun
    def propsUsed
    final File baseDirectory = new File('build/tmp/the_base_dir')
    final File zipFile = new File(baseDirectory, 'tmp/b2bExport.zip')

    @Before
    void setup() {
        mojo = new B2bExportMojo()
        if (baseDirectory.exists()) {
            baseDirectory.deleteDir()
        }
        baseDirectory.mkdirs()
        mojo.metaClass.getBaseDir = { -> baseDirectory }
        targetsRun = []
        propsUsed = [:]
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
        SimpleFileStub()

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
        SimpleFileStub()

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
        SimpleFileStub()

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
        SimpleFileStub()

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
        SimpleFileStub()

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

    private createTradingPartnerFile(String name, File directory) {
        def id = "tp_${name}"
        def path = new File(directory, "${id}.xml")
        path.write("<?xml version = '1.0' encoding = 'UTF-8'?>\n" +
                           "<TradingPartner hosted=\"true\" id=\"${id}\" logoSO=\"/soa/b2b/seed/defaultSeededHostTpIcon.png\" name=\"${name}\" version=\"12.2.1.1.0\" xmlns=\"http://xmlns.oracle.com/integration/b2b/profile\"/>")
    }

    private createTradingPartnerAgreementFile(String name, File directory) {
        def id = "tpa_${name}"
        def path = new File(directory, "${id}.xml")
        path.write("<?xml version='1.0' encoding='UTF-8'?>\n" +
                           "<Agreement agreementId=\"${id}\" id=\"${id}\" name=\"${name}\" xmlns=\"http://xmlns.oracle.com/integration/b2b/profile\"/>")
    }

    private SimpleFileStub() {
        getAntProjectMock(mojo) { t ->
            def zipDir = new File(baseDirectory, 'b2bzipcontents')
            def b2bDir = new File(zipDir, 'soa/b2b')
            b2bDir.mkdirs()
            def ediDir = new File(b2bDir, 'EDI_X12/v5010/837/837Default')
            ediDir.mkdirs()
            FileUtils.touch new File(b2bDir, 'doc_HL7.xml')
            createTradingPartnerFile 'partner1', b2bDir
            createTradingPartnerFile 'partner2', b2bDir
            createTradingPartnerAgreementFile 'agree1', b2bDir
            createTradingPartnerAgreementFile 'agree2', b2bDir
            FileUtils.touch new File(ediDir, 'X12-5010-837.xsd')

            def antBuilder = new AntBuilder()
            antBuilder.zip(destFile: zipFile.absolutePath) {
                fileset(dir: zipDir)
            }
            zipDir.deleteDir()
        }
    }

    private getAntProjectMock(mojo, Closure<String> onTarget) {
        def stub = new StubFor(Project)
        def demand = stub.demand
        demand.setProperty { prop, val ->
            println "setting prop ${prop}"
            propsUsed[prop] = val
        }
        demand.executeTarget { t ->
            targetsRun << t
            println "mock execute target ${t}"
            onTarget(t)
        }
        demand.fireBuildStarted {}
        demand.init {}
        demand.fireBuildFinished { e -> }
        mojo.metaClass.createAntProject = { ->
            GroovySystem.getMetaClassRegistry().setMetaClass(stub.proxy.theClass, stub.proxy)
            new Project()
        }
    }
}
