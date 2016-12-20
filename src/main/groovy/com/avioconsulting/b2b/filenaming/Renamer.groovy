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
        new FileNameFinder().getFileNames(directory.absolutePath, 'tp*.xml').each { file ->
            this.fixer.fix new File(file)
        }
    }
}
