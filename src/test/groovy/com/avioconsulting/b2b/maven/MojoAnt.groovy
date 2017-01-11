package com.avioconsulting.b2b.maven

import groovy.mock.interceptor.StubFor
import org.apache.commons.io.FileUtils
import org.apache.tools.ant.Project
import org.junit.Before

trait MojoAnt {
    def targetsRun
    def propsUsed
    final File baseDirectory = new File('build/tmp/the_base_dir')
    final File zipFile = new File(baseDirectory, 'tmp/b2bExport.zip')

    @Before
    void mojoAntSetup() {
        if (baseDirectory.exists()) {
            baseDirectory.deleteDir()
        }
        baseDirectory.mkdirs()
        targetsRun = []
        propsUsed = [:]
    }

    def createTradingPartnerFile(String name, File directory) {
        def id = "tp_${name}"
        def path = new File(directory, "${id}.xml")
        path.write("<?xml version = '1.0' encoding = 'UTF-8'?>\n" +
                           "<TradingPartner hosted=\"true\" id=\"${id}\" logoSO=\"/soa/b2b/seed/defaultSeededHostTpIcon.png\" name=\"${name}\" version=\"12.2.1.1.0\" xmlns=\"http://xmlns.oracle.com/integration/b2b/profile\"/>")
    }

    def createTradingPartnerAgreementFile(String name, File directory) {
        def id = "tpa_${name}"
        def path = new File(directory, "${id}.xml")
        path.write("<?xml version='1.0' encoding='UTF-8'?>\n" +
                           "<Agreement agreementId=\"${id}\" id=\"${id}\" name=\"${name}\" xmlns=\"http://xmlns.oracle.com/integration/b2b/profile\"/>")
    }

    def simpleFileStub() {
        getAntProjectMock(mojo) { t ->
            def zipDir = new File(baseDirectory, 'b2bzipcontents')
            def b2bDir = new File(zipDir, 'soa/b2b')
            b2bDir.mkdirs()
            def ediDir = new File(b2bDir, 'EDI_X12/v5010/837/837Default')
            ediDir.mkdirs()
            new File(b2bDir, 'doc_HL7.xml').write("<xml/>")
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

    def getAntProjectMock(mojo, Closure<String> onTarget) {
        def stub = new StubFor(Project)
        def demand = stub.demand
        demand.setProperty(0..20) { prop, val ->
            println "setting prop ${prop}"
            propsUsed[prop] = val
        }
        demand.executeTarget(0..20) { t ->
            targetsRun << t
            println "mock execute target ${t}"
            onTarget(t)
        }
        demand.fireBuildStarted(0..20) {}
        demand.init(0..20) {}
        demand.fireBuildFinished(0..20) { e -> }
        def project = stub.proxyInstance()
        mojo.metaClass.createAntProject = { -> project }
    }
}