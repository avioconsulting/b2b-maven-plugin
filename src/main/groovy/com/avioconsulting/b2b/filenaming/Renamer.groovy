package com.avioconsulting.b2b.filenaming

import sun.reflect.generics.reflectiveObjects.NotImplementedException

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
    }
}
