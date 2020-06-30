pipeline {
  environment {
    KEYSTORE_FILE = 'D:\\gdrive\\keystores\\fertilizer.jks'
  }
  agent any
  stages {

    stage('Starting up the pipeline') {
      steps {
        sh 'printenv | sort'
        sh 'git fetch --tags'
        sh 'ghr --version'
      }
    }

    stage('Download versiontag tool') {
      environment {
         PRE_RELEASE = true
         RELEASE_VERSION = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
      }
      steps {
        echo "is pre release $PRE_RELEASE"
        echo "Tag is $RELEASE_VERSION"
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
        sh 'gradle testDebug -x lint'
      }
    }

    stage('Run linting for develop branch only') {
      when {
        beforeAgent true
        anyOf {
          branch 'develop'
          branch 'master'
        }

      }
      steps {
        sh 'gradle :app:lintDebug -x test'
        androidLint(pattern: '**/lint-results*.xml')
      }
    }

    stage('Build and generate beta artifacts') {
      parallel {
        stage('generate android apk') {
          when {
            beforeAgent true
            anyOf {
              branch 'develop'
            }
          }
          environment {
            RELEASE_VERSION = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
          }
          steps {
            sh 'gradle assembleRelease -x test --no-daemon'
          }
        }

        stage('generate android aab') {
          when {
            beforeAgent true
            anyOf {
              branch 'develop'
            }
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

stage('Build and generate production artifacts') {
      parallel {
        stage('generate android apk') {
          when {
            beforeAgent true
            anyOf {
              branch 'master'
            }
          }
          environment {
            RELEASE_VERSION = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
          }
          steps {
            sh 'gradle assembleRelease -x test --no-daemon'
          }
        }

        stage('generate android aab') {
          when {
            beforeAgent true
            anyOf {
              branch 'master'
            }
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
          when {
            beforeAgent true
            anyOf {
              branch 'develop'
              branch 'master'
            }
          }
          steps {
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.apk', skipZipalign: true)
          }
        }

        stage('AAB Jar Signer') {
          when {
            beforeAgent true
            anyOf {
              branch 'develop'
              branch 'master'
            }
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
        anyOf {
          branch 'develop'
          branch 'master'
        }
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
        stage('aab upload to beta') {
          when {
            beforeAgent true
            branch 'develops'
          }
          steps {
            androidApkUpload(filesPattern: '**/build/outputs/**/*-release.aab', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
                                         text: '''This update includes:
                                   - New content
                                   - New features
                                   - Bug fixes
                                   - Performance improvements''']], trackName: 'beta')
          }
        }

        stage('apk upload to production') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
            androidApkUpload(filesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
                                         text: '''This update includes:
                                   - New content
                                   - New features
                                   - Bug fixes
                                   - Performance improvements''']], trackName: 'production')
          }
        }

      }
    }

    stage('Upload pre-release build artifacts') {
      when {
        beforeAgent true
        anyOf {
          branch 'develop'
        }
      }
      environment {
        TAG = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
        RELEASE_VERSION = "$TAG-rc-$BUILD_NUMBER"
      }
      steps {
        sh 'cp app/build/outputs/**/*.* uploads/'
        sh 'cp app/build/outputs/**/*/*.* uploads/'
        sh 'ghr -replace -prerelease $RELEASE_VERSION uploads/'
      }
    }

    stage('Upload production build artifacts') {
      when {
        beforeAgent true
        anyOf {
          branch 'master'
        }
      }
      environment {
        RELEASE_VERSION = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', , returnStdout: true).trim()
      }
      steps {
        sh 'cp app/build/outputs/**/*.* uploads/'
        sh 'cp app/build/outputs/**/*/*.* uploads/'
        sh 'ghr -replace $RELEASE_VERSION uploads/'
      }
    }
    stage('Fingerprint files') {
      when {
        beforeAgent true
        anyOf {
          branch 'develop'
          branch 'master'
        }
      }
      steps {
        fingerprint '**/build/outputs/**/*-release.*'
      }
    }
  }
}