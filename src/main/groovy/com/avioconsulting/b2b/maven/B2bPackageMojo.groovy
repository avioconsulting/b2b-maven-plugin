package com.avioconsulting.b2b.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo

@SuppressWarnings("GroovyUnusedDeclaration")
@Mojo(name = 'b2bPackage')
class B2bPackageMojo extends AbstractB2bMojo {
    void execute() throws MojoExecutionException, MojoFailureException {
        def mavenBuild = this.project.build
        def antBuilder = new AntBuilder()
        this.log.info 'Arranging B2B directory structure...'
        def tmpDir = this.tempDirectory
        if (tmpDir.exists()) {
            tmpDir.deleteDir()
        }
        def expectedSoaPath = join tmpDir, 'soa'
        expectedSoaPath.mkdirs()
        antBuilder.copy(todir: expectedSoaPath) {
            fileset(dir: mavenBuild.outputDirectory)
        }
        def zipFile = join new File(mavenBuild.directory), "${mavenBuild.finalName}.jar"
        this.log.info "Zipping up B2B artifacts into ${zipFile}..."
        antBuilder.zip(destFile: zipFile.absolutePath) {
            fileset(dir: tmpDir)
        }
        this.log.info 'ZIP file created....'
        this.project.artifact.file = zipFile
        tmpDir.deleteDir()
    }
}
