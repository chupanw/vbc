pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh 'sbt compile'
      }
    }
    stage('Small Test') {
      steps {
        sh 'sbt "testOnly edu.cmu.cs.vbc.analysis.*Test"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.utils.*Test"'
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.SmallExampleTest"'
      }
    }
    stage('BankAccount') {
      steps {
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.BankAccountTest"'
      }
    }
    stage('Elevator') {
      steps {
        sh 'sbt "testOnly edu.cmu.cs.vbc.prog.ElevatorTest"'
      }
    }
  }
}