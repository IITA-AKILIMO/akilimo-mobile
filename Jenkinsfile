pipeline {
  environment {
    KEYSTORE_FILE = 'D:\\gdrive\\keystores\\fertilizer.jks'
    LATEST_TAG_FILE = 'latest_tag_file.txt'
    TAG_FILE = 'nextrelease.txt'
    REPO_NAME = 'masgeek/akilimo-mobile'
  }
  agent any
  stages {
    stage('Execute python scripts') {
      steps {
	    sh 'java -version'
        sh 'curl -L https://raw.githubusercontent.com/masgeek/py-github/develop/get-tag-raw.py -o latest-tag.py'
        sh 'curl -L https://raw.githubusercontent.com/masgeek/py-github/develop/requirements.txt -o requirements.txt'
        sh 'pip install -r requirements.txt'
        sh 'python latest-tag.py'
      }
    }

    stage('Run tests) {
      environment {
          RELEASE_VERSION = sh(script: 'cat $LATEST_TAG_FILE', , returnStdout: true).trim()
      }
      steps {
        sh 'gradle test -x lint'
        junit 'build/reports/**/*.xml'
      }
    }

    stage('Run code coverage test) {
      environment {
          RELEASE_VERSION = sh(script: 'cat $LATEST_TAG_FILE', , returnStdout: true).trim()
      }
      steps {
        sh 'gradle jacocoTestReportRelease'
        jacoco changeBuildStatus: true, sourcePattern: '**/src/main/java,**/src/main/kotlin'
      }
    }

    stage('Run linting for develop branch only') {
      when {
        beforeAgent true
        anyOf {
          branch 'develop'
        }

      }
      environment {
          RELEASE_VERSION = sh(script: 'cat $LATEST_TAG_FILE', , returnStdout: true).trim()
      }
      steps {
        sh 'gradle :app:lintDebug -x test'
        recordIssues(tools: [androidLintParser(name: 'lintMe', pattern: '**/lint-results*.xml')])
      }
    }

    stage('Build and generate artifacts') {
      stages {
        stage('Generate android APK') {
          when {
            beforeAgent true
            anyOf {
              branch 'develop'
              branch 'master'
            }
          }
          environment {
            RELEASE_VERSION = sh(script: 'cat $LATEST_TAG_FILE', , returnStdout: true).trim()
          }
          steps {
            sh 'gradle assembleRelease -x test --no-daemon'
          }
        }

        stage('Generate android AAB') {
          when {
            beforeAgent true
            anyOf {
              branch 'develop'
              branch 'master'
            }
          }
          environment {
            RELEASE_VERSION = sh(script: 'cat $LATEST_TAG_FILE', , returnStdout: true).trim()
          }
          steps {
            sh 'gradle bundleRelease -x test --no-daemon'
          }
        }

      }
    }

    stage('Sign generated binaries') {
      parallel {
        stage('APK') {
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

        stage('AAB') {
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

    stage('Publish beta artifact to PlayStore') {
      parallel {
        stage('AAB upload') {
          when {
            beforeAgent true
            branch 'develop'
          }
          steps {
                androidApkUpload filesPattern: '**/build/outputs/**/*-release.aab', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB', text: '''This update includes:
                - Experimental features
                - New content
                - New features
                - Bug fixes
                - Performance improvements''']], rolloutPercentage: '100', trackName: 'beta'
          }
        }

        stage('APK upload') {
          when {
            beforeAgent true
            beforeInput true
            beforeOptions true
            branch 'legacy/develop'
          }
            options {
              timeout(activity: true, time: 2, unit: 'HOURS')
            }
          input {
            message 'Publish APK to beta channel'
            id 'YES'
          }
          steps {
                androidApkUpload filesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB', text: '''This update includes:
                - Experimental features
                - New content
                - New features
                - Bug fixes
                - Performance improvements''']], rolloutPercentage: '100', trackName: 'beta'
          }
        }

      }
    }

    stage('Publish release artifact to PlayStore') {
      parallel {
        stage('AAB upload') {
          when {
            beforeAgent true
            branch 'legacy/master'
          }
          steps {
                androidApkUpload filesPattern: '**/build/outputs/**/*-release.aab', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB', text: '''This update includes:
                - New content
                - New features
                - Bug fixes
                - Performance improvements''']], rolloutPercentage: '100', trackName: 'production'
          }
        }

        stage('APK upload') {
          when {
            beforeAgent true
            branch 'master'
          }
          steps {
                androidApkUpload filesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB', text: '''This update includes:
                - New content
                - New features
                - Bug fixes
                - Performance improvements''']], rolloutPercentage: '100', trackName: 'production'
          }
        }

      }
    }

    stage('Upload Beta artifacts to github') {
      when {
        beforeAgent true
        branch 'develop'
      }
      environment {
         RELEASE_VERSION = sh(script: 'cat $TAG_FILE', , returnStdout: true).trim()
      }
      steps {
        sh 'cp app/build/outputs/**/*.* uploads/'
        sh 'cp app/build/outputs/**/*/*.* uploads/'
        sh 'ghr -replace -prerelease $RELEASE_VERSION uploads/'
      }
    }
    stage('Upload Production to github') {
      when {
        beforeAgent true
        branch 'master'
      }
      environment {
        RELEASE_VERSION = sh(script: 'cat $TAG_FILE', , returnStdout: true).trim()
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
