pipeline {
  agent any
  stages {
    stage('Starting up the pipeline') {
      steps {
        sh 'env'
        sh 'echo $BUILD_NUMBER'
      }
    }

    stage('Make executable') {
      steps {
        sh 'chmod +x ./gradlew'
      }
    }

    stage('Run checks and linter') {
      steps {
        sh 'gralde check'
        androidLint(pattern: '**/lint-results*.xml')
      }
    }

    stage('Build artifacts') {
      parallel {
        stage('generate android apk') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            sh 'gradle assembleRelease'
          }
        }

        stage('generate android bundle') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            sh 'gradle bundleRelease'
          }
        }

      }
    }

    stage('Sign production binaries') {
      parallel {
        stage('apk signing') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.apk', skipZipalign: true)
          }
        }

        stage('aab Jar Signer') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            withCredentials(bindings: [usernamePassword(credentialsId: 'keystore-credentials', passwordVariable: 'pass', usernameVariable: 'alias')]) {
              sh 'jarsigner -keystore $KEYSTORE_PATH -storepass $pass **/build/outputs/**/*/*-release.aab $alias'
            }

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
        stage('aab upload') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            androidApkUpload(filesPattern: '**/build/outputs/**/*-release.aab', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
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

    stage('Tag releases') {
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
  environment {
    KEYSTORE_PATH='D:\gdrive\keystores\fertilizer.jks'
    VERSION_MAJOR ="9"
    VERSION_MINOR ="3"
    BETA_VERSION = '8.2.67'
  }
}