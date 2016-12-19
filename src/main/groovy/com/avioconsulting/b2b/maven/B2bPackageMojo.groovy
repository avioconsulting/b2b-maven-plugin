package com.avioconsulting.b2b.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo

@SuppressWarnings("GroovyUnusedDeclaration")
@Mojo(name = 'b2bPackage')
class B2bPackageMojo extends AbstractB2bMojo {
    void execute() throws MojoExecutionException, MojoFailureException {
    }
}
