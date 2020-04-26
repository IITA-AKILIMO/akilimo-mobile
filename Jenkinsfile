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

    stage('Run test for non release branch') {
    milestone()
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
    milestone()
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
        milestone()
          when {
            beforeAgent true
            branch 'masters'
          }
          environment {
                RELEASE_VERSION = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
           }
          steps {
            sh 'gradle assembleRelease -x test --no-daemon'
          }
        }

        stage('generate android bundle') {
        milestone()
          when {
            beforeAgent true
            branch 'master'
          }
          environment {
                RELEASE_VERSION = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
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
        milestone()
          when {
            beforeAgent true
            branch 'legacy/master'
          }
          steps {
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.apk', skipZipalign: true)
          }
        }

        stage('AAB Jar Signer') {
        milestone()
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
    milestone()
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
        milestone()
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
        milestone()
          when {
            beforeAgent true
            branch 'legacy/master'
          }
          steps {
            androidApkUpload(filesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
                             text: 'Bug fixes']], trackName: 'production')
          }
        }
      }
    }

    stage('Fingerprint files') {
    milestone()
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
