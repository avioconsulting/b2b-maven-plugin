package com.avioconsulting.b2b.maven

import com.avioconsulting.b2b.filenaming.Renamer
import groovy.xml.XmlUtil
import org.apache.commons.io.FileUtils
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@SuppressWarnings("GroovyUnusedDeclaration")
@Mojo(name = 'b2bExport')
class B2bExportMojo extends AbstractB2bMojo {
    public static final tradingPartnerPattern = '**/tp*.xml'

    @Parameter(property = 'b2b.export', defaultValue = 'false')
    private boolean doExport

    void execute() throws MojoExecutionException, MojoFailureException {
        def classesDir = new File(this.project.build.outputDirectory)
        if (classesDir.exists()) {
            this.log.info "Cleaning output directory to avoid B2B cruft ${classesDir}..."
            classesDir.deleteDir()
            classesDir.mkdirs()
        }
        if (!this.doExport) {
            this.log.info 'Skipping B2B export, use -Db2b.export=true to enable it'
            return
        }
        this.log.info "Exporting B2B artifacts from server to this project's directory..."
        def antProject = createAntProject()
        def tmpDir = this.tempDirectory
        if (tmpDir.exists()) {
            tmpDir.deleteDir()
        }
        def zipPath = new File(tmpDir, 'b2bExport.zip')
        antProject.setProperty 'exportfile', zipPath.absolutePath
        runAntTarget antProject, 'b2bexport'
        def antBuilder = new AntBuilder()
        this.log.info 'Unpacking temporary ZIP file'
        antBuilder.unzip src: zipPath.absolutePath,
                         dest: tmpDir.absolutePath,
                         overwrite: true
        def b2bPath = join(tmpDir, 'soa', 'b2b')
        def outputDir = join this.baseDir, 'src', 'main', 'resources', 'b2b'
        if (outputDir.exists()) {
            outputDir.deleteDir()
        }
        outputDir.mkdirs()
        FileUtils.copyDirectory(b2bPath, outputDir)
        this.log.info 'Removing temporary directory'
        tmpDir.deleteDir()
        this.log.info 'Fixing names on trading partner/agreement files...'
        def renamer = new Renamer({ str -> this.log.info str })
        renamer.fixNames outputDir
        clean outputDir
        // B2B rearranges order slightly, this will ensure consistent order for source control diffs, etc.
        consistentXmlOrder outputDir
    }

    private void consistentXmlOrder(File b2bDir) {
        def xmlFiles = new FileNameFinder().getFileNames(b2bDir.absolutePath, '**/*.xml')
        xmlFiles.each { xmlFile ->
            def rootNode = new XmlParser().parse(xmlFile)
            // XmlNodePrinter omits the <? xml declaration
            XmlUtil.serialize(rootNode, new FileWriter(xmlFile))
        }
    }

    private void clean(File b2bDir) {
        def filesToRemove
        def dirsToRemove = []
        def finder = new FileNameFinder()
        def tradingPartnerFiles = finder.getFileNames(b2bDir.absolutePath, tradingPartnerPattern)
        switch (this.b2BArtifactType) {
            case B2BArtifactTypes.DocumentDefinitions:
                filesToRemove = tradingPartnerFiles
                break
            case B2BArtifactTypes.PartnersAndAgreements:
                validate()

                filesToRemove = finder.getFileNames(b2bDir.absolutePath, '**/*', tradingPartnerPattern)
                def expectedFiles = partners.collect { p -> getPartnerFilename(p) } +
                        agreements.collect { a -> getAgreementFilename(a) }
                def otherTradingPartners = tradingPartnerFiles.findAll { file ->
                    !expectedFiles.any { expFile -> file.endsWith(expFile) }
                }
                filesToRemove += otherTradingPartners
                // trading partners all have files at the root level, get rid of any doc def directories
                dirsToRemove = b2bDir.listFiles().findAll { file -> file.directory }
                        .collect { file -> file.absolutePath }
                break
            default:
                throw new Exception("Unknown value ${this.b2BArtifactType}!")
        }
        filesToRemove.each { file -> FileUtils.forceDelete(new File(file)) }
        dirsToRemove.each { dir -> FileUtils.deleteDirectory(new File(dir)) }
    }
}
