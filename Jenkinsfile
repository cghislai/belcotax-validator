pipeline {
    agent {
            label 'docker'
    }
    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip tests')
        booleanParam(name: 'SKIP_PUBLISH', defaultValue: false, description: 'Skip publishing archive')
        string(name: 'ALT_DEPLOYMENT_REPOSITORY', defaultValue: '', description: 'Alternative deployment repo')
        string(name: 'GPG_KEY_CREDENTIAL_ID', defaultValue: 'jenkins-jenkins-charlyghislain-maven-deploy-gpg-key',
         description: 'Credential containing the private gpg key (pem)')
        string(name: 'GPG_KEY_FINGERPRINT', defaultValue: '508608F6CF097B4746CB291B5B72FDC1FF81F9ED',
         description: 'The fingerprint of this key to add to trust root')
        string(name: 'DOCKER_REPO', defaultValue: 'ghcr.io/cghislai', description: 'Docker repo')
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage ('Build') {
            steps {
                script {
                    env.MVN_ARGS="-Dquarkus.container-image.build=false"
                    if (params.ALT_DEPLOYMENT_REPOSITORY != '') {
                        env.MVN_ARGS="${env.MVN_ARGS} -DaltDeploymentRepository=${params.ALT_DEPLOYMENT_REPOSITORY}"
                    }
                }
                container('docker') {
                    withMaven(maven: 'maven', mavenSettingsConfig: 'nexus-mvn-settings') {
                        sh "mvn ${env.MVN_ARGS} -DskipTests=${params.SKIP_TESTS} clean compile install"
                    }
                }
            }
        }
        stage ('Publish') {
            when { anyOf {
                expression { return params.SKIP_PUBLISH != true }
            } }
            steps {
                script {
                    env.MVN_ARGS="-Dquarkus.container-image.build=false"
                    env.MVN_ARGS="${env.MVN_ARGS} -DskipTests=true -Possrh-deploy"

                    if (params.ALT_DEPLOYMENT_REPOSITORY != '') {
                        env.MVN_ARGS="${env.MVN_ARGS} -DaltDeploymentRepository=${params.ALT_DEPLOYMENT_REPOSITORY}"
                    }
                    if (env.BRANCH_NAME == 'master') {
                        env.MVN_ARGS="${env.MVN_ARGS}"
                    }
                }
                container('docker') {
                    withCredentials([file(credentialsId: "${params.GPG_KEY_CREDENTIAL_ID}", variable: 'GPGKEY')]) {
                        sh 'gpg --batch --allow-secret-key-import --import $GPGKEY'
                        sh "echo \"${params.GPG_KEY_FINGERPRINT}:6:\" | gpg --batch --import-ownertrust"
                    }
                    withMaven(maven: 'maven', mavenSettingsConfig: 'ossrh-cghislai-settings-xml', jdk: 'jdk11') {
                        sh "mvn deploy $MVN_ARGS"
                        script {
                            VERSION = sh(script: 'JENKINS_MAVEN_AGENT_DISABLED=true mvn help:evaluate -Dexpression=project.version -q -DforceStdout | tail -n1', returnStdout: true).trim()
                        }
                    }
                    dir('belcotax-validator-rest') {
                        script {
                            def image = docker.build("${params.DOCKER_REPO}/belcotax-validator:${VERSION}", "-f src/main/docker/Dockerfile.jvm .")
                            image.push()
                            image.push("${BRANCH_NAME}-latest")
                        }
                    }
                }
            }
        }
    }
}
