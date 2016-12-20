package com.avioconsulting.b2b.filenaming

class FixTradingPartner {
    /**
     * Fixes a single trading partner file and all references to it
     *
     * @param tradingPartnerFile - existing file to fix
     * @param directory - output directory containing all files
     * @return - the new ID of the trading partner
     */
    static String fix(String tradingPartnerFile, File directory) {
        def xmlFile = new File(directory, tradingPartnerFile)
        Node rootNode = new XmlParser().parse(xmlFile)
        def name = rootNode.@name
        def newId = "tp_${name}"
        rootNode.@id = newId
        new XmlNodePrinter(new IndentPrinter(new FileWriter(xmlFile))).print rootNode
        xmlFile.renameTo(new File(directory, "${newId}.xml"))
        name
    }
}
