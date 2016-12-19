package com.avioconsulting.b2b.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

@SuppressWarnings("GroovyUnusedDeclaration")
@Mojo(name = 'b2bExport')
class B2bExportMojo extends AbstractB2bMojo {
    @Parameter(property = 'b2b.export', defaultValue = 'false')
    private boolean doExport

    void execute() throws MojoExecutionException, MojoFailureException {
        if (!this.doExport) {
            this.log.info 'Skipping B2B export, use -Db2b.export=true to enable it'
            return
        }
        this.log.info "Exporting B2B artifacts from server to this project's directory..."
        def antProject = createAntProject()
        MavenProject mavenProject = this.pluginContext.project
        def basedir = mavenProject.basedir
        def tmpDir = new File(basedir, 'tmp')
        def zipPath = new File(tmpDir, 'b2bExport.zip')
        antProject.setProperty 'exportfile', zipPath.absolutePath
        runAntTarget antProject, 'b2bexport'
        def antBuilder = new AntBuilder()
        this.log.info 'Unpacking temporary ZIP file'
        antBuilder.unzip src: zipPath.absolutePath,
                         dest: tmpDir.absolutePath,
                         overwrite: true
        def b2bPath = join(tmpDir, 'soa', 'b2b')
        def outputDir = new File(basedir, 'src')
        if (outputDir.exists()) {
            outputDir.deleteDir()
        }
        b2bPath.renameTo(outputDir)
        this.log.info 'Removing temporary directory'
        tmpDir.deleteDir()
    }
}
