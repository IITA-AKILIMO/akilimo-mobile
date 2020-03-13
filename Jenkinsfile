pipeline {
  agent any
  environment{
    VERSION = "8.2";
    BETA_VERSION = "8.2.67"
  }

  stages {

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
            anyOf {
                    branch 'master'; branch 'develop'
               }
          }
          steps {
            sh './gradlew assembleRelease'
          }
        }

        stage('AAB') {
          when {
            beforeAgent true
                anyOf {
                        branch 'bundle/master'; branch 'bundle/develop'
                   }
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
            anyOf {
                    branch 'master'; branch 'develop'
               }
          }
          steps {
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.apk', skipZipalign: true)
          }
        }

        stage('aab') {
          when {
            beforeAgent true
            anyOf {
                    branch 'bundle/master'; branch 'bundle/develops'
               }
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
        anyOf {
                branch 'master'; branch 'develop'
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
        stage('beta aab') {
          when {
            beforeAgent true
            anyOf {
                    branch 'bundle/develop'
               }
          }
          steps {
        androidApkUpload filesPattern: '**/build/outputs/**/*-release.aab', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB', text: 'Bug fixes']], trackName: 'beta'
          }
        }

        stage('beta apk') {
          when {
            beforeAgent true
            anyOf {
                    branch 'develop'
               }
          }
          steps {
            androidApkUpload(filesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
                                              text: 'Bug fixes']], trackName: 'beta')
          }
        }

        stage('production apk') {
          when {
            beforeAgent true
            anyOf {
                    branch 'master';
               }
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
        anyOf {
                branch 'bundle/master'; branch 'bundle/develop'
           }
      }
      steps {
        fingerprint '**/build/outputs/**/*-release.*'
      }
    }

    stage('Tag production release commit') {
      when {
        beforeAgent true
        anyOf {
                branch 'master';branch 'develop';branch 'master';branch 'bundle/master';
           }
      }
      steps {
        sh 'git tag -a v$VERSION.$BUILD_NUMBER $GIT_COMMIT -m "Jenkins-release-$BUILD_NUMBER"'
      }
    }

    stage('Tag beta release commit') {
      when {
        beforeAgent true
        anyOf {
                branch 'develop';branch 'bundle/develop';
           }
      }
      steps {
        sh 'git tag -a v$BETA_VERSION.$BUILD_NUMBER."beta" $GIT_COMMIT -m "Jenkins-beta-$BUILD_NUMBER"'
      }
    }



    stage('Push tags') {
      when {
        beforeAgent true
        anyOf {
                branch 'master'; branch 'develop';branch 'bundle/master'; branch 'bundle/develop'
           }
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
