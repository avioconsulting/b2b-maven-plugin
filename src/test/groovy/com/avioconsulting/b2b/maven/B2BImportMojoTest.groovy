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

        // act
        mojo.execute()

        // assert
        assertThat propsUsed,
                   is(equalTo([
                           exportfile: zipFile.absolutePath,
                           overwrite : false.toString(),
                           args      : 'tp_partner1.xml,tpa_agree1.xml,tpa_agree2.xml',
                           tpanames  : 'agree1,agree2'
                   ]))
    }
}
