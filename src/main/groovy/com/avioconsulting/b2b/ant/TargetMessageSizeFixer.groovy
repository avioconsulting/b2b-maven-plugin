package com.avioconsulting.b2b.ant

class TargetMessageSizeFixer {
    public static final String settingName = 'weblogic.MaxMessageSize'
    private Closure logger

    TargetMessageSizeFixer(Closure logger) {
        this.logger = logger
    }

    void fixAntTarget(File b2bAntFilePath, long maxMessageSize) {
        def parser = new XmlParser()
        Node antNode = parser.parse(b2bAntFilePath)
        Node utilityTarget = antNode.target.find { t -> t.@name == 'utility' }
        if (!utilityTarget.java.any()) {
            throw new Exception('Unable to find java ANT task call!')
        }
        Node javaTask = utilityTarget.java[0]
        def setting = "-D${settingName}=${maxMessageSize}"
        def changed = false
        def existingNode = javaTask.jvmarg.find { node -> node.@value.contains(settingName) }
        if (existingNode && existingNode.@value != setting) {
            changed = true
            this.logger "Updating setting from ${existingNode.@value} to ${setting} in ${b2bAntFilePath}"
            existingNode.@value = setting
        } else if (existingNode == null) {
            changed = true
            this.logger "Adding setting ${setting} to ${b2bAntFilePath}"
            javaTask.appendNode 'jvmarg', [value: setting]
        }
        if (changed) {
            new XmlNodePrinter(new IndentPrinter(new FileWriter(b2bAntFilePath))).print antNode
        }
    }
}
