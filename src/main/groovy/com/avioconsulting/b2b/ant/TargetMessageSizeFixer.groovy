package com.avioconsulting.b2b.ant

class TargetMessageSizeFixer {
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
        javaTask.appendNode 'jvmarg', [value: "-Dweblogic.MaxMessageSize=40000000"]
        new XmlNodePrinter(new IndentPrinter(new FileWriter(b2bAntFilePath))).print antNode
    }
}
