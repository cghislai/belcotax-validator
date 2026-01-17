pipeline {
    agent {
        kubernetes {
            inheritFrom 'docker helm maven-jdk11'
            label 'belcotax-validator-build'
            defaultContainer 'maven'
        }
    }
    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip tests')
        booleanParam(name: 'FORCE_DEPLOY', defaultValue: false, description: 'Force deploy ')
        credentials(
                name: 'MAVEN_CREDENTIALS', defaultValue: 'jenkins-mavensettings-secrets', description: 'maven settings.xml',
                credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl'
        )
        string(
                name: 'IMAGE', defaultValue: 'belcotax-validator', description: 'Image to push'
        )
        string(
                name: 'DOCKER_REPO', defaultValue: 'ghcr.io/cghislai', description: 'Repo to push'
        )
        credentials(
                name: 'DOCKER_CREDENTIALS', defaultValue: 'jenkins-dockerconfigjson-secrets', description: 'docker config.json',
                credentialType: 'org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl'
        )
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Maven build') {
            steps {
                container('maven') {
                    script {
                        env.MVN_ARGS = "-Dquarkus.container-image.build=false"
                        if (params.SKIP_TESTS == true) {
                            env.MVN_ARGS += " -DskipTests=${params.SKIP_TESTS}"
                        }
                        env.MVN_PHASE = "package"
                    }
                    withCredentials([file(credentialsId: params.MAVEN_CREDENTIALS, variable: 'MAVEN_SETTINGS_XML')]) {
                        sh '''
                          mkdir -p ~/.m2
                          cp $MAVEN_SETTINGS_XML ~/.m2/settings.xml
                          
                          VERSION="$(JENKINS_MAVEN_AGENT_DISABLED=true mvn help:evaluate -Dexpression=project.version -q -DforceStdout | tail -n1)"
                          mvn $MVN_ARGS clean $MVN_PHASE
                          echo "$VERSION" > .version
                        '''
                    }
                }
            }
        }
        stage('Docker image') {
            when {
                anyOf {
                    environment name: 'BRANCH_NAME', value: 'master'
                    environment name: 'BRANCH_NAME', value: 'dev'
                    expression { return params.FORCE_DEPLOY == true }
                }
            }
            steps {
                container('docker') {
                    script {
                        env.VERSION = sh(script: 'head -n1 .version', returnStdout: true).trim()
                        env.DOCKER_IMAGE_TAG = env.VERSION
                        if (env.BRANCH_NAME != "master") {
                            def shortCommit = env.GIT_COMMIT.take(7)
                            env.DOCKER_IMAGE_TAG = "${env.VERSION}-${shortCommit}"
                        }
                        env.IMAGE_NAME = "${params.DOCKER_REPO}/${params.IMAGE}:${env.DOCKER_IMAGE_TAG}"
                        env.LATEST_IMAGE_NAME = "${params.DOCKER_REPO}/${params.IMAGE}:latest"
                    }
                    withCredentials([file(credentialsId: params.DOCKER_CREDENTIALS, variable: 'DOCKER_CONFIG_JSON')]) {
                        dir('belcotax-validator-rest') {
                            sh '''
                              mkdir -p ~/.docker
                              cp $DOCKER_CONFIG_JSON ~/.docker/config.json
                
                              docker buildx build -f src/main/docker/Dockerfile.jvm --load -t "$IMAGE_NAME" \
                                --label "org.opencontainers.image.created=$(date -Iseconds)" \
                                --label "org.opencontainers.image.source=$GIT_URL" \
                                --label "org.opencontainers.image.version=$VERSION" \
                                --label "org.opencontainers.image.revision=$GIT_COMMIT" \
                                .
                              docker push "$IMAGE_NAME"
                
                              docker tag "$IMAGE_NAME" "$LATEST_IMAGE_NAME"
                              docker push "$LATEST_IMAGE_NAME"
                            '''
                        }
                    }
                }
            }
        }
    }
}
