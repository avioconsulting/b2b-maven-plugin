package com.avioconsulting.b2b.maven

import com.avioconsulting.b2b.util.ListeningChannelFetcher
import groovy.mock.interceptor.StubFor
import org.apache.maven.model.Build
import org.apache.maven.project.MavenProject
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class B2BImportMojoTest implements MojoAnt {
    B2bImportMojo mojo
    final File zipFile = new File('theZipFile.zip')

    @Before
    void setup() {
        mojo = new B2bImportMojo()
        mojo.metaClass.getBaseDir = { -> baseDirectory }
        mojo.metaClass.getProjectArtifact = { -> zipFile }
        def build = [outputDirectory: new File(new File(baseDirectory, 'target'), 'classes')] as Build
        mojo.metaClass.project = [build: build] as MavenProject
    }

    @Test
    void runs_CorrectTargets_docDefs() {
        // arrange
        mojo.b2BArtifactType = B2BArtifactTypes.DocumentDefinitions
        simpleFileStub()

        // act
        mojo.execute()

        // assert
        assertThat targetsRun,
                   is(equalTo(['b2bimport']))
    }

    @Test
    void runs_CorrectTargets_partners() {
        // arrange
        mojo.b2BArtifactType = B2BArtifactTypes.PartnersAndAgreements
        mojo.partners = ['partner1']
        mojo.agreements = ['agree1']
        simpleFileStub()
        def stub = new StubFor(ListeningChannelFetcher)
        stub.demand.fetchListeningChannels { File file ->
            assertThat file,
                       is(equalTo(new File(new File(baseDirectory, 'target'), 'classes')))
            ['channel1', 'channel2']
        }
        stub.use {

            // act
            mojo.execute()
        }

        // assert
        // updatechannel runs twice because we have 2 different channels
        assertThat targetsRun,
                   is(equalTo(['b2bimport', 'b2bdeploy', 'updatechannel', 'updatechannel']))
    }

    @Test
    void runs_CorrectProperties() {
        // arrange
        mojo.b2BArtifactType = B2BArtifactTypes.DocumentDefinitions
        simpleFileStub()

        // act
        mojo.execute()

        // assert
        assertThat propsUsed,
                   is(equalTo([
                           exportfile: zipFile.absolutePath,
                           overwrite : false.toString()
                   ]))
    }

    @Test
    void runs_CorrectProperties_overwrite() {
        // arrange
        mojo.b2BArtifactType = B2BArtifactTypes.DocumentDefinitions
        mojo.overwrite = true
        simpleFileStub()

        // act
        mojo.execute()

        // assert
        assertThat propsUsed,
                   is(equalTo([
                           exportfile: zipFile.absolutePath,
                           overwrite : true.toString()
                   ]))
    }

    @Test
    void runs_properties_PartnersAndAgreements() {
        // arrange
        mojo.b2BArtifactType = B2BArtifactTypes.PartnersAndAgreements
        mojo.partners = ['partner1']
        mojo.agreements = ['agree1', 'agree2']
        simpleFileStub()

        def stub = new StubFor(ListeningChannelFetcher)
        stub.demand.fetchListeningChannels { File file ->
            assertThat file,
                       is(equalTo(new File(new File(baseDirectory, 'target'), 'classes')))
            ['channel1', 'channel2']
        }
        stub.use {

            // act
            mojo.execute()
        }

        // assert
        assertThat propsUsed,
                   is(equalTo([
                           exportfile : zipFile.absolutePath,
                           overwrite  : false.toString(),
                           tpanames   : 'agree1,agree2',
                           state      : 'active',
                           channelname: 'channel2' // will show last channel only
                   ]))
    }
}
