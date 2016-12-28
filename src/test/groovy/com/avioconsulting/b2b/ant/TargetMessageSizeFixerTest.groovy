package com.avioconsulting.b2b.ant

import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test

import static junit.framework.TestCase.fail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class TargetMessageSizeFixerTest {
    def directory = new File('build/tmp/antFix')
    def targetFile = new File(directory, 'file.xml')
    def fixer = new TargetMessageSizeFixer({ str -> println "From class: ${str}" })

    @Before
    void setupFreshData() {
        if (directory.exists()) {
            directory.deleteDir()
        }
        directory.mkdirs()
    }

    def getJvmArgs() {
        def antNode = new XmlParser().parse(targetFile)
        Node utilityTarget = antNode.target.find { t -> t.@name == 'utility' }
        if (!utilityTarget.java.any()) {
            throw new Exception('Unable to find java ANT task call!')
        }
        Node javaTask = utilityTarget.java[0]
        javaTask.jvmarg.collect { n -> n.@value }
    }

    @Test
    void notThere() {
        // arrange
        FileUtils.copyFile new File('src/test/resources/ant/noSetting.xml'), targetFile

        // act
        fixer.fixAntTarget targetFile, 40000000

        // assert
        assertThat this.jvmArgs,
                   is(equalTo(['-Xms512m',
                               '-Xmx1024m',
                               '-Djava.naming.provider.url=${java.naming.provider.url}',
                               '-Djava.naming.factory.initial=${java.naming.factory.initial}',
                               '-Djava.naming.security.principal=${java.naming.security.principal}',
                               '-Djava.naming.security.credentials=${java.naming.security.credentials}',
                               '-Dweblogic.MaxMessageSize=40000000']))
    }

    @Test
    void there_Matches() {
        // arrange
        FileUtils.copyFile new File('src/test/resources/ant/alreadyThere.xml'), targetFile

        // act
        fixer.fixAntTarget targetFile, 40000000

        // assert
        assertThat this.jvmArgs,
                   is(equalTo(['-Xms512m',
                               '-Xmx1024m',
                               '-Djava.naming.provider.url=${java.naming.provider.url}',
                               '-Djava.naming.factory.initial=${java.naming.factory.initial}',
                               '-Djava.naming.security.principal=${java.naming.security.principal}',
                               '-Djava.naming.security.credentials=${java.naming.security.credentials}',
                               '-Dweblogic.MaxMessageSize=40000000']))
    }

    @Test
    void there_DifferentSetting() {
        // arrange
        FileUtils.copyFile new File('src/test/resources/ant/wrongSetting.xml'), targetFile

        // act
        fixer.fixAntTarget targetFile, 50000000

        // assert
        assertThat this.jvmArgs,
                   is(equalTo(['-Xms512m',
                               '-Xmx1024m',
                               '-Djava.naming.provider.url=${java.naming.provider.url}',
                               '-Djava.naming.factory.initial=${java.naming.factory.initial}',
                               '-Djava.naming.security.principal=${java.naming.security.principal}',
                               '-Djava.naming.security.credentials=${java.naming.security.credentials}',
                               '-Dweblogic.MaxMessageSize=50000000']))
    }
}
