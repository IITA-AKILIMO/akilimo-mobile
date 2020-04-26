pipeline {
  agent any
  environment{
        VERSION_MAJOR ="13"
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
        sh 'printenv | sort'
        sh 'git tag -d $(git tag)'
        sh 'git fetch --tags'
        sh 'git describe --tags $(git rev-list --tags --max-count=1)'
      }
    }

    stage('Run tests') {
        when {
            beforeAgent true
            not {
                branch 'master'
            }
        }
      steps {
        milestone()
        sh 'gradle test --no-daemon'
      }
    }

    stage('Run linting') {
         when {
             beforeAgent true
             anyOf {
                 branch 'develop'
             }
         }
       steps {
         milestone()
         sh 'gradle lint -x test --no-daemon'
         androidLint(pattern: '**/lint-results*.xml')
       }
     }

    stage('Build and generate artifacts') {
      parallel {
        stage('Generate APK') {
          when {
            beforeAgent true
            branch 'masters'
          }
          environment {
                RELEASE_VERSION = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
           }
          steps {
            milestone()
            sh 'gradle assembleRelease -x test --no-daemon'
          }
        }

        stage('Generate AAB') {
          when {
            beforeAgent true
            branch 'master'
          }
          environment {
                RELEASE_VERSION = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
           }
          steps {
            milestone()
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
            milestone()
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.apk', skipZipalign: true)
          }
        }

        stage('AAB Jar Signer') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            milestone()
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
          milestone()
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
            milestone()
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
            milestone()
            androidApkUpload(filesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
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

    stage('clean WS') {
      steps {
        cleanWs()
      }
    }
  }
}
