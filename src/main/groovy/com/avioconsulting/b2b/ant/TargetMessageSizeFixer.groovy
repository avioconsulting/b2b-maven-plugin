package com.avioconsulting.b2b.ant

class TargetMessageSizeFixer {
    public static final String settingName = 'weblogic.MaxMessageSize'
    private Closure logger

    TargetMessageSizeFixer(Closure logger) {
        this.logger = logger
    }

    void fixAntTarget(File b2bAntFilePath) {
        def parser = new XmlParser()
        Node antNode = parser.parse(b2bAntFilePath)
        Node utilityTarget = antNode.target.find { t -> t.@name == 'utility' }
        if (!utilityTarget.java.any()) {
            throw new Exception('Unable to find java ANT task call!')
        }
        Node javaTask = utilityTarget.java[0]
        def setting = "-Dweblogic.MaxMessageSize=40000000"
        def changed = false
        if (javaTask.jvmarg.any { node -> node.@value.contains(settingName) }) {
            //throw new Exception('foo')
        } else {
            changed = true
            this.logger "Adding setting ${setting} to ${b2bAntFilePath}"
            javaTask.appendNode 'jvmarg', [value: setting]
        }
        if (changed) {
            new XmlNodePrinter(new IndentPrinter(new FileWriter(b2bAntFilePath))).print antNode
        }
    }
}
