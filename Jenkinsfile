#!groovy

pipeline {
    agent any
    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip tests')
        booleanParam(name: 'FORCE_DEPLOY', defaultValue: false,
                description: 'Force deploy on feature branches (packages published on rc, master branches)')
        string(name: 'ALT_DEPLOYMENT_REPOSITORY', defaultValue: '', description: 'Alternative deployment repo')
        string(name: 'GPG_KEY_CREDENTIAL_ID', defaultValue: 'jenkins-jenkins-charlyghislain-maven-deploy-gpg-key ',
                 description: 'Credential containing the private gpg key (pem)')
        string(name: 'GPG_KEY_FINGERPRINT', defaultValue: '508608F6CF097B4746CB291B5B72FDC1FF81F9ED',
         description: 'The fingerprint of this key to add to trust root')
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Build & publish ws') {
            agent {
                label 'docker'
            }
            steps {
                script {
                    env.MVN_ARGS = ""
                    env.MVN_GOALS = "clean package"
                    env.DO_DEPLOY = false
                    if (params.ALT_DEPLOYMENT_REPOSITORY != '') {
                        env.MVN_ARGS = "${env.MVN_ARGS} -DaltDeploymentRepository=${params.ALT_DEPLOYMENT_REPOSITORY}"
                    }
                    if (params.SKIP_TESTS) {
                        env.MVN_ARGS = "${env.MVN_ARGS} -DskipTests=true"
                    }
                    if (env.BRANCH_NAME == "master" || env.BRANCH_NAME == "dev" || params.FORCE_DEPLOY == true) {
                        env.MVN_GOALS = "clean deploy"
                        env.MVN_ARGS = "${env.MVN_ARGS} -Possrh-deploy"
                        env.DO_DEPLOY = true
                    }
                }
                withCredentials([file(credentialsId: "${params.GPG_KEY_CREDENTIAL_ID}", variable: 'GPGKEY')]) {
                    sh 'gpg --allow-secret-key-import --import $GPGKEY'
                    sh "echo \"${params.GPG_KEY_FINGERPRINT}:6:\" | gpg --import-ownertrust"
                }
                withMaven(maven: 'maven', mavenSettingsConfig: 'ossrh-cghislai-settings-xml', jdk: 'jdk11') {
                    sh "mvn $MVN_ARGS $MVN_GOALS"
                }
            }
        }
    }
}
