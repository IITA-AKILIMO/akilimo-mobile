pipeline {
  agent any
  environment{
    VERSION = 8.2
  }
  tools {
    gradle 'system-gradle'
  }
  stages {
    
    stage('Push tags test') {
        when {
        beforeAgent true
        anyOf {
                branch 'master'; branch 'develop'
           }
        }
      steps {
        sh 'git push --tags'
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
        sh 'chmod +x ./gradlews'
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

    stage('Sign production binaries') {
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

    stage('Upload production artifacts') {
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
