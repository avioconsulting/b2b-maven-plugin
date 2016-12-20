package com.avioconsulting.b2b.maven

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
    }

    @Test
    void runs_CorrectTarget() {
        // arrange
        mojo.b2BArtifactType = B2BArtifactTypes.DocumentDefinitions
        simpleFileStub()

        // act
        mojo.execute()

        // assert
        assertThat targetsRun,
                   is(equalTo(['b2bimport', 'b2bvalidate', 'b2bdeploy']))
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
                           overwrite: false.toString()
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
                           overwrite: true.toString()
                   ]))
    }
}
