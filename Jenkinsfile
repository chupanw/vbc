pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh 'sbt compile'
      }
    }
    stage('Small Tests') {
      steps {
        sh 'sbt "testOnly edu.cmu.cs.vbc.analysis.*Test"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.utils.*Test"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.SmallExampleTest"'
      }
    }
    stage('Bigger Tests') {
      steps {
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.BankAccountTest"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.ElevatorTest"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.EmailTest"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.GPLTest"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.PrevaylerTest"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.QuEvalTest"'
      }
    }
  }
  post {
    always {
      junit 'target/test-reports/*.xml'
    }
  }
}