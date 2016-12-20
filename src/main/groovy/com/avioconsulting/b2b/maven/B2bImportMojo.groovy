package com.avioconsulting.b2b.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@SuppressWarnings("GroovyUnusedDeclaration")
@Mojo(name = 'b2bImport')
class B2bImportMojo extends AbstractB2bMojo {
    @Parameter(property = 'b2b.overwrite', defaultValue = 'true')
    private boolean overwrite

    void execute() throws MojoExecutionException, MojoFailureException {
        def artifact = this.project.artifact.file
        this.log.info "Importing B2B artifacts from ${artifact} to server..."
        def antProject = createAntProject()
        antProject.setProperty 'exportfile', artifact.absolutePath
        antProject.setProperty 'overwrite', this.overwrite.toString()
        runAntTarget antProject, 'b2bimport'
    }
}
