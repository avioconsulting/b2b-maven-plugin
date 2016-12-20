package com.avioconsulting.b2b.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.tools.ant.Project

@SuppressWarnings("GroovyUnusedDeclaration")
@Mojo(name = 'b2bImport')
class B2bImportMojo extends AbstractB2bMojo {
    @Parameter(property = 'b2b.overwrite', defaultValue = 'true')
    private boolean overwrite
    @Parameter(property = 'b2b.purgeBeforeImport', defaultValue = 'false')
    private boolean purgeBeforeImport

    void execute() throws MojoExecutionException, MojoFailureException {
        def artifact = this.project.artifact.file
        this.log.info "Importing B2B artifacts from ${artifact} to server..."
        def antProject = createAntProject()
        antProject.setProperty 'exportfile', artifact.absolutePath
        antProject.setProperty 'overwrite', this.overwrite.toString()

        if (this.purgeBeforeImport) {
            doPurge antProject
        }

        ['b2bimport', 'b2bvalidate', 'b2bdeploy'].each { target ->
            runAntTarget antProject, target
        }
    }

    private void doPurge(Project antProject) {
        this.log.info 'Purging B2B artifacts/data before importing...'
        def runtimeArtifacts = 'RT'
        def modes = [
                runtimeArtifacts,
                'DT'// design time artifacts
        ]
        antProject.setProperty 'archive', false.toString()
        // want a full cleanup
        antProject.setProperty 'purgecontrolnumber', true.toString()
        modes.each { mode ->
            antProject.setProperty 'mode', mode
            runAntTarget antProject, 'b2bpurge'
        }
    }
}
