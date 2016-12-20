package com.avioconsulting.b2b.maven

import com.avioconsulting.b2b.ant.MavenLogger
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.apache.tools.ant.Project
import org.apache.tools.ant.ProjectHelper

abstract class AbstractB2bMojo extends AbstractMojo {
    @Parameter(property = 'weblogic.user', required = true)
    protected String weblogicUser

    @Parameter(property = 'weblogic.password', required = true)
    protected String weblogicPassword

    @Parameter(property = 'soa.deploy.url', required = true)
    protected String soaDeployUrl

    @Parameter(property = 'soa.oracle.home', required = true)
    protected String oracleSoaHome

    enum B2BArtifactType {
        DocumentDefinitions,
        PartnersAndAgreements
    }

    @Parameter(property = 'b2b.artifact.type', required = true)
    protected B2BArtifactType b2BArtifactType

    @Parameter(property = 'b2b.partners.agreements', required = false)
    protected String[] partnerAgreements

    @Component
    protected MavenProject project

    protected static File join(File parent, String... parts) {
        def separator = System.getProperty 'file.separator'
        new File(parent, parts.join(separator))
    }

    protected File getBaseDir() {
        this.project.basedir
    }

    protected File getTempDirectory() {
        join this.baseDir, 'tmp'
    }

    protected static void runAntTarget(Project antProject, String target) {
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

    protected Project createAntProject() {
        def antProject = new Project()
        def binPath = new File(this.oracleSoaHome, 'bin')
        def antXmlPath = new File(binPath, 'ant-b2b-util.xml')
        if (!antXmlPath.exists()) {
            throw new FileNotFoundException("Unable to find B2B ANT task @ ${antXmlPath}!")
        }
        antProject.setUserProperty 'ant.file', antXmlPath.absolutePath
        antProject.addBuildListener new MavenLogger(this.log)
        def helper = ProjectHelper.projectHelper
        antProject.addReference 'ant.projectHelper', helper
        helper.parse antProject, antXmlPath
        setStandardB2BAntProperties antProject
        antProject
    }
}
