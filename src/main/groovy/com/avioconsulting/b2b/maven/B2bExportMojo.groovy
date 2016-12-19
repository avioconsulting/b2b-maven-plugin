package com.avioconsulting.b2b.maven

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@Mojo(name = 'b2bExport')
public class B2bExportMojo extends AbstractMojo {
    @Parameter(property = 'b2b.overwrite', defaultValue = 'true')
    private boolean overwrite

    @Parameter(property = 'weblogic.user', required = true)
    private String weblogicUser

    @Parameter(property = 'weblogic.password', required = true)
    private String weblogicPassword

    @Parameter(property = 'soa.deploy.url', required = true)
    private String soaDeployUrl

    public void execute() throws MojoExecutionException, MojoFailureException {
        print "We would deploy using username ${this.weblogicUser} and password ${this.weblogicPassword} on URL ${this.soaDeployUrl}"
    }
}
