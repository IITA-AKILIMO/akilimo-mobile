pipeline {
  agent any
  environment{
        VERSION_MAJOR ="11"
        VERSION_MINOR ="0"
		CHANGELOG='''This update includes:
- New content
- New features
- Bug fixes
- Performance improvements'''

        KEYSTORE_FILE='D:\\gdrive\\keystores\\fertilizer.jks'
  }
  stages {
    stage('Starting up the pipeline') {
      steps {
        sh 'env'
        sh 'echo $BUILD_NUMBER'
      }
    }

    stage('Run test for non release branch') {
        when {
            beforeAgent true
            not {
                branch 'master'
            }
        }
      steps {
        sh 'gradle test --no-daemon'
      }
    }

    stage('Run linting for develop branch only') {
         when {
             beforeAgent true
             anyOf {
                 branch 'develop'
             }
         }
       steps {
         sh 'gradle lint -x test --no-daemon'
         androidLint(pattern: '**/lint-results*.xml')
       }
     }

    stage('Build and generate artifacts') {
      parallel {
        stage('generate android apk') {
          when {
            beforeAgent true
            branch 'masters'
          }
          steps {
            sh 'gradle assembleRelease -x test --no-daemon'
          }
        }

        stage('generate android bundle') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            sh 'gradle bundleRelease -x test --no-daemon'
          }
        }

      }
    }

    stage('Sign production binaries') {
      parallel {
        stage('apk signing') {
          when {
            beforeAgent true
            branch 'legacy/master'
          }
          steps {
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.apk', skipZipalign: true)
          }
        }

        stage('AAB Jar Signer') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            withCredentials(bindings: [usernamePassword(credentialsId: 'keystore-credentials', passwordVariable: 'pass', usernameVariable: 'alias')]) {
              sh 'jarsigner -keystore $KEYSTORE_FILE -storepass $pass app/build/outputs/**/*/*-release.aab $alias'
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
                             text: $CHANGELOG]], trackName: 'production')
          }
        }
        stage('apk upload') {
          when {
            beforeAgent true
            branch 'legacy/master'
          }
          steps {
            androidApkUpload(filesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
                             text: $CHANGELOG]], trackName: 'production')
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
        sh 'git tag -a v$VERSION_MAJOR.$VERSION_MINOR.$BUILD_NUMBER $GIT_COMMIT -m "Jenkins-release-v$VERSION_MAJOR.$VERSION_MINOR.$BUILD_NUMBER"'
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

    stage('Push coverage report') {
      steps {
        sh 'bash <(curl -s https://codecov.io/bash)'
      }
    }

    stage('clean WS') {
      steps {
        cleanWs()
      }
    }
  }
}
