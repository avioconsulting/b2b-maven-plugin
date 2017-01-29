package com.avioconsulting.b2b.util

class ListeningChannelFetcher {
    List<String> fetchActiveListeningChannels(File basePath) {
        def xmlFiles = new FileNameFinder().getFileNames(basePath.absolutePath, '**/tp*.xml')
        xmlFiles.collect { fileName ->
            def node = new XmlSlurper().parse(fileName)
            node.DeliveryChannel.findAll { channel ->
                channel.'@listening'.text() == 'true' && channel.'@active'.text() == 'true'
            }.collect { channel ->
                channel.'@name'.text()
            }
        }.flatten()
    }
}
