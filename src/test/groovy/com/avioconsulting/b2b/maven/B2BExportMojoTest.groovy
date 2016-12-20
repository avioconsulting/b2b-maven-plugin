package com.avioconsulting.b2b.maven

import groovy.mock.interceptor.StubFor
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
        mojo.b2BArtifactType = AbstractB2bMojo.B2BArtifactType.DocumentDefinitions
        getAntProjectMock(mojo) { t ->
            def zipDir = new File(baseDirectory, 'b2bzipcontents')
            def b2bDir = new File(zipDir, 'soa/b2b')
            b2bDir.mkdirs()
            def ediDir = new File(b2bDir, 'EDI_X12/v5010/837/837Default')
            ediDir.mkdirs()
            FileUtils.touch new File(b2bDir, 'doc_HL7.xml')
            createTradingPartnerFile 'id1', b2bDir
            createTradingPartnerAgreementFile 'aid1', b2bDir
            FileUtils.touch new File(ediDir, 'X12-5010-837.xsd')

            def antBuilder = new AntBuilder()
            antBuilder.zip(destFile: zipFile.absolutePath) {
                fileset(dir: zipDir)
            }
            zipDir.deleteDir()
        }

        // act
        mojo.execute()
        def files = new FileNameFinder().getFileNames(baseDirectory.absolutePath, '**/*')
                .collect { f -> f.replace(new File(baseDirectory, 'src/main/resources/b2b').absolutePath+'/', '') }

        // assert
        assertThat files,
                   is(equalTo([
                           'EDI_X12/v5010/837/837Default/X12-5010-837.xsd',
                           'doc_HL7.xml'
                   ]))
    }

    private createTradingPartnerFile(String id, File directory) {
        def path = new File(directory, "tp_${id}.xml")
        path.write("<?xml version = '1.0' encoding = 'UTF-8'?>\n" +
                           "<TradingPartner hosted=\"true\" id=\"${id}\" logoSO=\"/soa/b2b/seed/defaultSeededHostTpIcon.png\" name=\"${id}\" version=\"12.2.1.1.0\" xmlns=\"http://xmlns.oracle.com/integration/b2b/profile\"/>")
    }

    private createTradingPartnerAgreementFile(String id, File directory) {
        def path = new File(directory, "tpa_${id}.xml")
        path.write("<?xml version='1.0' encoding='UTF-8'?>\n" +
                           "<Agreement agreementId=\"${id}\" id=\"${id}\" name=\"${id}\" xmlns=\"http://xmlns.oracle.com/integration/b2b/profile\"/>")
    }

    private SimpleFileStub() {
        getAntProjectMock(mojo) { t ->
            def zipDir = new File(baseDirectory, 'b2bzipcontents')
            def file = new File(zipDir, 'soa/b2b/theFile.xml')
            file.parentFile.mkdirs()
            FileUtils.touch file
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
