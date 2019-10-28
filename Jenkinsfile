  environment {
    KEYSTORE_PWD = 'andalite6'
    KEY_ALIAS = 'akilimo'
    KEY_PASSWORD = 'andalite6'
  }
  
pipeline {
  agent any
  stages {
      stage('Make exectable') {
      steps {
        sh 'chmod +x ./gradlew'
        sh 'java -version'
      }
    }
    stage('Gradle tasks') {
      steps {
        sh 'gradle tasks --all'
        sh 'gradle -version'
        sh 'ls'
      }
    }
    stage('feature-branch') {
      when {
        beforeAgent true
        branch 'feature/*'
      }
      steps {
        echo 'run this stage - only if the branch name started with feature/'
      }
    }
    stage('Clean') {
      steps {
        sh 'gradle clean'
      }
    }
    stage('Build release') {
      steps {
        sh(script: 'gradle :app:bundleRelease :app:assembleRelease', returnStdout: true)
      }
    }
    stage('Archive Artifacts') {
      steps {
        script {
          archiveArtifacts allowEmptyArchive: true,
          artifacts: '**/*.apk, **/*.aab, app/build/**/mapping/**/*.txt, app/build/**/logs/**/*.txt'
          cleanWs()
        }

      }
    }
  }
}