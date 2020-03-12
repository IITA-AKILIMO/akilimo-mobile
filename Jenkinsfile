pipeline {
  agent any
  environment{
    VERSION = 8.1
  }
  tools {
    gradle 'system-gradle'
  }
  options { skipDefaultCheckout() }
  stages {

    stage('Clone repository') {
      steps {
        git url: "git@github.com:masgeek/akilimo-mobile.git",credentialsId: 'jenkins_ssh_key'
      }
    }

    stage('Checkout active branch') {
      steps {
        sh 'git checkout $BRANCH_NAME'
        sh 'git fetch'
        sh 'git pull'
        script {
            env.GIT_COMMIT = "${sh(script:'git rev-parse --verify HEAD', returnStdout: true)}"
        }
      }
    }

    stage('Rev up your engines') {
      steps {
        sh 'echo $BUILD_NUMBER'
        sh 'env'
      }
    }

    stage('Make executable') {
      steps {
        sh 'chmod +x ./gradlew'
      }
    }

    stage('Test') {
      steps {
        sh './gradlew test'
      }
    }

    stage('Lint') {
      steps {
        sh './gradlew lint'
        androidLint(pattern: '**/lint-results*.xml')
      }
    }

    stage('Build artifacts') {
      parallel {
        stage('APK') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            sh './gradlew assembleRelease'
          }
        }

        stage('AAB') {
          when {
            beforeAgent true
            branch 'bundle'
          }
          steps {
            sh './gradlew bundleRelease'
          }
        }

      }
    }

    stage('Sign build binaries') {
      parallel {
        stage('apk') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.apk', skipZipalign: true)
          }
        }

        stage('aab') {
          when {
            beforeAgent true
            branch 'bundle'
          }
          steps {
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.aab', skipZipalign: true)
          }
        }

      }
    }

    stage('Archive Artifacts') {
      when {
        beforeAgent true
        branch 'master'
      }
      steps {
        script {
          archiveArtifacts allowEmptyArchive: true,
          artifacts: '**/*.apk, **/*.aab, app/build/**/mapping/**/*.txt, app/build/**/logs/**/*.txt'
        }

      }
    }

    stage('Upload artifacts') {
      parallel {
        stage('aab') {
          when {
            beforeAgent true
            branch 'bundle'
          }
          steps {
            androidAabUpload(aabFilesPattern: '**/build/outputs/**/*-release.aab', applicationId: 'com.iita.akilimo', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
                                        text: 'Bug fixes']], trackName: 'beta')
          }
        }

        stage('apk') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            androidApkUpload(apkFilesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
                                              text: 'Bug fixes']], trackName: 'production')
          }
        }

      }
    }

    stage('Fingerprint files') {
      when {
        beforeAgent true
        branch 'master'
      }
      steps {
        fingerprint '**/build/outputs/**/*-release.*'
      }
    }

    stage('Tag release commit') {
      when {
        beforeAgent true
        branch 'master'
      }
      steps {
        sh 'git tag -a v$VERSION.$BUILD_NUMBER $GIT_COMMIT -m "Jenkins-release-$BUILD_NUMBER"'
      }
    }

    stage('Push tags') {
      when {
        beforeAgent true
        branch 'master'
      }
      steps {
        sh 'git push --tags'
      }
    }

    stage('clean WS') {
      steps {
        cleanWs()
      }
    }

  }
}
