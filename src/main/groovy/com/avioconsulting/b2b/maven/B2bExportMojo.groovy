package com.avioconsulting.b2b.maven

import com.avioconsulting.b2b.filenaming.Renamer
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
        b2bPath.renameTo(outputDir)
        this.log.info 'Removing temporary directory'
        tmpDir.deleteDir()
        this.log.info 'Fixing names on trading partner/agreement files...'
        def renamer = new Renamer({ str -> this.log.info str })
        renamer.fixNames outputDir
        clean outputDir
    }

    private void clean(File b2bDir) {
        def filesToRemove
        def finder = new FileNameFinder()
        def tradingPartnerFiles = finder.getFileNames(b2bDir.absolutePath, tradingPartnerPattern)
        switch (this.b2BArtifactType) {
            case B2BArtifactTypes.DocumentDefinitions:
                filesToRemove = tradingPartnerFiles
                break
            case B2BArtifactTypes.PartnersAndAgreements:
                if (partners == null || !partners.any() || agreements == null || !agreements.any()) {
                    throw new Exception('If b2b.artifact.type/PartnersAndAgreements is used, must supply b2b.partners/agreements!')
                }

                filesToRemove = finder.getFileNames(b2bDir.absolutePath, '**/*', tradingPartnerPattern)
                def expectedFiles = partners.collect { p -> "tp_${p}.xml" } + agreements.collect { a -> "tpa_${a}.xml" }
                def otherTradingPartners = tradingPartnerFiles.findAll { file ->
                    !expectedFiles.any { expFile -> file.endsWith(expFile) }
                }
                filesToRemove += otherTradingPartners
                break
            default:
                throw new Exception("Unknown value ${this.b2BArtifactType}!")
        }

        filesToRemove.each { file ->
            new File(file).delete()
        }
    }
}
