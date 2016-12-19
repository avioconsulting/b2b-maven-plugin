package com.avioconsulting.b2b.maven

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.apache.tools.ant.Project
import org.apache.tools.ant.ProjectHelper

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

    @Parameter(property = 'soa.oracle.home', required = true)
    private String oracleSoaHome

    public void execute() throws MojoExecutionException, MojoFailureException {
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

    private static File join(File parent, String... parts) {
        def separator = System.getProperty 'file.separator'
        new File(parent, parts.join(separator))
    }

    private void runAntTarget(Project antProject, String target) {
        try {
            antProject.fireBuildStarted()
            antProject.init()
            antProject.executeTarget target
            antProject.fireBuildFinished null
        }
        catch (e) {
            antProject.fireBuildFinished e
            throw e
        }
    }

    private void setStandardB2BAntProperties(Project antProject) {
        // want this to throw errors by default
        antProject.setProperty 'exitonerror', true.toString()
        antProject.setProperty 'java.naming.provider.url', this.soaDeployUrl
        antProject.setProperty 'java.naming.factory.initial', 'weblogic.jndi.WLInitialContextFactory'
        antProject.setProperty 'java.naming.security.principal', this.weblogicUser
        antProject.setProperty 'java.naming.security.credentials', this.weblogicPassword
    }

    private Project createAntProject() {
        def antProject = new Project()
        def binPath = new File(this.oracleSoaHome, 'bin')
        def antXmlPath = new File(binPath, 'ant-b2b-util.xml')
        if (!antXmlPath.exists()) {
            throw new FileNotFoundException("Unable to find B2B ANT task @ ${antXmlPath}!")
        }
        antProject.setUserProperty 'ant.file', antXmlPath.absolutePath
        antProject.addBuildListener new AntLogger(this.log)
        def helper = ProjectHelper.projectHelper
        antProject.addReference 'ant.projectHelper', helper
        helper.parse antProject, antXmlPath
        setStandardB2BAntProperties antProject
        antProject
    }
}
