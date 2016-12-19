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
    }
}
