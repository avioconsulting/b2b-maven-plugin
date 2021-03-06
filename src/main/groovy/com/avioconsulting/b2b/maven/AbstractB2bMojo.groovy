package com.avioconsulting.b2b.maven

import com.avioconsulting.b2b.ant.MavenLogger
import com.avioconsulting.b2b.ant.TargetMessageSizeFixer
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

    @Parameter(property = 'b2b.artifact.type', required = true)
    protected B2BArtifactTypes b2BArtifactType

    @Parameter(property = 'b2b.partners', required = false)
    protected String[] partners

    @Parameter(property = 'b2b.agreements', required = false)
    protected String[] agreements

    @Parameter(property = TargetMessageSizeFixer.settingName, defaultValue = '40000000')
    private long weblogicMaxMessageSize

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

    protected static String getPartnerFilename(name) {
        "tp_${name}.xml"
    }

    protected static String getAgreementFilename(name) {
        "tpa_${name}.xml"
    }

    protected static void runAntTarget(antProject, target) {
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

    protected void validate() {
        if (this.b2BArtifactType != B2BArtifactTypes.PartnersAndAgreements) {
            return
        }

        if (partners == null || !partners.any() || agreements == null || !agreements.any()) {
            throw new Exception('If b2b.artifact.type/PartnersAndAgreements is used, must supply b2b.partners/agreements!')
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
        // the max message size limit is quickly exceeded and the java ant task in the file does not pass on props
        new TargetMessageSizeFixer({ msg -> this.log.info msg }).fixAntTarget(antXmlPath, this.weblogicMaxMessageSize)
        antProject.setUserProperty 'ant.file', antXmlPath.absolutePath
        antProject.addBuildListener new MavenLogger(this.log)
        def helper = ProjectHelper.projectHelper
        antProject.addReference 'ant.projectHelper', helper
        helper.parse antProject, antXmlPath
        setStandardB2BAntProperties antProject
        antProject
    }
}
