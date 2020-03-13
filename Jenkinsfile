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
        stage('aab build') {
          when {
            beforeAgent true
            anyOf {
                    branch 'master';
               }
          }
          steps {
            sh './gradlew assembleRelease'
          }
        }

        stage('apk build') {
          when {
            beforeAgent true
                anyOf {
                        branch 'master-disable';
                   }
          }
          steps {
            sh './gradlew bundleRelease'
          }
        }

      }
    }

    stage('Jar Signer') {
      steps {
        when {
          beforeAgent true
          anyOf {
                  branch 'master';
             }
        }
        withCredentials([usernamePassword(credentialsId: 'keystore-credentials', passwordVariable: 'pass', usernameVariable: 'alias')]) {
            sh 'jarsigner -keystore /var/lib/jenkins/fertilizer.jks -storepass $pass **/build/outputs/**/*/*-release.aab $alias'
        }
      }
    }

    stage('Sign production binaries') {
      parallel {
        stage('apk signing') {
          when {
            beforeAgent true
            anyOf {
                    branch 'master-disable';
               }
          }
          steps {
            signAndroidApks(keyStoreId: 'akilimo', keyAlias: 'akilimo', apksToSign: '**/*-unsigned.apk', skipZipalign: true)
          }
        }

        stage('aab signing') {
          when {
            beforeAgent true
            anyOf {
                    branch 'master';
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
                branch 'master';
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
        stage('aab upload') {
          when {
            beforeAgent true
            anyOf {
                    branch 'master'
               }
          }
          steps {
            androidApkUpload(filesPattern: '**/build/outputs/**/*-release.aab', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB',
             text: 'Bug fixes']], trackName: 'beta')
          }
        }

        stage('apk upload') {
          when {
            beforeAgent true
            anyOf {
                    branch 'master-disabled';
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
                branch 'master'
           }
      }
      steps {
        fingerprint '**/build/outputs/**/*-release.*'
      }
    }

    stage('Tag releases') {
      when {
        beforeAgent true
        anyOf {
                branch 'master';
           }
      }
      steps {
        sh 'git tag -a v$VERSION.$BUILD_NUMBER $GIT_COMMIT -m "Jenkins-release-$BUILD_NUMBER"'
      }
    }


    stage('Push tags') {
      when {
        beforeAgent true
        anyOf {
                branch 'master';
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
