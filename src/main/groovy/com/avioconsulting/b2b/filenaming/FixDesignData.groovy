package com.avioconsulting.b2b.filenaming

import org.apache.commons.io.FileUtils

class FixDesignData {
    private final Closure logger

    FixDesignData(Closure logger) {
        this.logger = logger
    }

    /**
     * Fixes a single trading partner file and all references to it
     *
     * @param tradingPartnerFile - existing file to fix
     * @param directory - output directory containing all files
     * @return - the new ID of the trading partner
     */
    String fix(File tradingPartnerFile) {
        Node rootNode = new XmlParser().parse(tradingPartnerFile)
        def name = rootNode.@name
        def oldId = rootNode.@id as String
        def prefix = oldId.contains('tpa') ? 'tpa' : 'tp'
        def newId = "${prefix}_${name}"
        if (newId == oldId) {
            return oldId
        }
        rootNode.@id = newId
        def directory = tradingPartnerFile.parentFile
        def newFile = new File(directory, "${newId}.xml")
        this.logger "Renaming ${tradingPartnerFile} to ${newFile} and updating ID..."
        updateXml(tradingPartnerFile, rootNode)
        FileUtils.copyFile tradingPartnerFile, newFile
        tradingPartnerFile.delete()
        updateReferences(directory, oldId, newId)
        newId
    }

    private List<String> updateReferences(File directory, oldId, newId) {
        new FileNameFinder().getFileNames(directory.absolutePath, '*.xml').each { otherFile ->
            def otherFileObj = new File(otherFile)
            def lines = otherFileObj.readLines()
            def modified = false
            def newLines = lines.collect { line ->
                if (!modified && line.contains(oldId)) {
                    modified = true
                }
                line.replaceAll(oldId, newId)
            }
            if (modified) {
                this.logger "Fixing references found in ${otherFile}..."
                otherFileObj.write(newLines.join(System.getProperty('line.separator')))
            }
        }
    }

    private updateXml(File xmlFile, Node rootNode) {
        new XmlNodePrinter(new IndentPrinter(new FileWriter(xmlFile))).print rootNode
    }
}
