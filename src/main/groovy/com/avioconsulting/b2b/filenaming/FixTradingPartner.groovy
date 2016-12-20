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
        Node parsed = new XmlParser(false, true).parse(xmlFile)
        def name = parsed.attribute('name')
        xmlFile.renameTo(new File(directory, "tp_${name}.xml"))
        name
    }
}
