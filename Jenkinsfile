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

    stage('Run code coverage test') {
      when {
            beforeAgent true
            anyOf {
              branch 'develop'
              branch 'legacy-develop'
              branch 'master'
              branch 'legacy-master'
            }
        }
      steps {
        sh './gradlew jacocoTestReportRelease -x test'
      }
    }

    stage('Run linting checks') {
      when {
            beforeAgent true
            anyOf {
              branch 'develop'
              branch 'legacy-develop'
              branch 'master'
              branch 'legacy-master'
            }
        }
      steps {
        sh './gradlew :app:lint -x test'
      }
    }

    stage('Run tests') {
      steps {
        sh './gradlew test -x lint'
      }
    }

    stage('Build and generate artifacts') {
      stages {
        stage('Generate android APK') {
          when {
            beforeAgent true
            anyOf {
              branch 'legacy-develop'
              branch 'legacy-master'
            }
          }
          environment {
            RELEASE_VERSION = sh(script: 'cat $LATEST_TAG_FILE', , returnStdout: true).trim()
          }
          steps {
            sh './gradlew assembleRelease -x test --no-daemon'
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
            sh './gradlew bundleRelease -x test --no-daemon'
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
              branch 'legacy-develop'
              branch 'legacy-master'
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
            branch 'develop-'
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
            branch 'legacy-develop'
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
            branch 'master'
          }
          steps {
                androidApkUpload filesPattern: '**/build/outputs/**/*-release.aab', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB', text: '''This update includes:
                - New content
                - New UI features
                - Bug fixes
                - Performance improvements''']], rolloutPercentage: '100', trackName: 'production'
          }
        }

        stage('APK upload') {
          when {
            beforeAgent true
            branch 'legacy-master'
          }
          steps {
                androidApkUpload filesPattern: '**/build/outputs/**/*-release.apk', googleCredentialsId: 'akilimoservice-account', recentChangeList: [[language: 'en-GB', text: '''This update includes:
                - New content
                - New UI features
                - Bug fixes
                - Performance improvements''']], rolloutPercentage: '100', trackName: 'production'
          }
        }

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
  }

  post {
        success {
          recordIssues(tools: [androidLintParser(name: 'lintMe', pattern: '**/lint-results*.xml')])
          junit 'app/build/test-results/**/*/*.xml'
          jacoco changeBuildStatus: true, sourcePattern: '**/src/main/java,**/src/main/kotlin'
          cleanWs()
        }
      failure {
        cleanWs()
      }
  }
}
