pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh 'sbt compile'
      }
    }
    stage('Test') {
      steps {
        sh '''sbt testOnly edu.cmu.cs.vbc.analysis.*Test
sbt testOnly edu.cmu.cs.vbc.utils.*Test'''
      }
    }
  }
}