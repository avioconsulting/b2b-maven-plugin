package com.avioconsulting.b2b.ant

import org.apache.maven.plugin.logging.Log
import org.apache.tools.ant.DefaultLogger
import org.apache.tools.ant.Project

class MavenLogger extends DefaultLogger {
    private final Log mavenLogger

    MavenLogger(Log mavenLogger) {
        this.messageOutputLevel = Project.MSG_INFO
        this.mavenLogger = mavenLogger
    }

    @Override
    protected void printMessage(String message, PrintStream stream, int priority) {
        switch (priority) {
            case Project.MSG_DEBUG:
            case Project.MSG_VERBOSE:
                this.mavenLogger.debug message
                break
            case Project.MSG_INFO:
                this.mavenLogger.info message
                break
            case Project.MSG_WARN:
                this.mavenLogger.warn message
                break
            case Project.MSG_ERR:
                this.mavenLogger.error message
                break
            default:
                throw new Exception("Unknown message priority/type ${priority}!")
        }
    }
}
