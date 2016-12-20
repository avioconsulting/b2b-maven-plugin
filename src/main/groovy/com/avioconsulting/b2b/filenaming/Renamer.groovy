package com.avioconsulting.b2b.filenaming

class Renamer {
    private final FixDesignData fixer

    Renamer(Closure logger) {
        this.fixer = new FixDesignData(logger)
    }

    /**
     * Iterate through all of the trading partner/agreement files and fix their names
     *
     * Complains if there is a name collision
     *
     * @param directory - contains files
     */
    void fixNames(File directory) {
        def usedIds = [:]
        new FileNameFinder().getFileNames(directory.absolutePath, 'tp*.xml').each { file ->
            def newId = this.fixer.fix new File(file)
            if (usedIds.containsKey(newId)) {
                throw new Exception("While processing file ${file}, 2 ids were the same (${newId})!")
            }
            usedIds[newId] = true
        }
    }
}
