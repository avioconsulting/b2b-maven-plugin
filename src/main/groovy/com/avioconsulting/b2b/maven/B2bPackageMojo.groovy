package com.avioconsulting.b2b.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@SuppressWarnings("GroovyUnusedDeclaration")
@Mojo(name = 'b2bPackage')
class B2bPackageMojo extends AbstractB2bMojo {
    @Parameter(defaultValue = '${project.build.outputDirectory}', required = true)
    private File classesDirectory
    @Parameter(defaultValue = '${project.build.directory}', required = true)
    private File outputDirectory
    @Parameter(defaultValue = '${project.build.finalName}', readonly = true)
    private String finalName

    void execute() throws MojoExecutionException, MojoFailureException {
        def antBuilder = new AntBuilder()
        this.log.info 'Arranging B2B directory structure...'
        def tmpDir = this.tempDirectory
        if (tmpDir.exists()) {
            tmpDir.deleteDir()
        }
        def expectedSoaPath = join tmpDir, 'soa'
        expectedSoaPath.mkdirs()
        antBuilder.copy(todir: expectedSoaPath) {
            fileset(dir: join(this.classesDirectory))
        }
        def zipFile = join this.outputDirectory, "${this.finalName}.zip"
        this.log.info "Zipping up B2B artifacts into ${zipFile}..."
        antBuilder.zip(destFile: zipFile.absolutePath) {
            fileset(dir: tmpDir)
        }
        this.log.info 'ZIP file created....'
        this.project.artifact.file = zipFile
        tmpDir.deleteDir()
    }
}
