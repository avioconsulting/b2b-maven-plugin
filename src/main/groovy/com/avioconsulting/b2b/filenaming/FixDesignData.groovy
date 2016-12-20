package com.avioconsulting.b2b.filenaming

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
    String fix(String tradingPartnerFile, File directory) {
        def xmlFile = new File(directory, tradingPartnerFile)
        Node rootNode = new XmlParser().parse(xmlFile)
        def name = rootNode.@name
        def oldId = rootNode.@id as String
        def prefix = oldId.contains('tpa') ? 'tpa' : 'tp'
        def newId = "${prefix}_${name}"
        rootNode.@id = newId
        def newFile = new File(directory, "${newId}.xml")
        this.logger "Renaming ${xmlFile} to ${newFile} and updating ID..."
        updateXml(xmlFile, rootNode)
        xmlFile.renameTo(newFile)
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
